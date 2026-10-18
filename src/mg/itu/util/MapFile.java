package mg.itu.util;

public class MapFile {
    private final String filename;
    private final String extension;
    private final byte[] bytes;

    public MapFile(String filename, String extension, byte[] bytes){
        this.filename = filename;
        this.extension = extension;
        this.bytes = bytes;
    }

        public String getFilename() {
        return filename;
    }

    public String getExtension() {
        return extension;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public long getSize() {
        return bytes.length;
    }

    public boolean isEmpty() {
        return bytes.length == 0;
    }
}
