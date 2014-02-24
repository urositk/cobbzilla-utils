package org.cobbzilla.util.security;

import lombok.Cleanup;
import org.cobbzilla.util.string.Base64;
import org.cobbzilla.util.string.StringUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ShaUtil {

    public static byte[] sha256 (String data) throws Exception {
        return sha256(data.getBytes(StringUtil.UTF8));
    }

    public static byte[] sha256 (byte[] data) throws Exception {
        return md().digest(data);
    }

    private static MessageDigest md() throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("SHA-256");
    }

    public static String sha256_base64 (byte[] data) throws Exception {
        return Base64.encodeBytes(sha256(data));
    }

    public static String sha256_filename (String data) throws Exception {
        return sha256_filename(data.getBytes(StringUtil.UTF8cs));
    }

    public static String sha256_filename (byte[] data) throws Exception {
        return URLEncoder.encode(Base64.encodeBytes(sha256(data)), StringUtil.UTF8);
    }

    public static String sha256_file (String file) throws Exception {
        @Cleanup final InputStream input = new FileInputStream(file);
        final MessageDigest md = getMessageDigest(input);
        return StringUtil.tohex(md.digest());
    }

    public static String sha256_url (String urlString) throws Exception {

        final URL url = new URL(urlString);
        final URLConnection urlConnection = url.openConnection();
        @Cleanup final InputStream input = urlConnection.getInputStream();
        final MessageDigest md = getMessageDigest(input);

        return StringUtil.tohex(md.digest());
    }

    public static MessageDigest getMessageDigest(InputStream input) throws NoSuchAlgorithmException, IOException, DigestException {
        final byte[] buf = new byte[4096];
        final MessageDigest md = md();
        while (true) {
            int read = input.read(buf, 0, buf.length);
            if (read == -1) break;
            md.update(buf, 0, read);
        }
        return md;
    }

}
