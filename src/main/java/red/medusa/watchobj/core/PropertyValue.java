package red.medusa.watchobj.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.ContextValueFilter;

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author GHHu
 * @date 2023/3/31
 */
public class PropertyValue {
    @JSONField(name = "key", ordinal = 1)
    private String name;
    @JSONField(name = "isPrimitive", ordinal = 2)
    private Boolean isPrimitive;
    @JSONField(ordinal = 3)
    private Object value;
    @JSONField(name = "path", ordinal = 4)
    protected String nestedPath;
    @JSONField(name = "isUnableControl", ordinal = 5)
    private boolean isUnableControl;
    @JSONField(name = "isShowKey", ordinal = 6)
    private boolean isShowKey = true;
    @JSONField(ordinal = 7)
    protected List<PropertyValue> children = new ArrayList<>(1);

    @JSONField(serialize = false)
    protected PropertyValue parent;
    @JSONField(name = "isWrapper")
    protected Boolean isWrapper;
    @JSONField
    protected Class<?> type;
    @JSONField(serialize = false)
    protected ResolvableField resolvableField;
    @JSONField(serialize = false)
    protected List<Integer> genericTypeIndexList;
    @JSONField(serialize = false)
    private WeakReference<Object> reference;
    @JSONField(serialize = false)
    private Map<String, PropertyValue> elementMetaInfo = null;
    public static ContextValueFilter VALUE_FILTER = (context, object, name, value) -> VALUE_FILTER(name, object, value, true);
    public static ContextValueFilter VALUE_FILTER_NOT_CHILDREN = (context, object, name, value) -> VALUE_FILTER(name, object, value, false);

    public static Object VALUE_FILTER(String name, Object object, Object value, boolean withChildren) {
        if (object instanceof PropertyValue) {
            if (name.equals("value")) {
                if (MutableJson.isPrimitive(value)) {
                    return value;
                }
                PropertyValue propertyValue1 = (PropertyValue) object;
                if (propertyValue1.isUnableControl()) {
                    return JSON.toJSONString(value);
                }
                return "";
            }
            else if (name.equals("type")) {
                if (MutableJson.isMap(value)) {
                    return "map";
                }
                else if (MutableJson.isCollectionOrMap(value)) {
                    return "collection";
                }
                else if (MutableJson.isPrimitive(value)) {
                    return "primitive";
                }
                return value;
            }
            else if (name.equals("children") && !withChildren) {
                return new ArrayList<>();
            }
        }
        return value;
    }

    @JSONField(serialize = false)
    private final MutableJson mutableJson;

