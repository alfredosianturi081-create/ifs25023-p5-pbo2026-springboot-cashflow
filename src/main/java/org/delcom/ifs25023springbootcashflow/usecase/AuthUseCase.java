package org.delcom.ifs25023springbootcashflow.usecase;

import org.delcom.ifs25023springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25023springbootcashflow.domain.entity.Role;
import org.delcom.ifs25023springbootcashflow.domain.entity.User;
import org.delcom.ifs25023springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25023springbootcashflow.domain.repository.IUserRepository;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Use case autentikasi: registrasi, login, dan penyemaian admin awal.
 * Tidak melakukan I/O UI; kegagalan dilaporkan lewat {@link ValidationException}.
 */
public class AuthUseCase {

    public static final int MIN_PASSWORD_LENGTH = 6;

    private static final Pattern USERNAME_PATTERN = Pattern.compile("[A-Za-z0-9_.-]{3,50}");
    private static final String LOGIN_FAILED = "Username atau password salah";

    private final IUserRepository userRepository;

    public AuthUseCase(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Mendaftarkan pengguna baru dengan role USER. */
    public User register(String username, String password, String fullName) throws ValidationException {
        String cleanUsername = username == null ? "" : username.trim();
        if (!USERNAME_PATTERN.matcher(cleanUsername).matches()) {
            throw new ValidationException(
                    "Username 3-50 karakter: huruf, angka, titik, garis bawah, atau strip");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new ValidationException("Nama lengkap tidak boleh kosong");
        }
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException("Password minimal " + MIN_PASSWORD_LENGTH + " karakter");
        }
        if (userRepository.findByUsername(cleanUsername).isPresent()) {
            throw new ValidationException("Username '" + cleanUsername + "' sudah dipakai");
        }
        return userRepository.save(cleanUsername, PasswordHasher.hash(password), fullName.trim(), Role.USER);
    }

    /** Memverifikasi kredensial; pesan galat sengaja sama untuk username/password salah. */
    public User login(String username, String password) throws ValidationException {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            throw new ValidationException("Username dan password wajib diisi");
        }
        Optional<User> found = userRepository.findByUsername(username.trim());
        if (found.isEmpty() || !PasswordHasher.verify(password, found.get().getPasswordHash())) {
            throw new ValidationException(LOGIN_FAILED);
        }
        return found.get();
    }

    /** Membuat admin dari konfigurasi ROOT_* bila belum ada; bila sudah ada, dikembalikan apa adanya. */
    public User ensureRootAdmin(String username, String password) throws ValidationException {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            throw new ValidationException("ROOT_USERNAME dan ROOT_PASSWORD wajib diisi");
        }
        String cleanUsername = username.trim();
        Optional<User> existing = userRepository.findByUsername(cleanUsername);
        if (existing.isPresent()) {
            return existing.get();
        }
        return userRepository.save(cleanUsername, PasswordHasher.hash(password), "Administrator", Role.ADMIN);
    }
}
