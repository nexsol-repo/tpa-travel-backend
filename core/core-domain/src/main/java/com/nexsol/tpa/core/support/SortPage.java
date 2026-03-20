package com.nexsol.tpa.core.support;

public record SortPage(Integer page, Integer size, String sortBy, // [Refactor] property
																	// -> sortBy (최상위로 이동)
		Direction direction // [Refactor] sort.direction -> direction (최상위로 이동)
) {

	public enum Direction {

		ASC, DESC;

		public boolean isAscending() {
			return this == ASC;
		}

	}

	public SortPage {
		// page가 null이거나 0보다 작으면 0으로 초기화
		if (page == null || page < 0) {
			page = 0;
		}
		// size가 null이거나 1보다 작으면 10으로 초기화
		if (size == null || size < 1) {
			size = 10;
		}
		// sortBy, direction은 null일 수 있음 (Repository에서 처리)
	}

	public long offset() {
		return (long) page * size;
	}
}