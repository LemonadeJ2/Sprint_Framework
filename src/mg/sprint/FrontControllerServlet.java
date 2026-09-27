package mg.sprint;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.annotation.webapi.WebAPI;
import mg.itu.mapping.UrlMethode;
import mg.itu.util.JsonUtil;
import mg.itu.util.ModelView;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class FrontControllerServlet extends HttpServlet {

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

    @SuppressWarnings("unchecked")
    protected void processRequest(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        /* Les routes sont enregistrées au démarrage par le FrontControllerListener */
        Map<UrlMethode, Method> urlMethodMap =
                (Map<UrlMethode, Method>) getServletContext().getAttribute("urlMethodMap");

        if (urlMethodMap == null) {
            resp.sendError(500, "Le listener n'a pas initialisé les routes.");
            return;
        }

        UrlMethode key = new UrlMethode(getUrl(req), req.getMethod());
        Method method = urlMethodMap.get(key);

        if (method == null) {
            resp.sendError(404, "Aucune méthode annotée pour " + key
                    + "\nRoutes disponibles : " + urlMethodMap.keySet());
            return;
        }

        Object result;

        try {
            Object controller = method.getDeclaringClass()
                    .getDeclaredConstructor()
                    .newInstance();

            Object[] args = JsonUtil.buildArgs(method, req, resp, getServletContext());

            result = method.invoke(controller, args);

        } catch (InvocationTargetException e) {
            /* L'exception réellement levée par le controller */
            throw new ServletException("Erreur dans " + key + " : " + e.getCause().getMessage(), e.getCause());
        } catch (ReflectiveOperationException | IllegalArgumentException e) {
            throw new ServletException("Erreur lors de l'invocation de " + key, e);
        }

        /*
         * Condition Sprint 6 : si la méthode porte l'annotation @WebAPI,
         * on ne passe pas par la vue, on renvoie directement du JSON.
         */
        if (method.isAnnotationPresent(WebAPI.class)) {
            resp.setContentType("application/json;charset=UTF-8");
            PrintWriter out = resp.getWriter();
            try {
                out.print(JsonUtil.toJson(result));
            } catch (Exception e) {
                throw new ServletException("Erreur de sérialisation JSON de " + key, e);
            }
            out.flush();
            return;
        }

        /* Sinon : la valeur de retour décide de l'affichage */
        if (result instanceof ModelView) {
            forwardToView(req, resp, (ModelView) result);
            return;
        }

        resp.setContentType("text/html;charset=UTF-8");
        if (result != null) {
            resp.getWriter().print(result);
        }
    }

    /* Envoie les données du ModelView dans la requête et forwarde vers la JSP */
    private void forwardToView(HttpServletRequest req, HttpServletResponse resp, ModelView mv)
            throws ServletException, IOException {

        for (Map.Entry<String, Object> entry : mv.getData().entrySet()) {
            req.setAttribute(entry.getKey(), entry.getValue());
        }

        req.getRequestDispatcher("/WEB-INF/views/" + mv.getView() + ".jsp")
                .forward(req, resp);
    }

    /*
     * URL demandée sans le contexte ni le chemin du servlet (/app),
     * pour obtenir exactement la valeur déclarée dans @UrlMapping.
     */
    private String getUrl(HttpServletRequest req) {

        String url = req.getRequestURI().substring(req.getContextPath().length());

        String servletPath = req.getServletPath();
        if (servletPath != null && !servletPath.isEmpty() && url.startsWith(servletPath)) {
            url = url.substring(servletPath.length());
        }

        return url.isEmpty() ? "/" : url;
    }
}
