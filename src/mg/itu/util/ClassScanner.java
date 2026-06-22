package mg.itu.util;


import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class ClassScanner {
    public static List<Class<?>> listClass(String packageName) throws ClassNotFoundException{
        List<Class<?>> classList = new ArrayList<Class<?>>();
        String path = packageName.replace('.', '/');
        File dir = new File(
            Thread.currentThread()
                .getContextClassLoader()
                .getResource(path).getFile()
        );

        if (dir.listFiles() == null) return classList;

        for(File file : dir.listFiles()){
            if(file.getName().endsWith(".class")){
                String className = packageName + "." +
                    file.getName().replace((".class"), "");
                classList.add(Class.forName(className));
            }
        }
        return classList;
    }


    public static List<Class<?>> getClassesByAnnotation(Class<?> annotation, String packageName) throws ClassNotFoundException{
        List<Class<?>> annotatedClasses = new ArrayList<Class<?>>();
        List<Class<?>> classes = listClass(packageName);

        for (Class<?> class1 : classes) {
            if(class1.isAnnotationPresent(annotation.asSubclass(Annotation.class))){
                annotatedClasses.add(class1);
            }
        }

        return annotatedClasses;
    } 
}
