package org.lw.atari6502assembler.exception.expression;

import org.lw.atari6502assembler.exception.ExpressionException;

public class SymbolHasNoValueException extends ExpressionException {

	public SymbolHasNoValueException(String symbolName) {
		super("Symbol \"" + symbolName + "\" has no value");
	}
}