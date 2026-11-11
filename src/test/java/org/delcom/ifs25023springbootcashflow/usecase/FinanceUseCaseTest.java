package org.delcom.ifs25023springbootcashflow.usecase;

import org.delcom.ifs25023springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25023springbootcashflow.domain.entity.BalanceSummary;
import org.delcom.ifs25023springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25023springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25023springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25023springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25023springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinanceUseCaseTest {

    private static final int BUDI = 1;
    private static final int SARI = 2;

    private TransactionRepository repository;
    private FinanceUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new TransactionRepository();
        useCase = new FinanceUseCase(repository);
    }

    @Test
    void parseAmountAcceptsPlainAndGroupedNumbers() throws Exception {
        assertEquals(75000, useCase.parseAmount("75000"));
        assertEquals(1_500_000, useCase.parseAmount("1.500.000"));
        assertEquals(2_000_000, useCase.parseAmount("Rp 2,000,000"));
        assertEquals(10, useCase.parseAmount(" rp. 10 "));
    }

    @Test
    void parseAmountRejectsInvalidInput() {
        for (String bad : new String[]{null, "", "abc", "12,5", "1.5", "-5", "1.00.000", "0", "0000"}) {
            assertThrows(ValidationException.class, () -> useCase.parseAmount(bad), String.valueOf(bad));
        }
        assertThrows(ValidationException.class, () -> useCase.parseAmount("1000000000000"));
    }

    @Test
    void addIncomeAndExpenseValidateAndScopeToUser() throws Exception {
        Transaction income = useCase.addIncome(BUDI, "  Gaji  ", 5_000_000, null);
        Transaction expense = useCase.addExpense(BUDI, "Makan", 25_000, "  ");
        assertEquals("Gaji", income.getDescription());
        assertTrue(income.isIncome());
        assertFalse(expense.isIncome());
        assertNull(expense.getProofPath());
        assertEquals("b.png", useCase.addExpense(BUDI, "Parkir", 2_000, "b.png").getProofPath());

        assertThrows(ValidationException.class, () -> useCase.addIncome(BUDI, " ", 1, null));
        assertThrows(ValidationException.class, () -> useCase.addIncome(BUDI, null, 1, null));
        assertThrows(ValidationException.class, () -> useCase.addIncome(BUDI, "x".repeat(256), 1, null));
        assertThrows(ValidationException.class, () -> useCase.addIncome(BUDI, "x", 0, null));
        assertThrows(ValidationException.class, () -> useCase.addExpense(BUDI, "x", -1, null));
    }

    @Test
    void listFiltersSortsAndScopesPerUser() throws Exception {
        useCase.addIncome(BUDI, "Gaji", 5_000_000, null);
        useCase.addExpense(BUDI, "Makan siang", 25_000, null);
        useCase.addExpense(SARI, "Punya Sari", 99, null);

        assertEquals(2, useCase.getTransactions(BUDI).size());
        assertEquals(1, useCase.getTransactions(SARI).size());
        assertEquals(1, useCase.list(BUDI, "GAJI", null).size());
        assertEquals(2, useCase.list(BUDI, "  ", null).size());
        assertEquals(2, useCase.list(BUDI, null, SortOption.AMOUNT_DESC).size());
        assertEquals(5_000_000, useCase.list(BUDI, null, SortOption.AMOUNT_DESC).get(0).getAmount());
        assertEquals("Makan siang", useCase.list(BUDI, null, SortOption.EXPENSE_FIRST).get(0).getDescription());
    }

    @Test
    void summaryAndBalance() throws Exception {
        useCase.addIncome(BUDI, "Gaji", 5_000_000, null);
        useCase.addExpense(BUDI, "Makan", 25_000, null);
        BalanceSummary summary = useCase.summarize(BUDI);
        assertEquals(2, summary.count());
        assertEquals(5_000_000, summary.totalIncome());
        assertEquals(25_000, summary.totalExpense());
        assertEquals(4_975_000, useCase.getBalance(BUDI));
        assertEquals(0, useCase.getBalance(SARI));
    }

    @Test
    void getTransactionAndRemoveEnforceOwnership() throws Exception {
        Transaction t = useCase.addExpense(BUDI, "Makan", 25_000, "bukti.png");
        assertEquals(t.getId(), useCase.getTransaction(BUDI, t.getId()).getId());

        assertThrows(ValidationException.class, () -> useCase.getTransaction(BUDI, 0));
        assertThrows(ValidationException.class, () -> useCase.removeTransaction(BUDI, -3));
        assertThrows(EntityNotFoundException.class, () -> useCase.getTransaction(SARI, t.getId()));
        assertThrows(EntityNotFoundException.class, () -> useCase.removeTransaction(SARI, t.getId()));
        assertThrows(EntityNotFoundException.class, () -> useCase.removeTransaction(BUDI, 999));

        Transaction removed = useCase.removeTransaction(BUDI, t.getId());
        assertEquals("bukti.png", removed.getProofPath());
        assertTrue(useCase.getTransactions(BUDI).isEmpty());
    }

    @Test
    void updateChangesOnlyProvidedFields() throws Exception {
        Transaction t = useCase.addExpense(BUDI, "Makan", 25_000, "lama.png");

        useCase.updateTransaction(BUDI, t.getId(), null, null, null, null);
        assertEquals("Makan", t.getDescription());
        assertEquals("lama.png", t.getProofPath());

        useCase.updateTransaction(BUDI, t.getId(), " Makan malam ", 30_000L, TransactionType.INCOME, "baru.png");
        assertEquals("Makan malam", t.getDescription());
        assertEquals(30_000, t.getAmount());
        assertTrue(t.isIncome());
        assertEquals("baru.png", t.getProofPath());

        useCase.updateTransaction(BUDI, t.getId(), null, null, null, "");
        assertNull(t.getProofPath());
    }

    @Test
    void updateRejectsInvalidValuesAndForeignTransactions() throws Exception {
        Transaction t = useCase.addExpense(BUDI, "Makan", 25_000, null);
        assertThrows(ValidationException.class, () -> useCase.updateTransaction(BUDI, t.getId(), " ", null, null, null));
        assertThrows(ValidationException.class, () -> useCase.updateTransaction(BUDI, t.getId(), null, 0L, null, null));
        assertThrows(EntityNotFoundException.class,
                () -> useCase.updateTransaction(SARI, t.getId(), "x", null, null, null));
        assertEquals(List.of("Makan"), useCase.getTransactions(BUDI).stream().map(Transaction::getDescription).toList());
    }
}
