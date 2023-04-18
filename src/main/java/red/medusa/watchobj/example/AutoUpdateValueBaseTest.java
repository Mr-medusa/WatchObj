package red.medusa.watchobj.example;

import red.medusa.watchobj.core.MutableJson;
import red.medusa.watchobj.core.ResolvableField;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author GHHu
 * @date 2023/4/14
 */
public class AutoUpdateValueBaseTest {

    public static AtomicInteger counter = new AtomicInteger(10);

    public static void autoUpdateValue(Object containingObject, boolean isDelete) {
        Class<?> objClass = containingObject.getClass();
        while (objClass != Object.class) {
            Field[] declaredFields = objClass.getDeclaredFields();
            if (declaredFields.length == 0) {
                objClass = objClass.getSuperclass();
                continue;
            }
            counter.set(counter.get() * 2);
            for (Field declaredField : declaredFields) {
                declaredField.setAccessible(true);
                Object o = null;
                try {
                    o = declaredField.get(containingObject);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                updateValue(containingObject,
                        objClass,
                        declaredField,
                        new ResolvableField(declaredField, containingObject),
                        new ArrayList<>(),
                        o, isDelete);
            }
            objClass = objClass.getSuperclass();
        }
    }

    public static void updateValue(Object object,
                                   Class<?> objClass,
                                   Field declaredField,
                                   ResolvableField resolvableField, List<Integer> genericIndex, Object o, boolean isRemove) {
        String fieldName = findSetMethodName(declaredField);
        if (MutableJson.isCollection(resolvableField.resolved())) {
            genericIndex.add(0);
        }
        else if (MutableJson.isMap(resolvableField.resolved())) {
            genericIndex.add(1);
        }
        if (o instanceof List) {
            List oList = (List) o;
            int size = oList.size();
            if (isRemove) {
                oList.removeIf((it) -> {
                    sleep();
                    return true;
                });
            }
            else {
                Class<?> generics = resolvableField.generics(genericIndex);
                for (int i = 0; i < size; i++) {
                    if (MutableJson.isCollectionOrMap(oList.get(i))) {
                        updateValue(null, null, null, resolvableField, genericIndex, oList.get(i), isRemove);
                    }
                    else {
                        oList.set(i, createValue(fieldName, oList.get(i), generics));
                    }
                }
                if (MutableJson.isPrimitive(generics) && size == 0) {
                    for (int i = 0; i < 5; i++) {
                        oList.add(createValue(fieldName, i, generics));
                    }
                }
            }
        }
        else if (o instanceof Set) {
            Set oSet = (Set) o;
            if (isRemove) {
                oSet.removeIf((it) -> {
                    sleep();
                    return true;
                });
            }
            else {
                int size = oSet.size();
                Iterator iterator = oSet.iterator();
                while (iterator.hasNext()) {
                    Object next = iterator.next();
                    if (MutableJson.isCollectionOrMap(next)) {
                        updateValue(null, null, null, resolvableField, genericIndex, next, isRemove);
                    }
                    else {
                        iterator.remove();
                    }
                }
                for (int i = 0; i < (size == 0 ? 5 : size); i++) {
                    oSet.add(createValue(fieldName, i, resolvableField.generics(genericIndex)));
                }
            }
        }
        else if (declaredField.getType().isArray()) {
            Type componentType = declaredField.getGenericType();
            if (!(componentType instanceof Class<?>)) {
                genericIndex.add(0);
                componentType = resolvableField.generics(genericIndex);
            }
            else {
                componentType = declaredField.getType().getComponentType();
            }
            int size = Array.getLength(o);
            if (isRemove) {
                for (int i = 0; i < size; i++) {
                    Array.set(o, i, createValue(fieldName, null, (Class<?>) componentType));
                }
            }
            else {
                for (int i = 0; i < size; i++) {
                    if (MutableJson.isCollectionOrMap(Array.get(o, i))) {
                        updateValue(null, null, null, resolvableField, genericIndex, Array.get(o, i), isRemove);
                    }
                    else {

                        Array.set(o, i, createValue(fieldName, Array.get(o, i), (Class<?>) componentType));
                    }
                }
            }
        }
        else if (o instanceof Map) {
            Map map = (Map) o;
            if (isRemove) {
                Set set = map.keySet();
                set.removeIf((it) -> {
                    sleep();
                    return true;
                });
            }
        }
        else if (!MutableJson.isPrimitive(o)) {
            autoUpdateValue(o, isRemove);
        }
        else {
            try {
                Class<?> clazz = Object.class;
                if (declaredField.getGenericType() instanceof Class<?>) {
                    clazz = (Class<?>) declaredField.getGenericType();
                }
                Method declaredMethod = objClass.getDeclaredMethod(fieldName, clazz);
                declaredMethod.setAccessible(true);
                if (isRemove) {
                    sleep();
                    declaredMethod.invoke(object, (Object) null);
                }
                else {
                    declaredMethod.invoke(object, createValue(fieldName, o, resolvableField.generics(genericIndex)));
                }
            } catch (Exception ignore) {
            }
        }
    }

    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd@HH:mm:ss.SSS");

    public static Object createValue(String fieldName, Object o, Class<?> type) {
        if(type == Object.class && o != null ){
            type = o.getClass();
        }
        sleep();
        try {
            if (o == null && type.isPrimitive()) {
                return -1;
            }
            else if (Character.class.isAssignableFrom(type)) {
                return (char) new Random().nextInt();
            }
            else if (String.class.isAssignableFrom(type)) {
                return o == null ? "NULL" + counter.get() : o + "" + counter.get();
            }
            else if (BigDecimal.class.isAssignableFrom(type)) {
                return o == null ? new BigDecimal(counter.get()) : BigDecimal.ONE.multiply(new BigDecimal(counter.get()));
            }
            else if (Number.class.isAssignableFrom(type)) {
                return o == null ? -1 : (int) o + counter.get();
            }
            else if (Date.class.isAssignableFrom(type)) {
                return simpleDateFormat.format(new Date());
            }
            else if (type.isPrimitive()) {
                return counter.get();
            }
            else {
                return o;
            }

        } catch (Exception e) {
            System.err.println(fieldName);
            throw e;
        }
    }

    public static void sleep() {
        try {
            TimeUnit.SECONDS.sleep((int) (Math.random() * 2) + 1);
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }
    }

    public static void sleep(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }
    }

    public static String findSetMethodName(Field it) {
        String setterName;
        String fieldName = it.getName();
        String typeName = it.getType().getName();
        if ("boolean".equals(typeName) || "java.lang.Boolean".equals(typeName)) {
            if (fieldName.length() >= 2 && fieldName.startsWith("is") && Character.isUpperCase(fieldName.charAt(2))) {
                setterName = "set" + toUpperCaseFirst(fieldName.replaceFirst("is", ""));
            }
            else {
                setterName = "set" + toUpperCaseFirst(fieldName);
            }
        }
        else if (fieldName.length() >= 2 && Character.isUpperCase(fieldName.charAt(1))) {
            setterName = "set" + fieldName;
        }
        else {
            setterName = "set" + toUpperCaseFirst(fieldName);
        }
        return setterName;
    }

    public static String toUpperCaseFirst(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}
