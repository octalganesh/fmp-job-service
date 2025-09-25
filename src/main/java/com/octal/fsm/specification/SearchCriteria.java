package com.octal.fsm.specification;

import java.util.List;

public class SearchCriteria {

    private String key;
    private SearchOperation searchOperation;
    private boolean isOrOperation;
    private List<Object> arguments;
    private String reference;


    public SearchCriteria() {
    }

    public SearchCriteria(String reference, String key, SearchOperation searchOperation, boolean isOrOperation, List<Object> arguments) {
        this.key = key;
        this.searchOperation = searchOperation;
        this.isOrOperation = isOrOperation;
        this.arguments = arguments;
        this.reference = reference;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public SearchOperation getSearchOperation() {
        return searchOperation;
    }

    public void setSearchOperation(SearchOperation searchOperation) {
        this.searchOperation = searchOperation;
    }

    public boolean isOrOperation() {
        return isOrOperation;
    }

    public void setOrOperation(boolean orOperation) {
        isOrOperation = orOperation;
    }

    public List<Object> getArguments() {
        return arguments;
    }

    public void setArguments(List<Object> arguments) {
        this.arguments = arguments;
    }
}
