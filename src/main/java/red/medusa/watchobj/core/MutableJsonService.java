package red.medusa.watchobj.core;

import red.medusa.watchobj.server.HttpServer;

import java.lang.ref.Reference;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;


/**
 * @date 2023/4/4
 */
public class MutableJsonService {
    private final MutableJson mutableJson = new MutableJson();

    public static final String ENABLE_MUTABLE_JSON_SERVER = "ENABLE_MUTABLE_JSON_SERVER";
    public static final String ENABLE_MUTABLE_JSON_UPDATE_COLLECTION = "ENABLE_MUTABLE_JSON_UPDATE_COLLECTION";
    public static final String ENABLE_SERVER = "ENABLE_SERVER";
    public static final String OBJECT_UPDATE_TIME_MILLIS = "OBJECT_UPDATE_TIME_MILLIS";
    public static final String FIELD_UPDATE_TIME_MILLIS = "FIELD_UPDATE_TIME_MILLIS";
    public static final String COLLECTION_UPDATE_TIME_MILLIS = "COLLECTION_UPDATE_TIME_MILLIS";
    public static final String WEAK_REFERENCE_UPDATE_TIME_MILLIS = "WEAK_REFERENCE_UPDATE_TIMEMILLIS";
    public static final String SERVER_HTTP_PORT = "SERVER_HTTP_PORT";
    public static final String SERVER_WEBSOCKET_PORT = "SERVER_WEBSOCKET_PORT";

    private final Set<Object> watchedObjects = Collections.synchronizedSet(new LinkedHashSet<>());
    private final List<FieldPropertyValueHolder> watchedFields = Collections.synchronizedList(new LinkedList<>());

    private boolean enableMutableJson = true;
    private boolean enableMutableJsonUpdateCollection = true;
    private int objectUpdateTimeMillis = 200;
    private int fieldUpdateTimeMillis = 200;
    private int collectionUpdateTimeMillis = 2000;
    private int weakReferenceUpdateTimeMillis = 10000;

    private boolean enableServer = true;
    private int serverHttpPort = 8888;
    private int serverWebsocketPort = 9999;

    private volatile boolean finished = false;

    private MutableJsonService() {
        String enableMutableJson = System.getProperty(ENABLE_MUTABLE_JSON_SERVER);
        String enableServer = System.getProperty(ENABLE_SERVER);
        String triggerObjectsMillisTime = System.getProperty(OBJECT_UPDATE_TIME_MILLIS);
        String triggerFieldsMillis = System.getProperty(FIELD_UPDATE_TIME_MILLIS);
        String collectionUpdateTimeMillis = System.getProperty(COLLECTION_UPDATE_TIME_MILLIS);
        String weakReferenceUpdateTimeMillis = System.getProperty(WEAK_REFERENCE_UPDATE_TIME_MILLIS);
        String serverHttpPort = System.getProperty(SERVER_HTTP_PORT);
        String serverWebsocketPort = System.getProperty(SERVER_WEBSOCKET_PORT);
        String enableMutableJsonUpdateCollection = System.getProperty(ENABLE_MUTABLE_JSON_UPDATE_COLLECTION);
        if (enableMutableJson != null) {
            this.setEnableMutableJson(Boolean.parseBoolean(enableMutableJson));
        }
        if (enableServer != null) {
            this.setEnableServer(Boolean.parseBoolean(enableServer));
        }
        if (enableMutableJsonUpdateCollection != null) {
            this.enableMutableJsonUpdateCollection = Boolean.parseBoolean(enableMutableJsonUpdateCollection);
        }
        if (triggerObjectsMillisTime != null) {
            this.setObjectUpdateTimeMillis(Math.min(Integer.parseInt(triggerObjectsMillisTime), (int) TimeUnit.MINUTES.toMillis(1)));
        }
        if (triggerFieldsMillis != null) {
            this.setFieldUpdateTimeMillis(Math.min(Integer.parseInt(triggerFieldsMillis), (int) TimeUnit.MINUTES.toMillis(1)));
        }
        if (collectionUpdateTimeMillis != null) {
            this.setCollectionUpdateTimeMillis(Math.min(Integer.parseInt(collectionUpdateTimeMillis), (int) TimeUnit.MINUTES.toMillis(1)));
        }
        if (weakReferenceUpdateTimeMillis != null) {
            this.setWeakReferenceUpdateTimeMillis(Math.min(Integer.parseInt(weakReferenceUpdateTimeMillis), (int) TimeUnit.MINUTES.toMillis(1)));
        }
        if (serverHttpPort != null) {
            this.setServerHttpPort(Integer.parseInt(serverHttpPort));
        }
        if (serverWebsocketPort != null) {
            this.setServerHttpPort(Integer.parseInt(serverWebsocketPort));
        }
        if (Logger.isDebug()) {
            String parameters = String.format("is enable: %s, is enable server: %s, object update period:%s, " +
                            "field update periods: %s,collection update period: %s, http port: %s, websocket port: %s",
                    this.enableMutableJson, this.enableServer, this.objectUpdateTimeMillis,
                    this.fieldUpdateTimeMillis, this.collectionUpdateTimeMillis, this.serverHttpPort, this.serverWebsocketPort);
            Logger.debug("initializing handler with parameters: " + parameters);
        }

        this.enableUpdateMutableJsonCollection();
        this.enableUpdateWeakReferences();
        this.enableServer();
    }

