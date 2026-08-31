package dev.casteels.plukk.shopping.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import dev.casteels.plukk.shopping.list.ShoppingList;
import dev.casteels.plukk.shopping.list.ShoppingListApplicationService;
import dev.casteels.plukk.shopping.input.AddShoppingNeedApplicationService;
import dev.casteels.plukk.shopping.item.ShoppingItem;
import jakarta.annotation.security.PermitAll;

@Route("lists/:listId")
@PageTitle("Shopping list | Plukk")
@PermitAll
public class ShoppingListDetailView extends VerticalLayout implements BeforeEnterObserver {

    private final ShoppingListApplicationService lists;
    private final AddShoppingNeedApplicationService needs;
    private final H1 title = new H1();
    private final VerticalLayout items = new VerticalLayout();
    private long listId;

    public ShoppingListDetailView(ShoppingListApplicationService lists, AddShoppingNeedApplicationService needs) {
        this.lists = lists;
        this.needs = needs;
        setPadding(true);
        setWidthFull();
        Button back = new Button("All lists", event -> getUI().ifPresent(ui -> ui.navigate(ShoppingListsView.class)));
        back.getStyle().set("min-height", "44px");
        items.setPadding(false);
        items.setWidthFull();
        add(back, title, items);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        listId = event.getRouteParameters().getLong("listId").orElseThrow();
        ShoppingList list = lists.open(listId);
        title.setText(list.name());
        if (getChildren().noneMatch(AddShoppingNeedComponent.class::isInstance)) {
            addComponentAsFirst(new AddShoppingNeedComponent(listId, needs, this::handleAddResult));
        }
    }

    private void handleAddResult(AddShoppingNeedApplicationService.AddResult result) {
        if (result instanceof AddShoppingNeedApplicationService.Confirmed confirmed) {
            showItem(confirmed.item());
            Notification.show("Added " + itemLabel(confirmed.item()), 3000, Notification.Position.BOTTOM_START);
        } else if (result instanceof AddShoppingNeedApplicationService.Duplicate duplicate) {
            getUI().ifPresent(ui -> ui.getPage().executeJs("document.getElementById($0)?.focus()", "shopping-item-" + duplicate.itemId()));
            Notification.show("This item is already on the list.", 3000, Notification.Position.BOTTOM_START);
        } else if (result instanceof AddShoppingNeedApplicationService.Reformulation feedback) {
            Notification.show(feedback.message(), 5000, Notification.Position.BOTTOM_START);
        }
    }

    private void showItem(ShoppingItem item) {
        Div row = new Div(new Span(itemLabel(item)));
        row.setId("shopping-item-" + item.id());
        row.getElement().setAttribute("tabindex", "-1");
        row.getStyle().set("min-height", "44px");
        row.getStyle().set("padding", "0.75rem 0");
        items.addComponentAsFirst(row);
    }

    private String itemLabel(ShoppingItem item) {
        String details = item.variant() == null ? needs.productName(item.catalogProductId()) : item.variant();
        if (item.quantity() != null) {
            details = details.isEmpty() ? item.quantity().stripTrailingZeros().toPlainString() : details + " - " + item.quantity().stripTrailingZeros().toPlainString();
        }
        if (item.unit() != null) details += " " + item.unit();
        if (item.packageSize() != null) details += " x " + item.packageSize().stripTrailingZeros().toPlainString() + " " + item.packageUnit();
        if (item.packageDescriptor() != null) details += " " + item.packageDescriptor();
        return details.isEmpty() ? "Item" : details;
    }
}
