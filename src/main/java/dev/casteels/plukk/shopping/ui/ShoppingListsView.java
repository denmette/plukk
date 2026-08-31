package dev.casteels.plukk.shopping.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import dev.casteels.plukk.shopping.list.CreateShoppingListUseCase;
import dev.casteels.plukk.shopping.list.DeleteShoppingListUseCase;
import dev.casteels.plukk.shopping.list.FindShoppingListsUseCase;
import dev.casteels.plukk.shopping.list.RenameShoppingListUseCase;
import dev.casteels.plukk.shopping.list.ShoppingList;
import jakarta.annotation.security.PermitAll;

@Route("lists")
@PageTitle("Shopping lists | Plukk")
@PermitAll
public class ShoppingListsView extends VerticalLayout {
    private final CreateShoppingListUseCase createList;
    private final FindShoppingListsUseCase findLists;
    private final RenameShoppingListUseCase renameList;
    private final DeleteShoppingListUseCase deleteList;
    private final VerticalLayout listRows = new VerticalLayout();

    public ShoppingListsView(CreateShoppingListUseCase createList, FindShoppingListsUseCase findLists,
            RenameShoppingListUseCase renameList, DeleteShoppingListUseCase deleteList) {
        this.createList = createList;
        this.findLists = findLists;
        this.renameList = renameList;
        this.deleteList = deleteList;
        setPadding(true);
        setWidthFull();
        add(new H1("Shopping lists"));
        TextField name = new TextField("New list");
        name.setRequired(true);
        name.setWidthFull();
        Button create = new Button("Create list", event -> {
            CreateShoppingListUseCase.Result result = createList.execute(name.getValue());
            if (!result.notification().isSuccess()) {
                name.setInvalid(true);
                show(result.notification());
                return;
            }
            name.clear();
            name.setInvalid(false);
            refresh();
        });
        create.getStyle().set("min-height", "44px");
        add(name, create, listRows);
        refresh();
    }

    private void refresh() {
        listRows.removeAll();
        for (ShoppingList list : findLists.execute()) {
            Button open = new Button(list.name(), event -> getUI().ifPresent(ui -> ui.navigate("lists/" + list.id())));
            open.setWidthFull();
            open.getStyle().set("min-height", "48px");
            TextField renamedName = new TextField("Rename " + list.name());
            renamedName.setValue(list.name());
            renamedName.setRequired(true);
            renamedName.setValueChangeMode(ValueChangeMode.EAGER);
            renamedName.setWidthFull();
            Button rename = new Button("Rename", event -> {
                RenameShoppingListUseCase.Result result = renameList.execute(list.id(), renamedName.getValue());
                if (!result.notification().isSuccess()) {
                    renamedName.setInvalid(true);
                    show(result.notification());
                    return;
                }
                refresh();
            });
            Button delete = new Button("Delete", event -> {
                DeleteShoppingListUseCase.Result result = deleteList.execute(list.id());
                if (result.notification().isSuccess()) refresh(); else show(result.notification());
            });
            rename.getElement().setAttribute("aria-label", "Rename " + list.name());
            delete.getElement().setAttribute("aria-label", "Delete " + list.name());
            rename.getStyle().set("min-height", "44px");
            delete.getStyle().set("min-height", "44px");
            HorizontalLayout actions = new HorizontalLayout(rename, delete);
            actions.setWidthFull();
            listRows.add(open, renamedName, actions);
        }
    }

    private void show(dev.casteels.plukk.shared.notification.Notification notification) {
        Notification.show(notification.issues().getFirst().message(), 5000, Notification.Position.BOTTOM_START);
    }
}
