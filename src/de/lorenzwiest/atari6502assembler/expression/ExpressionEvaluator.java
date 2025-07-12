/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Lorenz Wiest
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package de.lorenzwiest.atari6502assembler.expression;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.lorenzwiest.atari6502assembler.exception.ExpressionException;
import de.lorenzwiest.atari6502assembler.exception.expression.DivisionByZeroException;
import de.lorenzwiest.atari6502assembler.exception.expression.InvalidExpressionException;
import de.lorenzwiest.atari6502assembler.exception.expression.SymbolHasNoValueException;
import de.lorenzwiest.atari6502assembler.exception.expression.UndefinedSymbolException;
import de.lorenzwiest.atari6502assembler.symbol.Symbol;
import de.lorenzwiest.atari6502assembler.symbol.SymbolTable;

public class ExpressionEvaluator {
	private static final String PLUS = "+";
	private static final String MINUS = "-";
	private static final String MULTIPLY = "*";
	private static final String DIVIDE = "/";
	private static final String AND = "&";
	private static final String OR = "!";
	private static final String XOR = "^";
	private static final String OPENING_BRACKET = "[";
	private static final String CLOSING_BRACKET = "]";
	private static final String HI_BYTE = ">";
	private static final String LO_BYTE = "<";

	private SymbolTable symbolTable;
	private String stringToEvaluate;
	private int pos;

	public ExpressionEvaluator(SymbolTable symbolTable) {
		this.symbolTable = symbolTable;
	}

	public int evaluate(String expression) throws ExpressionException {
		this.stringToEvaluate = expression.replaceAll("' ", "32").trim();
		this.pos = 0;

		Integer result = evaluateNumExpr();
		if (this.pos < this.stringToEvaluate.length()) {
			throw new InvalidExpressionException("Expression not completely parsed");
		}
		return result;
	}

	/**
	 * Expression grammar
	 *
	 * <numExpr>        ::= <numAddSubExpr> [&|!|^ <numAddSubExpr>]*
	 * <numAddSubExpr>  ::= <numMultDivExpr> [+|- <numMultDivExpr>]*
	 * <numMultDivExpr> ::= <numFactor> [*|/ <numFactor>]*
	 * <numFactor>      ::= <numConst> | <numSymbol> | -<numFactor> | ><numFactor> | <<numFactor> | (<numExpr>)
	 * <numSymbol>      ::= <symbolName>
	 * <numConst>       ::= <decConst> | <hexConst> | <binConst> | <charLiteral>
	 */

	private Integer evaluateNumExpr() {
		Integer resultLeft = evaluateNumAddSubExpr();
		if (resultLeft != null) {
			String token;

			// The while loop turns a left-recursive (= right-associative) binary
			// operation into a right-recursive (= left-associative) operation by
			// holding back the recursive descent until all adjacent operators of the
			// same production level are parsed.
			//
			// sample expression: 1 - 2 - 3
			// left-recursive:    1 - ( 2 - 3 ) = 2  (wrong)
			// right-recursive:   ( 1 - 2 ) - 3 = -4 (correct)

			while ((token = getNextTokenOutOf(AND, OR, XOR)) != null) {
				Integer resultRight = evaluateNumAddSubExpr();
				if (resultRight == null) {
					throw new InvalidExpressionException("Missing or invalid expression after binary operator \"" + token + "\"");
				}
				if (token.equals(AND)) {
					resultLeft = newInteger(resultLeft & resultRight);
				} else if (token.equals(OR)) {
					resultLeft = newInteger(resultLeft | resultRight);
				} else if (token.equals(XOR)) {
					resultLeft = newInteger(resultLeft ^ resultRight);
				}
			}
		}
		return resultLeft;
	}

	private Integer evaluateNumAddSubExpr() {
		Integer resultLeft = evaluateNumMultDivExpr();
		if (resultLeft != null) {
			String token;
			while ((token = getNextTokenOutOf(PLUS, MINUS)) != null) {
				Integer resultRight = evaluateNumMultDivExpr();
				if (resultRight == null) {
					throw new InvalidExpressionException("Missing or invalid expression after arithmetic operator \"" + token + "\"");
				}
				if (token.equals(PLUS)) {
					resultLeft = newInteger(resultLeft + resultRight);
				} else if (token.equals(MINUS)) {
					resultLeft = newInteger(resultLeft - resultRight);
				}
			}
		}
		return resultLeft;
	}

	private Integer evaluateNumMultDivExpr() {
		Integer resultLeft = evaluateNumFactor();
		if (resultLeft != null) {
			String token;
			while ((token = getNextTokenOutOf(MULTIPLY, DIVIDE)) != null) {
				Integer resultRight = evaluateNumFactor();
				if (resultRight == null) {
					throw new InvalidExpressionException("Missing or invalid expression after arithmetic operator \"" + token + "\"");
				}
				if (token.equals(MULTIPLY)) {
					resultLeft = newInteger(resultLeft * resultRight);
				} else if (token.equals(DIVIDE)) {
					if (resultRight == 0) {
						throw new DivisionByZeroException();
					}
					resultLeft = newInteger(resultLeft / resultRight);
				}
			}
		}
		return resultLeft;
	}

