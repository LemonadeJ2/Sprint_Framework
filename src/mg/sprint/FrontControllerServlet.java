package mg.sprint;

import jakarta.servlet.ServletException;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.annotation.controller.ControllerAnnotation;
import mg.itu.util.ClassScanner;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class FrontControllerServlet extends HttpServlet {
    private List<Class<?>> controllers;

    @Override
    public void init() throws ServletException {
        super.init();

        String packageName = getServletConfig().getInitParameter("controllerPackage");

        if (packageName == null || packageName.isEmpty()) {
            packageName = "controllers";
        }

        try {
            controllers = ClassScanner.getClassesByAnnotation(ControllerAnnotation.class, packageName);
        } catch (Exception e) {
            throw new ServletException("Erreur durant le scan des controllers", e);
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

    protected void processRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        out.println("<h2>Controllers détectés (" + controllers.size() + ")</h2>");
        out.println("<ul>");
        for (Class<?> c : controllers) {
            out.println("<li>" + c.getName() + "</li>");
        }
        out.println("</ul>");
    }
}