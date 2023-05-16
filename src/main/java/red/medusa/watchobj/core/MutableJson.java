package red.medusa.watchobj.core;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Predicate;

/**
 * 构建 PropertyValue Json 核心类
 *
 * @author GHHu
 * @date 2023/3/31
 */
public class MutableJson {
    /**
     * PropertyValue 数据更新事件队列
     */
    public final BlockingQueue<List<CollectionUpdateData>> eventQueue = new LinkedBlockingDeque<>();
    /**
     * 引用队列,对象 gc 后回收掉 PropertyValue
     *
     * @see MutableJsonService#enableUpdateWeakReferences()
     */
    private final ReferenceQueue<Object> queue = new ReferenceQueue<>();
    // 容纳 Object 及其容器类型
    private final Map<WeakReference<Object>, List<PropertyValue>> referencePropertyValues = new ConcurrentHashMap<>();
    /**
     * 父容器 PropertyValue 缓存
     */
    private final Map<Object, PropertyValue> parentContainingPropertyValues = Collections.synchronizedMap(new WeakHashMap<>());
    /**
     * 构建过程中从父容器缓存中移除的 PropertyValue 缓存
     */
    private final Map<Object, PropertyValue> removedContainingPropertyValues = Collections.synchronizedMap(new WeakHashMap<>());
    private final Map<Object, List<PropertyValue>> unableControlPropertyValues = Collections.synchronizedMap(new WeakHashMap<>());
    /**
     * 针对集合类型的 PropertyValue 缓存
     */
    private final List<PropertyValue> collectionToPropertyValues = Collections.synchronizedList(new ArrayList<>());
    public static Set<Class<?>> primitiveTypes = new HashSet<>(32);

    static {
        Collections.addAll(primitiveTypes,
                String.class, Date.class, LocalDate.class,
                LocalTime.class, LocalDateTime.class,
                Enum.class, Boolean.class,
                Byte.class, Character.class, Double.class, Float.class,
                Integer.class, Long.class, Short.class, BigDecimal.class);
        Collections.addAll(primitiveTypes, boolean.class, byte.class, char.class, double.class, float.class, int.class, long.class, short.class);
    }

