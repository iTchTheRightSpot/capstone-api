package dev.webserver.util;

import java.io.Serializable;

public final class Page implements Serializable {
    private final int page;
    private final int size;
    private final int offset;

    /**
     * Creates an instance of the {@link Page} class.
     *
     * @param page The page number of the current subset of data.
     * @param size The number of items per page.
     */
    private Page(final int page, final int size) {
        this.page = page;
        this.size = size;
        offset = page == 0 ? 0 : page * size;
    }

    /**
     * Creates a new instance of {@link Page} with the specified page number and page size.
     * <p>
     * Note: For example, if there are 25 items in a dataset, and we want to retrieve the first
     * 13 items, specifying page 0 signifies the start of the page for the first 13 items.
     *
     * @param page The page number. Should be greater than or equal to 0.
     * @param size The page size. Should be greater than 0.
     * @return A new {@link Page} instance representing the requested page.
     */
    public static Page of(final int page, final int size) {
        return new Page(page, size);
    }

    /**
     * The page number of the current subset of data.
     * Indicates which page of data is being retrieved.
     */
    public int page() {
        return page;
    }

    /**
     * The number of items per page. Indicates how many
     * items are displayed in the UI per page.
     */
    public int size() {
        return size;
    }

    /**
     * Just like sql function 'OFFSET' this represents the number of items
     * to skip before retrieving designated data. This property is calculated
     * based on the current page and page size during initialization. For
     * example, if the current page is 2 and the page size is 10, the skip
     * value will be 10, indicating that the first 10 items should be skipped.
     */
    public int offset() {
        return offset;
    }
}
