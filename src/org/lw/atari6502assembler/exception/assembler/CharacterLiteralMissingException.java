package org.lw.atari6502assembler.exception.assembler;

import org.lw.atari6502assembler.exception.AssemblerException;

public class CharacterLiteralMissingException extends AssemblerException {

	public CharacterLiteralMissingException() {
		super("Character literal missing");
	}
}
