package org.delcom.ifs25023springbootcashflow.framework.view;

import org.delcom.ifs25023springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25023springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25023springbootcashflow.domain.entity.BalanceSummary;
import org.delcom.ifs25023springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25023springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25023springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25023springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25023springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25023springbootcashflow.usecase.FinanceUseCase;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

/**
 * Panel transaksi yang dipakai bersama oleh {@link FinanceView} (milik sendiri, bisa diubah) dan
 * {@link UserTransactionsView} (milik orang lain, read-only untuk admin): kartu ringkasan,
 * toolbar cari/urut, grid, serta dialog tambah/ubah/hapus.
 */
public class TransactionPanel extends VerticalLayout {

    private final FinanceUseCase financeUseCase;
    private final FinancePresenter presenter;
    private final FileStorage fileStorage;
    private final boolean readOnly;

    private Integer userId;

    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private final TextField searchField = new TextField();
    private final Select<SortOption> sortSelect = new Select<>();
    private final Span emptyState = new Span();

    private final Span balanceValue = new Span("Rp 0.00");
    private final Span countValue = new Span("0");
    private final Span incomeValue = new Span("Rp 0.00");
    private final Span expenseValue = new Span("Rp 0.00");
    private final Div balanceCard = new Div();
    private final Div ribbon = new Div();
    private final Div incomeSegment = new Div();
    private final Div expenseSegment = new Div();
    private final Span ribbonLegend = new Span();

    private Button addIncomeButton;
    private Button addExpenseButton;

    public TransactionPanel(FinanceUseCase financeUseCase, FinancePresenter presenter,
                            FileStorage fileStorage, boolean readOnly) {
        this.financeUseCase = financeUseCase;
        this.presenter = presenter;
        this.fileStorage = fileStorage;
        this.readOnly = readOnly;

        setPadding(false);
        setSpacing(true);
        setWidthFull();

        configureGrid();
        add(buildSummary(), buildToolbar(), new PageCard(grid));
        refresh();
    }

    /** Menentukan pemilik transaksi yang ditampilkan; {@code null} berarti belum ada yang dipilih. */
    public void setUserId(Integer userId) {
        this.userId = userId;
        boolean enabled = userId != null;
        if (addIncomeButton != null) {
            addIncomeButton.setEnabled(enabled);
            addExpenseButton.setEnabled(enabled);
        }
    }

    /** Memuat ulang ringkasan dan isi grid sesuai kata kunci serta urutan terpilih. */
    public void refresh() {
        if (userId == null) {
            grid.setItems(List.of());
            applySummary(new BalanceSummary(0, 0, 0));
            emptyState.setText("Pilih pengguna untuk melihat transaksinya.");
            return;
        }
        String keyword = searchField.getValue();
        grid.setItems(financeUseCase.list(userId, keyword, sortSelect.getValue()));
        applySummary(financeUseCase.summarize(userId));
        boolean searching = keyword != null && !keyword.isBlank();
        if (searching) {
            emptyState.setText("Tidak ada transaksi yang cocok dengan \"" + keyword.trim() + "\".");
        } else if (readOnly) {
            emptyState.setText("Pengguna ini belum mencatat transaksi.");
        } else {
            emptyState.setText("Belum ada transaksi. Mulai dengan menekan Tambah Pemasukan atau Tambah Pengeluaran.");
        }
    }

    // ------------------------------------------------------------------ ringkasan

    private Div buildSummary() {
        Span balanceCaption = new Span("Saldo");
        balanceCaption.addClassName("card-caption");
        balanceValue.addClassName("balance-value");

        incomeSegment.addClassName("ribbon-income");
        expenseSegment.addClassName("ribbon-expense");
        ribbon.addClassName("ribbon");
        ribbon.add(incomeSegment, expenseSegment);
        ribbonLegend.addClassName("ribbon-legend");

        balanceCard.addClassNames("summary-card", "balance-card");
        balanceCard.add(balanceCaption, balanceValue, ribbon, ribbonLegend);

        Div summary = new Div(
                balanceCard,
                smallCard("Jumlah transaksi", countValue, "card-count"),
                smallCard("Pemasukan", incomeValue, "card-income"),
                smallCard("Pengeluaran", expenseValue, "card-expense"));
        summary.addClassName("summary-grid");
        return summary;
    }

