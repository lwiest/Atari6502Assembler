package org.lw.atari6502assembler.exception.expression;

import org.lw.atari6502assembler.exception.ExpressionException;

public class InvalidExpressionException extends ExpressionException {

	public InvalidExpressionException(String expression) {
		super("Invalid expression: " + expression);
	}
}