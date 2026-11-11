package org.delcom.ifs25023springbootcashflow;

import org.delcom.ifs25023springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25023springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25023springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25023springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25023springbootcashflow.usecase.AuthUseCase;
import org.delcom.ifs25023springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25023springbootcashflow.usecase.UserUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

/** Smoke test: seluruh konteks Spring naik memakai H2 (profil "test") dan admin awal tersemai. */
@SpringBootTest
@ActiveProfiles("test")
class AppTest {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private ITransactionRepository transactionRepository;

    @Autowired
    private AuthUseCase authUseCase;

    @Autowired
    private FinanceUseCase financeUseCase;

    @Autowired
    private UserUseCase userUseCase;

    @Autowired
    private FinancePresenter presenter;

    @Autowired
    private FileStorage fileStorage;

    @Test
    void contextLoadsAndWiresCleanArchitectureBeans() {
        assertNotNull(userRepository);
        assertNotNull(transactionRepository);
        assertNotNull(authUseCase);
        assertNotNull(financeUseCase);
        assertNotNull(userUseCase);
        assertNotNull(presenter);
        assertNotNull(fileStorage);
    }

    @Test
    void rootAdminIsSeededAndCanLogin() throws Exception {
        var admin = authUseCase.login("admin", "admin");
        assertTrue(admin.isAdmin());
        assertEquals(0, financeUseCase.getBalance(admin.getId()));
    }

    @Test
    void mainDelegatesToSpringApplicationRun() throws Exception {
        ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);
        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
            mocked.when(() -> SpringApplication.run(eq(App.class), any(String[].class))).thenReturn(context);

            App.main(new String[]{"--server.port=0"});

            mocked.verify(() -> SpringApplication.run(eq(App.class), any(String[].class)));
        }
    }
}
