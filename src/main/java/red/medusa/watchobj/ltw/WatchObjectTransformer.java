package red.medusa.watchobj.ltw;

import javassist.*;
import red.medusa.watchobj.core.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WatchObjectTransformer implements ClassFileTransformer {
    private Pattern[] patterns;

    public WatchObjectTransformer(String args) {
        try {

            String scanPackages;
            if (args != null) {
                scanPackages = args;
            }
            else {
                InputStream stream = ClassLoader.getSystemResourceAsStream("watch-obj.properties");
                if (stream == null) {
                    return;
                }
                Properties properties = new Properties();
                properties.load(stream);
                scanPackages = properties.getProperty("scanPackages");
            }

            String[] scanPackagesArr = scanPackages.split(",");
            patterns = new Pattern[scanPackagesArr.length];
            for (int i = 0; i < scanPackagesArr.length; i++) {
                if (scanPackagesArr[i].equals("") || scanPackagesArr[i].trim().length() == 0) {
                    continue;
                }
                patterns[i] = Pattern.compile(scanPackagesArr[i].trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        try {
            if (className == null) {
                return classfileBuffer;
            }
            if (patterns == null) {
                return classfileBuffer;
            }
            boolean permit = false;
            String clazzName = className.replace("/", ".");
            for (Pattern pattern : patterns) {
                Matcher matcher = pattern.matcher(clazzName);
                permit = matcher.matches();
                if (permit) {
                    break;
                }
            }
            if (!permit) {
                return classfileBuffer;
            }
            Logger.debug("watching you object class -> " + clazzName);
            ClassPool classPool = new ClassPool();
            classPool.appendClassPath(new LoaderClassPath(loader));
            CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));

            CtConstructor[] constructors = ctClass.getConstructors();
            for (CtConstructor constructor : constructors) {
                constructor.insertAfter("red.medusa.watchobj.core.MutableJsonService.getInstance().watchObject($0);");
            }
            Map<String, Object[]> fieldNameWithTypes = new HashMap<>();
            Arrays.stream(ctClass.getDeclaredFields())
                    .forEach(it -> {
                        try {
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
                            Object[] objects = new Object[2];
                            objects[0] = fieldName;
                            objects[1] = it.getType();
                            fieldNameWithTypes.put(setterName, objects);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
            CtMethod[] declaredMethods = ctClass.getDeclaredMethods();
            for (CtMethod declaredMethod : declaredMethods) {
                String name = declaredMethod.getName();
                CtClass[] parameterTypes = null;
                try {
                    parameterTypes = declaredMethod.getParameterTypes();
                } catch (Exception e) {
                    e.getStackTrace();
                }
                if (parameterTypes == null || parameterTypes.length != 1) {
                    continue;
                }
                if (fieldNameWithTypes.containsKey(name)) {
                    Object[] nameWithTypeArr = fieldNameWithTypes.get(name);
                    StringBuilder sb = new StringBuilder();
                    sb
                            .append("$0").append(",")
                            .append("\"").append(nameWithTypeArr[0]).append("\"").append(",")
                            .append("$args[0]").append(",")
                            .append("$sig[0]");
                    declaredMethod.insertBefore("red.medusa.watchobj.core.MutableJsonService.getInstance().beforeRoll();");
                    declaredMethod.insertAfter("red.medusa.watchobj.core.MutableJsonService.getInstance().roll(" + sb + ");");
                }
            }
            // ctClass.debugWriteFile();
            return ctClass.toBytecode();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return classfileBuffer;
    }


    public static String toUpperCaseFirst(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}
