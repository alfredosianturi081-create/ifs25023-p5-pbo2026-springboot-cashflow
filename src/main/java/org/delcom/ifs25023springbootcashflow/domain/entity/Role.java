package org.delcom.ifs25023springbootcashflow.domain.entity;

/** Peran pengguna di aplikasi. */
public enum Role {
    /** Dapat melihat daftar pengguna dan transaksi pengguna lain (read-only). */
    ADMIN,
    /** Hanya dapat mengelola data miliknya sendiri. */
    USER
}
