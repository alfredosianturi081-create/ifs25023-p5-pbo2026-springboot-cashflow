package org.delcom.ifs25023springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;

/**
 * Modal pratinjau gambar. Klik gambar untuk beralih zoom 1.0x ↔ 1.8x; tombol Perkecil,
 * Perbesar, dan Tutup ada di footer sisi kanan. Gambar besar bisa digulir di dalam bingkai.
 */
public class ImagePreviewDialog extends Dialog {

    static final double DEFAULT_ZOOM = 1.0;
    static final double CLICK_ZOOM = 1.8;
    static final double MIN_ZOOM = 0.5;
    static final double MAX_ZOOM = 3.0;
    static final double STEP = 0.4;

    private final Image image;
    private final Span zoomLabel = new Span();
    private double zoom = DEFAULT_ZOOM;

    public ImagePreviewDialog(String imageUrl, String title) {
        image = new Image(imageUrl, title);
        image.addClassName("preview-image");
        image.addClickListener(event -> setZoom(zoom > DEFAULT_ZOOM ? DEFAULT_ZOOM : CLICK_ZOOM));

        Div frame = new Div(image);
        frame.addClassName("preview-frame");

        Button zoomOut = new Button("Perkecil", VaadinIcon.SEARCH_MINUS.create(),
                event -> setZoom(Math.max(MIN_ZOOM, zoom - STEP)));
        Button zoomIn = new Button("Perbesar", VaadinIcon.SEARCH_PLUS.create(),
                event -> setZoom(Math.min(MAX_ZOOM, zoom + STEP)));
        Button close = new Button("Tutup", VaadinIcon.CLOSE.create(), event -> close());

        zoomLabel.addClassName("preview-zoom-label");
        Div body = new Div(frame, zoomLabel);

        DialogSupport.compose(this, title, "Pratinjau", body, zoomOut, zoomIn, close);
        setCloseOnOutsideClick(true);
        setZoom(DEFAULT_ZOOM);
    }

    /** Membuka pratinjau untuk URL gambar. */
    public static void show(String imageUrl, String title) {
        new ImagePreviewDialog(imageUrl, title).open();
    }

    double getZoom() {
        return zoom;
    }

    private void setZoom(double newZoom) {
        zoom = newZoom;
        image.getStyle().set("width", Math.round(zoom * 100) + "%");
        image.getStyle().set("cursor", zoom > DEFAULT_ZOOM ? "zoom-out" : "zoom-in");
        zoomLabel.setText("Zoom " + String.format(java.util.Locale.US, "%.1f", zoom) + "x — klik gambar untuk memperbesar atau mengembalikan");
    }
}
