package org.lw.atari6502assembler.exception.assembler;

import org.lw.atari6502assembler.exception.AssemblerException;

public class MissingSymbolException extends AssemblerException {

	public MissingSymbolException() {
		super("Missig symbol");
	}
}
