package org.delcom.ifs25023springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 1, 1, 10, 0);

    private Transaction tx(int id, long amount, TransactionType type, int minutes) {
        return new Transaction(id, 1, "t" + id, amount, type, null, T0.plusMinutes(minutes));
    }

    @Test
    void transactionExposesAndChangesState() {
        Transaction t = new Transaction(1, 7, "Gaji", 100, TransactionType.INCOME, "a.png", T0);
        assertEquals(1, t.getId());
        assertEquals(7, t.getUserId());
        assertEquals("Gaji", t.getDescription());
        assertEquals(100, t.getAmount());
        assertEquals(T0, t.getCreatedAt());
        assertEquals("a.png", t.getProofPath());
        assertTrue(t.isIncome());
        assertTrue(t.hasProof());
        assertEquals(100, t.signedAmount());

        t.changeDescription("Bonus");
        t.changeAmount(50);
        t.changeType(TransactionType.EXPENSE);
        t.changeProofPath(null);
        assertEquals("Bonus", t.getDescription());
        assertEquals(TransactionType.EXPENSE, t.getType());
        assertFalse(t.isIncome());
        assertFalse(t.hasProof());
        assertEquals(-50, t.signedAmount());
        t.changeProofPath("  ");
        assertFalse(t.hasProof());
    }

    @Test
    void userExposesAndChangesState() {
        User u = new User(1, "budi", "hash", "Budi", Role.USER, null);
        assertEquals(1, u.getId());
        assertEquals("budi", u.getUsername());
        assertEquals("hash", u.getPasswordHash());
        assertEquals(Role.USER, u.getRole());
        assertFalse(u.isAdmin());
        assertFalse(u.hasPhoto());

        u.changeFullName("Budi S");
        u.changePasswordHash("h2");
        u.changePhotoPath("p.png");
        assertEquals("Budi S", u.getFullName());
        assertEquals("h2", u.getPasswordHash());
        assertEquals("p.png", u.getPhotoPath());
        assertTrue(u.hasPhoto());
        u.changePhotoPath(" ");
        assertFalse(u.hasPhoto());
        assertTrue(new User(2, "a", "h", "A", Role.ADMIN, null).isAdmin());
    }

    @Test
    void balanceSummaryComputesBalanceAndShare() {
        BalanceSummary s = new BalanceSummary(3, 750, 250);
        assertEquals(500, s.balance());
        assertEquals(75, s.incomeSharePercent());
        assertEquals(0, new BalanceSummary(0, 0, 0).incomeSharePercent());
        assertEquals(-5, new BalanceSummary(1, 0, 5).balance());
    }

    @Test
    void sortOptionsOrderTransactions() {
        Transaction a = tx(1, 300, TransactionType.INCOME, 0);
        Transaction b = tx(2, 100, TransactionType.EXPENSE, 5);
        Transaction c = tx(3, 200, TransactionType.EXPENSE, 10);

        assertEquals(List.of(2, 3, 1), ids(sorted(SortOption.AMOUNT_ASC, a, b, c)));
        assertEquals(List.of(1, 3, 2), ids(sorted(SortOption.AMOUNT_DESC, a, b, c)));
        assertEquals(List.of(1, 3, 2), ids(sorted(SortOption.INCOME_FIRST, a, b, c)));
        assertEquals(List.of(3, 2, 1), ids(sorted(SortOption.EXPENSE_FIRST, a, b, c)));
        assertEquals(List.of(3, 2, 1), ids(sorted(SortOption.NEWEST, a, b, c)));
        assertEquals(List.of(1, 2, 3), ids(sorted(SortOption.OLDEST, a, b, c)));
        for (SortOption option : SortOption.values()) {
            assertFalse(option.label().isBlank());
        }
    }

    private List<Transaction> sorted(SortOption option, Transaction... items) {
        List<Transaction> list = new ArrayList<>(List.of(items));
        list.sort(option.comparator());
        return list;
    }

    private List<Integer> ids(List<Transaction> list) {
        return list.stream().map(Transaction::getId).toList();
    }
}
