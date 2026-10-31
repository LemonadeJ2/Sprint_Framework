package mg.itu.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import mg.itu.annotation.auth.Authenticated;
import mg.itu.annotation.auth.Role;

import java.lang.reflect.Method;
import java.util.Collection;

public class AuthUtil {

    public static boolean isAuthRequired(Method method) {
        return method.isAnnotationPresent(Authenticated.class)
                || method.getDeclaringClass().isAnnotationPresent(Authenticated.class)
                || getRequiredRoles(method) != null;
    }

    public static boolean isAuthenticated(HttpServletRequest req, String sessionKey) {
        HttpSession session = req.getSession(false);
        return session != null && session.getAttribute(sessionKey) != null;
    }

    public static String[] getRequiredRoles(Method method) {
        Role role = method.getAnnotation(Role.class);
        if (role == null) {
            role = method.getDeclaringClass().getAnnotation(Role.class);
        }
        return role == null ? null : role.value();
    }

    public static boolean hasRole(HttpServletRequest req, String roleKey, String[] required) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return false;
        }

        Object current = session.getAttribute(roleKey);
        if (current == null) {
            return false;
        }

        for (String role : required) {
            if (current instanceof Collection<?> roles) {
                if (roles.contains(role)) {
                    return true;
                }
            } else if (role.equals(current.toString())) {
                return true;
            }
        }
        return false;
    }
}
