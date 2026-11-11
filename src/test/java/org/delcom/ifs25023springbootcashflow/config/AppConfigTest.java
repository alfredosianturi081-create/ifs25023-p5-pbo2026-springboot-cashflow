package org.delcom.ifs25023springbootcashflow.config;

import org.delcom.ifs25023springbootcashflow.adapter.repository.JdbcTransactionRepository;
import org.delcom.ifs25023springbootcashflow.adapter.repository.JdbcUserRepository;
import org.delcom.ifs25023springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25023springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25023springbootcashflow.usecase.AuthUseCase;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.ApplicationRunner;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppConfigTest {

    private final AppConfig config = new AppConfig();

    private DataSource h2() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        return ds;
    }

    @Test
    void repositoryAndUseCaseBeansAreWired() {
        DataSource ds = h2();
        IUserRepository users = config.userRepository(ds);
        assertInstanceOf(JdbcUserRepository.class, users);
        assertInstanceOf(JdbcTransactionRepository.class, config.transactionRepository(ds));
        assertNotNull(config.authUseCase(users));
        assertNotNull(config.userUseCase(users));
        assertNotNull(config.financeUseCase(config.transactionRepository(ds)));
        assertNotNull(config.financePresenter());
    }

    @Test
    void seedRootAdminCreatesAdminOnce() throws Exception {
        IUserRepository users = config.userRepository(h2());
        AuthUseCase auth = config.authUseCase(users);
        ApplicationRunner runner = config.seedRootAdmin(auth, "root", "rahasia");
        runner.run(null);
        runner.run(null);
        assertEquals(1, users.findAll().size());
        assertTrue(users.findByUsername("root").orElseThrow().isAdmin());
    }

    @Test
    void seedRootAdminFailsOnBlankConfiguration() {
        AuthUseCase auth = config.authUseCase(config.userRepository(h2()));
        ApplicationRunner runner = config.seedRootAdmin(auth, " ", "x");
        assertThrows(IllegalStateException.class, () -> runner.run(null));
    }

    @Test
    void fileStorageCreatesFolderOrFails(@TempDir Path temp) throws Exception {
        FileStorage storage = config.fileStorage(temp.resolve("up").toString());
        assertTrue(Files.isDirectory(storage.getRoot()));

        Path file = Files.createFile(temp.resolve("bukan-folder"));
        assertThrows(IllegalStateException.class, () -> config.fileStorage(file.resolve("sub").toString()));
    }

    @Test
    void uploadsAreServedFromUploadFolderWithTrailingSlash(@TempDir Path temp) throws Exception {
        FileStorage existing = config.fileStorage(temp.resolve("ada").toString());
        assertLocation(existing, existing.getRoot().toUri().toString());

        FileStorage missing = new FileStorage(temp.resolve("belum").toString());
        assertLocation(missing, missing.getRoot().toUri().toString() + "/");
    }

    private void assertLocation(FileStorage storage, String expectedLocation) {
        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
        ResourceHandlerRegistration registration = mock(ResourceHandlerRegistration.class);
        when(registry.addResourceHandler("/uploads/**")).thenReturn(registration);

        config.uploadsResourceConfigurer(storage).addResourceHandlers(registry);

        verify(registration).addResourceLocations(expectedLocation);
    }
}
