package de.lorenzwiest.atari6502assembler.exception.assembler;

import de.lorenzwiest.atari6502assembler.exception.AssemblerException;

public class DuplicateSymbolException extends AssemblerException {

	public DuplicateSymbolException(String symbolName) {
		super("Duplicate symbol \"" + symbolName + "\"");
	}
}