package mg.itu.util;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class JsonUtil {

    // Sérialisation JSON
    public static String toJson(Object obj) throws Exception {
        if (obj == null) return "null";
        /* Un contrôleur qui renvoie déjà du JSON n'est pas re-sérialisé */
        if (obj instanceof CharSequence) return obj.toString();
        if (obj instanceof List<?>) {
            JSONArray array = new JSONArray();
            for (Object item : (List<?>) obj) {
                array.put(toJsonObject(item));
            }
            return array.toString();
        }
        return toJsonObject(obj).toString();
    }

    private static JSONObject toJsonObject(Object obj) {
        return obj instanceof Map ? new JSONObject((Map<?, ?>) obj) : new JSONObject(obj);
    }

    // Construction des arguments selon les types des paramètres
    public static Object[] buildArgs(Method method,
                                     HttpServletRequest req,
                                     HttpServletResponse resp,
                                     ServletContext context) {
        Class<?>[] paramTypes = method.getParameterTypes();
        Object[] args = new Object[paramTypes.length];

        for (int i = 0; i < paramTypes.length; i++) {
            String typeName = paramTypes[i].getName();
            if (typeName.equals("jakarta.servlet.http.HttpServletRequest")) {
                args[i] = req;
            } else if (typeName.equals("jakarta.servlet.http.HttpServletResponse")) {
                args[i] = resp;
            } else {
                String attrName = paramTypes[i].getSimpleName().substring(0, 1).toLowerCase()
                        + paramTypes[i].getSimpleName().substring(1);
                args[i] = context.getAttribute(attrName);
            }
        }
        return args;
    }
}
