package com.octal.fsm.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Naveen
 * PageItem - contains pagination information like total number of pages, total data etc
 */
@Data
public class PageItem<T> {
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final int totalPages;
    private final long totalItems;
    private final List<T> items;
    private final int pageNumber;
    private final int pageSize;

    public PageItem(int totalPage, long itemCount, List<T> items, int pageNumber, int pageSize) {
        this.totalPages = totalPage;
        this.totalItems = itemCount;
        this.items = new ArrayList<>(items);
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }

    public PageItem() {
        this.totalPages = 0;
        this.totalItems = 0;
        this.items = new ArrayList<>();
        this.pageNumber = 0;
        this.pageSize = DEFAULT_PAGE_SIZE;
    }

    @Override
    public String toString() {
        return "PageItem [totalPages=" + totalPages + ", totalItems=" + totalItems + ", items=" + items + ", pageNumber=" + pageNumber + ", pageSize=" + pageSize + "]";
    }
}
