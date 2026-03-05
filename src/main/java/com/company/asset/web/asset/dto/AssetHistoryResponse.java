package com.company.asset.web.asset.dto;

import com.company.asset.domain.asset.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AssetHistoryResponse {
    private Long id;
    private AssetAction action;
    private AssetStatus beforeStatus;
    private AssetStatus afterStatus;
    private String actorName;
    private String note;
    private LocalDateTime createdAt;
}
