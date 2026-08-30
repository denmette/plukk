package dev.casteels.plukk.shared.ui;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@AnonymousAllowed
public class LoginView extends VerticalLayout {

    public LoginView() {
        add(new H1("Plukk"));
        add(new Paragraph("Sign in with the configured household identity provider."));

        add(new Anchor("/oauth2/authorization/authentik", "Sign in with Authentik"));
    }
}
