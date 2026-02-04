package com.octal.fsm.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AssemblyCreateRequest {

    @Data
    public static class AssemblyComponentAdd {
        private String name;
        private String incomeAccount;
        private String cogsAccount;
        private String assetAccount;
        private BigDecimal salesPrice;
        private String requestId;//uuid from job
        private List<AssemblyComponentDto> components;
    }

    @Data
    public static class AssemblyComponentDto {
        private String itemName;
        private BigDecimal quantity;
    }
}
