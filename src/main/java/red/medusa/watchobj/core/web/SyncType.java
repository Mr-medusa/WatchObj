package red.medusa.watchobj.core.web;


import red.medusa.watchobj.core.CollectionUpdateData;

/**
 * 区分前段数据事件更新类型
 * 将更新事件写入到 MutableJson 或 其它 WebService Channel
 *
 * @author GHHu
 * @date 2023/5/17
 * @see CollectionUpdateData
 */
public enum SyncType {
    UPDATE_VALUE, ADD_CHILD,  REMOVE_ROOT, ADD_ROOT, DELETE
}