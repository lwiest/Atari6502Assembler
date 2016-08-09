package de.lorenzwiest.atari6502assembler.exception.expression;

import de.lorenzwiest.atari6502assembler.exception.ExpressionException;

public class InvalidExpressionException extends ExpressionException {

	public InvalidExpressionException(String expression) {
		super("Invalid expression: " + expression);
	}
}