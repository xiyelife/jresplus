package com.hundsun.jresplus.web.contain.pipeline;

import java.util.Comparator;

public class PipelineTaskComparator implements Comparator<PipelineTask> {

	public int compare(PipelineTask taskOne, PipelineTask taskTwo) {
		int order1 = taskOne.getOrder();
		int order2 = taskTwo.getOrder();
		if (order1 == order2) {
			return 0;
		}
		if (order1 > order2) {
			return 1;
		}
		return -1;
	}

}
