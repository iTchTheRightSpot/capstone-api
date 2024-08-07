package dev.webserver;

import java.util.List;
import java.util.stream.StreamSupport;

final public class TestUtility {

    public static <T> List<T> toList(final Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false).toList();
    }

}
