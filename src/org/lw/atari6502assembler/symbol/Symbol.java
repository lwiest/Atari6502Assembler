package org.lw.atari6502assembler.symbol;

public class Symbol {

	private String name;
	private int value;
	private boolean hasValue;
	private SymbolType type;
	private boolean isReferenced;

	public static boolean isSymbolName(String name) {
		char firstChar = name.charAt(0);
		return Character.isUpperCase(firstChar) || isLocalSymbolName(name);
	}

	public static boolean isLocalSymbolName(String name) {
		char firstChar = name.charAt(0);
		return firstChar == '?' || firstChar == ':';
	}

	private Symbol(String name, int value, boolean hasValue, SymbolType type) {
		this.name = name;
		this.value = value;
		this.hasValue = hasValue;
		this.type = type;
		this.isReferenced = false;
	}

	public Symbol(String name, SymbolType type) {
		this(name, -1, false, type);
	}

	public Symbol(String name, int value, SymbolType type) {
		this(name, value, true, type);
	}

	public String getName() {
		return this.name;
	}

	public void setValue(int value) {
		this.value = value;
		this.hasValue = true;
	}

	public boolean hasValue() {
		return this.hasValue;
	}

	public int getValue() {
		return this.value;
	}

	public SymbolType getType() {
		return this.type;
	}

	public void setReferenced(boolean isReferenced) {
		this.isReferenced = isReferenced;
	}

	public boolean isReferenced() {
		return this.isReferenced;
	}
}
