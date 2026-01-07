package com.nexsol.tpa.core.support;

public record SortPage(int page, int size, Sort sort) {

    // 정렬 개념 (필드명 + 방향)
    public record Sort(String property, Direction direction) {
        public static Sort of(String property, Direction direction) {
            return new Sort(property, direction);
        }
    }

    // 방향 개념 (SortPage 내부로 이동)
    public enum Direction {

        ASC, DESC;

        public boolean isAscending() {
            return this == ASC;
        }

    }

    public SortPage {
        if (page < 0)
            page = 0;
        if (size < 1)
            size = 10;
    }

    public long offset() {
        return (long) page * size;
    }

    // 정렬 없이 페이징만 요청할 때
    public static SortPage of(int page, int size) {
        return new SortPage(page, size, null);
    }

    // 정렬 포함 요청
    public static SortPage of(int page, int size, String property, Direction direction) {
        return new SortPage(page, size, new Sort(property, direction));
    }
}