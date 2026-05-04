package io.github.kkusylabs.useradmin.backend.dtos.common;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Generic paginated response.
 *
 * <p>Wraps a page of data along with pagination metadata. Typically created
 * from a Spring {@link Page} and returned by list endpoints.</p>
 *
 * @param content page content
 * @param pageNumber zero-based page index
 * @param pageSize size of the page
 * @param totalElements total number of elements across all pages
 * @param totalPages total number of pages
 * @param first whether this is the first page
 * @param last whether this is the last page
 * @param <T> type of content
 */
public record PagedResponse<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    /**
     * Creates a {@link PagedResponse} from a Spring {@link Page}.
     *
     * @param page source page
     * @param <T> element type
     * @return a response containing page content and metadata
     */
    public static <T> PagedResponse<T> from(Page<T> page) {
        return new PagedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}