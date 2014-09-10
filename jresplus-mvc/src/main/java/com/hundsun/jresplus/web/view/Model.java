package com.hundsun.jresplus.web.view;
/**
 * 参考模型对象，支持返回码、错误码、错误信息和数据的封装，用于规范化输出
 * @author XIE (xjj@hundsun.com)
 *
 */
public class Model {
	String retCode = "0";
	String errCode = "";
	String errInfo = "";
	Object data;

	public String getRetCode() {
		return retCode;
	}

	public void setRetCode(String retCode) {
		this.retCode = retCode;
	}

	public String getErrCode() {
		return errCode;
	}

	public void setErrCode(String errCode) {
		this.errCode = errCode;
	}

	public String getErrInfo() {
		return errInfo;
	}

	public void setErrInfo(String errInfo) {
		this.errInfo = errInfo;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

}
