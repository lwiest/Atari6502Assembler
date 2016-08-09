package de.lorenzwiest.atari6502assembler.exception.assembler;

import de.lorenzwiest.atari6502assembler.exception.AssemblerException;

public class MissingSymbolException extends AssemblerException {

	public MissingSymbolException() {
		super("Missig symbol");
	}
}
