package mg.itu.util;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import jakarta.servlet.http.HttpServletRequest;

public class GetArgController {
    public static Object[] buildArgs(Method method, HttpServletRequest httpServReq) {
        Parameter[] param = method.getParameters();
        Object[] args = new Object[param.length];

        for (int i = 0; i < param.length; i++) {
            Class<?> typeargs = param[i].getType();
            String name = param[i].getName();
            String value = httpServReq.getParameter(name);

            if (typeargs == String.class) {
                args[i] = value;
            } else if (typeargs == int.class || typeargs == Integer.class) {
                args[i] = Integer.parseInt(value);
            } else if (typeargs == float.class || typeargs == Float.class) {
                args[i] = Float.parseFloat(value);
            } else if (typeargs == double.class || typeargs == Double.class) {
                args[i] = Double.parseDouble(value);
            } else if (typeargs == long.class || typeargs == Long.class) {
                args[i] = Long.parseLong(value);
            } else if (typeargs == boolean.class || typeargs == Boolean.class) {
                args[i] = Boolean.parseBoolean(value);
            } else {
                args[i]= null;
            }
        }
        return args;
    }
}
