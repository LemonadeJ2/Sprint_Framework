package mg.sprint;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.annotation.url.UrlMapping;
import mg.itu.mapping.UrlMethode;
import mg.itu.util.ClassScanner;
import mg.itu.annotation.controller.ControllerAnnotation;
import java.io.*;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class FrontControllerServlet extends HttpServlet {

    private List<Class<?>> controllers;
    /* Map dont la clé est UrlMethode (url + verbe HTTP) au lieu d'une simple String */
    private Map<UrlMethode, Map<Class<?>, List<Method>>> urlMapping;
    private List<UrlMethode> urlMethodes;

    @Override
    public void init() throws ServletException {
        super.init();

        String packageName = getServletConfig().getInitParameter("controllerPackage");

        if (packageName == null || packageName.isEmpty()) {
            packageName = "controllers";
        }

        try {
            controllers = ClassScanner.getClassesByAnnotation(packageName, ControllerAnnotation.class);
            urlMapping = ClassScanner.getAnnotatedMethodsByUrl(ControllerAnnotation.class, packageName, UrlMapping.class);
            urlMethodes = ClassScanner.ifUrlExists(packageName);
        } catch (Exception e) {
            throw new ServletException("Erreur lors du scan des controllers", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        processRequest(req, resp);
    }

    /*
     * Traite la requête entrante.
     * Reconstitue un UrlMethode à partir de l'URL appelée et du verbe HTTP,
     * puis cherche la méthode correspondante dans la map de routage.
     */
    protected void processRequest(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        out.println("<h1>Controllers chargés au démarrage :</h1><ul>");
        for (Class<?> c : controllers) {
            out.println("<li>" + c.getName() + "</li>");
        }
        out.println("</ul>");

        /* Récupère l'URL demandée */
        String contextPath = req.getContextPath();
        String requestedUrl = req.getRequestURI()
                .substring(contextPath.length());

        /* Récupère le verbe HTTP (GET, POST, etc.) */
        String httpMethod = req.getMethod();

        /* Construit la clé de recherche */
        UrlMethode requestedKey = new UrlMethode(requestedUrl, httpMethod);

        out.println("<p>Requête : <strong>" + httpMethod + " " + requestedUrl + "</strong></p>");

        /* Vérifie que le couple (url, méthode) existe */
        if (!urlMapping.containsKey(requestedKey)) {
            throw new ServletException(
                    "Aucune méthode annotée pour " + httpMethod + " " + requestedUrl
                    + "\nRoutes disponibles : " + urlMapping.keySet());
        }

        /* Récupère les classes/méthodes associées */
        Map<Class<?>, List<Method>> classMap = urlMapping.get(requestedKey);

        out.println("<h2>Résultat : (url -> class -> méthode annotée)</h2>");
        out.println("<ul>");

        for (Map.Entry<Class<?>, List<Method>> entry : classMap.entrySet()) {

            Class<?> controller = entry.getKey();

            for (Method method : entry.getValue()) {

                out.println("<li>"
                        + requestedUrl + " (" + httpMethod + ") → "
                        + controller.getSimpleName()
                        + " -> "
                        + method.getName()
                        + "()"
                        + "</li>");
            }
        }

        out.println("</ul>");
    }
}
