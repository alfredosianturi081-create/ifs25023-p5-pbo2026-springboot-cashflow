package org.delcom.ifs25023springbootcashflow.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Hash dan verifikasi password memakai SHA-256 (hex huruf kecil). */
public final class PasswordHasher {

    private PasswordHasher() {
    }

    /** Menghasilkan hash SHA-256 heksadesimal dari password mentah. */
    public static String hash(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 wajib ada di setiap JVM; cabang ini tidak pernah tercapai.
            throw new IllegalStateException("SHA-256 tidak tersedia", e);
        }
    }

    /** Memeriksa apakah password mentah cocok dengan hash tersimpan. */
    public static boolean verify(String rawPassword, String expectedHash) {
        return hash(rawPassword).equals(expectedHash);
    }
}