    /**
     * 新增的 PropertyValue
     */
    private final Consumer<List<CollectionUpdateData>> objectCreatedConsumer = it -> {
        if (it == null || it.isEmpty()) {
            return;
        }
        mutableJson.eventQueue.offer(it);
    };

    /**
     * 更新的字段
     */
    private final Consumer<List<CollectionUpdateData>> fieldUpdateDataConsumer = it -> {
        if (it == null || it.isEmpty()) {
            return;
        }
        Logger.debug("更新的字段= " + it);
        mutableJson.eventQueue.offer(it);
    };

    /**
     * 构建对象
     */
    private final DebounceTask<List<CollectionUpdateData>> watchedObjectsTask = DebounceTask.build(() -> {
        Set<PropertyValue> roots = new HashSet<>();
        synchronized (watchedObjects) {
            Iterator<Object> iterator = watchedObjects.iterator();
            while (iterator.hasNext()) {
                Object target = iterator.next();
                Logger.debug("watched object " + target.getClass().getName() + "-" + target.hashCode());
                if (!mutableJson.getParentContainingPropertyValues().containsKey(target)) {
                    roots.add(mutableJson.getParentContainingPropertyValues().get(target));
                }
                mutableJson.buildNestedPropertyValue(target);
                iterator.remove();
            }
        }
        List<CollectionUpdateData> propertyValues = new ArrayList<>();
        roots.forEach(propertyValue -> propertyValues.add(CollectionUpdateData.addRoot(null, propertyValue)));
        return propertyValues;
    }, objectUpdateTimeMillis, watchedObjects::isEmpty, objectCreatedConsumer);

    /**
     * 更新集合字段
     */
    private final DebounceTask<List<CollectionUpdateData>> watchedFieldsTask = DebounceTask.build(() -> {
        List<CollectionUpdateData> list = new ArrayList<>();
        List<Object> updateObjects = new ArrayList<>();
        synchronized (watchedFields) {
            Iterator<FieldPropertyValueHolder> iterator = watchedFields.iterator();
            while (iterator.hasNext()) {
                FieldPropertyValueHolder holder = iterator.next();
                PropertyValue propertyValue = holder.propertyValue;
                PropertyValueChangeEvent changeHolder = propertyValue.setPropertyValueByName(holder.fieldName, holder.value);
                if (changeHolder.newValue != changeHolder.oldValue && !holder.isCollection) {
                    list.add(CollectionUpdateData.updateValue(changeHolder.newValue, changeHolder.propertyValue));
                }
                if (holder.isCollection) {
                    updateObjects.add(holder.value);
                }
                iterator.remove();
            }
        }
        if (!updateObjects.isEmpty()) {
            List<CollectionUpdateData> updateDataMap = mutableJson.updatePropertyValuesForCollection();
            list.addAll(updateDataMap);
        }
        return list;
    }, fieldUpdateTimeMillis, watchedFields::isEmpty, fieldUpdateDataConsumer);

    // --- init
    private void enableUpdateWeakReferences() {
        if (!enableMutableJson) {
            return;
        }
        Thread thread = new Thread(() -> {
            while (!finished) {
                try {
                    TimeUnit.MILLISECONDS.sleep(this.weakReferenceUpdateTimeMillis);
                } catch (InterruptedException exception) {
                    exception.printStackTrace();
                }
                Reference<?> poll = mutableJson.getQueue().poll();
                if (poll != null) {
                    if (Logger.isDebug()) {
                        Logger.debug("移除 poll->" + poll);
                    }
                }
                else {
                    continue;
                }
                synchronized (this) {
                    List<PropertyValue> propertyValues = mutableJson.getReferencePropertyValues().get(poll);
                    if (propertyValues != null && !propertyValues.isEmpty()) {
                        Iterator<PropertyValue> iterator = propertyValues.iterator();
                        while (iterator.hasNext()) {
                            PropertyValue propertyValue = iterator.next();
                            if (propertyValue.getParent() != null) {
                                boolean remove = propertyValue.getParent().getChildren().remove(propertyValue);
                                if (Logger.isDebug()) {
                                    Logger.debug("移除 PV->" + remove + "->" + propertyValue);
                                }
                            }
                            mutableJson.eventQueue.offer(Collections.singletonList(CollectionUpdateData.remove(propertyValue.getValue(), propertyValue)));
                            iterator.remove();
                        }
                        if (propertyValues.isEmpty()) {
                            mutableJson.getReferencePropertyValues().remove(poll);
                        }
                    }
                }
            }
        });
        thread.start();
    }

