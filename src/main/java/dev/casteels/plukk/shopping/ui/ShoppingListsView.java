package dev.casteels.plukk.shopping.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import dev.casteels.plukk.shopping.list.ShoppingList;
import dev.casteels.plukk.shopping.list.ShoppingListApplicationService;
import jakarta.annotation.security.PermitAll;

@Route("lists")
@PageTitle("Shopping lists | Plukk")
@PermitAll
public class ShoppingListsView extends VerticalLayout {

    private final ShoppingListApplicationService lists;
    private final VerticalLayout listRows = new VerticalLayout();

    public ShoppingListsView(ShoppingListApplicationService lists) {
        this.lists = lists;
        setPadding(true);
        setWidthFull();
        add(new H1("Shopping lists"));

        TextField name = new TextField("New list");
        name.setRequired(true);
        name.setWidthFull();
        Button create = new Button("Create list", event -> {
            if (name.isEmpty() || name.getValue().isBlank()) {
                name.setInvalid(true);
                return;
            }
            lists.create(name.getValue());
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
        for (ShoppingList list : lists.lists()) {
            Button open = new Button(list.name(), event -> getUI().ifPresent(ui -> ui.navigate("lists/" + list.id())));
            open.setWidthFull();
            open.getStyle().set("min-height", "48px");

            TextField renamedName = new TextField("Rename " + list.name());
            renamedName.setValue(list.name());
            renamedName.setRequired(true);
            renamedName.setValueChangeMode(ValueChangeMode.EAGER);
            renamedName.setWidthFull();
            Button rename = new Button("Rename", event -> {
                if (renamedName.isEmpty() || renamedName.getValue().isBlank()) {
                    renamedName.setInvalid(true);
                    return;
                }
                lists.rename(list.id(), renamedName.getValue());
                refresh();
            });
            Button delete = new Button("Delete", event -> {
                lists.delete(list.id());
                refresh();
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
}
