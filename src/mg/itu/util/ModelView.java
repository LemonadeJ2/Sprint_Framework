package mg.itu.util;

import java.util.HashMap;
import java.util.Map;

/*
 * Valeur de retour d'une méthode de controller qui doit passer par une vue (JSP).
 * Le nom de la vue est résolu dans /WEB-INF/views/<vue>.jsp
 */
public class ModelView {

    private String view;
    private Map<String, Object> data;

    public ModelView(String view) {
        this.view = view;
        this.data = new HashMap<>();
    }

    public void addData(String key, Object value) {
        data.put(key, value);
    }

    public String getView() {
        return view;
    }

    public Map<String, Object> getData() {
        return data;
    }
}