    private void enableUpdateMutableJsonCollection() {
        if (!enableMutableJson || !this.enableMutableJsonUpdateCollection) {
            return;
        }
        Thread thread = new Thread(() -> {
            while (!finished) {
                try {
                    TimeUnit.MILLISECONDS.sleep(this.collectionUpdateTimeMillis);
                } catch (InterruptedException exception) {
                    exception.printStackTrace();
                }
                synchronized (this) {
                    List<CollectionUpdateData> collectionUpdateData = this.getMutableJson().updatePropertyValuesForCollection();
                    fieldUpdateDataConsumer.accept(collectionUpdateData);
                }
            }
        });
        thread.start();
    }

    public void enableServer() {
        if (!enableServer || !enableMutableJson) {
            return;
            // new Thread(() -> {
            //     try {
            //         TimeUnit.SECONDS.sleep(5);
            //     } catch (InterruptedException exception) {
            //         exception.printStackTrace();
            //     }
            // new HttpServer(8,8).bind();
            // }).start();
        }
        System.setProperty(ENABLE_MUTABLE_JSON_SERVER, "true");
        System.setProperty(ENABLE_SERVER, "true");
        new HttpServer(this.serverHttpPort, this.serverWebsocketPort).bind();
    }


    /**
     * 字段更新后调用
     */
    public void beforeRoll() {
        if (!this.enableMutableJson) {
            return;
        }
        try {
            // 等待所有的字段都设置完成
            watchedObjectsTask.timerRun();
        } catch (Exception e) {
            if (Logger.isDebug()) {
                Logger.debug("beforeRoll exception -> " + e.getMessage() + "\n" + Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining("\n")));
            }
        }
    }

    /**
     * 字段更新后调用
     *
     * @param target
     * @param fieldName 字段名
     * @param value     字段值
     * @param classType 字段类型
     */
    public void roll(Object target, String fieldName, Object value, Class<?> classType) {
        if (!this.enableMutableJson) {
            return;
        }
        try {
            // watchedObjectsTask 执行之前这里都不会查询得到,就不用再设置了
            PropertyValue propertyValue = mutableJson.findPropertyValue(target, false);
            if (propertyValue == null) {
                return;
            }
            FieldPropertyValueHolder fieldPropertyValueHolder = new FieldPropertyValueHolder(propertyValue, fieldName, value, classType);
            watchedFields.add(fieldPropertyValueHolder);
            watchedFieldsTask.timerRun();
        } catch (Exception e) {
            if (Logger.isDebug()) {
                Logger.debug("roll exception -> " + e.getMessage() + "\n" + Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining("\n")));
            }
        }
    }

    public void watchObject(Object target) {
        try {
            if (!enableMutableJson) {
                return;
            }
            watchedObjects.add(target);
            watchedObjectsTask.timerRun();
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    public BlockingQueue<List<CollectionUpdateData>> getEventQueue() {
        return mutableJson.eventQueue;
    }

    public void updateMutableJsonCollection() {
        this.mutableJson.getCollectionToPropertyValues().forEach(it -> it.updateValue(new ArrayList<>()));
    }

    // ---
    public int getCollectionUpdateTimeMillis() {
        return collectionUpdateTimeMillis;
    }

    public void setCollectionUpdateTimeMillis(int collectionUpdateTimeMillis) {
        this.collectionUpdateTimeMillis = collectionUpdateTimeMillis;
    }

    public int getObjectUpdateTimeMillis() {
        return objectUpdateTimeMillis;
    }

    public void setObjectUpdateTimeMillis(int objectUpdateTimeMillis) {
        this.objectUpdateTimeMillis = objectUpdateTimeMillis;
    }

    public int getFieldUpdateTimeMillis() {
        return fieldUpdateTimeMillis;
    }

    public void setFieldUpdateTimeMillis(int fieldUpdateTimeMillis) {
        this.fieldUpdateTimeMillis = fieldUpdateTimeMillis;
    }

    public int getWeakReferenceUpdateTimeMillis() {
        return weakReferenceUpdateTimeMillis;
    }

    public void setWeakReferenceUpdateTimeMillis(int weakReferenceUpdateTimeMillis) {
        this.weakReferenceUpdateTimeMillis = weakReferenceUpdateTimeMillis;
    }

    public boolean isEnableServer() {
        return enableServer;
    }

    public void setEnableServer(boolean enableServer) {
        this.enableServer = enableServer;
    }

    public int getServerHttpPort() {
        return serverHttpPort;
    }

    public void setServerHttpPort(int serverHttpPort) {
        this.serverHttpPort = serverHttpPort;
    }

    public int getServerWebsocketPort() {
        return serverWebsocketPort;
    }

    public void setServerWebsocketPort(int serverWebsocketPort) {
        this.serverWebsocketPort = serverWebsocketPort;
    }

    public boolean isEnableMutableJson() {
        return enableMutableJson;
    }

    public void setEnableMutableJson(boolean enableMutableJson) {
        this.enableMutableJson = enableMutableJson;
    }

    public MutableJson getMutableJson() {
        return mutableJson;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    private volatile static MutableJsonService singleton;

    public static MutableJsonService getInstance() {
        if (null == singleton) {
            synchronized (MutableJsonService.class) {
                if (null == singleton) {
                    singleton = new MutableJsonService();
                }
            }
        }
        return singleton;
    }
}


