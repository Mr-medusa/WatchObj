package red.medusa.watchobj.core.web;

/**
 * 区分前段数据事件更新类型
 * 将更新事件写入到 MutableJson 或 其它 WebService Channel
 *
 * @author GHHu
 * @date 2023/4/13
 * @see PropertyValueChanelEventHandler
 */
public enum  EventType {
    TIP,
    addRoots,
    addRoot,
    removeRoot,
    propertyValueChange,
    propertyValueChangeForMapKey,
    addChild,
    deletePropertyValue
}
