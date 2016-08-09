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

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeMap;

import de.lorenzwiest.atari6502assembler.exception.AssemblerException;
import de.lorenzwiest.atari6502assembler.exception.assembler.DuplicateSymbolException;
import de.lorenzwiest.atari6502assembler.exception.assembler.IllegalSymbolNameException;

public class SymbolTable {

	private TreeMap<String, Symbol> symbolTable = new TreeMap<String, Symbol>();

	public void put(String name, SymbolType type) throws AssemblerException {
		putInternal(name, -1, false, type);
	}

	public void put(String name, int value, SymbolType type) throws AssemblerException {
		putInternal(name, value, true, type);
	}

	private void putInternal(String name, int value, boolean hasValue, SymbolType type) throws AssemblerException {
		name = name.toUpperCase();
		if (this.symbolTable.containsKey(name)) {
			throw new DuplicateSymbolException(name);
		}
		if (Symbol.isSymbolName(name)) {
			if (hasValue) {
				this.symbolTable.put(name, new Symbol(name, value, type));
			} else {
				this.symbolTable.put(name, new Symbol(name, type));
			}
		} else {
			throw new IllegalSymbolNameException(name);
		}
	}

	public boolean contains(String name) {
		return this.symbolTable.containsKey(name);
	}

	public Symbol get(String name) {
		return this.symbolTable.get(name);
	}

	public Set<String> keys() {
		return this.symbolTable.keySet();
	}

	public Symbol[] symbolsSortedByValue() {
		Collection<Symbol> values = this.symbolTable.values();
		Symbol[] array = values.toArray(new Symbol[values.size()]);
		Arrays.sort(array, new Comparator<Symbol>() {
			@Override
			public int compare(Symbol symbol1, Symbol symbol2) {
				return symbol1.getValue() - symbol2.getValue();
			}
		});
		return array;
	}
}
