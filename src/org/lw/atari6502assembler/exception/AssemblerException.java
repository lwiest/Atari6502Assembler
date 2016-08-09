package org.lw.atari6502assembler.exception;

public class AssemblerException extends RuntimeException {

	private String errorMessage;

	public AssemblerException(String errorMessage) {
		super();
		this.errorMessage = errorMessage;
	}

	public AssemblerException(AssemblerException e, int lineNumber) {
		super();
		this.errorMessage = "Error in line " + lineNumber + ": " + e.getErrorMessage();
	}

	public String getErrorMessage() {
		return this.errorMessage;
	}
}
