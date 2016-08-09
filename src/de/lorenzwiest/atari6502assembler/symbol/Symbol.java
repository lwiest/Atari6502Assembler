/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Lorenz Wiest
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package de.lorenzwiest.atari6502assembler.symbol;

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
