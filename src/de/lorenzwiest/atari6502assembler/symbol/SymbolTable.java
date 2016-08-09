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
