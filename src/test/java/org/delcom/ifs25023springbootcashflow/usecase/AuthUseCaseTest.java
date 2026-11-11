package org.delcom.ifs25023springbootcashflow.usecase;

import org.delcom.ifs25023springbootcashflow.adapter.repository.UserRepository;
import org.delcom.ifs25023springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25023springbootcashflow.domain.entity.Role;
import org.delcom.ifs25023springbootcashflow.domain.entity.User;
import org.delcom.ifs25023springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthUseCaseTest {

    private UserRepository repository;
    private AuthUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new UserRepository();
        useCase = new AuthUseCase(repository);
    }

    @Test
    void registerCreatesUserWithHashedPassword() throws Exception {
        User user = useCase.register("  budi  ", "rahasia1", " Budi Santoso ");
        assertEquals("budi", user.getUsername());
        assertEquals("Budi Santoso", user.getFullName());
        assertEquals(Role.USER, user.getRole());
        assertNotEquals("rahasia1", user.getPasswordHash());
        assertTrue(PasswordHasher.verify("rahasia1", user.getPasswordHash()));
    }

    @Test
    void registerRejectsInvalidInput() throws Exception {
        assertThrows(ValidationException.class, () -> useCase.register(null, "rahasia1", "A"));
        assertThrows(ValidationException.class, () -> useCase.register("ab", "rahasia1", "A"));
        assertThrows(ValidationException.class, () -> useCase.register("bad name", "rahasia1", "A"));
        assertThrows(ValidationException.class, () -> useCase.register("budi", "rahasia1", " "));
        assertThrows(ValidationException.class, () -> useCase.register("budi", "rahasia1", null));
        assertThrows(ValidationException.class, () -> useCase.register("budi", "123", "A"));
        assertThrows(ValidationException.class, () -> useCase.register("budi", null, "A"));
        useCase.register("budi", "rahasia1", "Budi");
        assertThrows(ValidationException.class, () -> useCase.register("BUDI", "rahasia1", "Lain"));
    }

    @Test
    void loginSucceedsOrFailsWithUniformMessage() throws Exception {
        useCase.register("budi", "rahasia1", "Budi");
        assertEquals("budi", useCase.login(" budi ", "rahasia1").getUsername());

        ValidationException wrongPassword =
                assertThrows(ValidationException.class, () -> useCase.login("budi", "salah"));
        ValidationException unknownUser =
                assertThrows(ValidationException.class, () -> useCase.login("hantu", "rahasia1"));
        assertEquals(wrongPassword.getMessage(), unknownUser.getMessage());

        assertThrows(ValidationException.class, () -> useCase.login(null, "x"));
        assertThrows(ValidationException.class, () -> useCase.login(" ", "x"));
        assertThrows(ValidationException.class, () -> useCase.login("budi", null));
        assertThrows(ValidationException.class, () -> useCase.login("budi", ""));
    }

    @Test
    void ensureRootAdminCreatesOnceAndValidates() throws Exception {
        User admin = useCase.ensureRootAdmin("admin", "admin");
        assertTrue(admin.isAdmin());
        assertEquals("Administrator", admin.getFullName());
        assertEquals(admin.getId(), useCase.ensureRootAdmin("admin", "lain").getId());
        assertEquals(1, repository.findAll().size());

        assertThrows(ValidationException.class, () -> useCase.ensureRootAdmin(null, "x"));
        assertThrows(ValidationException.class, () -> useCase.ensureRootAdmin(" ", "x"));
        assertThrows(ValidationException.class, () -> useCase.ensureRootAdmin("a", null));
        assertThrows(ValidationException.class, () -> useCase.ensureRootAdmin("a", ""));
    }
}
