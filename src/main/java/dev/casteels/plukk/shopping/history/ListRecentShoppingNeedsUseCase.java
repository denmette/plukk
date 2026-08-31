package dev.casteels.plukk.shopping.history;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import dev.casteels.plukk.household.api.AuthorizedHouseholdUser;
import dev.casteels.plukk.shared.notification.Notification;

/**
 * Lists the household's most recently purchased concrete needs for quick re-addition.
 */
@Component
public class ListRecentShoppingNeedsUseCase {

    private final AuthorizedHouseholdUser authUser;
    private final ShoppingHistoryRepository repository;

    ListRecentShoppingNeedsUseCase(AuthorizedHouseholdUser authUser, ShoppingHistoryRepository repository) {
        this.authUser = authUser;
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Result execute() {
        var user = authUser.currentUser();
        if (user.isEmpty()) {
            return new Result(List.of(), Notification.issue("identity.unauthorized", "Active membership required."));
        }
        return new Result(repository.findRecent(user.get().householdId()), Notification.success());
    }

    public record Result(List<ShoppingHistoryRepository.RecentShoppingNeed> needs, Notification notification) {}
}
