package mg.itu.util;

import mg.itu.annotation.controller.ControllerAnnotation;
import mg.itu.annotation.url.UrlMapping;
import mg.itu.mapping.UrlMethode;

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
            if (clazz.isAnnotationPresent(annotation)) {
                annotatedClasses.add(clazz);
            }
        }

        return annotatedClasses;
    }

    /*
     * Scanne toutes les classes du package et retourne une map
     * dont la clé est un UrlMethode (url + verbe HTTP).
     * Permet d'avoir deux méthodes sur la même URL si leurs verbes diffèrent.
     */
    public static Map<UrlMethode, Map<Class<?>, List<Method>>> getAnnotatedMethodsByUrl(Class<?> controllerClass, String packageName, Class<? extends Annotation> annotationClass)
            throws ClassNotFoundException, ReflectiveOperationException {

        Map<UrlMethode, Map<Class<?>, List<Method>>> result = new HashMap<>();

        List<Class<?>> classes = getClassesList(packageName);

        for (Class<?> clazz : classes) {

            for (Method method : clazz.getDeclaredMethods()) {

                if (method.isAnnotationPresent(annotationClass)) {

                    Annotation annotation = method.getAnnotation(annotationClass);

                    /* Récupère la valeur de l'URL */
                    String url = (String) annotationClass
                            .getMethod("value")
                            .invoke(annotation);

                    /* Récupère le verbe HTTP (GET par défaut) */
                    String httpMethod = (String) annotationClass
                            .getMethod("method")
                            .invoke(annotation);

                    /* Construit la clé composée (url, méthode HTTP) */
                    UrlMethode key = new UrlMethode(url, httpMethod);

                    result
                            .computeIfAbsent(key, k -> new HashMap<>())
                            .computeIfAbsent(clazz, k -> new ArrayList<>())
                            .add(method);
                }
            }
        }

        return result;
    }

    /*
     * Retourne la liste de tous les UrlMethode (url + verbe) trouvés dans le package.
     * Utile pour vérifier si un couple (url, httpMethod) est valide.
     */
    public static List<UrlMethode> ifUrlExists(String packageName) throws ClassNotFoundException {

        List<UrlMethode> urlMethodes = new ArrayList<>();

        List<Class<?>> classes = getClassesList(packageName);

        for (Class<?> clazz : classes) {

            for (Method method : clazz.getDeclaredMethods()) {

                UrlMapping urlMapping = method.getAnnotation(UrlMapping.class);

                if (urlMapping != null) {
                    urlMethodes.add(new UrlMethode(urlMapping.value(), urlMapping.method()));
                }
            }
        }

        return urlMethodes;
    }
}
