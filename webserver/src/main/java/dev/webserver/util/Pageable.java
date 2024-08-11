package dev.webserver.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
public final class Pageable <T> {
    private final int page;
    private final int size;
    private final int totalPages;
    private final int totalElements;
    private final int numberOfElements;
    private final boolean hasPreviousPage;
    private final boolean hasNextPage;
    private final boolean isEmpty;
    private final List<T> data;

    // Default constructor for JSON mapping or testing
    public Pageable() {
        page = 0;
        size = 0;
        totalPages = 0;
        totalElements = 0;
        numberOfElements = 0;
        hasPreviousPage = false;
        hasNextPage = false;
        isEmpty = true;
        data = new ArrayList<>();
    }

    /**
     * Creates a new {@link Pageable} instance.
     *
     * @param obj The {@link Page} metadata object.
     * @param count The total number of elements in the entire dataset.
     * @param data The subset of data for the current page.
     */
    public Pageable (final Page obj, final int count, final List<T> data) {
        page = obj.page();
        size = obj.size();
        totalPages = Math.ceilDiv(count, Math.max(size, 1));
        totalElements = count;
        numberOfElements = data.size();
        hasPreviousPage = page > 0;
        hasNextPage = Math.max(page, 1) < totalPages;
        isEmpty = data.isEmpty();
        this.data = data;
    }

    /**
     * The page number of the current subset of data. For example,
     * if there are 100 items in total and the UI displays 10 items per page,
     * then the page number represents which page of data is being retrieved.
     */
    @JsonProperty("page")
    public int page() {
        return page;
    }

    /**
     * The number of items per page. Indicates how many items are displayed
     * in the UI per page.
     */
    @JsonProperty("size")
    public int size() {
        return size;
    }

    /**
     * The total number of pages available based on the given page size and total elements.
     */
    @JsonProperty("total_pages")
    public int totalPages() {
        return totalPages;
    }

    /**
     * The total number of elements in the entire dataset, not just the current page.
     */
    @JsonProperty("total_elements")
    public int totalElements() {
        return totalElements;
    }

    /**
     * The number of elements in the current subset of data.
     */
    @JsonProperty("number_of_elements")
    public int numberOfElements() {
        return numberOfElements;
    }

    /**
     * Indicates whether there is a previous page of data.
     */
    @JsonProperty("has_previous_page")
    public boolean hasPreviousPage() {
        return hasPreviousPage;
    }

    /**
     * Indicates whether there is a next page of data.
     */
    @JsonProperty("has_next_page")
    public boolean hasNextPage() {
        return hasNextPage;
    }

    /**
     * Indicates whether the current page of data is empty.
     */
    @JsonProperty("is_empty")
    public boolean isEmpty() {
        return isEmpty;
    }

    /**
     * The subset of data for the current page.
     */
    @JsonProperty("data")
    public List<T> data() {
        return data;
    }
}
