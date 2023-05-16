package red.medusa.watchobj.core;

import java.util.Collection;
import java.util.Map;
import java.util.StringJoiner;

/**
 * 字段属性值包装类,字段值 set 后使用
 *
 * @author <a href="mail"></a>
 * @see MutableJsonService#watchedFields
 *
 * @author GHHu
 * @date 2023/5/16
 */
public class FieldPropertyValueHolder {
    public final PropertyValue propertyValue;
    public final String fieldName;
    public final Object value;
    public final boolean isCollection;

    public FieldPropertyValueHolder(PropertyValue propertyValue, String fieldName, Object value, Class<?> classType) {
        this.propertyValue = propertyValue;
        this.fieldName = fieldName;
        this.value = value;
        this.isCollection = classType != null && (Collection.class.isAssignableFrom(classType)
                || Object[].class.isAssignableFrom(classType)
                || Map.class.isAssignableFrom(classType));
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", FieldPropertyValueHolder.class.getSimpleName() + "[", "]")
                .add("fieldName='" + fieldName + "'")
                .add("value=" + value)
                .add("isCollection=" + isCollection)
                .toString();
    }
}