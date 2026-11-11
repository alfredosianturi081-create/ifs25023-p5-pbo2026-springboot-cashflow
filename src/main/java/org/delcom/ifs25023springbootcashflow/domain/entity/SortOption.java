package org.delcom.ifs25023springbootcashflow.domain.entity;

import java.util.Comparator;

/**
 * Kriteria pengurutan transaksi. Setiap opsi membawa comparator-nya sendiri
 * sehingga logika pengurutan terpusat di domain.
 */
public enum SortOption {
    /** Nominal terkecil lebih dulu. */
    AMOUNT_ASC("Jumlah: terkecil", Comparator.comparingLong(Transaction::getAmount)),

    /** Nominal terbesar lebih dulu. */
    AMOUNT_DESC("Jumlah: terbesar", Comparator.comparingLong(Transaction::getAmount).reversed()),

    /** Pemasukan di atas, lalu nominal terbesar. */
    INCOME_FIRST("Pemasukan dulu",
            Comparator.comparing(Transaction::isIncome).reversed()
                    .thenComparing(Comparator.comparingLong(Transaction::getAmount).reversed())),

    /** Pengeluaran di atas, lalu nominal terbesar. */
    EXPENSE_FIRST("Pengeluaran dulu",
            Comparator.comparing(Transaction::isIncome)
                    .thenComparing(Comparator.comparingLong(Transaction::getAmount).reversed())),

    /** Transaksi terbaru lebih dulu. */
    NEWEST("Terbaru",
            Comparator.comparing(Transaction::getCreatedAt)
                    .thenComparingInt(Transaction::getId).reversed()),

    /** Transaksi terlama lebih dulu. */
    OLDEST("Terlama",
            Comparator.comparing(Transaction::getCreatedAt)
                    .thenComparingInt(Transaction::getId));

    private final String label;
    private final Comparator<Transaction> comparator;

    SortOption(String label, Comparator<Transaction> comparator) {
        this.label = label;
        this.comparator = comparator;
    }

    /** Label singkat untuk ditampilkan di UI. */
    public String label() {
        return label;
    }

    /** Comparator yang sesuai dengan opsi ini. */
    public Comparator<Transaction> comparator() {
        return comparator;
    }
}