    public PropertyValue(String name, Object value, MutableJson mutableJson) {
        this.name = name;
        this.type = value != null ? value.getClass() : null;
        this.value = value;
        if (!MutableJson.isPrimitive(value)) {
            this.value = null;
            reference = new WeakReference<>(value, mutableJson.getQueue());
        }
        this.mutableJson = mutableJson;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PropertyValue> getChildren() {
        return children;
    }

    public void setChildren(List<PropertyValue> children) {
        this.children = children;
    }

    public PropertyValue getParent() {
        return parent;
    }

    public void setParent(PropertyValue parent) {
        this.parent = parent;
    }

    public void setNestedPath(String nestedPath) {
        this.nestedPath = nestedPath;
    }

    public boolean isUnableControl() {
        return isUnableControl;
    }

    protected void setIsUnableControl(boolean isUnableControl) {
        this.isUnableControl = isUnableControl;
    }

    public boolean isPrimitive() {
        if (this.isPrimitive == null) {
            if (this.type != null) {
                this.isPrimitive = MutableJson.isPrimitive(this.type) || MutableJson.isPrimitive(this.getValue());
            }
            else {
                this.isPrimitive = MutableJson.isPrimitive(this.getValue());
            }
        }
        return this.isPrimitive;
    }

    public boolean isWrapper() {
        if (this.isWrapper != null) {
            return this.isWrapper;
        }
        this.isWrapper = MutableJson.isCollectionOrMap(this.getType());
        return isWrapper;
    }

    public void setWrapper(Boolean wrapper) {
        isWrapper = wrapper;
    }

    public boolean isShowKey() {
        return isShowKey;
    }

    public void setShowKey(boolean showKey) {
        isShowKey = showKey;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public ResolvableField getResolvableField() {
        return resolvableField;
    }

    public void setResolvableField(ResolvableField resolvableField) {
        this.resolvableField = resolvableField;
    }

    public List<Integer> getGenericTypeIndexList() {
        return genericTypeIndexList;
    }

    public void setGenericTypeIndexList(List<Integer> genericTypeIndexList) {
        this.genericTypeIndexList = genericTypeIndexList;
    }

    // +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-
    // +                       implementation                                     -+-
    // +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-
    public PropertyValue getPropertyValue(String name) {
        // name = this.name + "." + name;
        LinkedList<PropertyValue> stack = new LinkedList<>();
        stack.push(this);
        while (!stack.isEmpty()) {
            List<PropertyValue> children = stack.pop().getChildren();
            for (PropertyValue child : children) {
                if (child.getNestedPath().equals(name)) {
                    return child;
                }
                stack.push(child);
            }
        }
        return new PropertyValue(name, null, mutableJson);
    }

    public PropertyValue getPropertyValueByName(String name) {
        LinkedList<PropertyValue> stack = new LinkedList<>();
        stack.push(this);
        while (!stack.isEmpty()) {
            List<PropertyValue> children = stack.pop().getChildren();
            for (PropertyValue child : children) {
                if (child.getName().equals(name)) {
                    return child;
                }
                stack.push(child);
            }
        }
        return null;
    }


    public PropertyValue setPropertyValue(String name, Object value) {
        LinkedList<PropertyValue> stack = new LinkedList<>();
        stack.push(this);
        while (!stack.isEmpty()) {
            List<PropertyValue> children = stack.pop().getChildren();
            for (PropertyValue child : children) {
                if (child.getNestedPath().equals(name)) {
                    child.setPropertyValue(value);
                    return child;
                }
                stack.push(child);
            }
        }
        return null;
    }

    public PropertyValue setDirectPropertyValue(Object value) {
        if (this.isPrimitive()) {
            this.value = value;
        }
        return this;
    }

    public PropertyValueChangeEvent setPropertyValueByName(String name, Object value) {
        try {
            LinkedList<PropertyValue> stack = new LinkedList<>();
            stack.push(this);
            while (!stack.isEmpty()) {
                List<PropertyValue> children = stack.pop().getChildren();
                for (PropertyValue child : children) {
                    if (child.getName().equals(name)) {
                        PropertyValueChangeEvent propertyValueChangeHolder = new PropertyValueChangeEvent(child, child.getValue(), value);
                        child.setPropertyValue(value);
                        return propertyValueChangeHolder;
                    }
                    stack.push(child);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setPropertyValue(Object value) {
        value = convertValueIfNecessary(value);
        if (MutableJson.isCollectionOrMap(value)) {
            //@see MutableJson.updatePropertyValuesForCollection()
            return;
        }
        else if (MutableJson.isMap(this.parent.getType())) {
            Map map = this.parent.getValue();
            map.put(this.getIndexKey(), value);
            this.setValue(value);
        }
        else if (MutableJson.isCollection(this.parent.getType()) || MutableJson.isArray(this.parent.getType())) {
            this.addOrUpdateSibling(this, value, "update");
            this.setValue(value);
        }
        else if (this.isPrimitive()) {
            Object parentValue = this.parent.getValue();
            try {
                Field declaredField = parentValue.getClass().getDeclaredField(this.name);
                declaredField.setAccessible(true);
                if (value != null && declaredField.getType().isEnum()) {
                    value = Enum.valueOf((Class) declaredField.getType(), String.valueOf(value));
                }
                declaredField.set(parentValue, value);
            } catch (Exception ignored) {
            }
            this.setValue(value);
        }
        else {
            if (value != null) {
                PropertyValue propertyValue = mutableJson.findPropertyValue(value);
                if (propertyValue != null) {
                    this.children = propertyValue.getChildren();
                    propertyValue.getChildren().forEach(it -> it.parent = this);
                    this.setValue(propertyValue.getValue());
                    propertyValue.parent = this;
                    mutableJson.getRemovedContainingPropertyValues().replace(value, this);
                }
                else {
                    this.setValue(value);
                    this.isUnableControl = true;
                }
            }
            else {
                PropertyValue removeContaining = mutableJson.getParentContainingPropertyValues().remove(this.getValue());
                PropertyValue removeDepends = mutableJson.getRemovedContainingPropertyValues().remove(this.getValue());
                if (MutableJson.isCollectionOrMap(this.type)) {
                    boolean remove = mutableJson.getCollectionToPropertyValues().remove(this);
                    Logger.debug("set property value with null & remove[" + remove + "] -> " + this);
                }
                Logger.debug("removeContaining=" + removeContaining + ",removeDepends=" + removeDepends + ",removeCollection=" + this);
                this.setValue(null);
                this.children.clear();
            }
        }
    }

    public PropertyValue addOrUpdateSibling(PropertyValue child, Object value, String operation) {
        value = child.convertValueIfNecessary(value);
        boolean isSuccess = false;
        try {
            // List
            if (operation.equals("update")) {
                List list = child.parent.getValue();
                list.set(Integer.parseInt(child.getIndexKey()), value);
            }
            else if (operation.equals("add")) {
                List list = child.parent.getValue();
                list.add(value);
                isSuccess = true;
            }
        } catch (Exception e) {
            try {
                // Set
                if (operation.equals("update")) {
                    Set set = child.parent.getValue();
                    set.add(value);
                }
                else if (operation.equals("add")) {
                    Set set = child.parent.getValue();
                    set.add(value);
                    isSuccess = true;
                }
            } catch (Exception e2) {
                try {
                    // array
                    if (operation.equals("update")) {
                        Array.set(child.parent.getValue(), Integer.parseInt(child.getIndexKey()), value);
                    }
                    else if (operation.equals("add")) {
                        Class<?> componentType = child.getType().getComponentType();
                        Object result = Array.newInstance(componentType, child.parent.children.size() + 1);
                        System.arraycopy(child.parent.getValue(), 0, result, 0, child.parent.children.size());
                        Array.set(result, child.parent.children.size() + 1, value);
                        isSuccess = true;
                    }
                } catch (Exception e3) {
                    e3.printStackTrace();
                    Logger.debug("Exception while setting value -> " + e3.getMessage() + "@" + Arrays.toString(e3.getStackTrace()));
                }
            }
        }
        if (isSuccess) {
            List<Integer> genericTypeIndexes = new ArrayList<>(child.parent.genericTypeIndexList);
            if (MutableJson.isCollection(child.parent.type)) {
                genericTypeIndexes.add(0);
            }
            else if (MutableJson.isMap(child.parent.type)) {
                genericTypeIndexes.add(1);
            }
            return child.getMutableJson().addChild(child.parent, value,
                    child.parent.children,
                    child.parent.getName() + "[" + child.parent.children.size() + "]",
                    this.resolvableField,
                    genericTypeIndexes
            );
        }
        return child;
    }

    static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private Object convertValueIfNecessary(Object value) {
        String valueStrToUse = String.valueOf(value);
        try {
            if (value == null || value.getClass() == Object.class || !(value instanceof String)) {
                return value;
            }
            else if (String.class.isAssignableFrom(this.type)) {
                return valueStrToUse;
            }
            else if (Boolean.class.isAssignableFrom(this.type)) {
                return Boolean.valueOf(valueStrToUse);
            }
            else if (Byte.class.isAssignableFrom(this.type)) {
                return Byte.valueOf(valueStrToUse);
            }
            else if (Character.class.isAssignableFrom(this.type)) {
                return valueStrToUse.charAt(0);
            }
            else if (Double.class.isAssignableFrom(this.type)) {
                return Double.valueOf(valueStrToUse);
            }
            else if (Float.class.isAssignableFrom(this.type)) {
                return Float.valueOf(valueStrToUse);
            }
            else if (Integer.class.isAssignableFrom(this.type)) {
                return Integer.valueOf(valueStrToUse);
            }
            else if (Long.class.isAssignableFrom(this.type)) {
                return Long.valueOf(valueStrToUse);
            }
            else if (Short.class.isAssignableFrom(this.type)) {
                return Short.valueOf(valueStrToUse);
            }
            else if (Number.class.isAssignableFrom(this.type)) {
                return new BigDecimal(valueStrToUse);
            }
            else if (Date.class.isAssignableFrom(this.type)) {
                try {
                    return format.parse(valueStrToUse);
                } catch (ParseException e) {
                    return value;
                }
            }
            else if (Calendar.class.isAssignableFrom(this.type)) {
                try {
                    Calendar instance = Calendar.getInstance();
                    instance.setTime(format.parse(valueStrToUse));
                    return instance;
                } catch (ParseException e) {
                    return value;
                }
            }
            else if (LocalDateTime.class.isAssignableFrom(this.type)) {
                return LocalTime.parse(valueStrToUse, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
            else if (LocalDate.class.isAssignableFrom(this.type)) {
                return LocalDate.parse(valueStrToUse, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
            else if (LocalTime.class.isAssignableFrom(this.type)) {
                return LocalTime.parse(valueStrToUse, DateTimeFormatter.ofPattern("HH:mm:ss"));
            }
            else if (Enum.class.isAssignableFrom(this.type)) {
                return Enum.valueOf(getEnumType(this.type), valueStrToUse.trim());
            }else if(value instanceof String) {
                return value;
            }
            else {
                throw new IllegalArgumentException("type mismatch for type[" + type + "] with value: " + value);
            }
        } catch (Exception e) {
            if ((valueStrToUse == null || valueStrToUse.trim().equals("")) && Number.class.isAssignableFrom(this.type)) {
                return 0;
            }
            throw new IllegalArgumentException("type mismatch for type[" + type + "] with value: " + value);
        }
    }

    public static Class<? extends Enum> getEnumType(Class<?> targetType) {
        Class<?> enumType = targetType;
        while (enumType != null && !enumType.isEnum()) {
            enumType = enumType.getSuperclass();
        }
        return (Class<? extends Enum>) enumType;
    }

    @JSONField(serialize = false)
    public String getIndexKey() {
        try {
            return name.substring(name.lastIndexOf("[") + 1, name.lastIndexOf("]"));
        } catch (Exception e) {
            Logger.debug(this.getName());
            e.printStackTrace();
            throw e;
        }
    }

    public String nestedPath() {
        PropertyValue currentPropertyValue = this;
        StringBuilder result = new StringBuilder();
        while (currentPropertyValue != null) {
            if (result.length() > 0) {
                result.insert(0, currentPropertyValue.name + ".");
            }
            else {
                result.insert(0, currentPropertyValue.name);
            }
            currentPropertyValue = currentPropertyValue.parent;
        }
        return result.toString();
    }

    public String getNestedPath() {
        if (this.nestedPath == null) {
            this.nestedPath = nestedPath();
        }
        return nestedPath;
    }

    protected void updateValue(List<CollectionUpdateData> updatedValue) {
        this.updateCollection(updatedValue);
        this.updateMap(updatedValue);
    }

    private void updateCollection(List<CollectionUpdateData> updatedValue) {
        if (Collection.class.isAssignableFrom(this.type) || MutableJson.isArray(this.type)) {
            if (hashCode(this.getValue()) == hashCode(this)) {
                return;
            }
            List<Object> newValueArray = MutableJson.unwrap(this.getValue());
            if (newValueArray == null) {
                return;
            }
            List<PropertyValue> children = this.children;
            if (children.size() <= newValueArray.size()) {
                for (int i = 0; i < children.size(); i++) {
                    Object o = newValueArray.get(i);
                    if ((o == null && children.get(i).getValue() != null) ||
                            o != null && o.hashCode() != children.get(i).hashCode()) {
                        if (MutableJson.isPrimitive(o)) {
                            children.get(i).setValue(newValueArray.get(i));
                            updatedValue.add(CollectionUpdateData.updateValue(o, children.get(i)));
                            Logger.debug("update child for collection: : " + children.get(i));
                        }
                        // 这里不用考虑对象模式
                    }
                }
            }
            int childrenSize = children.size();
            if (childrenSize < newValueArray.size()) {
                for (int i = childrenSize; i < newValueArray.size(); i++) {
                    PropertyValue child = mutableJson.addChild(
                            this,
                            newValueArray.get(i),
                            children,
                            name + "[" + i + "]",
                            this.resolvableField,
                            this.genericTypeIndexList);
                    updatedValue.add(CollectionUpdateData.add(newValueArray.get(i), child));
                    Logger.debug("add child for collection: " + child);
                }
            }
            else if (childrenSize > newValueArray.size()) {
                for (int i = newValueArray.size(); i < childrenSize; i++) {
                    PropertyValue remove = children.remove(i);
                    updatedValue.add(CollectionUpdateData.remove(remove.getValue(), remove));
                    Logger.debug("remove child for collection: " + remove);
                }
            }
        }
    }

    private void updateMap(List<CollectionUpdateData> updatedValue) {
        if (Map.class.isAssignableFrom(this.type)) {
            if (hashCode(this.getValue()) == hashCode(this)) {
                return;
            }
            if (this.elementMetaInfo == null) {
                this.elementMetaInfo = new HashMap<>();
            }
            this.elementMetaInfo.clear();
            for (PropertyValue child : this.children) {
                this.elementMetaInfo.put(child.getName(), child);
            }
            Map map = this.getValue() == null ? new HashMap<>() : this.getValue();
            Set<String> removedSet = new HashSet<>(this.elementMetaInfo.keySet());
            for (Object key : map.keySet()) {
                if (MutableJson.isPrimitive(key)) {
                    Object valueToUse = map.get(key);
                    // update
                    if (this.elementMetaInfo.containsKey(this.name + "[" + key + "]")) {
                        PropertyValue propertyValue = this.elementMetaInfo.get(this.name + "[" + key + "]");
                        if ((valueToUse == null && propertyValue.getValue() != null) ||
                                (valueToUse != null && valueToUse.hashCode() != propertyValue.hashCode())) {
                            if (MutableJson.isPrimitive(valueToUse)) {
                                propertyValue.setValue(valueToUse);
                                updatedValue.add(CollectionUpdateData.updateValue(valueToUse, propertyValue));
                                Logger.debug("update child for map: " + propertyValue);
                            }
                            else {
                                // 这里可能是 PropertyValue,交给它本身自己更新
                                // @see MutableJson#74
                                // PropertyValue pv = mutableJson.findPropertyValue(valueToUse);
                                // if (pv == null) {
                                //     pv = new PropertyValue(name + "[" + key + "]", valueToUse, mutableJson);
                                //     pv.setIsUnableControl(true);
                                //     pv.setParent(this);
                                // }
                            }
                        }
                    }
                    else {
                        PropertyValue propertyValue = mutableJson.addChild(
                                this,
                                valueToUse,
                                children,
                                name + "[" + key + "]",
                                this.resolvableField,
                                this.genericTypeIndexList
                        );
                        updatedValue.add(CollectionUpdateData.add(valueToUse, propertyValue));
                        Logger.debug("add child for map: " + propertyValue);
                    }
                    removedSet.remove(this.name + "[" + key + "]");
                }
            }
            // 只保留 key = String 类型的
            if (!removedSet.isEmpty()) {
                // remove...
                children.removeIf(next -> {
                            if (removedSet.contains(next.getName())) {
                                updatedValue.add(CollectionUpdateData.remove(next.getValue(), next));
                                Logger.debug("remove child for map: " + next);
                                return true;
                            }
                            else {
                                return false;
                            }
                        }
                );
            }
        }
    }

    public WeakReference<Object> getReference() {
        return reference;
    }

    public MutableJson getMutableJson() {
        return mutableJson;
    }

    private static class E {
        private final Object e;
        private final Object v;

        public E(Object e, Object v) {
            this.e = e;
            this.v = v;
        }

    }

    /**
     * @param value PropertyValue or Collection Or Map
     * @return hashCode
     */
    public static int hashCode(Object value) {
        if (value == null) {
            return -1;
        }
        boolean isMap = false;
        List<E> elements = new ArrayList<>(12);
        if (value instanceof PropertyValue) {
            PropertyValue pv = (PropertyValue) value;
            if (pv.getValue() == null) {
                return -1;
            }
            if (MutableJson.isMap(pv.getType())) {
                pv.getChildren().forEach(child -> {
                    elements.add(new E(child.getIndexKey(), child.getValue()));
                });
                isMap = true;
            }
            else {
                pv.getChildren().forEach(e -> elements.add(new E(e.getValue(), null)));
            }
        }
        else if (value instanceof Collection) {
            Collection<?> c = (Collection<?>) value;
            c.forEach(e -> elements.add(new E(e, null)));
        }
        else if (value instanceof Object[]) {
            Object[] oArr = ((Object[]) value);
            Arrays.stream(oArr).forEach(e -> elements.add(new E(e, null)));
        }
        else if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            map.forEach((k, v) -> elements.add(new E(k, v)));
            isMap = true;
        }
        int result = 1;
        for (E element : elements) {
            if (isMap) {
                result += (Objects.hashCode(element.e) ^ Objects.hashCode(element.v));
            }
            else {
                result = 31 * result + (element.e == null ? 0 : element.e.hashCode());
            }
        }
        return result;
    }

    public <T> T getValue() {
        if (this.reference != null) {
            // 检查一下JVM是否移除了 Value
            // if (this.reference.get() == null) {
            //     synchronized (this) {
            //         if (this.getParent() != null) {
            //             // boolean remove = this.getParent().getChildren().remove(this);
            //             // Logger.debug("移除 PV[获取值时动态更新]->" + remove + "->" + this.getNestedPath());
            //             Logger.debug("移除 PV[获取值时动态更新]->" + value + "->" + this.getNestedPath());
            //             // mutableJson.eventQueue.offer(Collections.singletonList(CollectionUpdateData.remove(null, this)));
            //         }
            //     }
            // }
            return (T) this.reference.get();
        }
        return (T) value;
    }

    public void setValue(Object value) {
        if (!MutableJson.isPrimitive(value)) {
            this.value = null;
            reference = new WeakReference<>(value, mutableJson.getQueue());
        }
        else {
            this.value = value;
            reference = null;
        }
    }

    public void printMsg(int... indent) {
        this.printMsg(false, indent);
    }

    public void printMsg(boolean isHideValue, int... indent) {
        int indentToUse = indent.length == 0 ? 0 : indent[0];
        String indentStr = "";
        for (int i = 0; i < indentToUse; i++) {
            indentStr += "        ";
        }
        System.out.println(indentStr + this.toString(isHideValue));
        this.children.forEach(child -> child.printMsg(isHideValue, indentToUse + 1));
    }

    @Override
    public String toString() {
        return this.nestedPath() + " -> type= " + this.type +
                " -> genericTypeIndexList = " + this.genericTypeIndexList +
                " -> value= " + this.getValue();

    }

    public String toString(boolean isHideValue) {
        if (isHideValue) {
            return this.nestedPath() + " -> isPrimitive = " + this.isPrimitive() + " -> type= " + this.type + " -> genericTypeIndexList = " + this.genericTypeIndexList + " -> isUnableControl = " + this.isUnableControl;
        }
        return this.nestedPath() + " -> isPrimitive= " + this.isPrimitive() + " -> type= " + this.type + " -> genericTypeIndexList = " + this.genericTypeIndexList + " -> value= " + this.getValue() + " -> isUnableControl = " + this.isUnableControl;
    }

    @Override
    public int hashCode() {
        return this.getValue() != null ? this.getValue().hashCode() : -999999999;
    }

}

