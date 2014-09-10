package com.hundsun.jresplus.web.cache;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PageHeader<T extends Serializable> implements Serializable {
	private static final long serialVersionUID = 1L;
	private final String name;
	private final T value;
	private final Type type;

	public PageHeader(String name, T value) {
		if (name == null) {
			throw new IllegalArgumentException(
					"Header cannnot have a null name");
		}
		if (value == null) {
			throw new IllegalArgumentException(
					"Header cannnot have a null value");
		}
		this.name = name;
		this.value = value;
		this.type = Type.determineType(value.getClass());
	}

	public String getName() {
		return this.name;
	}

	public T getValue() {
		return this.value;
	}

	public Type getType() {
		return this.type;
	}

	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = 31 * result + (this.name == null ? 0 : this.name.hashCode());
		result = 31 * result + (this.type == null ? 0 : this.type.hashCode());
		result = 31 * result + (this.value == null ? 0 : this.value.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		PageHeader other = (PageHeader) obj;
		if (this.name == null) {
			if (other.name != null)
				return false;
		} else if (!this.name.equals(other.name)) {
			return false;
		}
		if (this.type == null) {
			if (other.type != null)
				return false;
		} else if (!this.type.equals(other.type)) {
			return false;
		}
		if (this.value == null) {
			if (other.value != null)
				return false;
		} else if (!this.value.equals(other.value)) {
			return false;
		}
		return true;
	}

	public String toString() {
		return "Header<" + this.type.getTypeClass().getSimpleName()
				+ "> [name=" + this.name + ", value=" + this.value + "]";
	}

	public static enum Type {
		STRING(String.class),

		DATE(Long.class),

		INT(Integer.class);

		private static final Map<Class<? extends Serializable>, Type> TYPE_LOOKUP;
		private final Class<? extends Serializable> type;

		private Type(Class<? extends Serializable> type) {
			this.type = type;
		}

		public Class<? extends Serializable> getTypeClass() {
			return this.type;
		}

		public static Type determineType(Class<? extends Serializable> typeClass) {
			Type lookupType = (Type) TYPE_LOOKUP.get(typeClass);
			if (lookupType != null) {
				return lookupType;
			}

			for (Type t : values()) {
				if (typeClass == t.getTypeClass()) {
					TYPE_LOOKUP.put(typeClass, t);
					return t;
				}

				if (typeClass.isAssignableFrom(t.getTypeClass())) {
					return t;
				}
			}

			throw new IllegalArgumentException("No Type for class " + typeClass);
		}

		static {
			TYPE_LOOKUP = new ConcurrentHashMap();
		}
	}
}
