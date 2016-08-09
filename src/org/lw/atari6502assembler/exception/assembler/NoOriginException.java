package org.lw.atari6502assembler.exception.assembler;

import org.lw.atari6502assembler.exception.AssemblerException;

public class NoOriginException extends AssemblerException {

	public NoOriginException() {
		super("No origin");
	}
}
