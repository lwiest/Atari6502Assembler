package de.lorenzwiest.atari6502assembler.exception.assembler;

import de.lorenzwiest.atari6502assembler.exception.AssemblerException;

public class IllegalInstructionException extends AssemblerException {

	public IllegalInstructionException(String op) {
		super("Illegal instruction \"" + op + "\"");
	}
}
