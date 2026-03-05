package com.company.asset.web.extension.dto;

import com.company.asset.domain.rental.ExtensionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ExtensionResponse {
    private Long id;
    private Long rentalId;
    private String requesterEmail;
    private LocalDate requestedEndDate;
    private ExtensionStatus status;
    private String reason;
    private String adminNote;
    private LocalDateTime createdAt;
    private LocalDateTime actedAt;
}
