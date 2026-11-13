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
import com.vaadin.flow.component.html.Paragraph;
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

/** Halaman masuk. ID elemen login-username, login-password, dan login-submit dipakai oleh uji otomatis. */
@Route("login")
@PageTitle("Masuk")
public class LoginView extends Div implements BeforeEnterObserver {

    private final AuthUseCase authUseCase;
    private final IUserRepository userRepository;

    private final TextField username = new TextField("Username");
    private final PasswordField password = new PasswordField("Password");
    private final ProgressBar progress = DialogSupport.hiddenProgress();
    private final Button submit = new Button("Masuk");

    public LoginView(AuthUseCase authUseCase, IUserRepository userRepository) {
        this.authUseCase = authUseCase;
        this.userRepository = userRepository;

        addClassName("auth-page");
        add(buildShowcase(), buildForm());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.currentUser(userRepository).isPresent()) {
            event.forwardTo(FinanceView.class);
        }
    }

    private Div buildShowcase() {
        Span brand = new Span("Cashflow");
        brand.addClassName("showcase-brand");
        Paragraph headline = new Paragraph("Tahu persis ke mana uang pergi.");
        headline.addClassName("showcase-headline");
        Paragraph body = new Paragraph(
                "Catat pemasukan dan pengeluaran, lampirkan bukti, dan lihat saldo berubah seketika.");
        body.addClassName("showcase-body");

        Div ledger = new Div(
                ledgerRow("Gaji bulanan", "+ Rp 5,000,000.00", true),
                ledgerRow("Makan siang", "- Rp 25,000.00", false),
                ledgerRow("Token listrik", "- Rp 200,000.00", false));
        ledger.addClassName("showcase-ledger");

        Div showcase = new Div(brand, headline, body, ledger);
        showcase.addClassName("auth-showcase");
        return showcase;
    }

    private Div ledgerRow(String label, String amount, boolean income) {
        Span left = new Span(label);
        Span right = new Span(amount);
        right.addClassName(income ? "ledger-income" : "ledger-expense");
        Div row = new Div(left, right);
        row.addClassName("ledger-row");
        return row;
    }

    private Div buildForm() {
        H2 title = new H2("Masuk ke akun Anda");
        title.addClassName("auth-title");

        username.setId("login-username");
        username.setWidthFull();
        username.setAutofocus(true);
        password.setId("login-password");
        password.setWidthFull();

        submit.setId("login-submit");
        submit.setWidthFull();
        submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submit.addClickShortcut(Key.ENTER);
        submit.addClickListener(event -> attemptLogin());

        Span noAccount = new Span("Belum punya akun? ");
        Div footer = new Div(noAccount, new RouterLink("Daftar sekarang", RegisterView.class));
        footer.addClassName("auth-footer");

        VerticalLayout form = new VerticalLayout(title, progress, username, password, submit, footer);
        form.setPadding(false);
        form.addClassName("auth-form");

        Div panel = new Div(form);
        panel.addClassName("auth-panel");
        return panel;
    }

    private void attemptLogin() {
        progress.setVisible(true);
        submit.setEnabled(false);
        try {
            User user = authUseCase.login(username.getValue(), password.getValue());
            AuthSession.login(user);
            UI.getCurrent().navigate(FinanceView.class);
        } catch (ValidationException ex) {
            password.clear();
            Notices.error(ex.getMessage());
        } finally {
            progress.setVisible(false);
            submit.setEnabled(true);
        }
    }
}
