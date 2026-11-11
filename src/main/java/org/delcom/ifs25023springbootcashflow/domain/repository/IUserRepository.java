package org.delcom.ifs25023springbootcashflow.domain.repository;

import org.delcom.ifs25023springbootcashflow.domain.entity.Role;
import org.delcom.ifs25023springbootcashflow.domain.entity.User;

import java.util.Optional;

/** Port pengguna. Perubahan nama, password, dan foto disimpan lewat update(entity). */
public interface IUserRepository extends IRepository<User, Integer> {

    /** Membuat pengguna baru (ID diisi oleh repository). */
    User save(String username, String passwordHash, String fullName, Role role);

    /** Mencari pengguna berdasarkan username (tidak peka huruf besar/kecil). */
    Optional<User> findByUsername(String username);
}
