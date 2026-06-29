package mg.itu.util;

import mg.itu.annotation.controller.ControllerAnnotation;
import mg.itu.annotation.url.UrlMapping;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.annotation.*;

public class ClassScanner {

    public static List<Class<?>> getClassesList(String packageName) throws ClassNotFoundException {
        List<Class<?>> classList = new ArrayList<Class<?>>();

        String path = packageName.replace('.', '/');
        File dir = new File(
                Thread.currentThread()
                        .getContextClassLoader()
                        .getResource(path).getFile()
        );

        if (dir.listFiles() == null) return classList;

        for (File file : dir.listFiles()) {
            if (file.getName().endsWith(".class")) {
                String className = packageName + "." +
                    file.getName().replace(".class", "");
                classList.add(Class.forName(className));
            }
        }

        return classList;
    }

    public static boolean isClassInPackage(Class<?> clazz, String packageName) {
        return clazz.getPackage().getName().equals(packageName);
    }

    public static List<Class<?>> getClassesByAnnotation(String packageName, Class<? extends Annotation> annotation) throws ClassNotFoundException {
        List<Class<?>> annotatedClasses = new ArrayList<Class<?>>();
        List<Class<?>> classes = getClassesList(packageName);

        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(annotation.asSubclass(Annotation.class))) {
                annotatedClasses.add(clazz);
            }
        }

        return annotatedClasses;
    }

    public static Map<String, Map<Class<?>, List<Method>>> getAnnotatedMethodsByUrl(Class<?> controllerClass, String packageName, Class<? extends Annotation> annotationClass)
            throws ClassNotFoundException, ReflectiveOperationException {

        Map<String, Map<Class<?>, List<Method>>> result = new HashMap<>();

        List<Class<?>> classes = getClassesList(packageName);

        for (Class<?> clazz : classes) {

            for (Method method : clazz.getDeclaredMethods()) {

                if (method.isAnnotationPresent(annotationClass)) {

                    Annotation annotation = method.getAnnotation(annotationClass);

                    String url = (String) annotationClass
                            .getMethod("value")
                            .invoke(annotation);

                    result
                            .computeIfAbsent(url, k -> new HashMap<>())
                            .computeIfAbsent(clazz, k -> new ArrayList<>())
                            .add(method);
                }
            }
        }

        return result;
    }

    public static List<String> ifUrlExists(String packageName) throws ClassNotFoundException {

        List<String> urls = new ArrayList<>();

        List<Class<?>> classes = getClassesList(packageName);

        for (Class<?> clazz : classes) {

            for (Method method : clazz.getDeclaredMethods()) {

                UrlMapping urlMapping = method.getAnnotation(UrlMapping.class);

                if (urlMapping != null) {
                    urls.add(urlMapping.value());
                }
            }
        }

        return urls;
    }
}
