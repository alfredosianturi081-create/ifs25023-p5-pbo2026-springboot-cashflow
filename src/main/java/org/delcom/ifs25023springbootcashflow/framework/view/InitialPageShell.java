package org.delcom.ifs25023springbootcashflow.framework.view;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

/**
 * Menyisipkan "cangkang" awal ke HTML yang pertama kali dikirim ke browser.
 * <p>
 * Aplikasi Vaadin menggambar halamannya lewat JavaScript, sehingga selama bundel dimuat halaman
 * kosong: tanpa {@code <main>}, tanpa {@code <h1>}, dan tanpa teks yang bisa dicat. Pengaudit
 * (Axe dan Lighthouse) yang membaca halaman pada saat itu melaporkan pelanggaran landmark, dan
 * First Contentful Paint tertunda. Cangkang ini berisi {@code <main>} dengan {@code <h1>} statis
 * yang langsung tampil, lalu dibuang begitu Vaadin selesai menggambar tampilan sebenarnya.
 * <p>
 * Skrip yang sama juga memberi {@code role="status"} pada {@code vaadin-connection-indicator}
 * (elemen bawaan Vaadin yang dibuat saat runtime) agar dianggap live region oleh aturan
 * Axe "region", bukan konten liar di luar landmark.
 */
@Component
public class InitialPageShell implements VaadinServiceInitListener {

    static final String SHELL_ID = "app-shell";

    static final String STYLE = """
            <style id="app-shell-style">
              #app-shell {
                box-sizing: border-box;
                max-width: 32rem;
                margin: 0 auto;
                padding: 18vh 1.5rem 0;
                text-align: center;
                font-family: system-ui, -apple-system, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                color: #1E293B;
              }
              #app-shell h1 { margin: 0 0 0.5rem; font-size: 2rem; font-weight: 800; }
              #app-shell p { margin: 0; color: #475569; }
              body:has(#outlet > *) #app-shell { display: none; }
            </style>
            """;

    static final String SHELL = """
            <main id="app-shell">
              <h1>Cashflow</h1>
              <p>Memuat aplikasi&hellip;</p>
            </main>
            <script>
              (function () {
                var shell = document.getElementById('app-shell');
                var indicatorDone = false;
                var outletDone = false;
                var check = function () {
                  if (!indicatorDone) {
                    var items = document.querySelectorAll('vaadin-connection-indicator');
                    items.forEach(function (item) { item.setAttribute('role', 'status'); });
                    indicatorDone = items.length > 0;
                  }
                  if (!outletDone) {
                    var outlet = document.getElementById('outlet');
                    if (outlet && outlet.firstElementChild) {
                      if (shell) { shell.remove(); }
                      outletDone = true;
                    }
                  }
                  return indicatorDone && outletDone;
                };
                if (!check()) {
                  var observer = new MutationObserver(function () {
                    if (check()) { observer.disconnect(); }
                  });
                  observer.observe(document.documentElement, { childList: true, subtree: true });
                }
              })();
            </script>
            """;

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.addIndexHtmlRequestListener(response -> {
            Document document = response.getDocument();
            Element html = document.selectFirst("html");
            if (html != null) {
                html.attr("lang", "id");
            }
            document.head().append(STYLE);
            document.body().prepend(SHELL);
        });
    }
}
