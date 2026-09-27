package mg.itu.mapping;

import java.util.Objects;

/*
 * Classe représentant le couple (URL, méthode HTTP).
 * Utilisée comme clé dans la map de routage pour distinguer
 * deux mêmes URLs si leurs verbes HTTP sont différents.
 */
public class UrlMethode {

    private String url;        // ex: "/test"
    private String httpMethod;  // ex: "GET" ou "POST"

    public UrlMethode(String url, String httpMethod) {
        this.url = url;
        this.httpMethod = httpMethod;
    }

    public String getUrl() {
        return url;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UrlMethode that = (UrlMethode) o;
        return Objects.equals(url, that.url) &&
               Objects.equals(httpMethod, that.httpMethod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, httpMethod);
    }

    @Override
    public String toString() {
        return httpMethod + " " + url;
    }
}
