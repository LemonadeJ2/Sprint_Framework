package mg.itu.util;

import java.lang.reflect.Modifier;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import jakarta.servlet.http.HttpServletRequest;

public class GetArgController {
    public static Object[] buildArgs(Method method, HttpServletRequest httpServReq) throws ReflectiveOperationException{
        Parameter[] param = method.getParameters();
        Object[] args = new Object[param.length];

        for (int i = 0; i < param.length; i++) {
            Class<?> typeargs = param[i].getType();
            String name = param[i].getName();
            String value = httpServReq.getParameter(name);

            if (isSimple(typeargs)) {
                args[i] = convert(httpServReq.getParameter(name), typeargs);
            } else{
                args[i] = buildObject(typeargs, name, httpServReq);
            }
        }
        return args;
    }

    public static boolean isSimple(Class<?> type) {
        if (type == String.class
                || type == int.class
                || type == Integer.class
                || type == float.class
                || type == Float.class
                || type == double.class
                || type == Double.class
                || type == long.class
                || type == Long.class
                || type == boolean.class
                || type == Boolean.class) {
            return true;
        } else {
            return false;
        }
    }

    public static Object convert(String value, Class<?> typeargs) {
        if (value == null) {
            if (typeargs.isPrimitive()) {
                throw new IllegalArgumentException(
                        "Valeur manquante pour " + typeargs.getSimpleName());
            }
            return null;
        }

        if (typeargs == String.class) {
            return value;
        } else if (typeargs == int.class || typeargs == Integer.class) {
            return Integer.parseInt(value);
        } else if (typeargs == float.class || typeargs == Float.class) {
            return Float.parseFloat(value);
        } else if (typeargs == double.class || typeargs == Double.class) {
            return Double.parseDouble(value);
        } else if (typeargs == long.class || typeargs == Long.class) {
            return Long.parseLong(value);
        } else if (typeargs == boolean.class || typeargs == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else {
            return null;
        }
    }

    private static Object buildObject(Class<?> type, String prefix, HttpServletRequest req)throws ReflectiveOperationException {

        Object instance = type.getDeclaredConstructor().newInstance();

        for (Field field : type.getDeclaredFields()) {
            int modifiers = field.getModifiers();

            if (Modifier.isStatic(modifiers)
                    || Modifier.isFinal(modifiers)
                    || field.isSynthetic()) {
                continue;
            }

            String name = prefix + "." + field.getName();
            String value = req.getParameter(name);

            // Un champ absent conserve sa valeur initiale.
            if (value == null)
                continue;

            if (!isSimple(field.getType())) {
                throw new IllegalArgumentException(
                        "Champ non pris en charge : " + name);
            }

            field.setAccessible(true);
            field.set(instance, convert(value, field.getType()));
        }

        return instance;
    }
}
