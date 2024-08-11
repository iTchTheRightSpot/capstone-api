package dev.webserver;

import java.util.List;
import java.util.stream.StreamSupport;

public final class TestUtility {

    public static <T> List<T> toList(final Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false).toList();
    }

}
