package org.delcom.ifs25023springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;

/**
 * Menyusun dialog modal dengan pola seragam: header biru primer (#2563EB) berteks putih + lencana,
 * isi, dan footer abu-abu terang (#F3F4F6) dengan semua tombol aksi di sisi kanan.
 * Warna dipasang lewat styles.css pada kelas {@code app-dialog}.
 */
public final class DialogSupport {

    private DialogSupport() {
    }

    /** ProgressBar indeterminat yang tersembunyi sampai aksi submit berjalan. */
    public static ProgressBar hiddenProgress() {
        ProgressBar bar = new ProgressBar();
        bar.setIndeterminate(true);
        bar.setVisible(false);
        bar.setWidthFull();
        return bar;
    }

    /** Menata {@code dialog}: header (judul + lencana), isi, dan tombol-tombol footer rata kanan. */
    public static void compose(Dialog dialog, String title, String badge, Component body, Button... actions) {
        dialog.addClassName("app-dialog");
        dialog.setCloseOnOutsideClick(false);

        Span titleLabel = new Span(title);
        titleLabel.addClassName("app-dialog-title");
        Span badgeLabel = new Span(badge);
        badgeLabel.addClassName("app-dialog-badge");
        HorizontalLayout header = new HorizontalLayout(titleLabel, badgeLabel);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setSpacing(true);
        dialog.getHeader().add(header);

        Div content = new Div(body);
        content.addClassName("app-dialog-body");
        dialog.add(content);

        HorizontalLayout footer = new HorizontalLayout(actions);
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        footer.addClassName("app-dialog-footer");
        dialog.getFooter().add(footer);
    }
}
