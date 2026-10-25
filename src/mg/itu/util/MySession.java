package mg.itu.util;

import jakarta.servlet.http.HttpSession;

public class MySession {
    private final HttpSession session;

    public MySession(HttpSession session) {
        this.session = session;
    }

    public void set(String key, Object value) {
        session.setAttribute(key, value);
    }

    public Object get(String key) {
        return session.getAttribute(key);
    }

    public void invalidate() {
        session.invalidate();
    }

    public boolean has(String key){
        return  session.getAttribute(key) != null;
    }

    public void remove(String key) {
        session.removeAttribute(key);
    }
}