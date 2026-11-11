package org.delcom.ifs25023springbootcashflow.adapter.presenter;

import org.delcom.ifs25023springbootcashflow.domain.entity.BalanceSummary;
import org.delcom.ifs25023springbootcashflow.domain.entity.Role;
import org.delcom.ifs25023springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25023springbootcashflow.domain.entity.TransactionType;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Presenter: mengubah data domain menjadi teks siap tampil (uang, tanggal, pesan notifikasi).
 * Format bukan tanggung jawab entity maupun use case.
 */
public class FinancePresenter {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Format uang: {@code Rp 1,000,000.00}; nilai negatif menjadi {@code -Rp 1,000.00}. */
    public String formatMoney(long amount) {
        String number = String.format(Locale.US, "%,.2f", BigDecimal.valueOf(Math.abs(amount)));
        return (amount < 0 ? "-Rp " : "Rp ") + number;
    }

    public String typeLabel(TransactionType type) {
        return type == TransactionType.INCOME ? "Pemasukan" : "Pengeluaran";
    }

    public String roleLabel(Role role) {
        return role == Role.ADMIN ? "Admin" : "Pengguna";
    }

    public String formatCreatedAt(Transaction transaction) {
        return transaction.getCreatedAt().format(DATE_TIME_FORMAT);
    }

    /** Baris ringkas: {@code [1] Gaji | Rp 1,000,000.00 | [Pemasukan]}. */
    public String format(Transaction transaction) {
        return String.format("[%d] %s | %s | [%s]",
                transaction.getId(),
                transaction.getDescription(),
                formatMoney(transaction.getAmount()),
                typeLabel(transaction.getType()));
    }

    /** Baris saldo: {@code Saldo: Rp 4,975,000.00}. */
    public String formatBalance(long balance) {
        return "Saldo: " + formatMoney(balance);
    }

    public String formatBalance(BalanceSummary summary) {
        return formatBalance(summary.balance());
    }

    public String showAddSuccess(Transaction transaction) {
        return typeLabel(transaction.getType()) + " dicatat: " + format(transaction);
    }

    public String showUpdateSuccess(Transaction transaction) {
        return "Transaksi diperbarui: " + format(transaction);
    }

    public String showDeleteSuccess(Transaction transaction) {
        return "Transaksi dihapus: " + format(transaction);
    }

    public String showValidationError(String message) {
        return "Periksa kembali isian: " + message;
    }

    public String showNotFoundError(String message) {
        return "Data tidak ditemukan: " + message;
    }
}
