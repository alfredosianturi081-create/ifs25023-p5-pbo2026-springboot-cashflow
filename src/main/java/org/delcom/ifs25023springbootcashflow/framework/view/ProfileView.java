package org.delcom.ifs25023springbootcashflow.framework.view;

import org.delcom.ifs25023springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25023springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25023springbootcashflow.domain.entity.User;
import org.delcom.ifs25023springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25023springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25023springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25023springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25023springbootcashflow.usecase.UserUseCase;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.io.IOException;

/** Profil pengguna: ubah nama, password, dan foto. Username dan peran bersifat tetap. */
@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Profil")
public class ProfileView extends VerticalLayout {

    private final IUserRepository userRepository;
    private final UserUseCase userUseCase;
    private final FinancePresenter presenter;
    private final FileStorage fileStorage;

    private final Avatar avatar = new Avatar();
    private final Span displayName = new Span();
    private final Span meta = new Span();
    private final TextField fullName = new TextField("Nama lengkap");
    private final PasswordField newPassword = new PasswordField("Password baru");
    private final PasswordField confirmPassword = new PasswordField("Ulangi password baru");
    private final ImageUploadField photoUpload =
            new ImageUploadField("Pilih foto", "Seret foto profil ke sini (PNG/JPG, maks. 5 MB)");
    private final Button removePhoto = new Button("Hapus foto", VaadinIcon.TRASH.create());
    private final ProgressBar progress = DialogSupport.hiddenProgress();
    private final Button save = new Button("Simpan perubahan", VaadinIcon.CHECK.create());

    public ProfileView(IUserRepository userRepository, UserUseCase userUseCase,
                       FinancePresenter presenter, FileStorage fileStorage) {
        this.userRepository = userRepository;
        this.userUseCase = userUseCase;
        this.presenter = presenter;
        this.fileStorage = fileStorage;

        setPadding(true);
        addClassName("page");

        avatar.addThemeVariants(AvatarVariant.LUMO_XLARGE);
        displayName.addClassName("profile-name");
        meta.addClassName("profile-meta");
        Div identity = new Div(displayName, meta);
        HorizontalLayout head = new HorizontalLayout(avatar, identity);
        head.setAlignItems(FlexComponent.Alignment.CENTER);
        head.setSpacing(true);

        fullName.setWidthFull();
        newPassword.setWidthFull();
        newPassword.setHelperText("Kosongkan jika tidak ingin mengganti password");
        confirmPassword.setWidthFull();

        removePhoto.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        removePhoto.addClickListener(event -> deletePhoto());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(event -> saveProfile());

        VerticalLayout form = new VerticalLayout(head, progress, fullName, newPassword, confirmPassword,
                photoUpload, removePhoto, save);
        form.setPadding(false);
        form.setMaxWidth("520px");
        add(new PageCard(form));
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        AuthSession.currentUser(userRepository).ifPresent(this::show);
    }

    private void show(User user) {
        avatar.setName(user.getFullName());
        avatar.setImage(FileStorage.publicUrl(user.getPhotoPath()));
        displayName.setText(user.getFullName());
        meta.setText("@" + user.getUsername() + " · " + presenter.roleLabel(user.getRole()));
        fullName.setValue(user.getFullName());
        removePhoto.setVisible(user.hasPhoto());
    }

    private void saveProfile() {
        User user = AuthSession.currentUser(userRepository).orElse(null);
        if (user == null) {
            return;
        }
        if (!newPassword.getValue().equals(confirmPassword.getValue())) {
            Notices.error("Konfirmasi password tidak sama");
            return;
        }
        progress.setVisible(true);
        save.setEnabled(false);
        String newPhoto = null;
        try {
            String oldPhoto = user.getPhotoPath();
            if (photoUpload.hasFile()) {
                newPhoto = photoUpload.store(fileStorage);
            }
            userUseCase.updateProfile(user.getId(), fullName.getValue(), newPassword.getValue());
            if (newPhoto != null) {
                userUseCase.updatePhoto(user.getId(), newPhoto);
                fileStorage.delete(oldPhoto);
            }
            newPassword.clear();
            confirmPassword.clear();
            photoUpload.clear();
            Notices.success("Profil diperbarui");
            afterChange();
        } catch (ValidationException ex) {
            fileStorage.delete(newPhoto);
            Notices.error(ex.getMessage());
        } catch (EntityNotFoundException ex) {
            fileStorage.delete(newPhoto);
            Notices.error(presenter.showNotFoundError(ex.getMessage()));
        } catch (IOException ex) {
            Notices.error("Foto gagal disimpan: " + ex.getMessage());
        } finally {
            progress.setVisible(false);
            save.setEnabled(true);
        }
    }

    private void deletePhoto() {
        User user = AuthSession.currentUser(userRepository).orElse(null);
        if (user == null || !user.hasPhoto()) {
            return;
        }
        try {
            String oldPhoto = user.getPhotoPath();
            userUseCase.updatePhoto(user.getId(), null);
            fileStorage.delete(oldPhoto);
            Notices.success("Foto profil dihapus");
            afterChange();
        } catch (EntityNotFoundException ex) {
            Notices.error(presenter.showNotFoundError(ex.getMessage()));
        }
    }

    /** Menyegarkan tampilan profil dan avatar di MainLayout. */
    private void afterChange() {
        AuthSession.currentUser(userRepository).ifPresent(this::show);
        getParent().filter(MainLayout.class::isInstance)
                .map(MainLayout.class::cast)
                .ifPresent(MainLayout::refreshUser);
    }
}
