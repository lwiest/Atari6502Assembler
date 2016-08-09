package de.lorenzwiest.atari6502assembler.exception.expression;

import de.lorenzwiest.atari6502assembler.exception.ExpressionException;

public class UndefinedSymbolException extends ExpressionException {

	public UndefinedSymbolException(String symbolName) {
		super("Undefined symbol \"" + symbolName + "\"");
	}
}