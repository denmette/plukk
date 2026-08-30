package dev.casteels.plukk.identity;

import java.util.Optional;

public interface HouseholdMemberAccess {

    Optional<ActiveHouseholdMember> currentMember();

    record ActiveHouseholdMember(Long householdId, Long memberId, String subject, String displayName) {}
}
