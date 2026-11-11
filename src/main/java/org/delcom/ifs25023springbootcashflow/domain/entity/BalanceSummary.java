package org.delcom.ifs25023springbootcashflow.domain.entity;

/**
 * Ringkasan keuangan satu pengguna.
 *
 * @param count        jumlah transaksi
 * @param totalIncome  total pemasukan
 * @param totalExpense total pengeluaran
 */
public record BalanceSummary(int count, long totalIncome, long totalExpense) {

    /** Saldo = pemasukan - pengeluaran (bisa negatif). */
    public long balance() {
        return totalIncome - totalExpense;
    }

    /** Porsi pemasukan terhadap seluruh arus uang, 0..100. Nol bila belum ada transaksi. */
    public int incomeSharePercent() {
        long flow = totalIncome + totalExpense;
        if (flow == 0) {
            return 0;
        }
        return (int) Math.round(totalIncome * 100.0 / flow);
    }
}
