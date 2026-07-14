package mg.itu.listener;

import java.lang.reflect.Method;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import mg.itu.util.ClassScanner;
import mg.itu.mapping.*;

public class FrontControllerListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext sc = sce.getServletContext();

        String packageName = sc.getInitParameter("controllerPackage");
        if (packageName == null || packageName.isEmpty()) {
            packageName = "controllers";
        }

        try {
            Map<UrlMethode, Method> urlMethodMap = ClassScanner.getUrlMethodMap(packageName);

            sc.setAttribute("urlMethodMap", urlMethodMap);

            System.out.println("==== Listener : " + urlMethodMap.size() + " routes enregistrées ====");
        } catch (Exception e) {
            throw new RuntimeException("Erreur scan controllers", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("==== Application arrêtée ====");
    }
}
