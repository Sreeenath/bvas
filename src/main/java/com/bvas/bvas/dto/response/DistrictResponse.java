package com.bvas.bvas.dto.response;

import lombok.Data;

@Data
public class DistrictResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Boolean isActive;
}