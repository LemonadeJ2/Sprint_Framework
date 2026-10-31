package mg.sprint;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.annotation.webapi.WebAPI;
import mg.itu.mapping.UrlMethode;
import mg.itu.util.AuthUtil;
import mg.itu.util.ControllerRegistry;
import mg.itu.util.GetArgController;
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

        req.setCharacterEncoding("UTF-8");

        /* Les routes sont enregistrées au démarrage par le FrontControllerListener */
        Map<UrlMethode, Method> urlMethodMap = (Map<UrlMethode, Method>) getServletContext()
                .getAttribute("urlMethodMap");

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

        if (AuthUtil.isAuthRequired(method)) {
            String sessionKey = getServletContext().getInitParameter("authSessionKey");
            if (sessionKey == null || sessionKey.isEmpty()) {
                sessionKey = "user";
            }

            if (!AuthUtil.isAuthenticated(req, sessionKey)) {
                if (method.isAnnotationPresent(WebAPI.class)) {
                    resp.sendError(401, "Authentification requise.");
                } else {
                    String loginUrl = getServletContext().getInitParameter("loginUrl");

                    if (isValidLoginUrl(loginUrl)) {
                        resp.sendRedirect(req.getContextPath()
                                + req.getServletPath() + loginUrl);
                    } else {
                        resp.sendError(401, "Authentification requise.");
                    }
                }
                return;
            }
        }

        String[] requiredRoles = AuthUtil.getRequiredRoles(method);
        if (requiredRoles != null) {
            String roleKey = getServletContext().getInitParameter("roleSessionKey");
            if (roleKey == null || roleKey.isEmpty()) {
                roleKey = "role";
            }

            if (!AuthUtil.hasRole(req, roleKey, requiredRoles)) {
                resp.sendError(403, "Accès refusé : rôle insuffisant.");
                return;
            }
        }

        Object[] args;

        try {
            args = GetArgController.buildArgs(method, req);
        } catch (IllegalArgumentException e) {
            getServletContext().log("Paramètre invalides pour" + key, e);

            resp.sendError(400, "Paramètres du formulaire invalides.");
            return ;
        } catch (IllegalStateException e) {
            throw new ServletException(
                    "Impossible de traiter la requête. "
                            + "Vérifier la configuration multipart "
                            + "et les limites de l'application.",
                    e);
        } catch (ReflectiveOperationException e){
            throw new ServletException("Impossible de construire les arguments de " + key,
                e
            );
        }

        Object result;

        try {
            Object controller = ControllerRegistry.getInstance(method.getDeclaringClass());

            // Object[] args = GetArgController.buildArgs(method, req);

            result = method.invoke(controller, args);

        } catch (IllegalStateException e) {
            throw new ServletException("Impossible d'instancier le controller de " + key, e);
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
     * loginUrl doit être un chemin relatif au mapping du servlet
     * (ex: "/profil" pour /app/profil), jamais une URL externe.
     */
    private boolean isValidLoginUrl(String loginUrl) {
        return loginUrl != null
                && loginUrl.startsWith("/")
                && !loginUrl.startsWith("//")
                && !loginUrl.contains("://");
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
