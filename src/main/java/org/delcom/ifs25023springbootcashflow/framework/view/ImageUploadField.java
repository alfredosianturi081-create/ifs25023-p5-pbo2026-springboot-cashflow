package org.delcom.ifs25023springbootcashflow.framework.view;

import org.delcom.ifs25023springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25023springbootcashflow.domain.exception.ValidationException;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.UploadHandler;

import java.io.IOException;

/**
 * Kolom unggah satu gambar (bukti transaksi atau foto profil). Berkas ditampung di memori
 * sampai {@link #store(FileStorage)} dipanggil saat form disimpan, sehingga membatalkan dialog
 * tidak meninggalkan berkas yatim di folder upload.
 */
public class ImageUploadField extends Div {

    private final Upload upload;
    private volatile String pendingName;
    private volatile byte[] pendingData;

    public ImageUploadField(String buttonLabel, String dropHint) {
        addClassName("image-upload-field");
        upload = new Upload(UploadHandler.inMemory((metadata, data) -> {
            // Dijalankan di thread request upload (tanpa kunci sesi UI): hanya menampung data.
            pendingName = metadata.fileName();
            pendingData = data;
        }));
        upload.setMaxFiles(1);
        upload.setMaxFileSize((int) FileStorage.MAX_BYTES);
        upload.setAcceptedFileTypes("image/png", "image/jpeg", "image/gif", "image/webp",
                ".png", ".jpg", ".jpeg", ".gif", ".webp");
        upload.setUploadButton(new Button(buttonLabel, VaadinIcon.UPLOAD.create()));
        upload.setDropLabel(new Span(dropHint));
        upload.addFileRemovedListener(event -> clearPending());
        upload.addFileRejectedListener(event -> Notices.error(event.getErrorMessage()));
        add(upload);
    }

    /** True bila ada berkas yang selesai diunggah dan belum disimpan. */
    public boolean hasFile() {
        return pendingData != null;
    }

    /** Menyimpan berkas tertampung lewat {@link FileStorage}; mengembalikan path relatif. */
    public String store(FileStorage storage) throws ValidationException, IOException {
        return storage.store(pendingName, pendingData);
    }

    /** Mengosongkan tampungan dan daftar berkas di komponen. */
    public void clear() {
        clearPending();
        upload.clearFileList();
    }

    private void clearPending() {
        pendingName = null;
        pendingData = null;
    }
}
