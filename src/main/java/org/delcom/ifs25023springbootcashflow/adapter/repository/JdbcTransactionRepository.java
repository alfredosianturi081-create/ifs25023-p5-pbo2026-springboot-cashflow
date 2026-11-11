package org.delcom.ifs25023springbootcashflow.adapter.repository;

import org.delcom.ifs25023springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25023springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25023springbootcashflow.domain.repository.ITransactionRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/** Implementasi JDBC (PostgreSQL saat runtime, H2 saat test) untuk {@link ITransactionRepository}. */
public class JdbcTransactionRepository implements ITransactionRepository {

    private static final String COLUMNS = "id, user_id, description, amount, type, proof_path, created_at";

    private final DataSource dataSource;

    public JdbcTransactionRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Transaction save(int userId, String description, long amount, TransactionType type, String proofPath) {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        String sql = "INSERT INTO transactions (user_id, description, amount, type, proof_path, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, new String[]{"id"})) {
            statement.setInt(1, userId);
            statement.setString(2, description);
            statement.setLong(3, amount);
            statement.setString(4, type.name());
            if (proofPath == null) {
                statement.setNull(5, Types.VARCHAR);
            } else {
                statement.setString(5, proofPath);
            }
            statement.setTimestamp(6, Timestamp.valueOf(now));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return new Transaction(keys.getInt(1), userId, description, amount, type, proofPath, now);
            }
        } catch (SQLException e) {
            throw failure(e);
        }
    }

    @Override
    public Transaction save(Transaction entity) {
        return save(entity.getUserId(), entity.getDescription(), entity.getAmount(),
                entity.getType(), entity.getProofPath());
    }

    @Override
    public List<Transaction> findAll() {
        return query("SELECT " + COLUMNS + " FROM transactions ORDER BY id", null);
    }

    @Override
    public List<Transaction> findByUserId(int userId) {
        return query("SELECT " + COLUMNS + " FROM transactions WHERE user_id = ? ORDER BY id", userId);
    }

    @Override
    public Optional<Transaction> findById(Integer id) {
        return query("SELECT " + COLUMNS + " FROM transactions WHERE id = ?", id).stream().findFirst();
    }

    @Override
    public List<Transaction> findBy(Predicate<Transaction> predicate) {
        return findAll().stream().filter(predicate).toList();
    }

    @Override
    public boolean deleteById(Integer id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM transactions WHERE id = ?")) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw failure(e);
        }
    }

    @Override
    public void update(Transaction entity) {
        String sql = "UPDATE transactions SET description = ?, amount = ?, type = ?, proof_path = ? WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, entity.getDescription());
            statement.setLong(2, entity.getAmount());
            statement.setString(3, entity.getType().name());
            if (entity.getProofPath() == null) {
                statement.setNull(4, Types.VARCHAR);
            } else {
                statement.setString(4, entity.getProofPath());
            }
            statement.setInt(5, entity.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw failure(e);
        }
    }

    private List<Transaction> query(String sql, Integer parameter) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (parameter != null) {
                statement.setInt(1, parameter);
            }
            try (ResultSet rs = statement.executeQuery()) {
                List<Transaction> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(map(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            throw failure(e);
        }
    }

    private static Transaction map(ResultSet rs) throws SQLException {
        return new Transaction(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("description"),
                rs.getLong("amount"),
                TransactionType.valueOf(rs.getString("type")),
                rs.getString("proof_path"),
                rs.getTimestamp("created_at").toLocalDateTime());
    }

    private static IllegalStateException failure(SQLException e) {
        return new IllegalStateException("Gagal mengakses tabel transactions", e);
    }
}
