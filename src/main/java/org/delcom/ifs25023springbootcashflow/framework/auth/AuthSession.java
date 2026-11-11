package org.delcom.ifs25023springbootcashflow.framework.auth;

import org.delcom.ifs25023springbootcashflow.domain.entity.User;
import org.delcom.ifs25023springbootcashflow.domain.repository.IUserRepository;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WrappedSession;
import jakarta.servlet.http.Cookie;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Manajemen sesi login di sisi browser. Identitas disimpan di tiga tempat agar bertahan saat
 * refresh maupun saat sesi Vaadin dibuat ulang: VaadinSession, HTTP session, dan cookie
 * bertanda tangan (HMAC-SHA256) sehingga cookie tidak bisa dipalsukan dengan menebak ID.
 * <p>
 * Flag {@link #LOGGED_OUT_KEY} mencegah cookie lama memulihkan sesi segera setelah logout.
 */
public final class AuthSession {

    public static final String USER_ID_KEY = "cashflow.userId";
    public static final String LOGGED_OUT_KEY = "cashflow.loggedOut";
    public static final String COOKIE_NAME = "cashflow_uid";

    private static final int COOKIE_MAX_AGE_SECONDS = 8 * 60 * 60;
    private static final byte[] SECRET = loadSecret();

    private AuthSession() {
    }

    /** Menandai pengguna sebagai login pada sesi dan cookie saat ini. */
    public static void login(User user) {
        VaadinSession vaadinSession = VaadinSession.getCurrent();
        remember(vaadinSession, user.getId());
        writeCookie(signedValue(user.getId()), COOKIE_MAX_AGE_SECONDS);
    }

    /** Mengakhiri sesi: hapus identitas, pasang flag logout, dan kadaluarsakan cookie. */
    public static void logout() {
        VaadinSession vaadinSession = VaadinSession.getCurrent();
        if (vaadinSession != null) {
            vaadinSession.setAttribute(USER_ID_KEY, null);
            vaadinSession.setAttribute(LOGGED_OUT_KEY, Boolean.TRUE);
            WrappedSession http = vaadinSession.getSession();
            if (http != null) {
                http.removeAttribute(USER_ID_KEY);
                http.setAttribute(LOGGED_OUT_KEY, Boolean.TRUE);
            }
        }
        writeCookie("", 0);
    }

    /** Pengguna yang sedang login, atau kosong. Memulihkan sesi dari HTTP session/cookie bila perlu. */
    public static Optional<User> currentUser(IUserRepository userRepository) {
        VaadinSession vaadinSession = VaadinSession.getCurrent();
        if (vaadinSession == null || isLoggedOut(vaadinSession)) {
            return Optional.empty();
        }
        Integer userId = (Integer) vaadinSession.getAttribute(USER_ID_KEY);
        if (userId == null) {
            userId = httpSessionUserId(vaadinSession);
        }
        if (userId == null) {
            userId = cookieUserId();
        }
        if (userId == null) {
            return Optional.empty();
        }
        Optional<User> user = userRepository.findById(userId);
        user.ifPresent(found -> remember(vaadinSession, found.getId()));
        return user;
    }

    private static boolean isLoggedOut(VaadinSession vaadinSession) {
        if (Boolean.TRUE.equals(vaadinSession.getAttribute(LOGGED_OUT_KEY))) {
            return true;
        }
        WrappedSession http = vaadinSession.getSession();
        return http != null && Boolean.TRUE.equals(http.getAttribute(LOGGED_OUT_KEY));
    }

    private static void remember(VaadinSession vaadinSession, int userId) {
        if (vaadinSession == null) {
            return;
        }
        vaadinSession.setAttribute(USER_ID_KEY, userId);
        vaadinSession.setAttribute(LOGGED_OUT_KEY, null);
        WrappedSession http = vaadinSession.getSession();
        if (http != null) {
            http.setAttribute(USER_ID_KEY, userId);
            http.removeAttribute(LOGGED_OUT_KEY);
        }
    }

    private static Integer httpSessionUserId(VaadinSession vaadinSession) {
        WrappedSession http = vaadinSession.getSession();
        return http == null ? null : (Integer) http.getAttribute(USER_ID_KEY);
    }

    private static Integer cookieUserId() {
        VaadinRequest request = VaadinService.getCurrentRequest();
        if (request == null || request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return parseSignedValue(cookie.getValue());
            }
        }
        return null;
    }

    private static void writeCookie(String value, int maxAgeSeconds) {
        VaadinResponse response = VaadinService.getCurrentResponse();
        if (response == null) {
            return;
        }
        Cookie cookie = new Cookie(COOKIE_NAME, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAgeSeconds);
        response.addCookie(cookie);
    }

    static String signedValue(int userId) {
        return userId + "." + sign(Integer.toString(userId));
    }

    static Integer parseSignedValue(String value) {
        if (value == null) {
            return null;
        }
        int dot = value.indexOf('.');
        if (dot <= 0) {
            return null;
        }
        String idPart = value.substring(0, dot);
        String expected = sign(idPart);
        boolean valid = MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                value.substring(dot + 1).getBytes(StandardCharsets.UTF_8));
        if (!valid) {
            return null;
        }
        try {
            return Integer.valueOf(idPart);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String sign(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET, "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HmacSHA256 tidak tersedia", e);
        }
    }

    /** Kunci tanda tangan: COOKIE_SECRET bila diset, jika tidak acak per proses (cookie hangus saat restart). */
    private static byte[] loadSecret() {
        String configured = System.getProperty("COOKIE_SECRET");
        if (configured == null || configured.isBlank()) {
            configured = System.getenv("COOKIE_SECRET");
        }
        if (configured != null && !configured.isBlank()) {
            return configured.getBytes(StandardCharsets.UTF_8);
        }
        byte[] random = new byte[32];
        new SecureRandom().nextBytes(random);
        return random;
    }
}
