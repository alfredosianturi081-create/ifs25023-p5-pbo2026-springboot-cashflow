package org.delcom.ifs25023springbootcashflow.adapter.repository;

import org.delcom.ifs25023springbootcashflow.domain.entity.Role;
import org.delcom.ifs25023springbootcashflow.domain.entity.User;
import org.delcom.ifs25023springbootcashflow.domain.repository.IUserRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/** Implementasi JDBC untuk {@link IUserRepository}. */
public class JdbcUserRepository implements IUserRepository {

    private static final String COLUMNS = "id, username, password_hash, full_name, role, photo_path";

    private final DataSource dataSource;

    public JdbcUserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public User save(String username, String passwordHash, String fullName, Role role) {
        String sql = "INSERT INTO users (username, password_hash, full_name, role) VALUES (?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, new String[]{"id"})) {
            statement.setString(1, username);
            statement.setString(2, passwordHash);
            statement.setString(3, fullName);
            statement.setString(4, role.name());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return new User(keys.getInt(1), username, passwordHash, fullName, role, null);
            }
        } catch (SQLException e) {
            throw failure(e);
        }
    }

    @Override
    public User save(User entity) {
        return save(entity.getUsername(), entity.getPasswordHash(), entity.getFullName(), entity.getRole());
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return query("SELECT " + COLUMNS + " FROM users WHERE LOWER(username) = LOWER(?)", username)
                .stream().findFirst();
    }

    @Override
    public List<User> findAll() {
        return query("SELECT " + COLUMNS + " FROM users ORDER BY id", null);
    }

    @Override
    public Optional<User> findById(Integer id) {
        return query("SELECT " + COLUMNS + " FROM users WHERE id = ?", id).stream().findFirst();
    }

    @Override
    public List<User> findBy(Predicate<User> predicate) {
        return findAll().stream().filter(predicate).toList();
    }

    @Override
    public boolean deleteById(Integer id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM users WHERE id = ?")) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw failure(e);
        }
    }

    @Override
    public void update(User entity) {
        String sql = "UPDATE users SET password_hash = ?, full_name = ?, photo_path = ? WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, entity.getPasswordHash());
            statement.setString(2, entity.getFullName());
            if (entity.getPhotoPath() == null) {
                statement.setNull(3, Types.VARCHAR);
            } else {
                statement.setString(3, entity.getPhotoPath());
            }
            statement.setInt(4, entity.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw failure(e);
        }
    }

    private List<User> query(String sql, Object parameter) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (parameter instanceof Integer number) {
                statement.setInt(1, number);
            } else if (parameter instanceof String text) {
                statement.setString(1, text);
            }
            try (ResultSet rs = statement.executeQuery()) {
                List<User> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("full_name"),
                            Role.valueOf(rs.getString("role")),
                            rs.getString("photo_path")));
                }
                return result;
            }
        } catch (SQLException e) {
            throw failure(e);
        }
    }

    private static IllegalStateException failure(SQLException e) {
        return new IllegalStateException("Gagal mengakses tabel users", e);
    }
}
