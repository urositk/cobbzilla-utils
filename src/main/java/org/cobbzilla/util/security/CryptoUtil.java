package org.cobbzilla.util.security;

import org.apache.commons.io.IOUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CryptoUtil {

    public static final String CONFIG_BLOCK_CIPHER = "AES/CBC/PKCS5Padding";
    public static final String CONFIG_KEY_CIPHER = "AES";

    public static final String RSA_PREFIX = "-----BEGIN RSA PRIVATE KEY-----";
    public static final String RSA_SUFFIX = "-----END RSA PRIVATE KEY-----";

    private static final MessageDigest MESSAGE_DIGEST;
    static {
        try {
            MESSAGE_DIGEST = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("error creating SHA-256 MessageDigest: "+e);
        }
    }

    public static byte[] toBytes(InputStream data) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(data, out);
        return out.toByteArray();
    }

    public static String extractRsa (String data) {
        int startPos = data.indexOf(RSA_PREFIX);
        if (startPos == -1) return null;
        int endPos = data.indexOf(RSA_SUFFIX);
        if (endPos == -1) return null;
        return data.substring(startPos, endPos + RSA_SUFFIX.length());
    }

    public static byte[] encrypt (InputStream data, String passphrase) throws Exception {
        return encrypt(toBytes(data), passphrase);
    }

    public static byte[] encrypt (byte[] data, String passphrase) throws Exception {
        final Cipher cipher = Cipher.getInstance(CONFIG_BLOCK_CIPHER);
        final Key keySpec = new SecretKeySpec(sha256(passphrase), CONFIG_KEY_CIPHER);
        final IvParameterSpec initVector = new IvParameterSpec(new byte[cipher.getBlockSize()]);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, initVector);
        return cipher.doFinal(data);
    }

    public static byte[] sha256(String passphrase) throws Exception {
        return ShaUtil.sha256(passphrase);
    }

    public static byte[] decrypt (InputStream data, String passphrase) throws Exception {
        return decrypt(toBytes(data), passphrase);
    }

    public static byte[] decrypt (byte[] data, String passphrase) throws Exception {
        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        final Key keySpec = new SecretKeySpec(sha256(passphrase), CONFIG_KEY_CIPHER);
        final IvParameterSpec initVector = new IvParameterSpec(new byte[cipher.getBlockSize()]);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, initVector);
        return cipher.doFinal(data);
    }

    public static byte[] encryptOrDie(byte[] data, String passphrase) {
        try { return encrypt(data, passphrase); } catch (Exception e) {
            throw new IllegalStateException("Error encrypting: "+e, e);
        }
    }
}
