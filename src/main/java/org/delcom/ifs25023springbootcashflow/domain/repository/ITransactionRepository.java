package org.delcom.ifs25023springbootcashflow.domain.repository;

import org.delcom.ifs25023springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25023springbootcashflow.domain.entity.TransactionType;

import java.util.List;

/** Port transaksi: menambah pembuatan transaksi baru dan pencarian per pengguna. */
public interface ITransactionRepository extends IRepository<Transaction, Integer> {

    /** Membuat transaksi baru (ID dan waktu dibuat diisi oleh repository). */
    Transaction save(int userId, String description, long amount, TransactionType type, String proofPath);

    /** Semua transaksi milik satu pengguna. */
    List<Transaction> findByUserId(int userId);
}
