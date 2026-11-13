package org.delcom.ifs25023springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;

/**
 * Field Vaadin (TextField, PasswordField) adalah elemen khusus yang membungkus {@code <input>} asli.
 * Uji otomatis mencari {@code <input id="...">}, bukan elemen pembungkusnya, jadi id harus dipasang
 * pada input di dalamnya. Elemen pembungkus diberi id "<id>-field" agar tidak bentrok.
 */
final class InputIds {

    private InputIds() {
    }

    static void setInnerInputId(Component field, String id) {
        field.setId(id + "-field");
        field.addAttachListener(event -> field.getElement().executeJs(
                "const host = this;"
                        + "const apply = () => {"
                        + "  const input = host.inputElement || host.querySelector('input');"
                        + "  if (!input) { return false; }"
                        + "  input.id = $0;"
                        + "  const label = host.querySelector('label');"
                        + "  if (label) { label.setAttribute('for', $0); }"
                        + "  return true;"
                        + "};"
                        + "if (!apply()) {"
                        + "  const observer = new MutationObserver(() => { if (apply()) { observer.disconnect(); } });"
                        + "  observer.observe(host, { childList: true, subtree: true });"
                        + "}",
                id));
    }
}