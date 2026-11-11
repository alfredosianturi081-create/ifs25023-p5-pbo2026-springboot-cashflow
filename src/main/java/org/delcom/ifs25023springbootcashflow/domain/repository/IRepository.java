package org.delcom.ifs25023springbootcashflow.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Port generik CRUD + pencarian.
 *
 * @param <T>  tipe entitas domain
 * @param <ID> tipe identifier entitas
 */
public interface IRepository<T, ID> {
    /** Mengambil semua entitas. */
    List<T> findAll();

    /** Mencari satu entitas berdasarkan ID. */
    Optional<T> findById(ID id);

    /** Menyimpan entitas (baru). */
    T save(T entity);

    /** Menghapus berdasarkan ID; true bila ada yang terhapus. */
    boolean deleteById(ID id);

    /** Menyimpan perubahan entitas yang sudah ada. */
    void update(T entity);

    /** Mencari entitas yang memenuhi predikat. */
    List<T> findBy(Predicate<T> predicate);
}
