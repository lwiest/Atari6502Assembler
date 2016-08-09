package de.lorenzwiest.atari6502assembler.exception.expression;

import de.lorenzwiest.atari6502assembler.exception.ExpressionException;

public class DivisionByZeroException extends ExpressionException {

	public DivisionByZeroException() {
		super("Division by zero");
	}
}