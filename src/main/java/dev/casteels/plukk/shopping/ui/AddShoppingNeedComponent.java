package dev.casteels.plukk.shopping.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
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
    private final Consumer<ShoppingNeedOutcome> resultHandler;
    private final TextField input = new TextField("Add a need");

    public AddShoppingNeedComponent(long listId, AddShoppingNeedUseCase addNeed,
            CreateCustomProductAndAddShoppingNeedUseCase createCustomProduct, FindShoppingCategoriesUseCase findCategories,
            Consumer<ShoppingNeedOutcome> resultHandler) {
        this.listId = listId;
        this.addNeed = addNeed;
        this.createCustomProduct = createCustomProduct;
        this.findCategories = findCategories;
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
        add(entry);
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

    private void showUnexpectedError(RuntimeException exception) {
        logger.error("Could not add a shopping need to list {}", listId, exception);
        com.vaadin.flow.component.notification.Notification.show("The item could not be added. Please try again.", 5000,
                com.vaadin.flow.component.notification.Notification.Position.BOTTOM_START);
    }
}
