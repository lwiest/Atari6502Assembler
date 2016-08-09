package de.lorenzwiest.atari6502assembler.exception.assembler;

import de.lorenzwiest.atari6502assembler.exception.AssemblerException;

public class BranchRangeException extends AssemblerException {

	public BranchRangeException(int relAddress) {
		super("Branch range to large: " + relAddress + " (must be in -128..127)");
	}
}
