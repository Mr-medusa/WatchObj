package red.medusa.watchobj.core.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.ContextValueFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import red.medusa.watchobj.core.Logger;
import red.medusa.watchobj.core.MutableJson;
import red.medusa.watchobj.core.MutableJsonService;
import red.medusa.watchobj.core.PropertyValue;
import red.medusa.watchobj.server.handler.TextWebSocketHandler;

import java.lang.reflect.Array;
import java.util.*;

/**
 * 同步 Netty Server 数据更新的事件处理器
 * 将更新事件写入到 MutableJson 或 其它 WebService Channel
 *
 * @author GHHu
 * @date 2023/5/17
 * @see SyncObjectHandler
 * @see TextWebSocketHandler
 * @see EventType
 */
public class PropertyValueChanelEventHandler {
    private final Set<Channel> channels = Collections.synchronizedSet(new HashSet<>());

    public PropertyValueChanelEventHandler() {
        SyncObjectHandler syncObjectHandler = new SyncObjectHandler(channels);
        syncObjectHandler.start();
    }

    public void writeDataToChannel(String message, Channel channel) {
        MutableJsonService service = MutableJsonService.getInstance();
        if (service != null) {
            Logger.debug("收到消息:" + message);
            if (message.equals("1")) {
                MutableJson mutableJson = service.getMutableJson();
                String json = JSON.toJSONString(new R(true, "获取实例对象成功...", EventType.addRoots, mutableJson.getJSONArray()), PropertyValue.VALUE_FILTER,
                        SerializerFeature.WriteNullStringAsEmpty, SerializerFeature.WriteNullNumberAsZero, SerializerFeature.WriteMapNullValue);
                channel.writeAndFlush(new TextWebSocketFrame(json));
            }
            else {
                JSONObject jsonObject = JSON.parseObject(message);
                String path = jsonObject.getString("path");
                String substring = path.substring(0, path.indexOf("."));
                PropertyValue rootPropertyValue = null;
                for (PropertyValue propertyValue : service.getMutableJson().getJSONArray()) {
                    if (substring.equals(propertyValue.getName())) {
                        rootPropertyValue = propertyValue;
                        break;
                    }
                }
                if (rootPropertyValue == null) {
                    Logger.debug("web service [received message but not find property value] : " + jsonObject.toJSONString());
                    return;
                }
                PropertyValue propertyValue = rootPropertyValue.getPropertyValue(path);
                // 防止更新间隙此时正在进行更新集合 @see enableUpdateMutableJsonCollection
                synchronized (MutableJsonService.getInstance()) {
                    String typeStr = jsonObject.getString("type");
                    EventType type = EventType.valueOf(typeStr);
                    if (EventType.propertyValueChange.equals(type)) {
                        try {
                            rootPropertyValue.setPropertyValue(path, jsonObject.get("value"));
                            Logger.debug("update property value -> " + jsonObject.get("value"));
                            synchronizationToOtherChannel(EventType.propertyValueChange, propertyValue, null,
                                    channels, channel, "修改属性值成功->" + propertyValue, PropertyValue.VALUE_FILTER_NOT_CHILDREN);
                        } catch (Exception e) {
                            String json = JSON.toJSONString(new R(false, "值更新失败-> " + e.getMessage(), e.getMessage()));
                            channel.writeAndFlush(new TextWebSocketFrame(json));
                        }
                    }
                    else if (EventType.propertyValueChangeForMapKey.equals(type)) {
                        String newKey = jsonObject.getString("newKey");
                        String newKeyIndex = jsonObject.getString("newKeyIndex");
                        if (MutableJson.isMap(propertyValue.getParent().getType())) {
                            Map map = propertyValue.getParent().getValue();
                            if (map.containsKey(propertyValue.getIndexKey())) {
                                Logger.debug("update map old key -> " + propertyValue.getIndexKey() + " new key -> " + newKeyIndex);
                                map.put(newKeyIndex, map.remove(propertyValue.getIndexKey()));
                                propertyValue.setName(newKey);
                                // 重置 nestedPath
                                propertyValue.setNestedPath(null);
                                synchronizationToOtherChannel(EventType.propertyValueChangeForMapKey, propertyValue,
                                        path, channels, channel, "更新了Map的Key成功->" + propertyValue, PropertyValue.VALUE_FILTER_NOT_CHILDREN);
                            }
                        }
                    }
                    else if (EventType.addChild.equals(type)) {
                        PropertyValue childPropertyValue = null;
                        String newName = jsonObject.getString("newPath");
                        String newKeyIndex = jsonObject.getString("newKeyIndex");

                        if (MutableJson.isMap(propertyValue.getType())) {
                            if (propertyValue.getPropertyValueByName(newName) == null) {
                                Map map = propertyValue.getValue();
                                map.put(newKeyIndex, null);
                                Logger.debug("add map new key -> " + newKeyIndex);
                                List<Integer> genericTypeIndexList = new ArrayList<>(propertyValue.getGenericTypeIndexList());
                                genericTypeIndexList.add(1);
                                childPropertyValue = propertyValue.getMutableJson().addChild(
                                        propertyValue, null,
                                        propertyValue.getChildren(),
                                        propertyValue.getName() + "[" + newKeyIndex + "]",
                                        propertyValue.getResolvableField(),
                                        genericTypeIndexList
                                );
                            }
                        }
                        else if (MutableJson.isCollection(propertyValue.getType()) || MutableJson.isArray(propertyValue.getType())) {
                            Logger.debug("add collection new value -> " + newKeyIndex);
                            PropertyValue tempChild = new PropertyValue(propertyValue.getName() + "[" + newKeyIndex + "]",
                                    null, propertyValue.getMutableJson());
                            tempChild.setParent(propertyValue);
                            childPropertyValue = propertyValue.addOrUpdateSibling(tempChild, null, "add");
                        }

                        synchronizationToOtherChannel(EventType.addChild, propertyValue, childPropertyValue, channels,
                                channel, "添加新节点成功->" + childPropertyValue, PropertyValue.VALUE_FILTER_NOT_CHILDREN);

                    }
                    else if (EventType.deletePropertyValue.equals(type)) {
                        List<PropertyValue> children = propertyValue.getParent().getChildren();
                        Class<?> parentType = propertyValue.getParent().getType();
                        Object parentValue = propertyValue.getParent().getValue();
                        if (MutableJson.isMap(parentType)) {
                            Map map = (Map) parentValue;
                            Object v = map.remove(propertyValue.getIndexKey());
                            boolean remove = children.remove(propertyValue);
                            Logger.debug("remove child from property[" + remove + "] -> " + propertyValue);
                        }
                        else if (MutableJson.isArray(parentType)) {
                            boolean remove = children.remove(propertyValue);
                            Logger.debug("remove child from property[" + remove + "] -> " + propertyValue);
                            Class<?> componentType = parentType.getComponentType();
                            Object[] oldArray = children.stream().map(PropertyValue::getValue).toArray();
                            Object result = Array.newInstance(componentType, children.size());
                            System.arraycopy(oldArray, 0, result, 0, oldArray.length);
                            propertyValue.getParent().setValue(result);
                        }
                        else if (MutableJson.isCollection(parentType)) {
                            Collection collection = (Collection) parentValue;
                            Iterator iterator = collection.iterator();
                            int i = 0;
                            while (iterator.hasNext()) {
                                Object next = iterator.next();
                                if (Integer.parseInt(propertyValue.getIndexKey()) == i) {
                                    Logger.debug("remove array value -> " + next);
                                    iterator.remove();
                                    break;
                                }
                                i++;
                            }
                            boolean remove = children.remove(propertyValue);
                            Logger.debug("remove child from property[" + remove + "] -> " + propertyValue);
                        }

                        synchronizationToOtherChannel(EventType.deletePropertyValue, propertyValue, null,
                                channels, channel, "删除子节点成功->" + propertyValue, PropertyValue.VALUE_FILTER_NOT_CHILDREN);

                        if (MutableJson.isCollectionOrMap(parentType)) {
                            removeListenerForCollectionProperty(propertyValue);
                        }
                    }
                }
            }
        }
    }

