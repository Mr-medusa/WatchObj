package red.medusa.watchobj.core;


/**
 * PropertyValue 属性值改变事件
 *
 * @see PropertyValue#setPropertyValueByName(String, Object)
 */
public class PropertyValueChangeEvent {
    public final PropertyValue propertyValue;
    public final Object oldValue;
    public final Object newValue;

    public PropertyValueChangeEvent(PropertyValue propertyValue, Object oldValue, Object newValue) {
        this.propertyValue = propertyValue;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
}
