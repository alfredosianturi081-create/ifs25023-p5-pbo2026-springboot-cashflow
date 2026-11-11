package org.delcom.ifs25023springbootcashflow.framework.view;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;

/** Gaya grid yang konsisten di seluruh halaman. */
public final class GridStyles {

    private GridStyles() {
    }

    public static void apply(Grid<?> grid) {
        grid.addClassName("app-grid");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
    }
}
