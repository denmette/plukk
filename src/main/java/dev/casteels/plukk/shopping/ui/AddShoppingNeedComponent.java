package dev.casteels.plukk.shopping.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import dev.casteels.plukk.catalog.api.SearchCatalogProductsUseCase;
import dev.casteels.plukk.catalog.api.SearchCatalogProductsUseCase.CatalogProductMatch;
import dev.casteels.plukk.shopping.history.ListRecentShoppingNeedsUseCase;
import dev.casteels.plukk.shopping.history.ReAddShoppingNeedUseCase;
import dev.casteels.plukk.shopping.history.ShoppingHistoryRepository.RecentShoppingNeed;
import dev.casteels.plukk.shopping.input.AddShoppingNeedUseCase;
import dev.casteels.plukk.shopping.input.CreateCustomProductAndAddShoppingNeedUseCase;
import dev.casteels.plukk.shopping.input.FindShoppingCategoriesUseCase;
import dev.casteels.plukk.shopping.input.ShoppingCategory;
import dev.casteels.plukk.shopping.input.ShoppingInputParser;
import dev.casteels.plukk.shopping.input.ShoppingNeedOutcome;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddShoppingNeedComponent extends VerticalLayout {
    private static final Logger logger = LoggerFactory.getLogger(AddShoppingNeedComponent.class);
    private final long listId;
    private final AddShoppingNeedUseCase addNeed;
    private final CreateCustomProductAndAddShoppingNeedUseCase createCustomProduct;
    private final FindShoppingCategoriesUseCase findCategories;
    private final SearchCatalogProductsUseCase searchCatalog;
    private final ListRecentShoppingNeedsUseCase listRecentNeeds;
    private final ReAddShoppingNeedUseCase reAddNeed;
    private final Consumer<ShoppingNeedOutcome> resultHandler;
    private final TextField input = new TextField("Add a need");

    public AddShoppingNeedComponent(long listId, AddShoppingNeedUseCase addNeed,
            CreateCustomProductAndAddShoppingNeedUseCase createCustomProduct, FindShoppingCategoriesUseCase findCategories,
            SearchCatalogProductsUseCase searchCatalog, ListRecentShoppingNeedsUseCase listRecentNeeds,
            ReAddShoppingNeedUseCase reAddNeed, Consumer<ShoppingNeedOutcome> resultHandler) {
        this.listId = listId;
        this.addNeed = addNeed;
        this.createCustomProduct = createCustomProduct;
        this.findCategories = findCategories;
        this.searchCatalog = searchCatalog;
        this.listRecentNeeds = listRecentNeeds;
        this.reAddNeed = reAddNeed;
        this.resultHandler = resultHandler;
        setPadding(false);
        setSpacing(false);
        setWidthFull();
        input.setPlaceholder("e.g. kipfilet 400g");
        input.setWidthFull();
        input.setClearButtonVisible(true);
        Button add = new Button("Add", event -> submit());
        add.getStyle().set("min-height", "44px");
        add.getStyle().set("min-width", "72px");
        HorizontalLayout entry = new HorizontalLayout(input, add);
        entry.setWidthFull();
        entry.setFlexGrow(1, input);

        Button browse = new Button("Browse catalog", event -> showCatalogSearchDialog());
        browse.getStyle().set("min-height", "44px");
        Button recent = new Button("Recent", event -> showRecentNeedsDialog());
        recent.getStyle().set("min-height", "44px");
        HorizontalLayout shortcuts = new HorizontalLayout(browse, recent);
        shortcuts.setWidthFull();

        add(entry, shortcuts);
    }

    private void submit() {
        try {
            ShoppingNeedOutcome result = addNeed.execute(listId, input.getValue());
            if (result instanceof ShoppingNeedOutcome.CustomProductRequired customProductRequired) {
                showCustomProductDialog(customProductRequired.need());
                return;
            }
            input.clear();
            resultHandler.accept(result);
        } catch (RuntimeException exception) {
            showUnexpectedError(exception);
        }
    }

    private void showCustomProductDialog(ShoppingInputParser.InterpretedNeed need) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Create " + need.product());
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);
        Select<ShoppingCategory> category = new Select<>();
        category.setLabel("Category");
        category.setItems(findCategories.execute());
        category.setItemLabelGenerator(ShoppingCategory::name);
        category.setWidthFull();
        dialog.add(new Span("Choose a category for this household product."), category);
        Button create = new Button("Create product", event -> {
            if (category.isEmpty()) {
                category.setErrorMessage("Choose a category.");
                category.setInvalid(true);
                return;
            }
            try {
                ShoppingNeedOutcome result = createCustomProduct.execute(listId, need, category.getValue().id());
                if (!result.notification().isSuccess()) {
                    resultHandler.accept(result);
                    return;
                }
                dialog.close();
                input.clear();
                resultHandler.accept(result);
            } catch (RuntimeException exception) {
                showUnexpectedError(exception);
            }
        });
        Button cancel = new Button("Cancel", event -> dialog.close());
        dialog.getFooter().add(cancel, create);
        dialog.open();
    }

    private void showCatalogSearchDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Browse catalog");
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);
        dialog.setWidth("90vw");
        TextField query = new TextField();
        query.setPlaceholder("Search products");
        query.setWidthFull();
        query.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.EAGER);
        VerticalLayout results = new VerticalLayout();
        results.setPadding(false);
        results.setSpacing(false);
        results.setWidthFull();
        query.addValueChangeListener(event -> {
            results.removeAll();
            for (CatalogProductMatch match : searchCatalog.execute(event.getValue()).matches()) {
                Button pick = new Button(match.name() + " \u2013 " + match.categoryName(), pickEvent -> {
                    input.setValue(match.name().toLowerCase(java.util.Locale.ROOT) + " ");
                    dialog.close();
                });
                pick.getStyle().set("min-height", "44px");
                pick.setWidthFull();
                results.add(pick);
            }
        });
        dialog.add(query, results);
        Button close = new Button("Close", event -> dialog.close());
        dialog.getFooter().add(close);
        dialog.open();
    }

    private void showRecentNeedsDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Recently purchased");
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);
        dialog.setWidth("90vw");
        VerticalLayout results = new VerticalLayout();
        results.setPadding(false);
        results.setSpacing(false);
        results.setWidthFull();
        var needs = listRecentNeeds.execute().needs();
        if (needs.isEmpty()) {
            results.add(new Span("No recently purchased needs yet."));
        }
        for (RecentShoppingNeed need : needs) {
            Button pick = new Button(recentNeedLabel(need), event -> {
                try {
                    ShoppingNeedOutcome result = reAddNeed.execute(listId, need.entryId());
                    dialog.close();
                    resultHandler.accept(result);
                } catch (RuntimeException exception) {
                    showUnexpectedError(exception);
                }
            });
            pick.getStyle().set("min-height", "44px");
            pick.setWidthFull();
            results.add(pick);
        }
        dialog.add(results);
        Button close = new Button("Close", event -> dialog.close());
        dialog.getFooter().add(close);
        dialog.open();
    }

    private String recentNeedLabel(RecentShoppingNeed need) {
        String label = need.variant() != null ? need.variant() : need.productName();
        if (need.quantity() != null) label += " - " + need.quantity().stripTrailingZeros().toPlainString();
        if (need.unit() != null) label += " " + need.unit();
        if (need.packageSize() != null) label += " x " + need.packageSize().stripTrailingZeros().toPlainString() + " " + need.packageUnit();
        if (need.packageDescriptor() != null) label += " " + need.packageDescriptor();
        return label;
    }

    private void showUnexpectedError(RuntimeException exception) {
        logger.error("Could not add a shopping need to list {}", listId, exception);
        com.vaadin.flow.component.notification.Notification.show("The item could not be added. Please try again.", 5000,
                com.vaadin.flow.component.notification.Notification.Position.BOTTOM_START);
    }
}
