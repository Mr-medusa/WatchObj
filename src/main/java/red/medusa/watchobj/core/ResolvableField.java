package red.medusa.watchobj.core;

import sun.reflect.generics.reflectiveObjects.GenericArrayTypeImpl;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 解析具体的泛型字段类型
 * <p>
 * example:
 *
 * <pre>
 *     public static class O<I> {
 *         String str;
 *         I[] i;
 *     }
 *     public static class Foo<T> extends O<T> {
 *         T t;
 *     }
 *
 *     public static class Bar extends Foo<BigDecimal> {
 *
 *     }
 *     public static void main(String[] args) throws Exception {
 *         Field strField = O.class.getDeclaredField("str");
 *         Field iField = O.class.getDeclaredField("i");
 *         System.out.println(new ResolvableField(strField, Bar.class).resolved());
 *         System.out.println(new ResolvableField(iField, Bar.class).resolved());
 *         System.out.println(new ResolvableField(iField, Bar.class).generics(0));
 *         // output:
 *         class java.lang.String
 *         class [Ljava.math.BigDecimal;
 *         class java.math.BigDecimal
 *     }
 * </pre>
 *
 * @author GHHu
 * @date 2023/4/3
 */
public class ResolvableField {
    private ResolvableField pre;
    private Class<?> implementationClass;
    private String[] ownerNames;
    private Type[] ownerTypes;
    private Type[] ownerActualImplTypes;
    private TypeVariable<?>[] typeVariables;
    private Field field;
    private Class<?> resolved;
    private static final Map<Integer, Class<?>> cache = new ConcurrentHashMap<>();
    private static final Map<Integer, Map<Integer, Class<?>>> genericsCache = new ConcurrentHashMap<>();

    private ResolvableField(String[] ownerNames, Type[] ownerTypes, TypeVariable<?>[] typeVariables, Type[] ownerActualImplTypes) {
        this.ownerNames = ownerNames;
        this.ownerTypes = ownerTypes;
        this.typeVariables = typeVariables;
        this.ownerActualImplTypes = ownerActualImplTypes;
    }

    @Override
    public String toString() {
        return new StringJoiner("\n", "", "\n")
                .add("ownerNames=" + Arrays.toString(ownerNames))
                .add("ownerTypes=" + Arrays.toString(ownerTypes))
                .add("ownerActualImplTypes=" + Arrays.toString(ownerActualImplTypes))
                .toString();
    }

    private static ResolvableField fromClass(Class<?> objClass) {
        ResolvableField holder = null;
        while (objClass != Object.class) {
            Type genericSuperclass = objClass.getGenericSuperclass();
            if (genericSuperclass instanceof Class) {
                return holder;
            }
            TypeVariable<? extends Class<?>>[] superClassTypeParameters = objClass.getSuperclass().getTypeParameters();
            Type[] actualTypeArguments = ((ParameterizedTypeImpl) genericSuperclass).getActualTypeArguments();
            ResolvableField parentHolder = new ResolvableField(
                    new String[actualTypeArguments.length],
                    new Type[actualTypeArguments.length],
                    new TypeVariable[actualTypeArguments.length],
                    new Type[actualTypeArguments.length]
            );
            parentHolder.pre = holder;
            int count = 0;
            for (Type actualTypeArgument : actualTypeArguments) {
                String typeParameterName = superClassTypeParameters[count].getName();
                parentHolder.typeVariables[count] = superClassTypeParameters[count];
                parentHolder.ownerNames[count] = typeParameterName;
                parentHolder.ownerTypes[count] = actualTypeArgument;
                if (actualTypeArgument instanceof TypeVariable && holder != null) {
                    TypeVariable<?> actualTypeVariable = (TypeVariable<?>) actualTypeArgument;
                    String implementVariableName = actualTypeVariable.getName();
                    ResolvableField tempActualTypeArgumentHolder = holder;
                    while (tempActualTypeArgumentHolder.pre != null) {
                        tempActualTypeArgumentHolder = tempActualTypeArgumentHolder.pre;
                        implementVariableName = tempActualTypeArgumentHolder.ownerNames[count];
                        tempActualTypeArgumentHolder = tempActualTypeArgumentHolder.pre;
                    }
                    for (int i = 0; i < tempActualTypeArgumentHolder.ownerNames.length; i++) {
                        if (implementVariableName.equals(tempActualTypeArgumentHolder.ownerNames[i])) {
                            parentHolder.ownerActualImplTypes[count] = tempActualTypeArgumentHolder.ownerTypes[i];
                            break;
                        }
                    }
                }
                else {
                    parentHolder.ownerActualImplTypes[count] = actualTypeArgument;
                }
                count++;
            }
            holder = parentHolder;
            objClass = objClass.getSuperclass();
        }
        return holder;
    }


    public ResolvableField(Field field, Class<?> implementationClass) {
        if (!field.getDeclaringClass().isAssignableFrom(implementationClass)) {
            throw new IllegalArgumentException("type not assignable from " + implementationClass);
        }
        this.implementationClass = implementationClass;
        this.field = field;
    }

    public ResolvableField(Field field, Object instance) {
        this(field, instance.getClass());
    }

    public Class<?> resolved(Object fallbackFromGuessObject) {
        if (this.resolved == null) {
            this.resolved = this.resolved();
        }
        if (this.resolved == Object.class && fallbackFromGuessObject != null) {
            this.resolved = fallbackFromGuessObject.getClass();
        }
        return this.resolved;
    }

    public Class<?> resolved() {
        if (this.resolved != null) {
            return this.resolved;
        }
        if (cache.containsKey(key())) {
            return cache.get(key());
        }
        Type genericType = field.getGenericType();
        if (genericType instanceof Class<?>) {
            return (Class<?>) genericType;
        }
        ResolvableField resolvableField = fromClass(this.implementationClass);
        if (resolvableField == null) {
            return this.field.getType();
        }
        this.pre = resolvableField;
        ResolvableField tempResolvableField = resolvableField;
        String fieldGenericName = genericType.getTypeName();
        if (genericType instanceof GenericArrayType) {
            fieldGenericName = ((GenericArrayType) genericType).getGenericComponentType().getTypeName();
        }
        while (tempResolvableField != null) {
            for (int i = 0; i < tempResolvableField.ownerNames.length; i++) {
                Type ownerActualImplType = tempResolvableField.ownerActualImplTypes[i];
                if (ownerActualImplType instanceof TypeVariable<?>) {
                    this.resolved = (Class<?>) ((TypeVariable<?>) ownerActualImplType).getBounds()[0];
                }
                else if (ownerActualImplType instanceof Class<?>) {
                    this.resolved = (Class<?>) ownerActualImplType;
                }
                else if (ownerActualImplType instanceof ParameterizedType) {
                    this.resolved = (Class<?>) ((ParameterizedType) ownerActualImplType).getRawType();
                }
                if (this.resolved != null) {
                    this.resolved = resolvePrimitiveIfNecessary(this.resolved);
                }
                if (tempResolvableField.ownerNames[i].equals(fieldGenericName)) {
                    if (genericType instanceof GenericArrayType) {
                        this.resolved = Array.newInstance(this.resolved, 0).getClass();
                    }
                    cache.put(key(), this.resolved);
                    return this.resolved;
                }
            }
            tempResolvableField = tempResolvableField.pre;
        }
        this.resolved = this.field.getType();
        return resolved;
    }

    public void resolved0() {
        Type genericType = field.getGenericType();
        if (genericType instanceof Class<?>) {
            this.resolved = (Class<?>) genericType;
            return;
        }
        ResolvableField resolvableField = fromClass(this.implementationClass);
        if (resolvableField == null) {
            return;
        }
        this.pre = resolvableField;
        ResolvableField tempResolvableField = resolvableField;
        while (tempResolvableField != null) {
            for (int i = 0; i < tempResolvableField.ownerNames.length; i++) {
                Type ownerActualImplType = tempResolvableField.ownerActualImplTypes[i];
                if (ownerActualImplType instanceof TypeVariable<?>) {
                    this.resolved = (Class<?>) ((TypeVariable<?>) ownerActualImplType).getBounds()[0];
                }
                else {
                    if (ownerActualImplType instanceof Class<?>) {
                        this.resolved = (Class<?>) ownerActualImplType;
                    }
                    else if (ownerActualImplType instanceof ParameterizedType) {
                        this.resolved = (Class<?>) ((ParameterizedType) ownerActualImplType).getRawType();
                    }
                }
                if (this.resolved != null) {
                    this.resolved = resolvePrimitiveIfNecessary(this.resolved);
                }
                if (tempResolvableField.ownerNames[i].equals(genericType.getTypeName())) {
                    cache.put(key(), this.resolved);
                    return;
                }
            }
            tempResolvableField = tempResolvableField.pre;
        }
        this.resolved = this.field.getType();
    }

    private static final Map<Class<?>, Class<?>> primitiveTypeToWrapperMap = new IdentityHashMap<>(8);

    static {
        primitiveTypeToWrapperMap.put(boolean.class, Boolean.class);
        primitiveTypeToWrapperMap.put(byte.class, Byte.class);
        primitiveTypeToWrapperMap.put(char.class, Character.class);
        primitiveTypeToWrapperMap.put(double.class, Double.class);
        primitiveTypeToWrapperMap.put(float.class, Float.class);
        primitiveTypeToWrapperMap.put(int.class, Integer.class);
        primitiveTypeToWrapperMap.put(long.class, Long.class);
        primitiveTypeToWrapperMap.put(short.class, Short.class);
        primitiveTypeToWrapperMap.put(boolean[].class, Boolean[].class);
        primitiveTypeToWrapperMap.put(byte[].class, Byte[].class);
        primitiveTypeToWrapperMap.put(char[].class, Character[].class);
        primitiveTypeToWrapperMap.put(double[].class, Double[].class);
        primitiveTypeToWrapperMap.put(float[].class, Float[].class);
        primitiveTypeToWrapperMap.put(int[].class, Integer[].class);
        primitiveTypeToWrapperMap.put(long[].class, Long[].class);
        primitiveTypeToWrapperMap.put(short[].class, Short[].class);
        primitiveTypeToWrapperMap.put(void.class, Void.class);
    }

    public static Class<?> resolvePrimitiveIfNecessary(Class<?> clazz) {
        return (clazz.isPrimitive() && clazz != void.class ? primitiveTypeToWrapperMap.get(clazz) : clazz);
    }

    public Class<?> generics(List<Integer> indexList) {
        Map<Integer, Class<?>> integerClassMap = genericsCache.computeIfAbsent(key(), k -> new HashMap<>());
        if (integerClassMap.containsKey(Objects.hash(indexList.toArray()))) {
            // Logger.debug(field.getName()+" -> from cache get generic -> " + integerClassMap.get(Objects.hash(indexList.toArray())));
            return integerClassMap.get(Objects.hash(indexList.toArray()));
        }
        Class<?> generic = generics(indexList.stream().mapToInt(i -> i).toArray());
        if (generic != null) {
            integerClassMap.put(indexList.hashCode(), generic);
        }
        return generic;
    }

    /**
     * 解析数组或集合类型元素类型
     *
     * @param index 数组或集合类型索引:0 Map类型 key:0 value:1
     */
    public Class<?> generics(int... index) {
        if (this.resolved == null) {
            resolved0();
        }
        if (index.length == 0) {
            return this.resolved;
        }
        Type resultType;
        Type[] generics = this.fieldGenericType();
        if (index.length == 1) {
            resultType = generics[index[0]];
        }
        else {
            // from map or list or array
            for (int i = 0; i < index.length - 1; i++) {
                generics = types(generics[index[i]]);
            }
            resultType = generics[index[index.length - 1]];
        }
        resultType = resolveParameterizedTypeForGeneric(resultType, index[index.length - 1]);
        if (resultType instanceof Class<?>) {
            return (Class<?>) resultType;
        }
        return Object.class;
    }


    private Type[] types(Type type) {
        if (type instanceof ParameterizedType) {
            return resolveParameterizedType((ParameterizedType) type);
        }
        else if (type instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) type;
            return new Type[]{genericArrayType.getGenericComponentType()};
        }
        return new Type[]{type};
    }

    /**
     * 字段类型的泛型类型
     */
    public Type[] fieldGenericType() {
        Type genericType = this.field.getGenericType();
        if (genericType instanceof Class<?>) {
            return new Type[]{genericType};
        }
        else if (genericType instanceof ParameterizedType) {
            return resolveParameterizedType((ParameterizedType) genericType);

        }
        else if (genericType instanceof TypeVariable) {
            return new Type[]{resolveTypeVariable((TypeVariable<?>) genericType)};
        }
        else if (genericType instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) genericType;
            return new Type[]{genericArrayType.getGenericComponentType()};
        }
        return new Type[]{genericType};
    }

    private Type resolveParameterizedTypeForGeneric(Type actualType, int index) {
        if (actualType instanceof Class<?> && ((Class<?>) actualType).isArray() && index == 0) {
            return ((Class<?>) actualType).getComponentType();
        }
        else if (actualType instanceof Class<?>) {
            return actualType;
        }
        Type resultType = actualType;
        // 判断泛型类型是否为 参数化类型
        if (resultType instanceof ParameterizedType) {
            // 字段本身就是一个 类型变量的话则获取真时的类型 再解析类型
            Type[] resultTypes = resolveParameterizedType((ParameterizedType) resultType);
            // 肯能存在类型变量
            return resolveParameterizedTypeForGeneric(resultTypes[index], index);
        }
        if (resultType instanceof TypeVariable) {
            // 字段本身就是一个 类型变量的话则获取真时的类型 再解析类型
            resultType = this.resolveTypeVariable((TypeVariable<?>) resultType);
            if (resultType instanceof TypeVariable<?>) {
                TypeVariable<?> resultTypeVariable = (TypeVariable<?>) resultType;
                Type[] bounds = resultTypeVariable.getBounds();
                if (bounds != null && bounds.length > 0) {
                    resultType = bounds[0];
                }
            }
            return resolveParameterizedTypeForGeneric(resultType, index);
        }
        else if (resultType instanceof GenericArrayType) {
            Type genericComponentType = ((GenericArrayTypeImpl) resultType).getGenericComponentType();
            if (genericComponentType instanceof ParameterizedType) {
                return resolveParameterizedTypeForGeneric(genericComponentType, index);
            }
        }
        Logger.debug("ambiguous class type -> " + actualType);
        return actualType;
    }

    private Type[] resolveParameterizedType(ParameterizedType parameterizedType) {
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        Type[] generics = new Type[actualTypeArguments.length];
        for (int i = 0; i < actualTypeArguments.length; i++) {
            Type actualTypeArgument = actualTypeArguments[i];
            if (actualTypeArgument instanceof Class<?>) {
                generics[i] = actualTypeArgument;
            }
            else if (actualTypeArgument instanceof WildcardType) {
                WildcardType wildcardType = (WildcardType) actualTypeArgument;
                if (wildcardType.getLowerBounds() != null && wildcardType.getLowerBounds().length != 0) {
                    generics[i] = wildcardType.getLowerBounds()[0];
                }
                else if (wildcardType.getUpperBounds() != null && wildcardType.getUpperBounds().length != 0) {
                    generics[i] = wildcardType.getUpperBounds()[0];
                }
            }
            else if (actualTypeArgument instanceof TypeVariable) {
                TypeVariable<?> typeVariable = (TypeVariable<?>) actualTypeArgument;
                if (this.resolved == null) {
                    this.resolved();
                }
                generics[i] = resolveTypeVariable(typeVariable);
            }
            else if (actualTypeArgument instanceof ParameterizedType) {
                generics[i] = actualTypeArgument;
            }
        }
        return generics;
    }

    private Type resolveTypeVariable(TypeVariable<?> actualType) {
        ResolvableField currentResolved = this;
        while (currentResolved != null) {
            if (currentResolved.typeVariables != null) {
                for (int j = 0; j < currentResolved.typeVariables.length; j++) {
                    if (actualType == currentResolved.typeVariables[j]) {
                        return currentResolved.ownerActualImplTypes[j];
                    }
                }
            }
            currentResolved = currentResolved.pre;
        }
        return actualType;
    }

    private int key() {
        return this.field.hashCode() + this.implementationClass.hashCode();
    }

}