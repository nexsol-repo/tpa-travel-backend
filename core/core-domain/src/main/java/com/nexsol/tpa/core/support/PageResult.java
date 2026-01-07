package com.nexsol.tpa.core.support;

import java.util.List;

public class PageResult<T> extends SliceResult<T> {

    private final long totalElements;

    private final int totalPages;

    private final int currentPage;

    public PageResult(List<T> content, long totalElements, int totalPages, int currentPage, boolean hasNext) {
        super(content, hasNext);
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    // 팩토리 메서드: 도메인 로직이나 테스트에서 쉽게 객체를 생성하기 위함
    public static <T> PageResult<T> of(List<T> content, long totalElements, int size, int currentPage) {
        int totalPages = (size == 0) ? 1 : (int) Math.ceil((double) totalElements / (double) size);
        boolean hasNext = currentPage + 1 < totalPages;
        return new PageResult<>(content, totalElements, totalPages, currentPage, hasNext);
    }

}