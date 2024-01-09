package com.sarabrandserver.category.projection;

// Using spring data projection
public interface CategoryPojo {

    Long getId();
    String getName();
    Object getStatus();
    Long getParent();

    /**
     * Since {@code CategoryRepository} interface contains a native
     * query method and this method returns a boolean column and
     * mysql stores boolean values in 0 or 1, this method converts
     * to boolean values.
     *
     * @return {@code Boolean}
     * */
    default Boolean statusImpl() {
        Number status = (Number) getStatus();
        return status.intValue() == 1;
    }

}