    private Div smallCard(String caption, Span value, String tone) {
        Span captionLabel = new Span(caption);
        captionLabel.addClassName("card-caption");
        value.addClassName("card-value");
        Div card = new Div(captionLabel, value);
        card.addClassNames("summary-card", tone);
        return card;
    }

    private void applySummary(BalanceSummary summary) {
        balanceValue.setText(presenter.formatMoney(summary.balance()));
        balanceCard.getClassNames().set("negative", summary.balance() < 0);
        countValue.setText(String.valueOf(summary.count()));
        incomeValue.setText(presenter.formatMoney(summary.totalIncome()));
        expenseValue.setText(presenter.formatMoney(summary.totalExpense()));

        boolean hasFlow = summary.totalIncome() + summary.totalExpense() > 0;
        ribbon.getClassNames().set("ribbon-empty", !hasFlow);
        int incomeShare = summary.incomeSharePercent();
        incomeSegment.getStyle().set("width", (hasFlow ? incomeShare : 0) + "%");
        expenseSegment.getStyle().set("width", (hasFlow ? 100 - incomeShare : 0) + "%");
        ribbonLegend.setText(hasFlow
                ? "Pemasukan " + incomeShare + "% · Pengeluaran " + (100 - incomeShare) + "%"
                : "Belum ada arus uang yang tercatat");
    }

    // ------------------------------------------------------------------ toolbar

    private HorizontalLayout buildToolbar() {
        searchField.setLabel("Cari");
        searchField.setPlaceholder("Ketik keterangan transaksi...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(event -> refresh());

        sortSelect.setItems(SortOption.values());
        sortSelect.setItemLabelGenerator(SortOption::label);
        sortSelect.setValue(SortOption.NEWEST);
        sortSelect.setLabel("Urutkan");
        sortSelect.addValueChangeListener(event -> {
            grid.sort(List.of());
            refresh();
        });

        Button refreshButton = new Button("Muat Ulang", VaadinIcon.REFRESH.create(), event -> refresh());

        HorizontalLayout toolbar = new HorizontalLayout(searchField, sortSelect, refreshButton);
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.END);
        toolbar.setFlexGrow(1, searchField);
        toolbar.getStyle().set("flex-wrap", "wrap");

        if (!readOnly) {
            addIncomeButton = new Button("Tambah Pemasukan", VaadinIcon.ARROW_UP.create(),
                    event -> openFormDialog(null, TransactionType.INCOME));
            addIncomeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
            addExpenseButton = new Button("Tambah Pengeluaran", VaadinIcon.ARROW_DOWN.create(),
                    event -> openFormDialog(null, TransactionType.EXPENSE));
            addExpenseButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
            toolbar.add(addIncomeButton, addExpenseButton);
        }
        return toolbar;
    }

    // ------------------------------------------------------------------ grid

