package dev.casteels.plukk.shared.ui;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.html.Div;

/** Displays browser connectivity without queuing or retrying mutations. */
public class ConnectivityStatusBanner extends Div {

    public ConnectivityStatusBanner() {
        addClassName("connectivity-status");
        getElement().setAttribute("role", "status");
        getElement().executeJs("""
                const status = () => this.$server.updateConnectivity(navigator.onLine);
                window.addEventListener('online', status);
                window.addEventListener('offline', status);
                status();
                """);
    }

    @ClientCallable
    public void updateConnectivity(boolean connected) {
        setText(connected
                ? "Connected. Changes are saved only after confirmation."
                : "Disconnected. You can view loaded information, but changes cannot be saved.");
        getElement().setAttribute("data-connected", Boolean.toString(connected));
    }
}
