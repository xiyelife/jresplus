package com.hundsun.jresplus.web.nosession;

public class StoreContext {

	private Object value;

	private boolean modified;

	public StoreContext(Object value) {
		this.value = value;
		this.modified = false;
	}

	public StoreContext(Object value, boolean modified) {
		this.value = value;
		this.modified = modified;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.modified = true;
		this.value = value;
	}

	public boolean isModified() {
		return modified;
	}

	public void setModified(boolean modified) {
		this.modified = modified;
	}

}
