package org.delcom.ifs25023springbootcashflow.framework.view;

import org.delcom.ifs25023springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25023springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25023springbootcashflow.domain.entity.User;
import org.delcom.ifs25023springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25023springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25023springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25023springbootcashflow.usecase.UserUseCase;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.Comparator;

/** Daftar seluruh pengguna beserta saldonya. Khusus ADMIN; pengguna biasa dialihkan ke beranda. */
@Route(value = "users", layout = MainLayout.class)
@PageTitle("Daftar Pengguna")
public class UsersView extends VerticalLayout implements BeforeEnterObserver {

    private final IUserRepository userRepository;
    private final UserUseCase userUseCase;
    private final FinanceUseCase financeUseCase;
    private final FinancePresenter presenter;

    private final Grid<User> grid = new Grid<>(User.class, false);
    private final TextField search = new TextField("Cari pengguna");

    public UsersView(IUserRepository userRepository, UserUseCase userUseCase,
                     FinanceUseCase financeUseCase, FinancePresenter presenter) {
        this.userRepository = userRepository;
        this.userUseCase = userUseCase;
        this.financeUseCase = financeUseCase;
        this.presenter = presenter;

        setPadding(true);
        addClassName("page");

        search.setPlaceholder("Nama atau username...");
        search.setPrefixComponent(VaadinIcon.SEARCH.create());
        search.setClearButtonVisible(true);
        search.setValueChangeMode(ValueChangeMode.LAZY);
        search.setWidth("320px");
        search.addValueChangeListener(event -> refresh());

        configureGrid();
        add(search, new PageCard(grid));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        var user = AuthSession.currentUser(userRepository);
        if (user.isEmpty()) {
            event.forwardTo(LoginView.class);
        } else if (!user.get().isAdmin()) {
            event.forwardTo(FinanceView.class);
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        refresh();
    }

    private void refresh() {
        grid.setItems(userUseCase.searchUsers(search.getValue()));
    }

    private void configureGrid() {
        GridStyles.apply(grid);
        grid.addColumn(User::getId).setHeader("ID").setKey("id")
                .setAutoWidth(true).setFlexGrow(0).setSortable(true).setComparator(User::getId);
        grid.addComponentColumn(this::identityCell).setHeader("Pengguna").setKey("name")
                .setFlexGrow(1).setSortable(true)
                .setComparator(Comparator.comparing(User::getFullName, String.CASE_INSENSITIVE_ORDER));
        grid.addColumn(User::getUsername).setHeader("Username").setKey("username")
                .setAutoWidth(true).setSortable(true)
                .setComparator(Comparator.comparing(User::getUsername, String.CASE_INSENSITIVE_ORDER));
        grid.addComponentColumn(this::roleBadge).setHeader("Peran").setKey("role")
                .setAutoWidth(true).setSortable(true).setComparator(User::getRole);
        grid.addColumn(user -> presenter.formatMoney(financeUseCase.getBalance(user.getId())))
                .setHeader("Saldo").setKey("balance").setAutoWidth(true)
                .setComparator(Comparator.comparingLong((User user) -> financeUseCase.getBalance(user.getId())))
                .setSortable(true);
        grid.addComponentColumn(this::actions).setHeader("Tindakan").setKey("actions")
                .setAutoWidth(true).setFlexGrow(0).setSortable(false);
        grid.setHeight("520px");
        Span empty = new Span("Tidak ada pengguna yang cocok.");
        empty.addClassName("empty-state");
        grid.setEmptyStateComponent(empty);
    }

    private HorizontalLayout identityCell(User user) {
        Avatar avatar = new Avatar(user.getFullName());
        if (user.hasPhoto()) {
            avatar.setImage(FileStorage.publicUrl(user.getPhotoPath()));
        }
        HorizontalLayout cell = new HorizontalLayout(avatar, new Span(user.getFullName()));
        cell.setAlignItems(FlexComponent.Alignment.CENTER);
        return cell;
    }

    private Span roleBadge(User user) {
        Span badge = new Span(presenter.roleLabel(user.getRole()));
        badge.addClassNames("role-badge", user.isAdmin() ? "role-admin" : "role-user");
        return badge;
    }

    private Div actions(User user) {
        Button view = new Button("Lihat transaksi", VaadinIcon.EYE.create(),
                event -> UI.getCurrent().navigate(UserTransactionsView.class, user.getId()));
        view.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        return new Div(view);
    }
}
