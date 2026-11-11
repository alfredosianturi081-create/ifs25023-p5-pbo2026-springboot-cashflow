package org.delcom.ifs25023springbootcashflow.framework.view;

import org.delcom.ifs25023springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25023springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25023springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25023springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25023springbootcashflow.usecase.FinanceUseCase;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

/** Halaman utama: catatan pemasukan dan pengeluaran milik pengguna yang sedang login. */
@Route(value = "", layout = MainLayout.class)
@PageTitle("Transaksi Saya")
public class FinanceView extends VerticalLayout {

    private final IUserRepository userRepository;
    private final TransactionPanel panel;
    private final H2 greeting = new H2();

    @Autowired
    public FinanceView(IUserRepository userRepository, FinanceUseCase financeUseCase,
                       FinancePresenter presenter, FileStorage fileStorage) {
        this.userRepository = userRepository;
        this.panel = new TransactionPanel(financeUseCase, presenter, fileStorage, false);

        setPadding(true);
        setSpacing(true);
        addClassName("page");

        greeting.addClassName("page-heading");
        Paragraph hint = new Paragraph("Catat setiap uang masuk dan keluar. Klik dua kali sebuah baris untuk mengubahnya.");
        hint.addClassName("page-hint");
        add(new Div(greeting, hint), panel);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        AuthSession.currentUser(userRepository).ifPresent(user -> {
            greeting.setText("Halo, " + user.getFullName().split("\\s+")[0]);
            panel.setUserId(user.getId());
            panel.refresh();
        });
    }
}
