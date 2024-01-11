package com.sarabrandserver.category.projection;

// Using spring data projection
public interface CategoryPojo {

    Long getId();
    String getName();

    /**
     * Do not return this value instead return
     * {@code statusImpl} as it returns the appropriate
     * type.
     * */
    Object getStatus();
    Long getParent();

    /**
     * Since {@code CategoryRepository} interface contains jpa
     * and native sql queries, {@code statusImpl} helps catch
     * cases where jpa returns a boolean variable but native
     * sql query returns 0 or 1.
     *
     * @return {@code Boolean}
     * */
    default Boolean statusImpl() {
        return switch (getStatus()) {
            case Number n -> n.intValue() == 1;
            case Boolean b -> b;
            default -> false;
        };
    }

}
