package org.lw.atari6502assembler.exception.assembler;

import org.lw.atari6502assembler.exception.AssemblerException;

public class IllegalSymbolNameException extends AssemblerException {

	public IllegalSymbolNameException(String symbolName) {
		super("Illegal symbol name \"" + symbolName + "\"");
	}
}
