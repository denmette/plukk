package dev.casteels.plukk.architecture;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import dev.casteels.plukk.shopping.input.AddShoppingNeedUseCase;
import dev.casteels.plukk.shopping.input.CreateCustomProductAndAddShoppingNeedUseCase;
import dev.casteels.plukk.shopping.input.FindCatalogProductNameUseCase;
import dev.casteels.plukk.shopping.input.FindShoppingCategoriesUseCase;
import dev.casteels.plukk.shopping.item.GetShoppingListSectionsUseCase;
import dev.casteels.plukk.shopping.item.PurchaseShoppingItemUseCase;
import dev.casteels.plukk.shopping.item.RemoveShoppingItemUseCase;
import dev.casteels.plukk.shopping.item.RestoreShoppingItemUseCase;
import dev.casteels.plukk.shopping.list.CreateShoppingListUseCase;
import dev.casteels.plukk.shopping.list.DeleteShoppingListUseCase;
import dev.casteels.plukk.shopping.list.FindShoppingListsUseCase;
import dev.casteels.plukk.shopping.list.OpenShoppingListUseCase;
import dev.casteels.plukk.shopping.list.RenameShoppingListUseCase;

class UseCaseArchitectureTest {

    @Test
    void givenApplicationUseCases_whenInspectingPublicBehavior_thenEachExposesOnlyExecute() {
        List<Class<?>> useCases = List.of(
                AddShoppingNeedUseCase.class,
                CreateCustomProductAndAddShoppingNeedUseCase.class,
                FindCatalogProductNameUseCase.class,
                FindShoppingCategoriesUseCase.class,
                GetShoppingListSectionsUseCase.class,
                PurchaseShoppingItemUseCase.class,
                RestoreShoppingItemUseCase.class,
                RemoveShoppingItemUseCase.class,
                CreateShoppingListUseCase.class,
                DeleteShoppingListUseCase.class,
                FindShoppingListsUseCase.class,
                OpenShoppingListUseCase.class,
                RenameShoppingListUseCase.class);

        assertThat(useCases).allSatisfy(useCase -> assertThat(List.of(useCase.getDeclaredMethods()).stream()
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> !method.isSynthetic())
                .map(Method::getName))
                .containsExactly("execute"));
    }
}
