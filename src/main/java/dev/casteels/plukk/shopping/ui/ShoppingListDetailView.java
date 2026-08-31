package dev.casteels.plukk.shopping.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import dev.casteels.plukk.catalog.api.SearchCatalogProductsUseCase;
import dev.casteels.plukk.shopping.history.ListRecentShoppingNeedsUseCase;
import dev.casteels.plukk.shopping.history.ReAddShoppingNeedUseCase;
import dev.casteels.plukk.shopping.input.AddShoppingNeedUseCase;
import dev.casteels.plukk.shopping.input.CreateCustomProductAndAddShoppingNeedUseCase;
import dev.casteels.plukk.shopping.input.FindShoppingCategoriesUseCase;
import dev.casteels.plukk.shopping.input.ShoppingNeedOutcome;
import dev.casteels.plukk.shopping.item.GetShoppingListSectionsUseCase;
import dev.casteels.plukk.shopping.item.PurchaseShoppingItemUseCase;
import dev.casteels.plukk.shopping.item.RemoveShoppingItemUseCase;
import dev.casteels.plukk.shopping.item.RestoreShoppingItemUseCase;
import dev.casteels.plukk.shopping.item.ShoppingItem;
import dev.casteels.plukk.shopping.item.ShoppingListSection;
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
    private final SearchCatalogProductsUseCase searchCatalog;
    private final ListRecentShoppingNeedsUseCase listRecentNeeds;
    private final ReAddShoppingNeedUseCase reAddNeed;
    private final GetShoppingListSectionsUseCase getSections;
    private final PurchaseShoppingItemUseCase purchaseItem;
    private final RestoreShoppingItemUseCase restoreItem;
    private final RemoveShoppingItemUseCase removeItem;
    private final H1 title = new H1();
    private final VerticalLayout sections = new VerticalLayout();
    private long listId;

    public ShoppingListDetailView(OpenShoppingListUseCase openList, AddShoppingNeedUseCase addNeed,
            CreateCustomProductAndAddShoppingNeedUseCase createCustomProduct, FindShoppingCategoriesUseCase findCategories,
            SearchCatalogProductsUseCase searchCatalog, ListRecentShoppingNeedsUseCase listRecentNeeds, ReAddShoppingNeedUseCase reAddNeed,
            GetShoppingListSectionsUseCase getSections, PurchaseShoppingItemUseCase purchaseItem,
            RestoreShoppingItemUseCase restoreItem, RemoveShoppingItemUseCase removeItem) {
        this.openList = openList;
        this.addNeed = addNeed;
        this.createCustomProduct = createCustomProduct;
        this.findCategories = findCategories;
        this.searchCatalog = searchCatalog;
        this.listRecentNeeds = listRecentNeeds;
        this.reAddNeed = reAddNeed;
        this.getSections = getSections;
        this.purchaseItem = purchaseItem;
        this.restoreItem = restoreItem;
        this.removeItem = removeItem;
        setPadding(true);
        setWidthFull();
        Button back = new Button("All lists", event -> getUI().ifPresent(ui -> ui.navigate(ShoppingListsView.class)));
        back.getStyle().set("min-height", "44px");
        sections.setPadding(false);
        sections.setSpacing(false);
        sections.setWidthFull();
        add(back, title, sections);
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
            addComponentAsFirst(new AddShoppingNeedComponent(listId, addNeed, createCustomProduct, findCategories, searchCatalog,
                    listRecentNeeds, reAddNeed, this::handleAddResult));
        }
        refreshSections();
    }

    private void handleAddResult(ShoppingNeedOutcome result) {
        if (result instanceof ShoppingNeedOutcome.Confirmed confirmed) {
            refreshSections();
            Notification.show("Added to the list", 3000, Notification.Position.BOTTOM_START);
            focusItem(confirmed.item().id());
        } else if (result instanceof ShoppingNeedOutcome.Duplicate duplicate) {
            focusItem(duplicate.itemId());
            show(result.notification());
        } else if (result instanceof ShoppingNeedOutcome.Rejected rejected) {
            show(rejected.notification());
        }
    }

    private void refreshSections() {
        sections.removeAll();
        GetShoppingListSectionsUseCase.Result result = getSections.execute(listId);
        if (!result.notification().isSuccess()) {
            show(result.notification());
            return;
        }
        for (ShoppingListSection section : result.sections()) {
            sections.add(renderSection(section));
        }
    }

    private Component renderSection(ShoppingListSection section) {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setWidthFull();
        H2 header = new H2(section.categoryName());
        header.getStyle().set("margin", "1rem 0 0.25rem 0").set("font-size", "1rem");
        layout.add(header);
        for (ShoppingListSection.Entry entry : section.items()) {
            layout.add(renderItem(entry));
        }
        return layout;
    }

    private Component renderItem(ShoppingListSection.Entry entry) {
        ShoppingItem item = entry.item();
        boolean purchased = item.state() == ShoppingItem.State.PURCHASED;

        Span label = new Span(itemLabel(entry));
        Button toggle = new Button(purchased ? "Restore" : "Purchase", event -> togglePurchased(item, purchased));
        toggle.getStyle().set("min-height", "44px");
        Button remove = new Button("Remove", event -> removeItem(item));
        remove.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        remove.getStyle().set("min-height", "44px");

        HorizontalLayout row = new HorizontalLayout(label, toggle, remove);
        row.setId("shopping-item-" + item.id());
        row.getElement().setAttribute("tabindex", "-1");
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setFlexGrow(1, label);
        row.getStyle().set("padding", "0.5rem 0");
        if (purchased) {
            row.addClassName("shopping-item-purchased");
        }
        return row;
    }

    private void togglePurchased(ShoppingItem item, boolean purchased) {
        dev.casteels.plukk.shared.notification.Notification notification = purchased
                ? restoreItem.execute(listId, item.id()).notification()
                : purchaseItem.execute(listId, item.id()).notification();
        if (!notification.isSuccess()) {
            show(notification);
            return;
        }
        refreshSections();
    }

    private void removeItem(ShoppingItem item) {
        dev.casteels.plukk.shared.notification.Notification notification = removeItem.execute(listId, item.id());
        if (!notification.isSuccess()) {
            show(notification);
            return;
        }
        refreshSections();
    }

    private void focusItem(long itemId) {
        getUI().ifPresent(ui -> ui.getPage().executeJs("document.getElementById($0)?.focus()", "shopping-item-" + itemId));
    }

    private String itemLabel(ShoppingListSection.Entry entry) {
        ShoppingItem item = entry.item();
        String details = item.variant() != null ? item.variant() : entry.productName();
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
