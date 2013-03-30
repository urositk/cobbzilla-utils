package org.cobbzilla.util.security;

import org.slf4j.Logger;

import java.io.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {

    private MD5Util () {}

    public static byte[] getMD5 ( byte[] bytes ) {
        return getMD5(bytes, 0, bytes.length);
    }
    public static byte[] getMD5 ( byte[] bytes, int start, int len ) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update( bytes, start, len );
            return md5.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Error calculating MD5: " + e);
        }
    }

    public static String md5hex (Logger log, File file) throws IOException {
        int BUFSIZ = 4096;
        try (FileInputStream fin = new FileInputStream(file)) {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            BufferedInputStream in = new BufferedInputStream(fin);
            byte[] buf = new byte[BUFSIZ];
            int bytesRead = in.read(buf);
            while (bytesRead != -1) {
                md5.update(buf, 0, bytesRead);
                bytesRead = in.read(buf);
            }
            return tohex(md5.digest());

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Error calculating MD5: " + e);

        }

    }

    public static String md5hex ( String s ) {
        byte[] bytes = getMD5(s.getBytes());
        return tohex(bytes);
    }

    public static String md5hex (MessageDigest md) {
        return tohex(md.digest());
    }

    public static String md5hex (byte[] data) {
        return md5hex(data, 0, data.length);
    }
    public static String md5hex (byte[] data, int start, int len) {
        byte[] bytes = getMD5(data, start, len);
        return tohex(bytes);
    }

    public static String tohex (byte[] data) {
        return tohex(data, 0, data.length);
    }
    public static String tohex (byte[] data, int start, int len) {
        StringBuffer b = new StringBuffer();
        int stop = start+len;
        for (int i=start; i<stop; i++) {
            b.append(getHexValue(data[i]));
        }
        return b.toString();
    }

    public static final String[] HEX_DIGITS = {"0", "1", "2", "3",
            "4", "5", "6", "7",
            "8", "9", "a", "b",
            "c", "d", "e", "f"};


    /**
     * Get the hexadecimal string representation for a byte.
     * The leading 0x is not included.
     *
     * @param b the byte to process
     * @return a String representing the hexadecimal value of the byte
     */
    public static String getHexValue(byte b) {
        int i = (int) b;
        return HEX_DIGITS[((i >> 4) + 16) % 16] + HEX_DIGITS[(i + 128) % 16];
    }

    public static MD5InputStream getMD5InputStream (InputStream in) {
        try {
            return new MD5InputStream(in);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Bad algorithm: " + e);
        }
    }

    public static final class MD5InputStream extends DigestInputStream {

        public MD5InputStream(InputStream stream) throws NoSuchAlgorithmException {
            super(stream, MessageDigest.getInstance("MD5"));
        }

        public String md5hex () {
            return MD5Util.md5hex(getMessageDigest());
        }
    }
}
