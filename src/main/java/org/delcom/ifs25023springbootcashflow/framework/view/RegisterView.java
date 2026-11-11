package org.delcom.ifs25023springbootcashflow.framework.view;

import org.delcom.ifs25023springbootcashflow.domain.entity.User;
import org.delcom.ifs25023springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25023springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25023springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25023springbootcashflow.usecase.AuthUseCase;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

/** Halaman pendaftaran akun baru (role USER). Setelah berhasil, pengguna langsung masuk. */
@Route("register")
@PageTitle("Daftar")
public class RegisterView extends Div implements BeforeEnterObserver {

    private final AuthUseCase authUseCase;
    private final IUserRepository userRepository;

    private final TextField fullName = new TextField("Nama lengkap");
    private final TextField username = new TextField("Username");
    private final PasswordField password = new PasswordField("Password");
    private final PasswordField confirm = new PasswordField("Ulangi password");
    private final ProgressBar progress = DialogSupport.hiddenProgress();
    private final Button submit = new Button("Buat akun");

    public RegisterView(AuthUseCase authUseCase, IUserRepository userRepository) {
        this.authUseCase = authUseCase;
        this.userRepository = userRepository;

        addClassNames("auth-page", "auth-page-single");

        H2 title = new H2("Buat akun baru");
        title.addClassName("auth-title");
        fullName.setWidthFull();
        username.setWidthFull();
        password.setWidthFull();
        confirm.setWidthFull();
        username.setHelperText("Huruf, angka, titik, garis bawah, atau strip (3-50 karakter)");
        password.setHelperText("Minimal " + AuthUseCase.MIN_PASSWORD_LENGTH + " karakter");

        submit.setWidthFull();
        submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submit.addClickShortcut(Key.ENTER);
        submit.addClickListener(event -> attemptRegister());

        Div footer = new Div(new Span("Sudah punya akun? "), new RouterLink("Masuk", LoginView.class));
        footer.addClassName("auth-footer");

        VerticalLayout form = new VerticalLayout(title, progress, fullName, username, password, confirm, submit, footer);
        form.setPadding(false);
        form.addClassName("auth-form");
        Div panel = new Div(form);
        panel.addClassName("auth-panel");
        add(panel);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.currentUser(userRepository).isPresent()) {
            event.forwardTo(FinanceView.class);
        }
    }

    private void attemptRegister() {
        if (!password.getValue().equals(confirm.getValue())) {
            Notices.error("Konfirmasi password tidak sama");
            return;
        }
        progress.setVisible(true);
        submit.setEnabled(false);
        try {
            User user = authUseCase.register(username.getValue(), password.getValue(), fullName.getValue());
            AuthSession.login(user);
            Notices.success("Akun dibuat. Selamat datang, " + user.getFullName() + "!");
            UI.getCurrent().navigate(FinanceView.class);
        } catch (ValidationException ex) {
            Notices.error(ex.getMessage());
        } finally {
            progress.setVisible(false);
            submit.setEnabled(true);
        }
    }
}
