package org.lw.atari6502assembler.exception.assembler;

import org.lw.atari6502assembler.exception.AssemblerException;

public class DuplicateSymbolException extends AssemblerException {

	public DuplicateSymbolException(String symbolName) {
		super("Duplicate symbol \"" + symbolName + "\"");
	}
}
