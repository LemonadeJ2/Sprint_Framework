package mg.itu.annotation.webapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
 * Annotation à mettre sur une méthode de controller (en plus de @UrlMapping).
 * Si elle est présente, le FrontControllerServlet n'affiche aucune vue :
 * il sérialise la valeur retournée en JSON et l'écrit dans la réponse.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WebAPI {
}
