package org.delcom.ifs25023springbootcashflow.adapter.storage;

import org.delcom.ifs25023springbootcashflow.domain.exception.ValidationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Menyimpan berkas unggahan (foto profil dan bukti transaksi) ke folder UPLOAD_DIR.
 * Nama berkas diganti UUID sehingga aman dari tabrakan nama dan path traversal.
 * Path yang dikembalikan relatif terhadap folder upload; URL publiknya {@code /uploads/<path>}.
 */
public class FileStorage {

    public static final long MAX_BYTES = 5L * 1024 * 1024;
    public static final String URL_PREFIX = "/uploads/";

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "gif", "webp");

    private final Path root;

    public FileStorage(String uploadDir) {
        this.root = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    /** Folder upload absolut. */
    public Path getRoot() {
        return root;
    }

    /** Membuat folder upload bila belum ada (dipanggil sekali saat aplikasi start). */
    public void init() throws IOException {
        Files.createDirectories(root);
    }

    /** Menyimpan data gambar dan mengembalikan path relatif (UUID + ekstensi). */
    public String store(String originalFileName, byte[] data) throws ValidationException, IOException {
        if (data == null || data.length == 0) {
            throw new ValidationException("Berkas kosong");
        }
        if (data.length > MAX_BYTES) {
            throw new ValidationException("Ukuran gambar maksimal 5 MB");
        }
        String extension = extensionOf(originalFileName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ValidationException("Format gambar harus PNG, JPG, GIF, atau WEBP");
        }
        Files.createDirectories(root);
        String storedName = UUID.randomUUID() + "." + extension;
        Files.write(root.resolve(storedName), data);
        return storedName;
    }

    /** Menghapus berkas; mengembalikan true bila benar-benar terhapus. Path di luar root diabaikan. */
    public boolean delete(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        Path target = root.resolve(path).normalize();
        if (!target.startsWith(root)) {
            return false;
        }
        try {
            return Files.deleteIfExists(target);
        } catch (IOException e) {
            return false;
        }
    }

    /** URL publik untuk path relatif; {@code null} bila path kosong. */
    public static String publicUrl(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        return URL_PREFIX + path;
    }

    private static String extensionOf(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
