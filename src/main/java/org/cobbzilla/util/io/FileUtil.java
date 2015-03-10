package org.cobbzilla.util.io;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.cobbzilla.util.string.StringUtil;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.apache.commons.lang3.StringUtils.chop;
import static org.cobbzilla.util.daemon.ZillaRuntime.die;

@Slf4j
public class FileUtil {

    public static final File DEFAULT_TEMPDIR = new File(System.getProperty("java.io.tmpdir"));

    public static File[] list(File dir) {
        final File[] files = dir.listFiles();
        if (files == null) die("list: dir could not be listed: "+abs(dir));
        return files;
    }

    public static File[] listFiles(File dir) {
        final File[] files = dir.listFiles(RegularFileFilter.instance);
        if (files == null) die("listFiles: dir could not be listed: "+abs(dir));
        return files;
    }

    public static String chopSuffix(String path) {
        if (path == null) return null;
        final int lastDot = path.lastIndexOf('.');
        if (lastDot == -1 || lastDot == path.length()-1) return path;
        return path.substring(0, lastDot);
    }

    public static File createTempDir(String prefix) throws IOException {
        return createTempDir(DEFAULT_TEMPDIR, prefix);
    }

    public static File createTempDir(File parentDir, String prefix) throws IOException {
        final Path parent = FileSystems.getDefault().getPath(abs(parentDir));
        return new File(Files.createTempDirectory(parent, prefix).toAbsolutePath().toString());
    }

    public static File createTempDirOrDie(String prefix) {
        return createTempDirOrDie(DEFAULT_TEMPDIR, prefix);
    }

    public static File createTempDirOrDie(File parentDir, String prefix) {
        try {
            return createTempDir(parentDir, prefix);
        } catch (IOException e) {
            return die("createTempDirOrDie: error creating directory with prefix="+abs(parentDir)+"/"+prefix+": "+e, e);
        }
    }

    public static void writeResourceToFile(String resourcePath, File outFile, Class clazz) throws IOException {
        if (!outFile.getParentFile().exists() || !outFile.getParentFile().canWrite() || (outFile.exists() && !outFile.canWrite())) {
            throw new IllegalArgumentException("outFile is not writeable: "+abs(outFile));
        }
        try (InputStream in = clazz.getClassLoader().getResourceAsStream(resourcePath);
             OutputStream out = new FileOutputStream(outFile)) {
            if (in == null) throw new IllegalArgumentException("null data at resourcePath: "+resourcePath);
            IOUtils.copy(in, out);
        }
    }

    public static List<String> loadResourceAsStringListOrDie(String resourcePath, Class clazz) {
        try {
            return loadResourceAsStringList(resourcePath, clazz);
        } catch (IOException e) {
            throw new IllegalArgumentException("loadResourceAsStringList error: "+e, e);
        }
    }

    public static List<String> loadResourceAsStringList(String resourcePath, Class clazz) throws IOException {
        @Cleanup final Reader reader = StreamUtil.loadResourceAsReader(resourcePath, clazz);
        return toStringList(reader);
    }

    public static List<String> toStringList(String f) throws IOException {
        return toStringList(new File(f));
    }

    public static List<String> toStringList(File f) throws IOException {
        @Cleanup final Reader reader = new FileReader(f);
        return toStringList(reader);
    }

