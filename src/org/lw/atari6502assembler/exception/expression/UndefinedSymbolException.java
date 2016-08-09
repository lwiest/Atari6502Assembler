package org.lw.atari6502assembler.exception.expression;

import org.lw.atari6502assembler.exception.ExpressionException;

public class UndefinedSymbolException extends ExpressionException {

	public UndefinedSymbolException(String symbolName) {
		super("Undefined symbol \"" + symbolName + "\"");
	}
}