package org.lw.atari6502assembler.exception;

public class ExpressionException extends RuntimeException {

	private String errorMessage;

	public ExpressionException(String errorMessage) {
		super();
		this.errorMessage = errorMessage;
	}

	public ExpressionException(ExpressionException e, int lineNumber) {
		super();
		this.errorMessage = "Expression error in line " + lineNumber + ": " + e.getErrorMessage();
	}

	public String getErrorMessage() {
		return this.errorMessage;
	}
}