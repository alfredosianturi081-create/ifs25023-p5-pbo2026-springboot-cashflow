package org.delcom.ifs25023springbootcashflow.usecase;

import org.delcom.ifs25023springbootcashflow.domain.entity.BalanceSummary;
import org.delcom.ifs25023springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25023springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25023springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25023springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25023springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25023springbootcashflow.domain.repository.ITransactionRepository;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Use case pengelolaan transaksi keuangan. Semua operasi dibatasi pada satu pengguna (userId):
 * transaksi milik orang lain diperlakukan seolah tidak ada.
 */
public class FinanceUseCase {

    /** Batas nominal agar penjumlahan saldo tidak pernah overflow. */
    public static final long MAX_AMOUNT = 999_999_999_999L;
    public static final int MAX_DESCRIPTION_LENGTH = 255;

    private static final Pattern PLAIN_NUMBER = Pattern.compile("\\d{1,15}");
    private static final Pattern GROUPED_NUMBER = Pattern.compile("\\d{1,3}([.,]\\d{3})+");

    private final ITransactionRepository transactionRepository;

    public FinanceUseCase(ITransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /** Mengubah teks input jumlah ("1500000", "1.500.000", "Rp 1,500,000") menjadi angka bulat positif. */
    public long parseAmount(String raw) throws ValidationException {
        String text = raw == null ? "" : raw.trim().replaceAll("(?i)^rp\\.?\\s*", "").replace(" ", "");
        long value;
        if (PLAIN_NUMBER.matcher(text).matches()) {
            value = Long.parseLong(text);
        } else if (GROUPED_NUMBER.matcher(text).matches()) {
            value = Long.parseLong(text.replace(".", "").replace(",", ""));
        } else {
            throw new ValidationException("Jumlah harus berupa angka bulat, contoh: 150000 atau 150.000");
        }
        return requirePositive(value);
    }

    /** Mencatat pemasukan baru. */
    public Transaction addIncome(int userId, String description, long amount, String proofPath)
            throws ValidationException {
        return add(userId, description, amount, TransactionType.INCOME, proofPath);
    }

    /** Mencatat pengeluaran baru. */
    public Transaction addExpense(int userId, String description, long amount, String proofPath)
            throws ValidationException {
        return add(userId, description, amount, TransactionType.EXPENSE, proofPath);
    }

    /** Semua transaksi pengguna, terbaru di atas. */
    public List<Transaction> getTransactions(int userId) {
        return list(userId, null, null);
    }

    /**
     * Daftar transaksi pengguna, difilter berdasarkan kata kunci keterangan (case-insensitive;
     * {@code null}/kosong berarti semua) dan diurutkan ({@code null} berarti terbaru di atas).
     */
    public List<Transaction> list(int userId, String keyword, SortOption option) {
        String needle = keyword == null ? "" : keyword.trim().toLowerCase();
        SortOption sort = option == null ? SortOption.NEWEST : option;
        return transactionRepository.findBy(t -> t.getUserId() == userId
                        && t.getDescription().toLowerCase().contains(needle))
                .stream()
                .sorted(sort.comparator())
                .toList();
    }

    /** Mengambil satu transaksi milik pengguna. */
    public Transaction getTransaction(int userId, int id) throws ValidationException, EntityNotFoundException {
        return findOwned(userId, id);
    }

    /** Menghapus transaksi milik pengguna; mengembalikan transaksi yang dihapus (untuk membersihkan bukti). */
    public Transaction removeTransaction(int userId, int id) throws ValidationException, EntityNotFoundException {
        Transaction owned = findOwned(userId, id);
        transactionRepository.deleteById(owned.getId());
        return owned;
    }

    /**
     * Mengubah transaksi. Parameter {@code null} berarti tidak diubah; untuk {@code proofPath},
     * string kosong berarti bukti dihapus.
     */
    public Transaction updateTransaction(int userId, int id, String description, Long amount,
                                         TransactionType type, String proofPath)
            throws ValidationException, EntityNotFoundException {
        Transaction transaction = findOwned(userId, id);
        if (description != null) {
            transaction.changeDescription(requireDescription(description));
        }
        if (amount != null) {
            transaction.changeAmount(requirePositive(amount));
        }
        if (type != null) {
            transaction.changeType(type);
        }
        if (proofPath != null) {
            transaction.changeProofPath(proofPath.isBlank() ? null : proofPath);
        }
        transactionRepository.update(transaction);
        return transaction;
    }

    /** Ringkasan jumlah transaksi, total pemasukan, dan total pengeluaran pengguna. */
    public BalanceSummary summarize(int userId) {
        List<Transaction> all = transactionRepository.findByUserId(userId);
        long income = all.stream().filter(Transaction::isIncome).mapToLong(Transaction::getAmount).sum();
        long expense = all.stream().filter(t -> !t.isIncome()).mapToLong(Transaction::getAmount).sum();
        return new BalanceSummary(all.size(), income, expense);
    }

    /** Saldo pengguna: pemasukan dikurangi pengeluaran. */
    public long getBalance(int userId) {
        return summarize(userId).balance();
    }

    private Transaction add(int userId, String description, long amount, TransactionType type, String proofPath)
            throws ValidationException {
        String cleanDescription = requireDescription(description);
        requirePositive(amount);
        String cleanProof = proofPath == null || proofPath.isBlank() ? null : proofPath;
        return transactionRepository.save(userId, cleanDescription, amount, type, cleanProof);
    }

    private Transaction findOwned(int userId, int id) throws ValidationException, EntityNotFoundException {
        if (id <= 0) {
            throw new ValidationException("ID transaksi tidak valid");
        }
        return transactionRepository.findById(id)
                .filter(t -> t.getUserId() == userId)
                .orElseThrow(() -> new EntityNotFoundException("Transaksi", id));
    }

    private static String requireDescription(String description) throws ValidationException {
        if (description == null || description.isBlank()) {
            throw new ValidationException("Keterangan tidak boleh kosong");
        }
        String clean = description.trim();
        if (clean.length() > MAX_DESCRIPTION_LENGTH) {
            throw new ValidationException("Keterangan maksimal " + MAX_DESCRIPTION_LENGTH + " karakter");
        }
        return clean;
    }

    private static long requirePositive(long amount) throws ValidationException {
        if (amount <= 0) {
            throw new ValidationException("Jumlah harus lebih besar dari nol");
        }
        if (amount > MAX_AMOUNT) {
            throw new ValidationException("Jumlah terlalu besar (maksimal 999.999.999.999)");
        }
        return amount;
    }
}
