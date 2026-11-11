package org.delcom.ifs25023springbootcashflow.framework.view;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

/** Notifikasi seragam: sukses di kanan bawah, galat tampil sedikit lebih lama. */
public final class Notices {

    private Notices() {
    }

    public static void success(String message) {
        show(message, 3000, NotificationVariant.LUMO_SUCCESS);
    }

    public static void error(String message) {
        show(message, 5000, NotificationVariant.LUMO_ERROR);
    }

    private static void show(String message, int durationMs, NotificationVariant variant) {
        Notification notification = Notification.show(message, durationMs, Notification.Position.BOTTOM_END);
        notification.addThemeVariants(variant);
    }
}
