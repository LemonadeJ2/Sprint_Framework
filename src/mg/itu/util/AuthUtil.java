package mg.itu.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import mg.itu.annotation.auth.Authenticated;

import java.lang.reflect.Method;

public class AuthUtil {

    public static boolean isAuthRequired(Method method) {
        return method.isAnnotationPresent(Authenticated.class)
                || method.getDeclaringClass().isAnnotationPresent(Authenticated.class);
    }

    public static boolean isAuthenticated(HttpServletRequest req, String sessionKey) {
        HttpSession session = req.getSession(false);
        return session != null && session.getAttribute(sessionKey) != null;
    }
}
