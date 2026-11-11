package org.delcom.ifs25023springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;

/** Kartu putih berisi konten halaman; gayanya didefinisikan di styles.css (.page-card). */
public class PageCard extends Div {

    public PageCard(Component... content) {
        addClassName("page-card");
        add(content);
    }
}
