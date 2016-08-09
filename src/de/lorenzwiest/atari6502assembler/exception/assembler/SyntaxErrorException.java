package de.lorenzwiest.atari6502assembler.exception.assembler;

import de.lorenzwiest.atari6502assembler.exception.AssemblerException;

public class SyntaxErrorException extends AssemblerException {

	public SyntaxErrorException() {
		super("Syntax error");
	}
}
