package com.nexsol.tpa.core.support;

import java.util.Collections;
import java.util.List;

public class SliceResult<T> {

	private final List<T> content;

	private final boolean hasNext;

	public SliceResult(List<T> content, boolean hasNext) {
		this.content = (content != null) ? content : Collections.emptyList();
		this.hasNext = hasNext;
	}

	public List<T> getContent() {
		return content;
	}

	public boolean hasNext() {
		return hasNext;
	}

}