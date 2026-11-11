package org.delcom.ifs25023springbootcashflow.domain.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExceptionTest {

    @Test
    void domainExceptionCarriesMessageAndCause() {
        Throwable cause = new RuntimeException("akar");
        DomainException ex = new DomainException("gagal", cause);
        assertEquals("gagal", ex.getMessage());
        assertSame(cause, ex.getCause());
        assertEquals("x", new DomainException("x").getMessage());
    }

    @Test
    void entityNotFoundFormatsMessageAndKeepsId() {
        EntityNotFoundException ex = new EntityNotFoundException("Transaksi", 9);
        assertEquals(9, ex.getId());
        assertEquals("Transaksi dengan ID 9 tidak ditemukan.", ex.getMessage());
        assertTrue(ex instanceof DomainException);
    }

    @Test
    void validationExceptionIsDomainException() {
        ValidationException ex = new ValidationException("salah");
        assertEquals("salah", ex.getMessage());
        assertTrue(ex instanceof DomainException);
    }
}
