package org.delcom.ifs25023springbootcashflow.framework.view;

import org.delcom.ifs25023springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25023springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25023springbootcashflow.domain.entity.User;
import org.delcom.ifs25023springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25023springbootcashflow.framework.auth.AuthSession;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;

import java.util.Optional;

/**
 * Kerangka halaman setelah login: drawer navigasi (menu admin hanya untuk ADMIN), judul halaman,
 * dan menu avatar (Profil, Keluar). Sekaligus penjaga rute: tanpa login, pengguna dialihkan ke /login.
 */
public class MainLayout extends AppLayout implements BeforeEnterObserver, AfterNavigationObserver {

    private final IUserRepository userRepository;
    private final FinancePresenter presenter;

    private final H1 pageTitle = new H1("Cashflow");
    private final Div drawerContent = new Div();
    private final Div accountMenuHost = new Div();

    public MainLayout(IUserRepository userRepository, FinancePresenter presenter) {
        this.userRepository = userRepository;
        this.presenter = presenter;

        setPrimarySection(Section.DRAWER);
        pageTitle.addClassName("layout-title");
        accountMenuHost.addClassName("account-menu-host");
        drawerContent.addClassName("drawer-content");

        Div spacer = new Div();
        spacer.getStyle().set("flex", "1");
        addToNavbar(true, new DrawerToggle(), pageTitle, spacer, accountMenuHost);
        addToDrawer(drawerContent);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<User> user = AuthSession.currentUser(userRepository);
        if (user.isEmpty()) {
            event.forwardTo(LoginView.class);
            return;
        }
        render(user.get());
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        String title = Optional.ofNullable(getContent())
                .map(content -> content.getClass().getAnnotation(PageTitle.class))
                .map(PageTitle::value)
                .orElse("Cashflow");
        pageTitle.setText(title);
    }

    /** Menggambar ulang drawer dan avatar (dipanggil setelah profil/foto berubah). */
    public void refreshUser() {
        AuthSession.currentUser(userRepository).ifPresent(this::render);
    }

    private void render(User user) {
        drawerContent.removeAll();
        drawerContent.add(brand(), navigation(user), identity(user));

        accountMenuHost.removeAll();
        accountMenuHost.add(accountMenu(user));
    }

    private Div brand() {
        Div mark = new Div(VaadinIcon.EXCHANGE.create());
        mark.addClassName("brand-mark");
        Span name = new Span("Cashflow");
        name.addClassName("brand-name");
        Div brand = new Div(mark, name);
        brand.addClassName("brand");
        return brand;
    }

    private SideNav navigation(User user) {
        SideNav nav = new SideNav();
        nav.addItem(new SideNavItem("Transaksi saya", FinanceView.class, VaadinIcon.WALLET.create()));
        nav.addItem(new SideNavItem("Profil", ProfileView.class, VaadinIcon.USER.create()));
        if (user.isAdmin()) {
            nav.addItem(new SideNavItem("Daftar pengguna", UsersView.class, VaadinIcon.USERS.create()));
            nav.addItem(new SideNavItem("Transaksi pengguna", UserTransactionsView.class, VaadinIcon.LIST.create()));
        }
        return nav;
    }

    private Div identity(User user) {
        Span name = new Span(user.getFullName());
        name.addClassName("identity-name");
        Span role = new Span(presenter.roleLabel(user.getRole()) + " · @" + user.getUsername());
        role.addClassName("identity-role");
        Div identity = new Div(name, role);
        identity.addClassName("identity");
        return identity;
    }

    private MenuBar accountMenu(User user) {
        Avatar avatar = new Avatar(user.getFullName());
        if (user.hasPhoto()) {
            avatar.setImage(FileStorage.publicUrl(user.getPhotoPath()));
        }
        avatar.addClassName("nav-avatar");
        Span firstName = new Span(user.getFullName().split("\\s+")[0]);
        firstName.addClassName("nav-username");
        HorizontalLayout trigger = new HorizontalLayout(avatar, firstName);
        trigger.setAlignItems(FlexComponent.Alignment.CENTER);
        trigger.setSpacing(true);

        MenuBar menuBar = new MenuBar();
        menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
        var root = menuBar.addItem(trigger);
        root.getSubMenu().addItem("Profil", event -> UI.getCurrent().navigate(ProfileView.class));
        root.getSubMenu().addItem("Keluar", event -> logout());
        return menuBar;
    }

    private void logout() {
        AuthSession.logout();
        // Muat ulang penuh agar seluruh state UI lama dibuang.
        UI.getCurrent().getPage().setLocation("/login");
    }
}
