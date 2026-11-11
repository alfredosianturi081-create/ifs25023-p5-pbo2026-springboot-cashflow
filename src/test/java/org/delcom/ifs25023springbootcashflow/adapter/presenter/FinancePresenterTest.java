package org.delcom.ifs25023springbootcashflow.adapter.presenter;

import org.delcom.ifs25023springbootcashflow.domain.entity.BalanceSummary;
import org.delcom.ifs25023springbootcashflow.domain.entity.Role;
import org.delcom.ifs25023springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25023springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FinancePresenterTest {

    private final FinancePresenter presenter = new FinancePresenter();
    private final Transaction income = new Transaction(1, 1, "Gaji", 1_000_000, TransactionType.INCOME, null,
            LocalDateTime.of(2026, 3, 5, 9, 7));
    private final Transaction expense = new Transaction(2, 1, "Makan", 25_000, TransactionType.EXPENSE, null,
            LocalDateTime.of(2026, 3, 5, 12, 30));

    @Test
    void formatMoneyUsesGroupingAndTwoDecimals() {
        assertEquals("Rp 1,000,000.00", presenter.formatMoney(1_000_000));
        assertEquals("Rp 0.00", presenter.formatMoney(0));
        assertEquals("-Rp 1,500.00", presenter.formatMoney(-1500));
    }

    @Test
    void labelsAndLineFormat() {
        assertEquals("Pemasukan", presenter.typeLabel(TransactionType.INCOME));
        assertEquals("Pengeluaran", presenter.typeLabel(TransactionType.EXPENSE));
        assertEquals("Admin", presenter.roleLabel(Role.ADMIN));
        assertEquals("Pengguna", presenter.roleLabel(Role.USER));
        assertEquals("05/03/2026 09:07", presenter.formatCreatedAt(income));
        assertEquals("[1] Gaji | Rp 1,000,000.00 | [Pemasukan]", presenter.format(income));
    }

    @Test
    void balanceLines() {
        assertEquals("Saldo: Rp 975,000.00", presenter.formatBalance(975_000));
        assertEquals("Saldo: Rp 975,000.00", presenter.formatBalance(new BalanceSummary(2, 1_000_000, 25_000)));
    }

    @Test
    void notificationMessages() {
        assertEquals("Pemasukan dicatat: [1] Gaji | Rp 1,000,000.00 | [Pemasukan]", presenter.showAddSuccess(income));
        assertEquals("Pengeluaran dicatat: [2] Makan | Rp 25,000.00 | [Pengeluaran]", presenter.showAddSuccess(expense));
        assertEquals("Transaksi diperbarui: [2] Makan | Rp 25,000.00 | [Pengeluaran]", presenter.showUpdateSuccess(expense));
        assertEquals("Transaksi dihapus: [2] Makan | Rp 25,000.00 | [Pengeluaran]", presenter.showDeleteSuccess(expense));
        assertEquals("Periksa kembali isian: kosong", presenter.showValidationError("kosong"));
        assertEquals("Data tidak ditemukan: hilang", presenter.showNotFoundError("hilang"));
    }
}
