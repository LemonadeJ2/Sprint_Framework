package mg.itu.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ControllerRegistry {

    private static final Map<Class<?>, Object> INSTANCES = new ConcurrentHashMap<>();

    public static Object getInstance(Class<?> clazz) {
        return INSTANCES.computeIfAbsent(clazz, c -> {
            try {
                return c.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new IllegalStateException("Impossible d'instancier " + c.getName(), e);
            }
        });
    }

    public static void clear() {
        INSTANCES.clear();
    }
}