    public static List<String> toStringList(Reader reader) throws IOException {
        final List<String> strings = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(reader)) {
            String line;
            while ((line = r.readLine()) != null) {
                strings.add(line.trim());
            }
        }
        return strings;
    }

    public static File toFile (List<String> lines) throws IOException {
        final File temp = File.createTempFile(FileUtil.class.getSimpleName()+".toFile", "tmp");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(temp))) {
            for (String line : lines) {
                writer.write(line+"\n");
            }
        }
        return temp;
    }

    public static String toStringOrDie (String f) {
        return toStringOrDie(new File(f));
    }

    public static String toStringOrDie (File f) {
        try {
            return toString(f);
        } catch (FileNotFoundException e) {
            log.warn("toStringOrDie: returning null; file not found: "+abs(f));
            return null;
        } catch (IOException e) {
            final String path = f == null ? "null" : abs(f);
            throw new IllegalArgumentException("Error reading file ("+ path +"): "+e, e);
        }
    }

    public static String toString (String f) throws IOException {
        return toString(new File(f));
    }

    public static String toString (File f) throws IOException {
        StringWriter writer = new StringWriter();
        try (Reader r = new FileReader(f)) {
            IOUtils.copy(r, writer);
        }
        return writer.toString();
    }

    public static byte[] toBytes (File f) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (InputStream in = new FileInputStream(f)) {
            IOUtils.copy(in, out);
        }
        return out.toByteArray();
    }

    public static Properties toPropertiesOrDie (String f) {
        return toPropertiesOrDie(new File(f));
    }

    private static Properties toPropertiesOrDie(File f) {
        try {
            return toProperties(f);
        } catch (IOException e) {
            final String path = f == null ? "null" : abs(f);
            throw new IllegalArgumentException("Error reading properties file ("+ path +"): "+e, e);
        }
    }

    public static Properties toProperties (String f) throws IOException {
        return toProperties(new File(f));
    }

    public static Properties toProperties (File f) throws IOException {
        Properties props = new Properties();
        try (InputStream in = new FileInputStream(f)) {
            props.load(in);
        }
        return props;
    }

    public static Properties resourceToPropertiesOrDie (String path, Class clazz) {
        try {
            return resourceToProperties(path, clazz);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading resource ("+ path +"): "+e, e);
        }
    }

    public static Properties resourceToProperties (String path, Class clazz) throws IOException {
        Properties props = new Properties();
        try (InputStream in = StreamUtil.loadResourceAsStream(path, clazz)) {
            props.load(in);
        }
        return props;
    }

    public static File toFileOrDie (String file, String data) {
        return toFileOrDie(new File(file), data);
    }

    public static File toFileOrDie(File file, String data) {
        try {
            return toFile(file, data);
        } catch (IOException e) {
            String path = (file == null) ? "null" : abs(file);
            return die("toFileOrDie: error writing to file: "+ path);
        }
    }

    public static File toFile (String file, String data) throws IOException {
        return toFile(new File(file), data);
    }

    public static File toFile(File file, InputStream in) throws IOException {
        return toFile(file, StreamUtil.toString(in));
    }

    public static File toFile(File file, String data) throws IOException {
        if (!ensureDirExists(file.getParentFile())) {
            throw new IOException("Error creating directory: "+file.getParentFile());
        }
        try (OutputStream out = new FileOutputStream(file)) {
            IOUtils.copy(new ByteArrayInputStream(data.getBytes()), out);
        }
        return file;
    }

    public static void renameOrDie (File from, File to) {
        if (!from.renameTo(to)) die("Error renaming "+abs(from)+" -> "+abs(to));
    }

    public static void writeString (File target, String data) throws IOException {
        try (FileWriter w = new FileWriter(target)) {
            w.write(data);
        }
    }

    public static void writeStringOrDie (File target, String data) {
        try {
            writeString(target, data);
        } catch (IOException e) {
            die("Error writing to file ("+abs(target)+"): "+e, e);
        }
    }

    public static void truncate (File file) { _touch(file, false); }

    public static void touch (String file) { _touch(new File(file), true); }

    public static void touch (File file) { _touch(file, true); }

    private static void _touch(File file, boolean append) {
        try (FileWriter ignored = new FileWriter(file, append)) {
            // do nothing -- if append is false, we truncate the file,
            // otherwise just update the mtime/atime, and possible create an empty file if it doesn't already exist
        } catch (IOException e) {
            final String path = (file == null) ? "null" : abs(file);
            throw new IllegalArgumentException("error "+(append ? "touching" : "truncating")+" "+path +": "+e, e);
        }
    }

    public static Path path(File f) {
        return FileSystems.getDefault().getPath(abs(f));
    }

    public static boolean isSymlink(File file) {
        return Files.isSymbolicLink(path(file));
    }

    public static String toStringExcludingLines(File file, String prefix) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().startsWith(prefix)) sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    public static String dirname(String path) {
        if (StringUtil.empty(path)) throw new NullPointerException("dirname: path was empty");
        int pos = path.lastIndexOf('/');
        if (pos == -1) return ".";
        if (path.endsWith("/")) path = chop(path);
        return path.substring(0, pos);
    }

    public static String basename(String path) {
        if (StringUtil.empty(path)) throw new NullPointerException("basename: path was empty");
        int pos = path.lastIndexOf('/');
        if (pos == -1) return path;
        if (pos == path.length()-1) throw new IllegalArgumentException("basename: invalid path: "+path);
        return path.substring(pos + 1);
    }

    // quick alias for getting an absolute path
    public static String abs(File path) { return path.getAbsolutePath(); }
    public static String abs(Path path) { return abs(path.toFile()); }
    public static String abs(String path) { return abs(new File(path)); }

    public static File mkdirOrDie(File dir) {
        if (!dir.exists() && !dir.mkdirs()) {
            final String msg = "mkdirOrDie: error creating: " + abs(dir);
            log.error(msg);
            die(msg);
        }
        assertIsDir(dir);
        return dir;
    }

    public static boolean ensureDirExists(File dir) {
        if (!dir.exists() && !dir.mkdirs()) {
            log.error("ensureDirExists: error creating: " + abs(dir));
            return false;
        }
        if (!dir.isDirectory()) {
            log.error("ensureDirExists: not a directory: " + abs(dir));
            return false;
        }
        return true;
    }

    public static void assertIsDir(File dir) {
        if (!dir.isDirectory()) {
            final String msg = "assertIsDir: not a dir: " + abs(dir);
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
    }

    public static String extension(File f) { return extension(abs(f)); }

    public static String extension(String name) {
        int lastDot = name.lastIndexOf('.');
        if (lastDot == -1) return "";
        return name.substring(lastDot);
    }

    public static String removeExtension(File f, String ext) {
        return f.getName().substring(0, f.getName().length() - ext.length());
    }

}
