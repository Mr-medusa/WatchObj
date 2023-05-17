package red.medusa.watchobj.core;

import com.alibaba.fastjson.annotation.JSONField;
import red.medusa.watchobj.core.web.SyncType;

import java.util.StringJoiner;

/**
 * MutableJson 发生的更新数据集
 *
 * @author GHHu
 * @date 2023/3/31
 *
 * @see MutableJson#eventQueue
 * @see MutableJsonService
 */
public class CollectionUpdateData {
    @JSONField(ordinal = 1)
    private final SyncType type;
    @JSONField(ordinal = 2)
    private Object value;
    @JSONField(ordinal = 3)
    private final PropertyValue propertyValue;

    private CollectionUpdateData(SyncType type, Object value, PropertyValue propertyValue) {
        this.type = type;
        this.value = value;
        this.propertyValue = propertyValue;
    }

    public static CollectionUpdateData updateValue(Object value, PropertyValue propertyValue) {
        return new CollectionUpdateData(SyncType.UPDATE_VALUE, value, propertyValue);
    }

    public static CollectionUpdateData add(Object value, PropertyValue propertyValue) {
        return new CollectionUpdateData(SyncType.ADD_CHILD, value, propertyValue);
    }

    public static CollectionUpdateData remove(Object value, PropertyValue propertyValue) {
        return new CollectionUpdateData(SyncType.DELETE, value, propertyValue);
    }

    public static CollectionUpdateData removeRoot(PropertyValue propertyValue) {
        return new CollectionUpdateData(SyncType.REMOVE_ROOT, null, propertyValue);
    }

    public static CollectionUpdateData addRoot(Object value, PropertyValue propertyValue) {
        return new CollectionUpdateData(SyncType.ADD_ROOT, value, propertyValue);
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public PropertyValue getPropertyValue() {
        return propertyValue;
    }

    public SyncType getType() {
        return type;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CollectionUpdateData.class.getSimpleName() + "[", "]")
                .add("type=" + type)
                .add("value=" + value)
                .add("propertyValue=" + propertyValue)
                .toString();
    }
}
