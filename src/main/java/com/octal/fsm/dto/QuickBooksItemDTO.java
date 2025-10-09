package com.octal.fsm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuickBooksItemDTO {

    private String name;

    @JsonProperty("IncomeAccountRef")
    private AccountRef incomeAccountRef;

    @JsonProperty("ExpenseAccountRef")
    private AccountRef expenseAccountRef;

    @JsonProperty("AssetAccountRef")
    private AccountRef assetAccountRef;

    private String type;

    private boolean trackQtyOnHand;

    private int qtyOnHand;

    private String invStartDate; // Can use LocalDate with custom serializer if needed

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountRef {
        private String value;
        private String name;
    }
}
