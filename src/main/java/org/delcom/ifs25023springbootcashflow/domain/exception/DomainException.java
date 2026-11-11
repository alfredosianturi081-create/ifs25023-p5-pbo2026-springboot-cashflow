package org.delcom.ifs25023springbootcashflow.domain.exception;

/** Checked exception dasar untuk seluruh kegagalan aturan domain aplikasi Cashflow. */
public class DomainException extends Exception {
    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
