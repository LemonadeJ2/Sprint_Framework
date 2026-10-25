package mg.itu.util;

import java.lang.reflect.Modifier;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Locale;

// import org.omg.CORBA.portable.InputStream;
import java.io.InputStream;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import java.io.IOException;

public class GetArgController {
    public static Object[] buildArgs(Method method, HttpServletRequest req)
            throws ReflectiveOperationException, IOException, ServletException {
        Parameter[] param = method.getParameters();
        Object[] args = new Object[param.length];

        for (int i = 0; i < param.length; i++) {
            Class<?> type = param[i].getType();
            String name = param[i].getName();
            // String value = req.getParameter(name);
            if (type == MySession.class) {
                args[i] = new MySession(req.getSession());
            } else if (type == MapFile.class) {
                args[i] = buildMapFile(req, name);

            } else if (isSimple(type)) {
                args[i] = convert(req.getParameter(name), type);

            } else {
                args[i] = buildObject(type, name, req);
            }
        }
        return args;
    }

    public static boolean isSimple(Class<?> type) {
        if (type == String.class
                || type == int.class
                || type == Integer.class
                || type == float.class
                || type == Float.class
                || type == double.class
                || type == Double.class
                || type == long.class
                || type == Long.class
                || type == boolean.class
                || type == Boolean.class) {
            return true;
        } else {
            return false;
        }
    }

    public static Object convert(String value, Class<?> typeargs) {
        if (value == null) {
            if (typeargs.isPrimitive()) {
                throw new IllegalArgumentException(
                        "Valeur manquante pour " + typeargs.getSimpleName());
            }
            return null;
        }

        if (typeargs == String.class) {
            return value;
        } else if (typeargs == int.class || typeargs == Integer.class) {
            return Integer.parseInt(value);
        } else if (typeargs == float.class || typeargs == Float.class) {
            return Float.parseFloat(value);
        } else if (typeargs == double.class || typeargs == Double.class) {
            return Double.parseDouble(value);
        } else if (typeargs == long.class || typeargs == Long.class) {
            return Long.parseLong(value);
        } else if (typeargs == boolean.class || typeargs == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else {
            return null;
        }
    }

    private static Object buildObject(Class<?> type, String prefix, HttpServletRequest req)
            throws ReflectiveOperationException, IOException, ServletException {

        Object instance = type.getDeclaredConstructor().newInstance();

        for (Field field : type.getDeclaredFields()) {
            int modifiers = field.getModifiers();

            if (Modifier.isStatic(modifiers)
                    || Modifier.isFinal(modifiers)
                    || field.isSynthetic()) {
                continue;
            }

            String name = prefix + "." + field.getName();
            if (field.getType() == MapFile.class) {
                MapFile file = buildMapFile(req, name);

                if (file != null) {
                    field.setAccessible(true);
                    field.set(instance, file);
                }

                continue;
            }

            String value = req.getParameter(name);

            // Un champ absent conserve sa valeur initiale.
            if (value == null)
                continue;

            if (!isSimple(field.getType())) {
                throw new IllegalArgumentException(
                        "Champ non pris en charge : " + name);
            }

            field.setAccessible(true);
            field.set(instance, convert(value, field.getType()));
        }

        return instance;
    }

    private static MapFile buildMapFile(HttpServletRequest req, String name)
            throws IOException, ServletException {
        if (!isMultipart(req)) {
            return null;
        }

        Part part = req.getPart(name);

        if (part == null) {
            return null;
        }

        String submittedFilename = part.getSubmittedFileName();

        if (submittedFilename == null) {
            throw new IllegalArgumentException(
                    "Le champ" + name + "doit etre un fichier");
        }

        if (submittedFilename.isEmpty()) {
            return null;
        }

        String filename = extractFileName(submittedFilename);
        String extension = extractExtension(filename);
        byte[] bytes = readBytes(part);

        return new MapFile(filename, extension, bytes);

    }

    private static boolean isMultipart(HttpServletRequest req) {
        String contentType = req.getContentType();

        if (contentType == null) {
            return false;
        }

        int separator = contentType.indexOf(';');

        String mediaType = separator >= 0
                ? contentType.substring(0, separator)
                : contentType;

        return "multipart/form-data".equalsIgnoreCase(mediaType.trim());
    }

    private static String extractFileName(String submittedFilename) {

        String normalized = submittedFilename.replace('\\', '/');
        int lastSlash = normalized.lastIndexOf('/');

        return normalized.substring(lastSlash + 1);
    }

    private static String extractExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');

        if (lastDot <= 0 || lastDot == filename.length() - 1) {
            return "";
        }

        return filename.substring(lastDot + 1)
                .toLowerCase(Locale.ROOT);
    }

    // A demander part 1
    private static byte[] readBytes(Part part) throws IOException {
        try (InputStream input = part.getInputStream(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[8192];
            int count;

            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }

            return output.toByteArray();
        }
    }
}
