package org.delcom.ifs25023springbootcashflow.config;

import org.delcom.ifs25023springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25023springbootcashflow.adapter.repository.JdbcTransactionRepository;
import org.delcom.ifs25023springbootcashflow.adapter.repository.JdbcUserRepository;
import org.delcom.ifs25023springbootcashflow.adapter.repository.SchemaInitializer;
import org.delcom.ifs25023springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25023springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25023springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25023springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25023springbootcashflow.usecase.AuthUseCase;
import org.delcom.ifs25023springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25023springbootcashflow.usecase.UserUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;
import java.io.IOException;

/**
 * Composition root Spring: satu-satunya tempat dependency antar-layer dirakit.
 * View hanya menerima use case/presenter lewat constructor, tidak pernah memanggil {@code new}.
 */
@Configuration
public class AppConfig {

    @Bean
    public IUserRepository userRepository(DataSource dataSource) {
        SchemaInitializer.initialize(dataSource);
        return new JdbcUserRepository(dataSource);
    }

    @Bean
    public ITransactionRepository transactionRepository(DataSource dataSource) {
        // Idempoten: memastikan tabel ada walau bean ini dibuat lebih dulu dari userRepository.
        SchemaInitializer.initialize(dataSource);
        return new JdbcTransactionRepository(dataSource);
    }

    @Bean
    public AuthUseCase authUseCase(IUserRepository userRepository) {
        return new AuthUseCase(userRepository);
    }

    @Bean
    public FinanceUseCase financeUseCase(ITransactionRepository transactionRepository) {
        return new FinanceUseCase(transactionRepository);
    }

    @Bean
    public UserUseCase userUseCase(IUserRepository userRepository) {
        return new UserUseCase(userRepository);
    }

    @Bean
    public FinancePresenter financePresenter() {
        return new FinancePresenter();
    }

    @Bean
    public FileStorage fileStorage(@Value("${app.upload-dir}") String uploadDir) {
        FileStorage storage = new FileStorage(uploadDir);
        try {
            storage.init();
        } catch (IOException e) {
            throw new IllegalStateException("Folder upload tidak dapat dibuat: " + storage.getRoot(), e);
        }
        return storage;
    }

    /** Membuat akun admin dari ROOT_USERNAME / ROOT_PASSWORD bila belum ada. */
    @Bean
    public ApplicationRunner seedRootAdmin(
            AuthUseCase authUseCase,
            @Value("${app.root-username}") String rootUsername,
            @Value("${app.root-password}") String rootPassword
    ) {
        return args -> {
            try {
                authUseCase.ensureRootAdmin(rootUsername, rootPassword);
            } catch (ValidationException e) {
                throw new IllegalStateException("Konfigurasi admin tidak valid: " + e.getMessage(), e);
            }
        };
    }

    /** Melayani foto profil dan bukti transaksi dari folder upload di path {@code /uploads/**}. */
    @Bean
    public WebMvcConfigurer uploadsResourceConfigurer(FileStorage fileStorage) {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                String location = fileStorage.getRoot().toUri().toString();
                if (!location.endsWith("/")) {
                    location += "/";
                }
                registry.addResourceHandler(FileStorage.URL_PREFIX + "**").addResourceLocations(location);
            }
        };
    }
}
