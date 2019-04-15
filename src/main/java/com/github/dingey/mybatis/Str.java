package com.github.dingey.mybatis;

class Str {
	private String s;

	private Str(String s) {
		this.s = s;
	}

	public static Str of(String s) {
		return new Str(s);
	}

	public Str camelCase() {
		s = StringUtil.camelCase(s);
		return this;
	}

	public Str firstLower() {
		s = StringUtil.firstLower(s);
		return this;
	}

	public Str firstUpper() {
		s = StringUtil.firstUpper(s);
		return this;
	}

	public Str escape() {
		s = StringUtil.escape(s);
		return this;
	}

	@Override
	public String toString() {
		return s;
	}
}
