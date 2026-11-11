package org.delcom.ifs25023springbootcashflow.usecase;

import org.delcom.ifs25023springbootcashflow.adapter.repository.UserRepository;
import org.delcom.ifs25023springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25023springbootcashflow.domain.entity.Role;
import org.delcom.ifs25023springbootcashflow.domain.entity.User;
import org.delcom.ifs25023springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25023springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserUseCaseTest {

    private UserRepository repository;
    private UserUseCase useCase;
    private User budi;

    @BeforeEach
    void setUp() {
        repository = new UserRepository();
        useCase = new UserUseCase(repository);
        budi = repository.save("budi", PasswordHasher.hash("rahasia1"), "Budi Santoso", Role.USER);
        repository.save("sari", PasswordHasher.hash("rahasia2"), "Sari Dewi", Role.USER);
    }

    @Test
    void listAndSearchUsers() {
        assertEquals(2, useCase.listUsers().size());
        assertEquals(1, useCase.searchUsers("SAR").size());
        assertEquals(1, useCase.searchUsers("santoso").size());
        assertEquals(2, useCase.searchUsers(null).size());
        assertTrue(useCase.searchUsers("zzz").isEmpty());
    }

    @Test
    void getUserOrThrow() throws Exception {
        assertEquals("budi", useCase.getUser(budi.getId()).getUsername());
        assertThrows(EntityNotFoundException.class, () -> useCase.getUser(99));
    }

    @Test
    void updateProfileChangesNameAndOptionalPassword() throws Exception {
        useCase.updateProfile(budi.getId(), " Budi S. ", null);
        assertEquals("Budi S.", budi.getFullName());
        assertTrue(PasswordHasher.verify("rahasia1", budi.getPasswordHash()));

        useCase.updateProfile(budi.getId(), "Budi S.", "");
        assertTrue(PasswordHasher.verify("rahasia1", budi.getPasswordHash()));

        useCase.updateProfile(budi.getId(), "Budi S.", "barupass1");
        assertTrue(PasswordHasher.verify("barupass1", budi.getPasswordHash()));
    }

    @Test
    void updateProfileValidates() {
        assertThrows(ValidationException.class, () -> useCase.updateProfile(budi.getId(), " ", null));
        assertThrows(ValidationException.class, () -> useCase.updateProfile(budi.getId(), null, null));
        assertThrows(ValidationException.class, () -> useCase.updateProfile(budi.getId(), "Budi", "123"));
        assertThrows(EntityNotFoundException.class, () -> useCase.updateProfile(99, "Budi", null));
    }

    @Test
    void updatePhotoStoresAndClearsPath() throws Exception {
        useCase.updatePhoto(budi.getId(), "foto.png");
        assertEquals("foto.png", budi.getPhotoPath());
        useCase.updatePhoto(budi.getId(), null);
        assertEquals(null, budi.getPhotoPath());
        assertThrows(EntityNotFoundException.class, () -> useCase.updatePhoto(99, "x.png"));
    }
}
