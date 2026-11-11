package org.delcom.ifs25023springbootcashflow.domain.entity;

/** Jenis transaksi keuangan. */
public enum TransactionType {
    /** Uang masuk (menambah saldo). */
    INCOME,
    /** Uang keluar (mengurangi saldo). */
    EXPENSE
}
