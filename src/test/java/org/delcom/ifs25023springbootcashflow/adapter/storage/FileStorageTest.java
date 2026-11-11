package org.delcom.ifs25023springbootcashflow.adapter.storage;

import org.delcom.ifs25023springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileStorageTest {

    @Test
    void initCreatesFolderAndStoreWritesUuidNamedFile(@TempDir Path temp) throws Exception {
        FileStorage storage = new FileStorage(temp.resolve("uploads").toString());
        storage.init();
        assertTrue(Files.isDirectory(storage.getRoot()));

        String stored = storage.store("Foto Saya.PNG", new byte[]{1, 2, 3});
        assertTrue(stored.endsWith(".png"));
        assertFalse(stored.contains("Foto"));
        assertTrue(Files.exists(storage.getRoot().resolve(stored)));
    }

    @Test
    void storeCreatesMissingFolderOnDemand(@TempDir Path temp) throws Exception {
        FileStorage storage = new FileStorage(temp.resolve("belum-ada").toString());
        String stored = storage.store("a.jpg", new byte[]{9});
        assertTrue(Files.exists(storage.getRoot().resolve(stored)));
    }

    @Test
    void storeRejectsBadInput(@TempDir Path temp) {
        FileStorage storage = new FileStorage(temp.toString());
        assertThrows(ValidationException.class, () -> storage.store("a.png", null));
        assertThrows(ValidationException.class, () -> storage.store("a.png", new byte[0]));
        assertThrows(ValidationException.class,
                () -> storage.store("a.png", new byte[(int) FileStorage.MAX_BYTES + 1]));
        assertThrows(ValidationException.class, () -> storage.store("virus.exe", new byte[]{1}));
        assertThrows(ValidationException.class, () -> storage.store("tanpaekstensi", new byte[]{1}));
        assertThrows(ValidationException.class, () -> storage.store(null, new byte[]{1}));
    }

    @Test
    void deleteHandlesAllCases(@TempDir Path temp) throws Exception {
        FileStorage storage = new FileStorage(temp.toString());
        String stored = storage.store("a.gif", new byte[]{1});

        assertFalse(storage.delete(null));
        assertFalse(storage.delete("  "));
        assertFalse(storage.delete("../../etc/passwd"));
        assertFalse(storage.delete("tidak-ada.png"));
        assertTrue(storage.delete(stored));
        assertFalse(Files.exists(storage.getRoot().resolve(stored)));

        Path folder = Files.createDirectory(temp.resolve("isi"));
        Files.writeString(folder.resolve("x.txt"), "x");
        assertFalse(storage.delete("isi"));
    }

    @Test
    void publicUrlPrefixesPath() {
        assertEquals("/uploads/abc.png", FileStorage.publicUrl("abc.png"));
        assertNull(FileStorage.publicUrl(null));
        assertNull(FileStorage.publicUrl(" "));
    }
}
