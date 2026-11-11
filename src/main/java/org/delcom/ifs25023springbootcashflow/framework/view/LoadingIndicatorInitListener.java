package org.delcom.ifs25023springbootcashflow.framework.view;

import com.vaadin.flow.component.page.LoadingIndicatorConfiguration;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.stereotype.Component;

/**
 * Mengatur indikator loading global Vaadin: muncul setelah 150 ms, berubah warna di 600 ms,
 * dan 1500 ms. Animasinya (bar menyala di atas layar) didefinisikan di styles.css.
 */
@Component
public class LoadingIndicatorInitListener implements VaadinServiceInitListener {

    public static final int FIRST_DELAY_MS = 150;
    public static final int SECOND_DELAY_MS = 600;
    public static final int THIRD_DELAY_MS = 1500;

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> {
            LoadingIndicatorConfiguration config = uiEvent.getUI().getLoadingIndicatorConfiguration();
            config.setFirstDelay(FIRST_DELAY_MS);
            config.setSecondDelay(SECOND_DELAY_MS);
            config.setThirdDelay(THIRD_DELAY_MS);
        });
    }
}
