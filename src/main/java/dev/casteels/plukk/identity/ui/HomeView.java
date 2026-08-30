package dev.casteels.plukk.identity.ui;

import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Section;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import dev.casteels.plukk.identity.HouseholdMemberAccess;
import dev.casteels.plukk.shared.ui.ConnectivityStatusBanner;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.access.AccessDeniedException;

@Route("")
@PageTitle("Plukk")
@PermitAll
public class HomeView extends Main implements BeforeEnterObserver {

    private final HouseholdMemberAccess householdMemberAccess;

    public HomeView(HouseholdMemberAccess householdMemberAccess) {
        this.householdMemberAccess = householdMemberAccess;
        addClassName("home-view");

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);
        layout.setWidthFull();

        Section card = new Section();
        card.addClassName("intro-card");
        card.add(new Paragraph("Plukk is bootstrapped. Shopping list flows land in the next slice."));

        layout.add(new ConnectivityStatusBanner(), card);
        add(layout);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (householdMemberAccess.currentMember().isEmpty()) {
            event.rerouteToError(AccessDeniedException.class, "An active household membership is required.");
        }
    }
}
