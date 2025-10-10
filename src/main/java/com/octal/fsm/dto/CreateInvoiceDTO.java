package com.octal.fsm.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateInvoiceDTO {

    private Invoice Invoice;
    private String time;

    // Getters and Setters
    public Invoice getInvoice() {
        return Invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.Invoice = invoice;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Data
    public static class Invoice {
        private boolean AllowIPNPayment;
        private boolean AllowOnlinePayment;
        private boolean AllowOnlineCreditCardPayment;
        private boolean AllowOnlineACHPayment;
        private String domain;
        private boolean sparse;
        private String Id;
        private String SyncToken;
        private MetaData MetaData;
        private List<Object> CustomField;
        private String DocNumber;
        private String TxnDate;
        private CurrencyRef CurrencyRef;
        private List<Object> LinkedTxn;
        private List<Line> Line;
        private TxnTaxDetail TxnTaxDetail;
        private CustomerRef CustomerRef;
        private BillAddr BillAddr;
        private ShipAddr ShipAddr;
        private boolean FreeFormAddress;
        private ShipFromAddr ShipFromAddr;
        private String DueDate;
        private double TotalAmt;
        private boolean ApplyTaxAfterDiscount;
        private String PrintStatus;
        private String EmailStatus;
        private double Balance;

        // Getters and Setters
        // (Generate using IDE or manually)
    }

    @Data
    public static class MetaData {
        private String CreateTime;
        private LastModifiedByRef LastModifiedByRef;
        private String LastUpdatedTime;

        // Getters and Setters
    }

    @Data
    public static class LastModifiedByRef {
        private String value;

        // Getters and Setters
    }

    public static class CurrencyRef {
        private String value;
        private String name;

        // Getters and Setters
    }

    public static class Line {
        private String Id;
        private int LineNum;
        private double Amount;
        private String DetailType;
        private SalesItemLineDetail SalesItemLineDetail;
        private SubTotalLineDetail SubTotalLineDetail;
        private List<Object> CustomExtensions;

        // Getters and Setters
    }

    public static class SalesItemLineDetail {
        private ItemRef ItemRef;
        private ItemAccountRef ItemAccountRef;
        private TaxCodeRef TaxCodeRef;

        // Getters and Setters
    }

    public static class ItemRef {
        private String value;
        private String name;

        // Getters and Setters
    }

    public static class ItemAccountRef {
        private String value;
        private String name;

        // Getters and Setters
    }

    public static class TaxCodeRef {
        private String value;

        // Getters and Setters
    }

    public static class SubTotalLineDetail {
        // Empty for now
    }

    public static class TxnTaxDetail {
        private double TotalTax;

        // Getters and Setters
    }

    public static class CustomerRef {
        private String value;
        private String name;

        // Getters and Setters
    }

    public static class BillAddr {
        private String Id;
        private String Line1;
        private String City;
        private String CountrySubDivisionCode;
        private String PostalCode;
        private String Lat;
        private String Long;

        // Getters and Setters
    }

    public static class ShipAddr {
        private String Id;
        private String Line1;
        private String City;
        private String CountrySubDivisionCode;
        private String PostalCode;
        private String Lat;
        private String Long;

        // Getters and Setters
    }

    public static class ShipFromAddr {
        private String Id;
        private String Line1;
        private String Line2;

        // Getters and Setters
    }
}
