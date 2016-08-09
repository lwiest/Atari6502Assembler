package org.lw.atari6502assembler.exception.assembler;

import org.lw.atari6502assembler.exception.AssemblerException;

public class SyntaxErrorException extends AssemblerException {

	public SyntaxErrorException() {
		super("Syntax error");
	}
}
