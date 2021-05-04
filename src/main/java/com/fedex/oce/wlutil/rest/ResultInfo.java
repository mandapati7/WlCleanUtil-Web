package com.fedex.oce.wlutil.rest;

import com.fedex.oce.wlutil.rest.ExecutionEnum.ExecutionCd;

public class ResultInfo {

	private ExecutionCd executionCd;

	private String message;

	public ResultInfo(ExecutionCd executionCd, String message) {
		this.executionCd = executionCd;
		this.message = message;
	}

	public ExecutionCd getExecutionCd() {
		return executionCd;
	}

	public String getMessage() {
		return message;
	}

}
