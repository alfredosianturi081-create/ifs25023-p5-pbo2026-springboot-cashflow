package org.delcom.ifs25023springbootcashflow.usecase;

import org.delcom.ifs25023springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25023springbootcashflow.domain.entity.User;
import org.delcom.ifs25023springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25023springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25023springbootcashflow.domain.repository.IUserRepository;

import java.util.Comparator;
import java.util.List;

/** Use case pengelolaan pengguna: daftar, profil, dan foto. */
public class UserUseCase {

    private final IUserRepository userRepository;

    public UserUseCase(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Semua pengguna, urut ID. */
    public List<User> listUsers() {
        return userRepository.findAll().stream()
                .sorted(Comparator.comparingInt(User::getId))
                .toList();
    }

    /** Mencari pengguna berdasarkan potongan username atau nama (case-insensitive). */
    public List<User> searchUsers(String keyword) {
        String needle = keyword == null ? "" : keyword.trim().toLowerCase();
        return userRepository.findBy(u -> u.getUsername().toLowerCase().contains(needle)
                        || u.getFullName().toLowerCase().contains(needle))
                .stream()
                .sorted(Comparator.comparingInt(User::getId))
                .toList();
    }

    /** Mengambil satu pengguna. */
    public User getUser(int id) throws EntityNotFoundException {
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Pengguna", id));
    }

    /** Mengubah nama dan (opsional) password. {@code newPassword} kosong/null berarti password tetap. */
    public User updateProfile(int id, String fullName, String newPassword)
            throws EntityNotFoundException, ValidationException {
        User user = getUser(id);
        if (fullName == null || fullName.isBlank()) {
            throw new ValidationException("Nama lengkap tidak boleh kosong");
        }
        boolean changePassword = newPassword != null && !newPassword.isEmpty();
        if (changePassword && newPassword.length() < AuthUseCase.MIN_PASSWORD_LENGTH) {
            throw new ValidationException("Password minimal " + AuthUseCase.MIN_PASSWORD_LENGTH + " karakter");
        }
        user.changeFullName(fullName.trim());
        if (changePassword) {
            user.changePasswordHash(PasswordHasher.hash(newPassword));
        }
        userRepository.update(user);
        return user;
    }

    /** Menyimpan path foto profil baru. */
    public User updatePhoto(int id, String photoPath) throws EntityNotFoundException {
        User user = getUser(id);
        user.changePhotoPath(photoPath);
        userRepository.update(user);
        return user;
    }
}
