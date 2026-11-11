package org.delcom.ifs25023springbootcashflow.domain.exception;

/** Dilempar saat input atau aturan bisnis tidak terpenuhi. */
public class ValidationException extends DomainException {
    public ValidationException(String message) {
        super(message);
    }
}
