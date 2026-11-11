package org.delcom.ifs25023springbootcashflow.adapter.repository;

import org.delcom.ifs25023springbootcashflow.domain.entity.Role;
import org.delcom.ifs25023springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25023springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25023springbootcashflow.domain.entity.User;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.lang.reflect.Constructor;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Menguji repository JDBC terhadap H2 (mode PostgreSQL), termasuk jalur kegagalan SQL. */
class JdbcRepositoriesTest {

    private DataSource dataSource;
    private JdbcUserRepository users;
    private JdbcTransactionRepository transactions;

    @BeforeEach
    void setUp() {
        JdbcDataSource h2 = new JdbcDataSource();
        h2.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        h2.setUser("sa");
        dataSource = h2;
        SchemaInitializer.initialize(dataSource);
        users = new JdbcUserRepository(dataSource);
        transactions = new JdbcTransactionRepository(dataSource);
    }

    @Test
    void schemaInitializerIsIdempotent() {
        SchemaInitializer.initialize(dataSource);
        SchemaInitializer.initialize(dataSource);
        assertTrue(users.findAll().isEmpty());
    }

    @Test
    void userRepositoryCrud() {
        User budi = users.save("budi", "hash", "Budi", Role.USER);
        User admin = users.save(new User(0, "admin", "h", "Administrator", Role.ADMIN, null));

        assertTrue(budi.getId() > 0);
        assertEquals(2, users.findAll().size());
        assertEquals("Budi", users.findById(budi.getId()).orElseThrow().getFullName());
        assertTrue(users.findById(999).isEmpty());
        assertEquals(admin.getId(), users.findByUsername("ADMIN").orElseThrow().getId());
        assertEquals(Role.ADMIN, users.findByUsername("admin").orElseThrow().getRole());
        assertTrue(users.findByUsername("hantu").isEmpty());
        assertEquals(1, users.findBy(User::isAdmin).size());

        budi.changeFullName("Budi S.");
        budi.changePasswordHash("h2");
        budi.changePhotoPath("p.png");
        users.update(budi);
        User reloaded = users.findById(budi.getId()).orElseThrow();
        assertEquals("Budi S.", reloaded.getFullName());
        assertEquals("h2", reloaded.getPasswordHash());
        assertEquals("p.png", reloaded.getPhotoPath());

        budi.changePhotoPath(null);
        users.update(budi);
        assertNull(users.findById(budi.getId()).orElseThrow().getPhotoPath());

        assertTrue(users.deleteById(admin.getId()));
        assertFalse(users.deleteById(admin.getId()));
    }

    @Test
    void transactionRepositoryCrud() {
        User budi = users.save("budi", "hash", "Budi", Role.USER);
        User sari = users.save("sari", "hash", "Sari", Role.USER);

        Transaction income = transactions.save(budi.getId(), "Gaji", 5_000_000, TransactionType.INCOME, null);
        Transaction expense = transactions.save(budi.getId(), "Makan", 25_000, TransactionType.EXPENSE, "b.png");
        transactions.save(new Transaction(0, sari.getId(), "Punya Sari", 99, TransactionType.EXPENSE, null,
                java.time.LocalDateTime.now()));

        assertTrue(income.getId() > 0);
        assertEquals(3, transactions.findAll().size());
        assertEquals(2, transactions.findByUserId(budi.getId()).size());
        assertEquals(1, transactions.findBy(t -> t.getAmount() == 99).size());
        assertTrue(transactions.findById(999).isEmpty());

        Transaction loaded = transactions.findById(expense.getId()).orElseThrow();
        assertEquals("Makan", loaded.getDescription());
        assertEquals(25_000, loaded.getAmount());
        assertEquals(TransactionType.EXPENSE, loaded.getType());
        assertEquals("b.png", loaded.getProofPath());
        assertEquals(expense.getCreatedAt(), loaded.getCreatedAt());

        loaded.changeDescription("Makan malam");
        loaded.changeAmount(30_000);
        loaded.changeType(TransactionType.INCOME);
        loaded.changeProofPath(null);
        transactions.update(loaded);
        Transaction updated = transactions.findById(expense.getId()).orElseThrow();
        assertEquals("Makan malam", updated.getDescription());
        assertEquals(TransactionType.INCOME, updated.getType());
        assertNull(updated.getProofPath());

        loaded.changeProofPath("baru.png");
        transactions.update(loaded);
        assertEquals("baru.png", transactions.findById(expense.getId()).orElseThrow().getProofPath());

        assertTrue(transactions.deleteById(income.getId()));
        assertFalse(transactions.deleteById(income.getId()));
    }

    @Test
    void sqlFailuresAreWrappedAsIllegalState() {
        DataSource broken = new FailingDataSource();
        JdbcUserRepository badUsers = new JdbcUserRepository(broken);
        JdbcTransactionRepository badTransactions = new JdbcTransactionRepository(broken);
        User user = new User(1, "u", "h", "U", Role.USER, null);
        Transaction tx = new Transaction(1, 1, "t", 1, TransactionType.INCOME, null, java.time.LocalDateTime.now());

        assertThrows(IllegalStateException.class, () -> SchemaInitializer.initialize(broken));
        assertThrows(IllegalStateException.class, () -> badUsers.save("u", "h", "U", Role.USER));
        assertThrows(IllegalStateException.class, () -> badUsers.findAll());
        assertThrows(IllegalStateException.class, () -> badUsers.findById(1));
        assertThrows(IllegalStateException.class, () -> badUsers.findByUsername("u"));
        assertThrows(IllegalStateException.class, () -> badUsers.deleteById(1));
        assertThrows(IllegalStateException.class, () -> badUsers.update(user));
        assertThrows(IllegalStateException.class, () -> badTransactions.save(1, "t", 1, TransactionType.INCOME, null));
        assertThrows(IllegalStateException.class, () -> badTransactions.findAll());
        assertThrows(IllegalStateException.class, () -> badTransactions.findByUserId(1));
        assertThrows(IllegalStateException.class, () -> badTransactions.findById(1));
        assertThrows(IllegalStateException.class, () -> badTransactions.deleteById(1));
        assertThrows(IllegalStateException.class, () -> badTransactions.update(tx));
    }

    @Test
    void schemaInitializerPrivateConstructorCovered() throws Exception {
        Constructor<SchemaInitializer> constructor = SchemaInitializer.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
    }
}
