package org.delcom.ifs25023springbootcashflow.framework.view;

import org.delcom.ifs25023springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25023springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25023springbootcashflow.domain.entity.User;
import org.delcom.ifs25023springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25023springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25023springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25023springbootcashflow.usecase.UserUseCase;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Transaksi milik pengguna lain, hanya-baca. Khusus ADMIN. Pengguna dipilih lewat kotak pilihan
 * atau lewat URL {@code /user-transactions/{id}} (mis. dari tombol di Daftar Pengguna).
 */
@Route(value = "user-transactions", layout = MainLayout.class)
@PageTitle("Transaksi Pengguna")
public class UserTransactionsView extends VerticalLayout
        implements BeforeEnterObserver, HasUrlParameter<Integer> {

    private final IUserRepository userRepository;
    private final UserUseCase userUseCase;
    private final TransactionPanel panel;
    private final ComboBox<User> picker = new ComboBox<>("Pilih pengguna");

    private Integer requestedUserId;

    public UserTransactionsView(IUserRepository userRepository, UserUseCase userUseCase,
                                FinanceUseCase financeUseCase, FinancePresenter presenter,
                                FileStorage fileStorage) {
        this.userRepository = userRepository;
        this.userUseCase = userUseCase;
        this.panel = new TransactionPanel(financeUseCase, presenter, fileStorage, true);

        setPadding(true);
        addClassName("page");

        picker.setWidth("360px");
        picker.setPlaceholder("Cari nama atau username...");
        picker.setItemLabelGenerator(user -> user.getFullName() + " (@" + user.getUsername() + ")");
        picker.addValueChangeListener(event -> {
            User selected = event.getValue();
            panel.setUserId(selected == null ? null : selected.getId());
            panel.refresh();
        });

        Paragraph note = new Paragraph("Tampilan hanya-baca: admin tidak dapat mengubah transaksi pengguna.");
        note.addClassName("page-hint");
        add(picker, note, panel);
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Integer parameter) {
        this.requestedUserId = parameter;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        var current = AuthSession.currentUser(userRepository);
        if (current.isEmpty()) {
            event.forwardTo(LoginView.class);
            return;
        }
        if (!current.get().isAdmin()) {
            event.forwardTo(FinanceView.class);
            return;
        }
        var users = userUseCase.listUsers();
        picker.setItems(users);
        users.stream()
                .filter(user -> requestedUserId != null && user.getId() == requestedUserId)
                .findFirst()
                .ifPresent(picker::setValue);
    }
}