    /**
     * 构建 JSON
     */
    public synchronized MutableJson buildNestedPropertyValue(Object containingObject) {
        Class<?> objClass = containingObject.getClass();
        String readableName;
        try {
            Field watchNameField = objClass.getDeclaredField("watchName");
            watchNameField.setAccessible(true);
            readableName = watchNameField.get(containingObject).toString();
        } catch (Exception ignored) {
            readableName = objClass.getSimpleName();
        }
        readableName = readableName + "#" + containingObject.hashCode();
        // 记录 ContainingObject 到一个队列，下一次将会发现它的名字
        PropertyValue containingPropertyValue = new PropertyValue(readableName, containingObject, this);
        // 可能已经存在parent
        if (this.updatePropertyValueForUnableControlPropertyValue(containingPropertyValue)) {
            parentContainingPropertyValues.put(containingObject, containingPropertyValue);
        }
        while (objClass != Object.class) {
            Field[] declaredFields = objClass.getDeclaredFields();
            if (declaredFields.length == 0) {
                objClass = objClass.getSuperclass();
                continue;
            }
            for (Field declaredField : declaredFields) {
                String name = declaredField.getName();
                if (name.startsWith("ajc$") || name.equals("watchName") || name.endsWith("this$0")) {
                    continue;
                }
                declaredField.setAccessible(true);
                Object o = null;
                try {
                    o = declaredField.get(containingObject);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                ResolvableField resolvableField = new ResolvableField(declaredField, containingObject);

                Class<?> resolved = resolvableField.resolved(o);
                PropertyValue propertyValue = null;
                // collection
                if (isCollectionOrMap(resolved)) {
                    if (o == null) {
                        Logger.debug("skip the null collection -> " + name);
                        continue;
                    }
                    propertyValue = buildNestedPropertyValueFromCollection(
                            o,
                            name,
                            resolved,
                            resolvableField,
                            new ArrayList<>());
                    propertyValue.parent = containingPropertyValue;
                    containingPropertyValue.children.add(propertyValue);
                }
                // Object type
                if (propertyValue == null) {
                    if (!isPrimitive(o)) {
                        propertyValue = addChild(
                                containingPropertyValue,
                                o,
                                containingPropertyValue.children,
                                name,
                                resolvableField,
                                new ArrayList<>()
                        );
                    }
                }
                // primitive type
                if (propertyValue == null) {
                    propertyValue = new PropertyValue(name, o, this);
                    propertyValue.parent = containingPropertyValue;
                    containingPropertyValue.children.add(propertyValue);
                }

                propertyValue.type = resolved;
                containingPropertyValue.resolvableField = resolvableField;
            }
            objClass = objClass.getSuperclass();
        }
        return this;
    }

    /**
     * 对集合的处理
     *
     * @param o
     * @param propertyName
     * @return
     */
    protected PropertyValue buildNestedPropertyValueFromCollection(Object o, String propertyName, Class<?> wrapperClass, ResolvableField rootResolvableField, List<Integer> indexList) {
        Logger.debug(wrapperClass + " -> buildNestedPropertyValueFromCollection[ " + propertyName + "] ");
        PropertyValue propertyValue = new PropertyValue(propertyName, o, this);
        propertyValue.isWrapper = true;
        propertyValue.type = wrapperClass;
        propertyValue.genericTypeIndexList = new ArrayList<>(indexList);
        referencePropertyValues.computeIfAbsent(propertyValue.getReference(), k -> new LinkedList<>()).add(propertyValue);
        if (o != null) {
            if (MutableJson.isCollection(wrapperClass) || MutableJson.isArray(wrapperClass)) {
                indexList.add(0);
                int counter = 0;
                for (Object e : unwrap(o)) {
                    addChild(propertyValue, e,
                            propertyValue.children,
                            propertyName + "[" + counter + "]",
                            rootResolvableField,
                            indexList);
                    counter++;
                }
            }
            else if (o instanceof Map) {
                indexList.add(1);
                Map oToUse = (Map) o;
                for (Object oKey : oToUse.keySet()) {
                    if (isPrimitive(oKey)) {
                        Object value = oToUse.get(oKey);
                        addChild(propertyValue,
                                value,
                                propertyValue.children,
                                propertyName + "[" + oKey + "]",
                                rootResolvableField,
                                indexList);
                    }
                    else {
                        if (Logger.isDebug()) {
                            Logger.debug("key type is not supported...");
                        }
                    }
                }
            }
        }
        propertyValue.resolvableField = rootResolvableField;
        collectionToPropertyValues.add(propertyValue);
        return propertyValue;
    }

    public PropertyValue addChild(PropertyValue containingPropertyValue, Object value,
                                  List<PropertyValue> children,
                                  String name, ResolvableField rootResolvableField,
                                  List<Integer> indexList) {
        Logger.debug(containingPropertyValue.getClass() + " -> addChild[ " + name + " ]");
        PropertyValue propertyValue;
        if (isPrimitive(value) || isPrimitive(rootResolvableField.generics(indexList))) {
            propertyValue = new PropertyValue(name, value, this);
            try {
                Class<?> generics = rootResolvableField.generics(indexList);
                propertyValue.type = generics == Object.class ? value != null ? value.getClass() : Object.class : generics;
            } catch (Exception e) {
                Logger.debug("Error generating - > " + name + " -> " + e.getMessage());
                // maybe map[map<string,string>]
                propertyValue.type = MutableJson.isPrimitive(value) ? value.getClass() : Object.class;
                propertyValue.setIsUnableControl(true);
            }
        }
        else {
            propertyValue = findPropertyValue(value);
            if (propertyValue == null) {
                propertyValue = new PropertyValue(name, value, this);
                propertyValue.setIsUnableControl(true);
                Logger.debug("find unableControlPropertyValues -> " + propertyValue);
                this.unableControlPropertyValues.computeIfAbsent(value, k -> Collections.synchronizedList(new ArrayList<>())).add(propertyValue);
            }
            else {
                Logger.debug("find PropertyValue -> " + propertyValue);
                propertyValue.setName(name);
                propertyValue.setIsUnableControl(false);

                eventQueue.offer(Collections.singletonList(CollectionUpdateData
                        .add(containingPropertyValue, propertyValue)));
            }
            if (isCollectionOrMap(value)) {
                PropertyValue childPropertyValue = buildNestedPropertyValueFromCollection(
                        value,
                        name,
                        unwrapType(value),
                        rootResolvableField,
                        indexList);
                childPropertyValue.parent = propertyValue;
                propertyValue.children.add(childPropertyValue);
                // 这里不需要加入到 Update Collection, 仅作为 collection 的父节点使用
                if (isMap(value)) {
                    propertyValue.setType(Map.class);
                }
                else {
                    propertyValue.setType(Collection.class);
                }
            }
        }
        propertyValue.resolvableField = rootResolvableField;
        propertyValue.genericTypeIndexList = new ArrayList<>(indexList);
        propertyValue.parent = containingPropertyValue;
        children.add(propertyValue);
        return propertyValue;
    }

    private boolean updatePropertyValueForUnableControlPropertyValue(PropertyValue containingPropertyValue) {
        List<PropertyValue> propertyValues = this.unableControlPropertyValues.get(containingPropertyValue.getValue());
        if (propertyValues == null) {
            return true;
        }
        Iterator<PropertyValue> iterator = propertyValues.iterator();
        while (iterator.hasNext()) {
            PropertyValue next = iterator.next();
            List<PropertyValue> children = next.getParent().getChildren();
            children.remove(next);
            children.add(containingPropertyValue);
            containingPropertyValue.setParent(next.getParent());
            containingPropertyValue.setName(next.getName());
            Logger.debug("reset unable control property -> unable property: " + next + ",new property: " + containingPropertyValue);
            iterator.remove();
        }
        return !this.unableControlPropertyValues.containsKey(containingPropertyValue.getValue());
    }

    protected static List<Object> unwrap(Object o) {
        if (o == null) {
            return null;
        }
        List<Object> oToUse = new ArrayList<>();
        if (o instanceof Collection) {
            oToUse.addAll(((Collection<?>) o));
        }
        else if (o instanceof Object[]) {
            Object[] oArray = (Object[]) o;
            Collections.addAll(oToUse, oArray);
        }
        else if (o.getClass().isArray()) {
            int length = Array.getLength(o);
            for (int i = 0; i < length; i++) {
                oToUse.add(Array.get(o, i));
            }
        }
        return oToUse;
    }

    public static boolean isPrimitive(Object value) {
        if (value instanceof Class) {
            return Enum.class.isAssignableFrom((Class<?>) value) || Number.class.isAssignableFrom((Class<?>) value) || primitiveTypes.contains(value);
        }
        return value == null || value instanceof Number || value instanceof Enum || primitiveTypes.contains(value.getClass());
    }

    public static boolean isCollectionOrMap(Object o) {
        if (o instanceof Class) {
            return Collection.class.isAssignableFrom((Class<?>) o) ||
                    Object[].class.isAssignableFrom((Class<?>) o) ||
                    Map.class.isAssignableFrom((Class<?>) o) ||
                    ((Class<?>) o).isArray();
        }
        return o instanceof Collection || o instanceof Object[] || o instanceof Map;
    }

    public static Class<?> unwrapType(Object o) {
        if (o instanceof Class<?>) {
            if (Map.class.isAssignableFrom((Class<?>) o)) {
                return Map.class;
            }
            if (Collection.class.isAssignableFrom((Class<?>) o)) {
                return Collection.class;
            }
            if (Object[].class.isAssignableFrom((Class<?>) o)) {
                return Object[].class;
            }
        }
        if (o instanceof Map) {
            return Map.class;
        }
        if (o instanceof Collections) {
            return Collection.class;
        }
        if (o.getClass().isArray()) {
            return Object[].class;
        }
        return String.class;
    }

    public static boolean isCollection(Object o) {
        if (o instanceof Class) {
            return Collection.class.isAssignableFrom((Class<?>) o);
        }
        return o instanceof Collection;
    }

    public static boolean isArray(Object o) {
        if (o instanceof Class) {
            return Object[].class.isAssignableFrom((Class<?>) o) || ((Class<?>) o).isArray();
        }
        return o instanceof Object[];
    }

    public static boolean isMap(Object o) {
        if (o instanceof Class) {
            return Map.class.isAssignableFrom((Class<?>) o);
        }
        return o instanceof Map;
    }


    /**
     * 更新集合
     */
    public List<CollectionUpdateData> updatePropertyValuesForCollection() {
        return updatePropertyValuesForCollection(new ArrayList<>(), p -> true);
    }

    public List<CollectionUpdateData> updatePropertyValuesForCollection(Predicate<PropertyValue> propertyValue) {
        return updatePropertyValuesForCollection(new ArrayList<>(), propertyValue);
    }

    public synchronized List<CollectionUpdateData> updatePropertyValuesForCollection(List<Object> updateObjects, Predicate<PropertyValue> predicate) {
        List<CollectionUpdateData> collectionUpdateData = new ArrayList<>();
        for (PropertyValue collectionToPropertyValue : new ArrayList<>(collectionToPropertyValues)) {
            if (updateObjects == null || !updateObjects.contains(collectionToPropertyValue.getValue())) {
                if (predicate.test(collectionToPropertyValue)) {
                    collectionToPropertyValue.updateValue(collectionUpdateData);
                }
            }
        }
        return collectionUpdateData;
    }

    public PropertyValue findPropertyValue(Object target, boolean... needRemoveParam) {
        boolean needRemove = needRemoveParam.length == 0 || needRemoveParam[0];
        if (parentContainingPropertyValues.containsKey(target)) {
            PropertyValue propertyValue = parentContainingPropertyValues.get(target);
            if (needRemove) {
                propertyValue = parentContainingPropertyValues.remove(target);
                if (Logger.isDebug()) {
                    Logger.debug("pc-remove: " + propertyValue);
                }
                eventQueue.offer(Collections.singletonList(CollectionUpdateData.removeRoot(propertyValue)));
                removedContainingPropertyValues.put(target, propertyValue);
            }
            return propertyValue;
        }
        if (removedContainingPropertyValues.containsKey(target)) {
            return removedContainingPropertyValues.get(target);
        }
        return null;
    }

    public List<PropertyValue> getJSONArray() {
        return new ArrayList<>(parentContainingPropertyValues.values());
    }

    public void printMsg() {
        List<PropertyValue> parentContainings = getJSONArray();
        for (PropertyValue propertyValue : parentContainings) {
            propertyValue.printMsg();
        }
    }

    public Map<Object, PropertyValue> getParentContainingPropertyValues() {
        return parentContainingPropertyValues;
    }

    public Map<Object, PropertyValue> getRemovedContainingPropertyValues() {
        return removedContainingPropertyValues;
    }


    // --- getter
    public ReferenceQueue<Object> getQueue() {
        return queue;
    }

    public Map<WeakReference<Object>, List<PropertyValue>> getReferencePropertyValues() {
        return referencePropertyValues;
    }

    public List<PropertyValue> getCollectionToPropertyValues() {
        return collectionToPropertyValues;
    }
}









