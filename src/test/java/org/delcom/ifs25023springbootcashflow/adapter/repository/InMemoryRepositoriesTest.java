package org.delcom.ifs25023springbootcashflow.adapter.repository;

import org.delcom.ifs25023springbootcashflow.domain.entity.Role;
import org.delcom.ifs25023springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25023springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25023springbootcashflow.domain.entity.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryRepositoriesTest {

    @Test
    void transactionRepositoryCrudAndPerUserQuery() {
        TransactionRepository repo = new TransactionRepository();
        Transaction a = repo.save(1, "Gaji", 100, TransactionType.INCOME, null);
        Transaction b = repo.save(2, "Makan", 5, TransactionType.EXPENSE, "b.png");

        assertEquals(1, a.getId());
        assertEquals(2, b.getId());
        assertEquals(2, repo.findAll().size());
        assertEquals(1, repo.findByUserId(1).size());
        assertEquals("Makan", repo.findById(2).orElseThrow().getDescription());
        assertTrue(repo.findById(9).isEmpty());
        assertEquals(1, repo.findBy(t -> t.getAmount() > 50).size());

        a.changeDescription("Gaji Januari");
        repo.update(a);
        assertEquals("Gaji Januari", repo.findById(1).orElseThrow().getDescription());

        assertTrue(repo.deleteById(1));
        assertFalse(repo.deleteById(1));
        assertEquals(1, repo.findAll().size());
    }

    @Test
    void userRepositoryFindsByUsernameIgnoringCase() {
        UserRepository repo = new UserRepository();
        User budi = repo.save("budi", "hash", "Budi", Role.USER);
        assertEquals(1, budi.getId());
        assertEquals(budi.getId(), repo.findByUsername("BUDI").orElseThrow().getId());
        assertTrue(repo.findByUsername("hantu").isEmpty());
    }
}
