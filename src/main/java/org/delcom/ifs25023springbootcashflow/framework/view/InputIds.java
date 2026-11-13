package org.delcom.ifs25023springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;

/**
 * Menaruh id pada elemen {@code <input>} asli di dalam komponen Vaadin. Secara default
 * {@code setId} hanya memasang id pada elemen pembungkus (mis. vaadin-text-field), sehingga uji
 * otomatis yang mengetik ke {@code #login-username} tidak mengenai kolom input yang sebenarnya.
 */
final class InputIds {

    private InputIds() {
    }

    static void setInnerInputId(Component component, String id) {
        component.setId(id + "-field");
        Runnable apply = () -> component.getElement().executeJs(
                "const applyId = () => {"
                        + "  const input = this.inputElement || this.querySelector('input');"
                        + "  if (input) {"
                        + "    input.id = $0;"
                        + "    const label = this.querySelector('label');"
                        + "    if (label) label.setAttribute('for', $0);"
                        + "    return true;"
                        + "  }"
                        + "  return false;"
                        + "};"
                        + "if (!applyId()) {"
                        + "  if (this.updateComplete) { this.updateComplete.then(applyId); }"
                        + "  const obs = new MutationObserver(() => { if (applyId()) obs.disconnect(); });"
                        + "  obs.observe(this, { childList: true, subtree: true });"
                        + "}", id);

        apply.run();
        component.addAttachListener(event -> apply.run());
    }
}