    public static void synchronizationToOtherChannel(EventType type,
                                                     PropertyValue propertyValue,
                                                     Object hint,
                                                     Set<Channel> channels,
                                                     Channel thisChannel,
                                                     String tip, ContextValueFilter filter) {
        for (Channel channel : new HashSet<>(channels)) {
            String json;
            if (thisChannel != channel) {
                json = JSON.toJSONString(new R(type, propertyValue, hint, "[" + (thisChannel == null ? "服务" : thisChannel) + "]" + tip),
                        filter,
                        SerializerFeature.WriteNullStringAsEmpty,
                        SerializerFeature.WriteNullNumberAsZero,
                        SerializerFeature.WriteMapNullValue);
            }
            else {
                json = JSON.toJSONString(new R(true, tip, EventType.TIP, null));
            }
            channel.writeAndFlush(new TextWebSocketFrame(json));
        }

    }

    private void removeListenerForCollectionProperty(PropertyValue propertyValue) {
        if (MutableJson.isCollectionOrMap(propertyValue.getType())) {
            List<PropertyValue> collectionToPropertyValues = propertyValue.getMutableJson().getCollectionToPropertyValues();
            boolean remove = collectionToPropertyValues.remove(propertyValue);
            Logger.debug("property value update-collection remove listen[" + remove + "] -> " + propertyValue);
            propertyValue.getChildren().forEach(this::removeListenerForCollectionProperty);
        }
    }

    public void add(Channel channel) {
        channels.add(channel);
    }

    public boolean remove(Channel channel) {
        return channels.remove(channel);
    }


}
