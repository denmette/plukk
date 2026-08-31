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

import dev.casteels.plukk.shopping.input.AddShoppingNeedUseCase;
import dev.casteels.plukk.shopping.input.CreateCustomProductAndAddShoppingNeedUseCase;
import dev.casteels.plukk.shopping.input.FindCatalogProductNameUseCase;
import dev.casteels.plukk.shopping.input.FindShoppingCategoriesUseCase;
import dev.casteels.plukk.shopping.input.ShoppingNeedOutcome;
import dev.casteels.plukk.shopping.item.ShoppingItem;
import dev.casteels.plukk.shopping.list.OpenShoppingListUseCase;
import dev.casteels.plukk.shopping.list.ShoppingList;
import jakarta.annotation.security.PermitAll;

@Route("lists/:listId")
@PageTitle("Shopping list | Plukk")
@PermitAll
public class ShoppingListDetailView extends VerticalLayout implements BeforeEnterObserver {
    private final OpenShoppingListUseCase openList;
    private final AddShoppingNeedUseCase addNeed;
    private final CreateCustomProductAndAddShoppingNeedUseCase createCustomProduct;
    private final FindShoppingCategoriesUseCase findCategories;
    private final FindCatalogProductNameUseCase findProductName;
    private final H1 title = new H1();
    private final VerticalLayout items = new VerticalLayout();
    private long listId;

    public ShoppingListDetailView(OpenShoppingListUseCase openList, AddShoppingNeedUseCase addNeed,
            CreateCustomProductAndAddShoppingNeedUseCase createCustomProduct, FindShoppingCategoriesUseCase findCategories,
            FindCatalogProductNameUseCase findProductName) {
        this.openList = openList;
        this.addNeed = addNeed;
        this.createCustomProduct = createCustomProduct;
        this.findCategories = findCategories;
        this.findProductName = findProductName;
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
        OpenShoppingListUseCase.Result result = openList.execute(listId);
        if (!result.notification().isSuccess()) {
            event.rerouteTo(ShoppingListsView.class);
            return;
        }
        ShoppingList list = result.list();
        title.setText(list.name());
        if (getChildren().noneMatch(AddShoppingNeedComponent.class::isInstance)) {
            addComponentAsFirst(new AddShoppingNeedComponent(listId, addNeed, createCustomProduct, findCategories, this::handleAddResult));
        }
    }

    private void handleAddResult(ShoppingNeedOutcome result) {
        if (result instanceof ShoppingNeedOutcome.Confirmed confirmed) {
            String label = itemLabel(confirmed.item());
            showItem(confirmed.item(), label);
            Notification.show("Added " + label, 3000, Notification.Position.BOTTOM_START);
        } else if (result instanceof ShoppingNeedOutcome.Duplicate duplicate) {
            getUI().ifPresent(ui -> ui.getPage().executeJs("document.getElementById($0)?.focus()", "shopping-item-" + duplicate.itemId()));
            show(result.notification());
        } else if (result instanceof ShoppingNeedOutcome.Rejected rejected) {
            show(rejected.notification());
        }
    }

    private void showItem(ShoppingItem item, String label) {
        Div row = new Div(new Span(label));
        row.setId("shopping-item-" + item.id());
        row.getElement().setAttribute("tabindex", "-1");
        row.getStyle().set("min-height", "44px");
        row.getStyle().set("padding", "0.75rem 0");
        items.addComponentAsFirst(row);
    }

    private String itemLabel(ShoppingItem item) {
        String productName;
        if (item.variant() != null) {
            productName = item.variant();
        } else {
            try {
                productName = findProductName.execute(item.catalogProductId());
            } catch (RuntimeException e) {
                // Fallback if product name lookup fails (e.g., custom product not yet visible in read model)
                productName = "Product #" + item.catalogProductId();
            }
        }
        
        String details = productName;
        if (item.quantity() != null) details = details + " - " + item.quantity().stripTrailingZeros().toPlainString();
        if (item.unit() != null) details += " " + item.unit();
        if (item.packageSize() != null) details += " x " + item.packageSize().stripTrailingZeros().toPlainString() + " " + item.packageUnit();
        if (item.packageDescriptor() != null) details += " " + item.packageDescriptor();
        return details;
    }

    private void show(dev.casteels.plukk.shared.notification.Notification notification) {
        Notification.show(notification.issues().getFirst().message(), 5000, Notification.Position.BOTTOM_START);
    }
}