    private void configureGrid() {
        GridStyles.apply(grid);

        grid.addColumn(Transaction::getId)
                .setHeader("ID").setKey("id")
                .setAutoWidth(true).setFlexGrow(0)
                .setSortable(true).setComparator(Transaction::getId);

        grid.addColumn(Transaction::getDescription)
                .setHeader("Keterangan").setKey("description")
                .setFlexGrow(1).setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getDescription, String.CASE_INSENSITIVE_ORDER));

        grid.addComponentColumn(this::moneyCell)
                .setHeader("Jumlah").setKey("amount")
                .setAutoWidth(true).setSortable(true)
                .setComparator(Comparator.comparingLong(Transaction::getAmount));

        grid.addComponentColumn(this::typeBadge)
                .setHeader("Tipe").setKey("type")
                .setAutoWidth(true).setSortable(true)
                .setComparator(Comparator.comparing(Transaction::isIncome));

        grid.addComponentColumn(this::proofCell)
                .setHeader("Bukti").setKey("proof")
                .setAutoWidth(true).setFlexGrow(0)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::hasProof));

        grid.addColumn(presenter::formatCreatedAt)
                .setHeader("Waktu").setKey("createdAt")
                .setAutoWidth(true).setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getCreatedAt));

        if (!readOnly) {
            grid.addComponentColumn(this::actionButtons)
                    .setHeader("Tindakan").setKey("actions")
                    .setAutoWidth(true).setFlexGrow(0).setSortable(false);
            grid.addItemDoubleClickListener(event -> openFormDialog(event.getItem(), event.getItem().getType()));
        }

        emptyState.addClassName("empty-state");
        grid.setEmptyStateComponent(emptyState);
        grid.setHeight("520px");
    }

    private Span moneyCell(Transaction transaction) {
        Span money = new Span(presenter.formatMoney(transaction.getAmount()));
        money.addClassNames("money", transaction.isIncome() ? "money-income" : "money-expense");
        return money;
    }

    private Span typeBadge(Transaction transaction) {
        Span badge = new Span(
                (transaction.isIncome() ? VaadinIcon.ARROW_UP : VaadinIcon.ARROW_DOWN).create(),
                new Span(presenter.typeLabel(transaction.getType())));
        badge.addClassNames("type-badge", transaction.isIncome() ? "type-income" : "type-expense");
        return badge;
    }

    private Div proofCell(Transaction transaction) {
        Div cell = new Div();
        if (transaction.hasProof()) {
            String url = FileStorage.publicUrl(transaction.getProofPath());
            Image thumbnail = new Image(url, "Bukti transaksi " + transaction.getId());
            thumbnail.addClassName("proof-thumb");
            thumbnail.addClickListener(event -> ImagePreviewDialog.show(url, transaction.getDescription()));
            cell.add(thumbnail);
        } else {
            Span none = new Span("—");
            none.addClassName("muted");
            cell.add(none);
        }
        return cell;
    }

    private HorizontalLayout actionButtons(Transaction transaction) {
        Button edit = new Button("Ubah", VaadinIcon.EDIT.create(),
                event -> openFormDialog(transaction, transaction.getType()));
        edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        Button delete = new Button("Hapus", VaadinIcon.TRASH.create(), event -> openDeleteDialog(transaction));
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
        return new HorizontalLayout(edit, delete);
    }

    // ------------------------------------------------------------------ dialog tambah / ubah

    private void openFormDialog(Transaction existing, TransactionType initialType) {
        boolean editing = existing != null;

        TextField description = new TextField("Keterangan");
        description.setWidthFull();
        description.setMaxLength(FinanceUseCase.MAX_DESCRIPTION_LENGTH);
        description.setPlaceholder(initialType == TransactionType.INCOME ? "contoh: Gaji bulanan" : "contoh: Makan siang");

        TextField amount = new TextField("Jumlah");
        amount.setWidthFull();
        amount.setPrefixComponent(new Span("Rp"));
        amount.setPlaceholder("150000 atau 150.000");

        Select<TransactionType> typeSelect = new Select<>();
        typeSelect.setLabel("Jenis");
        typeSelect.setItems(TransactionType.values());
        typeSelect.setItemLabelGenerator(presenter::typeLabel);
        typeSelect.setValue(initialType);
        typeSelect.setWidthFull();

        ImageUploadField proof = new ImageUploadField("Pilih bukti", "Seret gambar bukti ke sini (opsional)");
        Checkbox removeProof = new Checkbox("Hapus bukti yang sudah ada");
        removeProof.setVisible(editing && existing.hasProof());

        if (editing) {
            description.setValue(existing.getDescription());
            amount.setValue(String.valueOf(existing.getAmount()));
        }

        ProgressBar progress = DialogSupport.hiddenProgress();
        VerticalLayout form = new VerticalLayout(progress, description, amount);
        if (editing) {
            form.add(typeSelect);
        }
        form.add(proof, removeProof);
        form.setPadding(false);
        form.setWidth("min(440px, 80vw)");

        Dialog dialog = new Dialog();
        Button save = new Button("Simpan", VaadinIcon.CHECK.create());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());

        save.addClickListener(event -> {
            progress.setVisible(true);
            save.setEnabled(false);
            cancel.setEnabled(false);
            String newProof = null;
            try {
                long value = financeUseCase.parseAmount(amount.getValue());
                if (proof.hasFile()) {
                    newProof = proof.store(fileStorage);
                }
                if (editing) {
                    String proofArgument = newProof != null ? newProof : (removeProof.getValue() ? "" : null);
                    Transaction updated = financeUseCase.updateTransaction(userId, existing.getId(),
                            description.getValue(), value, typeSelect.getValue(), proofArgument);
                    if (proofArgument != null) {
                        fileStorage.delete(existing.getProofPath());
                    }
                    Notices.success(presenter.showUpdateSuccess(updated));
                } else {
                    Transaction created = initialType == TransactionType.INCOME
                            ? financeUseCase.addIncome(userId, description.getValue(), value, newProof)
                            : financeUseCase.addExpense(userId, description.getValue(), value, newProof);
                    Notices.success(presenter.showAddSuccess(created));
                }
                dialog.close();
                refresh();
            } catch (ValidationException ex) {
                fileStorage.delete(newProof);
                Notices.error(presenter.showValidationError(ex.getMessage()));
            } catch (EntityNotFoundException ex) {
                fileStorage.delete(newProof);
                Notices.error(presenter.showNotFoundError(ex.getMessage()));
                dialog.close();
                refresh();
            } catch (IOException ex) {
                Notices.error("Gambar gagal disimpan: " + ex.getMessage());
            } finally {
                progress.setVisible(false);
                save.setEnabled(true);
                cancel.setEnabled(true);
            }
        });

        String title = editing ? "Ubah Transaksi"
                : (initialType == TransactionType.INCOME ? "Tambah Pemasukan" : "Tambah Pengeluaran");
        DialogSupport.compose(dialog, title, presenter.typeLabel(initialType), form, cancel, save);
        dialog.open();
        description.focus();
    }

    // ------------------------------------------------------------------ dialog hapus

    private void openDeleteDialog(Transaction transaction) {
        Paragraph question = new Paragraph("Transaksi berikut akan dihapus permanen:");
        Paragraph detail = new Paragraph(presenter.format(transaction));
        detail.addClassName("delete-detail");
        ProgressBar progress = DialogSupport.hiddenProgress();
        VerticalLayout body = new VerticalLayout(progress, question, detail);
        body.setPadding(false);
        body.setWidth("min(440px, 80vw)");

        Dialog dialog = new Dialog();
        Button confirm = new Button("Hapus", VaadinIcon.TRASH.create());
        confirm.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        Button cancel = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());

        confirm.addClickListener(event -> {
            progress.setVisible(true);
            confirm.setEnabled(false);
            cancel.setEnabled(false);
            try {
                Transaction removed = financeUseCase.removeTransaction(userId, transaction.getId());
                fileStorage.delete(removed.getProofPath());
                Notices.success(presenter.showDeleteSuccess(removed));
            } catch (ValidationException ex) {
                Notices.error(presenter.showValidationError(ex.getMessage()));
            } catch (EntityNotFoundException ex) {
                Notices.error(presenter.showNotFoundError(ex.getMessage()));
            } finally {
                progress.setVisible(false);
                dialog.close();
                refresh();
            }
        });

        DialogSupport.compose(dialog, "Hapus Transaksi", "Konfirmasi", body, cancel, confirm);
        dialog.open();
    }
}