	private Integer evaluateNumFactor() {
		Integer result = evaluateNumConst();
		if (result == null) {
			result = evaluateNumSymbol();
		}
		if (result == null) {
			String token = getNextTokenOutOf(MINUS, HI_BYTE, LO_BYTE);
			if (token != null) {
				Integer subResult = evaluateNumFactor();
				if (subResult == null) {
					throw new InvalidExpressionException("Missing or invalid expression after unary operator \"" + token + "\"");
				}
				if (token.equals(MINUS)) {
					result = newInteger(-subResult);
				} else if (token.equals(HI_BYTE)) {
					result = new Integer((subResult >> 8) & 0xFF);
				} else if (token.equals(LO_BYTE)) {
					result = new Integer(subResult & 0xFF);
				}
			}
		}
		if (result == null) {
			if (isNextToken(OPENING_BRACKET)) {
				result = evaluateNumExpr();
				if (result == null) {
					throw new InvalidExpressionException("Missing or invalid expression after \"" + OPENING_BRACKET + "\"");
				}
				if (isNextToken(CLOSING_BRACKET) == false) {
					throw new InvalidExpressionException("Missing closing bracket in expression");
				}
			}
		}
		return result;
	}

	/*
	 * ^                  | Start of string
	 * \s*?               | Match any whitespace, consumed lazily
	 * (                  | Start capture group 1
	 * [A-Za-z@\\?:]      | Match one character out of "A-Za-z@?:"
	 * [A-Za-z0-9@\\?:]*? | Match one character out of "A-Za-z0-9@?:", consumed progressively
	 * )                  | End capture group 1
	 */
	private static final Pattern SYMBOL_NAME_PATTERN = Pattern.compile("^\\s*?([A-Za-z@\\?:][A-Za-z0-9\\.@\\?:]*+)");

	private Integer evaluateNumSymbol() {
		String symbolName = findMatch(SYMBOL_NAME_PATTERN);
		if (symbolName != null) {
			symbolName = symbolName.toUpperCase();
			if ((this.symbolTable != null) && this.symbolTable.contains(symbolName)) {
				Symbol symbol = this.symbolTable.get(symbolName);
				if (symbol.hasValue() == false) {
					throw new SymbolHasNoValueException(symbolName);
				}
				symbol.setReferenced(true);
				return new Integer(symbol.getValue());
			}
			throw new UndefinedSymbolException(symbolName);
		}
		return null;
	}

	/*
	 * ^                  | Start of string
	 * \s*?               | Match any whitespace, consumed lazily
	 * (                  | Start capture group 1
	 * [0-9]{1,5}         | Match one to five decimal digits (0..9)
	 * )                  | End capture group 1
	 */
	private static final Pattern DECIMAL_CONSTANT_PATTERN = Pattern.compile("^\\s*?([0-9]{1,5})");

	/*
	 * ^                  | Start of string
	 * \s*?               | Match any whitespace, consumed lazily
	 * (                  | Start capture group 1
	 * $                  | Match a dollar sign ($)
	 * [0-9A-F-a-f]{1,4}  | Match one to four hexadecimal digits (0..9A..F)
	 * )                  | End capture group 1
	 */
	private static final Pattern HEX_CONSTANT_PATTERN = Pattern.compile("^\\s*?(\\$[0-9A-Fa-f]{1,4})");

	/*
	 * ^                  | Start of string
	 * \s*?               | Match any whitespace, consumed lazily
	 * (                  | Start capture group 1
	 * %                  | Match a percent sign (%)
	 * [01]{1,16}         | Match one to sixteen binary digits (0..1)
	 * )                  | End capture group 1
	 */
	private static final Pattern BINARY_CONSTANT_PATTERN = Pattern.compile("^\\s*?(%[01]{1,16})");

	/*
	 * ^                  | Start of string
	 * \s*?               | Match any whitespace, consumed lazily
	 * (                  | Start capture group 1
	 * '                  | Match a single quote (')
	 * .                  | Match one character
	 * )                  | End capture group 1
	 */
	private static final Pattern CHAR_LITERAL_PATTERN = Pattern.compile("^\\s*?('.)");

	private Integer evaluateNumConst() {
		String strNumConstant;
		try {
			strNumConstant = findMatch(DECIMAL_CONSTANT_PATTERN);
			if (strNumConstant != null) {
				return new Integer(Integer.parseInt(strNumConstant, 10) & 0xFFFF);
			}
			strNumConstant = findMatch(HEX_CONSTANT_PATTERN);
			if (strNumConstant != null) {
				return new Integer(Integer.parseInt(strNumConstant.substring(1), 16) & 0xFFFF);
			}
			strNumConstant = findMatch(BINARY_CONSTANT_PATTERN);
			if (strNumConstant != null) {
				return new Integer(Integer.parseInt(strNumConstant.substring(1), 2) & 0xFFFF);
			}
		} catch (NumberFormatException e) {
			// can never happen, parsed strings are always valid
		}

		strNumConstant = findMatch(CHAR_LITERAL_PATTERN);
		if (strNumConstant != null) {
			return new Integer(strNumConstant.charAt(1));
		}
		return null;
	}

	/*
	 * ^                  | Start of string
	 * (                  | Start capture group 1
	 * \s*+               | Match any whitespace, consumed possessively
	 * )                  | End capture group 1
	 */
	private static final Pattern LEADING_WHITESPACE_PATTERN = Pattern.compile("^(\\s*+)");

	private boolean isNextToken(String token) {
		int posSave = this.pos;
		findMatch(LEADING_WHITESPACE_PATTERN);
		boolean isNextToken = this.stringToEvaluate.startsWith(token, this.pos);
		this.pos = isNextToken ? this.pos + token.length() : posSave;
		return isNextToken;
	}

	private String getNextTokenOutOf(String... tokens) {
		for (String token : tokens) {
			if (isNextToken(token)) {
				return token;
			}
		}
		return null;
	}

	private String findMatch(Pattern pattern) {
		Matcher matcher = pattern.matcher(this.stringToEvaluate);
		matcher.region(this.pos, this.stringToEvaluate.length());
		if (matcher.find()) {
			this.pos = matcher.end();
			return matcher.group(1);
		}
		return null;
	}

	private Integer newInteger(int value) {
		return new Integer(value & 0xFFFF);
	}
}
