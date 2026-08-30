package dev.casteels.plukk.shopping.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import dev.casteels.plukk.shopping.list.ShoppingList;
import dev.casteels.plukk.shopping.list.ShoppingListApplicationService;
import jakarta.annotation.security.PermitAll;

@Route("lists/:listId")
@PageTitle("Shopping list | Plukk")
@PermitAll
public class ShoppingListDetailView extends VerticalLayout implements BeforeEnterObserver {

    private final ShoppingListApplicationService lists;
    private final H1 title = new H1();

    public ShoppingListDetailView(ShoppingListApplicationService lists) {
        this.lists = lists;
        setPadding(true);
        setWidthFull();
        Button back = new Button("All lists", event -> getUI().ifPresent(ui -> ui.navigate(ShoppingListsView.class)));
        back.getStyle().set("min-height", "44px");
        add(back, title);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        long listId = event.getRouteParameters().getLong("listId").orElseThrow();
        ShoppingList list = lists.open(listId);
        title.setText(list.name());
    }
}
