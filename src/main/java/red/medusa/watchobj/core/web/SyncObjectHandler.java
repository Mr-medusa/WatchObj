package red.medusa.watchobj.core.web;

import io.netty.channel.Channel;
import red.medusa.watchobj.core.CollectionUpdateData;
import red.medusa.watchobj.core.Logger;
import red.medusa.watchobj.core.MutableJsonService;
import red.medusa.watchobj.core.PropertyValue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author GHHu
 * @date 2023/4/13
 */
public class SyncObjectHandler {
    public static final String SYNC_OBJECT_TIME_MILLIS = "SYNC_OBJECT_TIME_MILLIS";
    /**
     * 每秒同步一次实例
     */
    private static int syncObjectTimeMillis = 1000;

    private final Set<Channel> channels;

    public SyncObjectHandler(Set<Channel> channels) {
        this.channels = channels;
    }

    public void start() {
        new Thread(() -> {
            while (true) {
                String syncObjectTimeMillis = System.getProperty(SYNC_OBJECT_TIME_MILLIS);
                if (syncObjectTimeMillis != null) {
                    SyncObjectHandler.syncObjectTimeMillis = Integer.parseInt(syncObjectTimeMillis);
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(SyncObjectHandler.syncObjectTimeMillis);
                } catch (InterruptedException exception) {
                    exception.printStackTrace();
                }
                MutableJsonService service = MutableJsonService.getInstance();
                if (service == null) {
                    continue;
                }
                BlockingQueue<List<CollectionUpdateData>> blockingQueue = service.getEventQueue();
                for (Channel channel : new HashSet<>(channels)) {
                    if (!channel.isWritable()) {
                        channels.remove(channel);
                        Logger.debug(channel + " -> isWritable = " + channel.isWritable());
                    }
                }
                List<CollectionUpdateData> updateDataList = blockingQueue.poll();
                if (updateDataList == null) {
                    continue;
                }

                Logger.debug("syncObjects...");
                for (CollectionUpdateData collectionUpdateData : updateDataList) {
                    switch (collectionUpdateData.getType()) {
                        case ADD_ROOT:
                            PropertyValueChanelEventHandler.synchronizationToOtherChannel(EventType.addRoot,
                                    collectionUpdateData.getPropertyValue(),
                                    null,
                                    channels,
                                    null,
                                    "添加新根实例成功[服务]->" + collectionUpdateData.getPropertyValue(),
                                    PropertyValue.VALUE_FILTER);
                            break;
                        case REMOVE_ROOT:
                            PropertyValueChanelEventHandler.synchronizationToOtherChannel(EventType.removeRoot,
                                    collectionUpdateData.getPropertyValue(),
                                    null,
                                    channels,
                                    null,
                                    "删除新根实例成功[服务]->" + collectionUpdateData.getPropertyValue(),
                                    PropertyValue.VALUE_FILTER_NOT_CHILDREN);
                            break;
                        case ADD_CHILD:
                            PropertyValueChanelEventHandler.synchronizationToOtherChannel(EventType.addChild,
                                    collectionUpdateData.getPropertyValue().getParent(),
                                    collectionUpdateData.getPropertyValue(),
                                    channels,
                                    null,
                                    "添加新子实例成功[服务]->" + collectionUpdateData.getPropertyValue(),
                                    PropertyValue.VALUE_FILTER);
                            break;
                        case UPDATE_VALUE:
                            PropertyValueChanelEventHandler.synchronizationToOtherChannel(EventType.propertyValueChange,
                                    collectionUpdateData.getPropertyValue(),
                                    null,
                                    channels,
                                    null,
                                    "修改属性值成功->" + collectionUpdateData.getPropertyValue(),
                                    PropertyValue.VALUE_FILTER_NOT_CHILDREN);

                            break;
                        case DELETE:
                            PropertyValueChanelEventHandler.synchronizationToOtherChannel(EventType.deletePropertyValue,
                                    collectionUpdateData.getPropertyValue(),
                                    null,
                                    channels,
                                    null,
                                    "删除子节点成功->" + collectionUpdateData.getPropertyValue(),
                                    PropertyValue.VALUE_FILTER_NOT_CHILDREN);
                            break;
                    }
                }
            }
        }).start();
    }

}
