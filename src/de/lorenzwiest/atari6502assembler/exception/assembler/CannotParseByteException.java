package de.lorenzwiest.atari6502assembler.exception.assembler;

import de.lorenzwiest.atari6502assembler.exception.AssemblerException;

public class CannotParseByteException extends AssemblerException {

	public CannotParseByteException(String arg) {
		super("Cannot parse .BYTE directive with argument(s) \"" + arg + "\"");
	}
}
