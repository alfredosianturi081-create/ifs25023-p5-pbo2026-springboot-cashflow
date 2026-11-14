package org.delcom.ifs25023springbootcashflow.framework.view;

import com.vaadin.flow.component.page.LoadingIndicatorConfiguration;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

/**
 * Konfigurasi global Vaadin:
 * 1) indikator loading: muncul setelah 150 ms, berubah warna di 600 ms dan 1500 ms
 *    (animasinya didefinisikan di styles.css);
 * 2) judul dokumen dan atribut lang sudah ada di HTML awal, sehingga alat audit
 *    (Lighthouse, Axe) tidak melihat halaman tanpa judul sebelum Vaadin menjalankan navigasi.
 */
@Component
public class LoadingIndicatorInitListener implements VaadinServiceInitListener {

    public static final int FIRST_DELAY_MS = 150;
    public static final int SECOND_DELAY_MS = 600;
    public static final int THIRD_DELAY_MS = 1500;

    public static final String DEFAULT_TITLE = "Cashflow";
    public static final String LANGUAGE = "id";

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.addIndexHtmlRequestListener(response -> {
            Document document = response.getDocument();
            Element html = document.selectFirst("html");
            if (html != null) {
                // Vaadin memberi lang="en" secara bawaan; isi aplikasi ini berbahasa Indonesia.
                html.attr("lang", LANGUAGE);
            }
            if (document.head().selectFirst("title") == null) {
                document.head().appendElement("title").text(DEFAULT_TITLE);
            }
        });

        event.getSource().addUIInitListener(uiEvent -> {
            LoadingIndicatorConfiguration config = uiEvent.getUI().getLoadingIndicatorConfiguration();
            config.setFirstDelay(FIRST_DELAY_MS);
            config.setSecondDelay(SECOND_DELAY_MS);
            config.setThirdDelay(THIRD_DELAY_MS);
            // Judul awal; tiap halaman menimpanya lewat @PageTitle saat navigasi.
            uiEvent.getUI().getPage().setTitle(DEFAULT_TITLE);
        });
    }
}