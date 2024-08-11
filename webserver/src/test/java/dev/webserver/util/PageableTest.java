package dev.webserver.util;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

final class PageableTest {

    private record Obj(String str, int count) {}

    private static final Function<Integer, List<Obj>> list = (count) -> IntStream
            .range(0, count)
            .mapToObj(i -> new Obj("index-" + i, i))
            .toList();

    @Test
    void shouldTestPaginationLogic() {
        // given
        final var list = PageableTest.list.apply(10);

        // class to test
        final var page = new Pageable<>(Page.of(0, 10), list.size(), list);

        // assert
        assertThat(page.hasNextPage()).isFalse();
        assertThat(page.hasPreviousPage()).isFalse();
        assertThat(page.totalPages()).isEqualTo(1);
        assertThat(page.isEmpty()).isFalse();
        assertThat(page.totalElements()).isEqualTo(10);
        assertThat(page.numberOfElements()).isEqualTo(10);
    }

    @Test
    void shouldTestPaginationLogicMultiplePages() {
        // given
        final var list = PageableTest.list.apply(20);

        // assert
        assertThat(new Pageable<>(Page.of(0, 5), list.size(), list).totalPages()).isEqualTo(4);
        assertThat(new Pageable<>(Page.of(0, 3), list.size(), list).totalPages()).isEqualTo(7);
        assertThat(new Pageable<>(Page.of(0, 8), list.size(), list).totalPages()).isEqualTo(3);
        assertThat(new Pageable<>(Page.of(2, 8), list.size(), list).hasNextPage()).isTrue();
        assertThat(new Pageable<>(Page.of(0, 8), list.size(), list).hasNextPage()).isTrue();
    }

}