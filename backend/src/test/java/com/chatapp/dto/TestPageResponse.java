package com.chatapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * Test-specific Page implementation that can be properly serialized to JSON
 */
public class TestPageResponse<T> implements Page<T> {
    private final List<T> content;
    @JsonIgnore
    private final Pageable pageable;
    private final long total;

    public TestPageResponse(List<T> content, Pageable pageable, long total) {
        this.content = content;
        this.pageable = pageable;
        this.total = total;
    }

    public TestPageResponse(List<T> content) {
        this.content = content;
        this.pageable = Pageable.unpaged();
        this.total = content.size();
    }

    @Override
    @JsonProperty("content")
    public List<T> getContent() {
        return content;
    }

    @Override
    @JsonProperty("totalElements")
    public long getTotalElements() {
        return total;
    }

    @Override
    @JsonProperty("totalPages")
    public int getTotalPages() {
        return getSize() == 0 ? 1 : (int) Math.ceil((double) total / (double) getSize());
    }

    @Override
    @JsonProperty("number")
    public int getNumber() {
        return pageable.getPageNumber();
    }

    @Override
    @JsonProperty("size")
    public int getSize() {
        return pageable.getPageSize();
    }

    @Override
    @JsonProperty("numberOfElements")
    public int getNumberOfElements() {
        return content.size();
    }

    @Override
    @JsonProperty("first")
    public boolean isFirst() {
        return !hasPrevious();
    }

    @Override
    @JsonProperty("last")
    public boolean isLast() {
        return !hasNext();
    }

    @Override
    public boolean hasNext() {
        return getNumber() + 1 < getTotalPages();
    }

    @Override
    public boolean hasPrevious() {
        return getNumber() > 0;
    }

    @Override
    @JsonIgnore
    public Pageable nextPageable() {
        return hasNext() ? pageable.next() : Pageable.unpaged();
    }

    @Override
    @JsonIgnore
    public Pageable previousPageable() {
        return hasPrevious() ? pageable.previousOrFirst() : Pageable.unpaged();
    }

    @Override
    public Iterator<T> iterator() {
        return content.iterator();
    }

    @Override
    public <U> Page<U> map(Function<? super T, ? extends U> converter) {
        List<U> convertedContent = getContent().stream().map(converter).collect(java.util.stream.Collectors.toList());
        return new TestPageResponse<>(convertedContent, pageable, total);
    }

    @Override
    public boolean hasContent() {
        return !isEmpty();
    }

    @Override
    @JsonProperty("empty")
    public boolean isEmpty() {
        return content.isEmpty();
    }

    @Override
    @JsonIgnore
    public Sort getSort() {
        return pageable.getSort();
    }

    @Override
    @JsonIgnore
    public Pageable getPageable() {
        return pageable;
    }
}