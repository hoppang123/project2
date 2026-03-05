package com.company.asset.domain.approval;

import com.company.asset.domain.rental.RentalRequest;
import com.company.asset.domain.user.Role;

public class ApprovalStepFactory {
    public static ApprovalStep create(RentalRequest request, int stepNo, Role role) {
        return new ApprovalStep(request, stepNo, role);
    }
}
