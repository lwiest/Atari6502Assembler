package de.lorenzwiest.atari6502assembler.exception.assembler;

import de.lorenzwiest.atari6502assembler.exception.AssemblerException;

public class NoOriginException extends AssemblerException {

	public NoOriginException() {
		super("No origin");
	}
}
