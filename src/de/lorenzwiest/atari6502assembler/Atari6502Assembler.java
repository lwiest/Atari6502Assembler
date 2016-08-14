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

package de.lorenzwiest.atari6502assembler;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.lorenzwiest.atari6502assembler.exception.AssemblerException;
import de.lorenzwiest.atari6502assembler.exception.ExpressionException;
import de.lorenzwiest.atari6502assembler.exception.assembler.BranchRangeException;
import de.lorenzwiest.atari6502assembler.exception.assembler.CannotParseByteException;
import de.lorenzwiest.atari6502assembler.exception.assembler.IllegalInstructionException;
import de.lorenzwiest.atari6502assembler.exception.assembler.MissingSymbolException;
import de.lorenzwiest.atari6502assembler.exception.assembler.NoOriginException;
import de.lorenzwiest.atari6502assembler.exception.assembler.SyntaxErrorException;
import de.lorenzwiest.atari6502assembler.exception.expression.SymbolHasNoValueException;
import de.lorenzwiest.atari6502assembler.exception.expression.UndefinedSymbolException;
import de.lorenzwiest.atari6502assembler.expression.ExpressionEvaluator;
import de.lorenzwiest.atari6502assembler.symbol.Symbol;
import de.lorenzwiest.atari6502assembler.symbol.SymbolTable;
import de.lorenzwiest.atari6502assembler.symbol.SymbolType;

public class Atari6502Assembler {

	///////////////////////////////////////////////////////////////////////////

	private class AssemblySegment {
		private int startAddress;
		private byte[] bytes;

		public AssemblySegment(int startAddress, byte[] bytes) {
			this.startAddress = startAddress;
			this.bytes = bytes;
		}

		public int getStartAddress() {
			return this.startAddress;
		}

		public byte[] getBytes() {
			return this.bytes;
		}
	}

	///////////////////////////////////////////////////////////////////////////

	static public class LineOfCode {
		final private static String EMPTY_STRING = "";

		private String lineNumber = EMPTY_STRING;
		private String label = EMPTY_STRING;
		private String op = EMPTY_STRING;
		private String arg = EMPTY_STRING;
		private String comment = EMPTY_STRING;
		private int commentPos = -1;

		private LineOfCode() {
			// prevent external instantiation
		}

		public String getLineNumber() {
			return this.lineNumber;
		}

		public String getLabel() {
			return this.label;
		}

		public String getOp() {
			return this.op;
		}

		public String getArg() {
			return this.arg;
		}

		public String getComment() {
			return this.comment;
		}

		public int getCommentPos() {
			return this.commentPos;
		}

		public boolean isEmpty() {
			return getLabel().isEmpty() && getOp().isEmpty() && getArg().isEmpty() && getComment().isEmpty();
		}

		/*
		 * (                 | Start capture group 1
		 * "                 | Match opening double quote (")
		 * [^"]*             | Match any characters that are not a double quote ("), consumed ###
		 * "                 | Match closing double quote (")
		 * |                 | ..or..
		 * '.                | Match a single quote and any character
		 * |                 | ..or..
		 * [^;]              | Match one character that is not a semicolon (;)
		 * )                 | End capture group 1
		 * *?                | Match any of those, consumed ###
		 * ;                 | Match semicolon (;)
		 */
		final static private Pattern COMMENT_PATTERN = Pattern.compile("(\"[^\"]*\"|'.|[^;])*;");

		/*
		 * ^                 | Start of string
		 * \s*               | Match any whitespace, consumed ###
		 * (                 | Start capture group 1
		 * \d{1,5}           | Match 1 to 5 digits
		 * )                 | End capture group 1
		 * \s                | Match one whitespace
		 */
		final static private Pattern PATTERN_LINE_NUMBER = Pattern.compile("^\\s*(\\d{1,5})\\s");

		/*
		 * ^                 | Start of string
		 * (                 | Start capture group 1
		 * [A-Z@:\\?]        | Match one character that is an upper-case letter, at-sign (@), colon (:), or question mark (?)
		 * [A-Z0-9@:\\?\\.]* | Match any characters that are an upper-case letter, digit, at-sign (@), colon (:), question mark (?), or period (.), consumed ###
		 * )                 | End capture group 1
		 */
		final static private Pattern PATTERN_LABEL = Pattern.compile("^([A-Z@:\\?][A-Z0-9@:\\?\\.]*)");

		/*
		 * ^                 | Start of string
		 * \s*               | Match any whitespace, consumed ###
		 * (                 | Start capture group 1
		 * \*=               | Match "*="
		 * |                 | ..or..
		 * =                 | Match an equal sign (=)
		 * |                 | ..or..
		 * \.BYTE\s          | Match ".BYTE "
		 * |                 | ..or..
		 * \.WORD\s          | Match ".WORD "
		 * |                 | ..or..
		 * [A-Z]{1,3}        | 3 upper-case letters
		 * )                 | End capture group 1
		 */
		final static private Pattern PATTERN_OP = Pattern.compile("^\\s+(\\*=|=|\\.BYTE\\s|\\.WORD\\s|[A-Z]{3})");

		public static LineOfCode parse(String str) {
			LineOfCode instance = new LineOfCode();

			int posStart = 0;
			int posEnd = str.length();

			Matcher matcher = COMMENT_PATTERN.matcher(str);
			if (matcher.find()) {
				int commentPos = matcher.end() - 1;
				instance.commentPos = commentPos;
				instance.comment = str.substring(commentPos);
				posEnd = commentPos;
			}

			if (matcher.usePattern(PATTERN_LINE_NUMBER).region(posStart, posEnd).find()) {
				instance.lineNumber = matcher.group(1);
				posStart = matcher.end();
			}

			if (matcher.usePattern(PATTERN_LABEL).region(posStart, posEnd).find()) {
				instance.label = matcher.group(1);
				posStart = matcher.end();
			}

			if (matcher.usePattern(PATTERN_OP).region(posStart, posEnd).find()) {
				instance.op = matcher.group(1).trim();
				posStart = matcher.end();
			}

			instance.arg = str.substring(posStart, posEnd).trim();
			return instance;
		}
	}

	///////////////////////////////////////////////////////////////////////////

	private static final int UNDEFINED = -1;

	private static final String CR = System.getProperty("line.separator");

	private final static String VERSION_CREATED_DATE = "20-Dec-2001";
	private final static String VERSION_LAST_CHANGE_DATE = "14-Aug-2016";

	private final static String HELP_TEXT =
					"   _  _            _    __ ___  __ ___     _                     _    _" + CR + //
					"  /_\\| |_ __ _ _ _(_)  / /| __|/  \\_  )   /_\\   ______ ___ _ __ | |__| |___ _ _" + CR + //
					" / _ \\  _/ _` | '_| | / _ \\__ \\ () / /   / _ \\ (_-<_-</ -_) '  \\| '_ \\ | -_) '_|" + CR + //
					"/_/ \\_\\__\\__,_|_| |_| \\___/___/\\__/___| /_/ \\_\\/__/__/\\___|_|_|_|_.__/_|___|_|" + CR + //
					CR + //
					"Atari 6502 Assembler V2.0 (C) by Lorenz Wiest, created: %s, last change: %s" + CR + //
					CR + //
					"Usage: java Atari6502Assembler [<options>] <infile> [<outfile>] [> <listfile>]" + CR + //
					CR + //
					"<options> (default values in {})" + CR + //
					"  -showHeader=<true|false>      | If true then show assembly header {true}" + CR + //
					"  -showObject=<true|false>      | If true then show object addresses and bytes {true}" + CR + //
					"  -showLineNumbers=<true|false> | If true then show line numbers {true}" + CR + //
					"  -lineNumberStart=<n>          | Line number start {1}" + CR + //
					"  -lineNumberInc=<n>            | Line number increment {1}" + CR + //
					"  -padLineNumbers=<true|false>  | If true then pad line numbers with \"0\" {true}" + CR + //
					"  -instructionPos=<n>           | Column number of instructions {16}" + CR + //
					"  -labelExprPos=<n>             | Column number of label expressions {16}" + CR + //
					"  -commentPos=<n>               | Column number of comments. If 0 then ignored. {0}" + CR + //
					"<infile>   - Assembler source file" + CR + //
					"<outfile>  - Assembled object file" + CR + //
					"<listfile> - Assembler listing file";

	private final static String HEADER_TEXT = "Atari 6502 Assembler V2.0 - Assembly Date: %s";

	private SymbolTable symbolTable = new SymbolTable();
	private ExpressionEvaluator evaluator = new ExpressionEvaluator(this.symbolTable);

	private boolean optShowHeader = true;
	private boolean optShowObject = true;
	private boolean optShowLineNumbers = true;
	private int optLineNumberStart = 1;
	private int optLineNumberInc = 1;
	private boolean optPadLineNumbers = true;
	private int optInstructionPos = 16;
	private int optLabelExprPos = 16;
	private int optCommentPos = 0;
	private String inFilename;
	private String outFilename;

	private String strSrcLineNumbersDigitCount;

	private static int PARSE_ARGS_OK = 0;
	private static int PARSE_ARGS_ERROR = -1;

	public static void main(String[] args) {
		Atari6502Assembler instance = new Atari6502Assembler();
		if (instance.parseCommandLineArgs(args) == PARSE_ARGS_OK) {
			instance.assemble(instance.inFilename, instance.outFilename);
		}
	}

	private int parseCommandLineArgs(String[] args) {
		final String OPT_SHOW_HEADER = "-showHeader=";
		final String OPT_SHOW_OBJECT = "-showObject=";
		final String OPT_SHOW_LINE_NUMBERS = "-showLineNumbers=";
		final String OPT_LINE_NUMBER_START = "-lineNumberStart=";
		final String OPT_LINE_NUMBER_INC = "-lineNumberInc=";
		final String OPT_PAD_LINE_NUMBERS = "-padLineNumbers=";
		final String OPT_INSTRUCTION_POS = "-instructionPos=";
		final String OPT_LABEL_EXPR_POS = "-labelExprPos=";
		final String OPT_COMMENT_POS = "-commentPos=";

		if (args.length == 0) {
			System.out.println(String.format(HELP_TEXT, VERSION_CREATED_DATE, VERSION_LAST_CHANGE_DATE));
			return PARSE_ARGS_ERROR;
		}

		int inFilenameArgIndex = -1;
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];

			if (arg.startsWith(OPT_INSTRUCTION_POS)) {
				String strValue = arg.substring(OPT_INSTRUCTION_POS.length());
				try {
					int value = Integer.parseInt(strValue);
					if (value < 0) {
						throw new NumberFormatException();
					}
					this.optInstructionPos = value;
				} catch (NumberFormatException e) {
					System.out.println("Option " + OPT_INSTRUCTION_POS + " has invalid value \""+ strValue + "\". Valid values are positive integers.");
					return PARSE_ARGS_ERROR;
				}
			} else if (arg.startsWith(OPT_LABEL_EXPR_POS)) {
				String strValue = arg.substring(OPT_LABEL_EXPR_POS.length());
				try {
					int value = Integer.parseInt(strValue);
					if (value < 0) {
						throw new NumberFormatException();
					}
					this.optLabelExprPos = value;
				} catch (NumberFormatException e) {
					System.out.println("Option " + OPT_LABEL_EXPR_POS + " has invalid value \""+ strValue + "\". Valid values are positive integers.");
					return PARSE_ARGS_ERROR;
				}
			} else if (arg.startsWith(OPT_COMMENT_POS)) {
				String strValue = arg.substring(OPT_COMMENT_POS.length());
				try {
					int value = Integer.parseInt(strValue);
					if (value < 0) {
						throw new NumberFormatException();
					}
					this.optCommentPos = value;
				} catch (NumberFormatException e) {
					System.out.println("Option " + OPT_COMMENT_POS + " has invalid value \""+ strValue + "\". Valid values are positive integers.");
					return PARSE_ARGS_ERROR;
				}
			} else if (arg.startsWith(OPT_SHOW_LINE_NUMBERS)) {
				String strValue = arg.substring(OPT_SHOW_LINE_NUMBERS.length());
				if (strValue.toLowerCase().equals("true") || strValue.toLowerCase().equals("false")) {
					this.optShowLineNumbers = Boolean.parseBoolean(strValue);
				} else {
					System.out.println("Option " + OPT_SHOW_LINE_NUMBERS + " has invalid value \""+ strValue + "\". Valid values are \"true\" or \"false\".");
					return PARSE_ARGS_ERROR;
				}
			} else if (arg.startsWith(OPT_LINE_NUMBER_START)) {
				String strValue = arg.substring(OPT_LINE_NUMBER_START.length());
				try {
					int value = Integer.parseInt(strValue);
					if (value < 0) {
						throw new NumberFormatException();
					}
					this.optLineNumberStart = value;
				} catch (NumberFormatException e) {
					System.out.println("Option " + OPT_LINE_NUMBER_START + " has invalid value \""+ strValue + "\". Valid values are positive integers.");
					return PARSE_ARGS_ERROR;
				}
			} else if (arg.startsWith(OPT_LINE_NUMBER_INC)) {
				String strValue = arg.substring(OPT_LINE_NUMBER_INC.length());
				try {
					int value = Integer.parseInt(strValue);
					if (value < 0) {
						throw new NumberFormatException();
					}
					this.optLineNumberInc = value;
				} catch (NumberFormatException e) {
					System.out.println("Option " + OPT_LINE_NUMBER_INC + " has invalid value \""+ strValue + "\". Valid values are positive integers.");
					return PARSE_ARGS_ERROR;
				}
			} else if (arg.startsWith(OPT_PAD_LINE_NUMBERS)) {
				String strValue = arg.substring(OPT_PAD_LINE_NUMBERS.length());
				if (strValue.toLowerCase().equals("true") || strValue.toLowerCase().equals("false")) {
					this.optPadLineNumbers = Boolean.parseBoolean(strValue);
				} else {
					System.out.println("Option " + OPT_PAD_LINE_NUMBERS + " has invalid value \""+ strValue + "\". Valid values are \"true\" or \"false\".");
					return PARSE_ARGS_ERROR;
				}
			} else if (arg.startsWith(OPT_SHOW_OBJECT)) {
				String strValue = arg.substring(OPT_SHOW_OBJECT.length());
				if (strValue.toLowerCase().equals("true") || strValue.toLowerCase().equals("false")) {
					this.optShowObject = Boolean.parseBoolean(strValue);
				} else {
					System.out.println("Option " + OPT_SHOW_OBJECT + " has invalid value \""+ strValue + "\". Valid values are \"true\" or \"false\".");
					return PARSE_ARGS_ERROR;
				}
			} else if (arg.startsWith(OPT_SHOW_HEADER)) {
				String strValue = arg.substring(OPT_SHOW_HEADER.length());
				if (strValue.toLowerCase().equals("true") || strValue.toLowerCase().equals("false")) {
					this.optShowHeader = Boolean.parseBoolean(strValue);
				} else {
					System.out.println("Option " + OPT_SHOW_HEADER + " has invalid value \""+ strValue + "\". Valid values are \"true\" or \"false\".");
					return PARSE_ARGS_ERROR;
				}
			} else if (arg.startsWith("-")) {
				System.out.println("Option \""+ arg + "\" is invalid.");
				return PARSE_ARGS_ERROR;
			} else {
				inFilenameArgIndex = i;
				break;
			}
		}
		if (inFilenameArgIndex == -1) {
			System.out.println("No infile.");
			return PARSE_ARGS_ERROR;
		}
		this.inFilename = args[inFilenameArgIndex];
		this.outFilename = (args.length > inFilenameArgIndex + 1) ? args[inFilenameArgIndex + 1] : null;
		return PARSE_ARGS_OK;
	}

	private void assemble(String inFilename, String outFilename) {
		BufferedReader in = null;
		BufferedOutputStream  out = null;
		try {
			printHeader();

			in = new BufferedReader(new FileReader(inFilename));
			assemblePass1(in);
			closeGracefully(in);

			List<AssemblySegment> segments = new ArrayList<AssemblySegment>();
			in = new BufferedReader(new FileReader(inFilename));
			assemblePass2(in, segments);

			printSymbols();

			if (outFilename != null) {
				out = new BufferedOutputStream(new FileOutputStream(outFilename));
				writeSegments(out, segments);
			}
		} catch (FileNotFoundException e) {
			System.out.println("Error: File not found: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("Error: I/O exception");
		} catch (ExpressionException e) {
			System.out.println(e.getErrorMessage());
		} catch (AssemblerException e) {
			System.out.println(e.getErrorMessage());
		} finally {
			closeGracefully(out);
			closeGracefully(in);
		}
	}

	private void printHeader() {
		if (this.optShowHeader) {
			Calendar today = new GregorianCalendar();
			String assemblyDate = formatDate(today);
			System.out.println(String.format(HEADER_TEXT, assemblyDate));
			System.out.println();
		}
	}

	private static void closeGracefully(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	// The first pass collects all symbols and labels and stores them in the
	// symbol table. While doing so, some guesses are made about the operation
	// argument length, which could lead to phase errors due to forward
	// referencing.

	private void assemblePass1(BufferedReader in) throws IOException, AssemblerException, ExpressionException {
		int pc = UNDEFINED; // program counter

		int srcLineNumber = 1;
		int lstLineNumber = this.optLineNumberStart;

		try {
			while (true) {
				String lineOfCode = in.readLine();
				if (lineOfCode == null) {
					this.strSrcLineNumbersDigitCount = Integer.toString((Integer.toString(lstLineNumber).length()));
					break;
				}

				LineOfCode parsedLine = LineOfCode.parse(lineOfCode);
				String label = parsedLine.getLabel();
				String op = parsedLine.getOp();
				String arg = parsedLine.getArg();
				boolean lineHasLabel = label.length() > 0;

				if (op.isEmpty() && arg.length() > 0) {
					throw new SyntaxErrorException();
				}

				if (op.equals("*=")) {
					pc = this.evaluator.evaluate(arg);
				} else if (op.equals("=")) {
					if (lineHasLabel == false) {
						throw new MissingSymbolException();
					}
					try {
						int argValue = this.evaluator.evaluate(arg);
						this.symbolTable.put(label, argValue, SymbolType.EXPRESSION);
					} catch (ExpressionException e) {
						this.symbolTable.put(label, SymbolType.EXPRESSION);
					}
				} else {
					if (lineHasLabel) {
						if (pc == UNDEFINED) {
							throw new NoOriginException();
						} else {
							this.symbolTable.put(label, pc, SymbolType.LABEL);
						}
					}
					if (op.equals(".BYTE")) {
						pc += getNumBytes(".BYTE", arg);
					} else if (op.equals(".WORD")) {
						pc += getNumBytes(".WORD", arg);
					} else if (isOpMemnonic(op)) {
						if (pc == UNDEFINED) {
							throw new NoOriginException();
						}
						pc += getNumBytes(op, arg);
					} else if (op.length() > 0) {
						throw new IllegalInstructionException(op);
					}
				}
				srcLineNumber++;
				lstLineNumber += this.optLineNumberInc;
			}
		} catch (AssemblerException e) {
			throw new AssemblerException(e, srcLineNumber);
		} catch (ExpressionException e) {
			throw new ExpressionException(e, srcLineNumber);
		}
	}

	// The second pass assembles bytes, writes them to the out file, and prints
	// the assembler listing to the output.

	private void assemblePass2(BufferedReader in, List<AssemblySegment> segments) throws IOException, AssemblerException, ExpressionException {
		int pc = UNDEFINED;
		int startAddress = UNDEFINED;
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		int srcLineNumber = 1;
		int lstLineNumber = this.optLineNumberStart;

		try {
			while (true) {
				String lineOfCode = in.readLine();
				if (lineOfCode == null) {
					break;
				}

				LineOfCode parsedLineOfCode = LineOfCode.parse(lineOfCode);
				String label = parsedLineOfCode.getLabel();
				String op = parsedLineOfCode.getOp();
				String arg = parsedLineOfCode.getArg();

				String strPc = toHexString(pc);
				String strBytes = "";

				if (label.isEmpty() && op.isEmpty() && arg.isEmpty()) {
					strPc = "";
				} else if (op.equals("*=")) {
					if (pc != UNDEFINED) {
						segments.add(new AssemblySegment(startAddress, out.toByteArray()));
					}
					out.reset();
					pc = this.evaluator.evaluate(arg);
					startAddress = pc;

					strPc = "";
				} else if (op.equals("=")) {
					int argValue = this.evaluator.evaluate(arg);
					this.symbolTable.get(label).setValue(argValue);
					strPc = "";
					strBytes = toHexString(argValue);
				} else if (op.equals(".BYTE")) {
					byte[] bytes = getBytes(".BYTE", arg, pc);
					pc += bytes.length;
					strBytes = toHexString(bytes);
					writeBytes(out, bytes);
				} else if (op.equals(".WORD")) {
					byte[] bytes = getBytes(".WORD", arg, pc);
					pc += bytes.length;
					strBytes = toHexString(bytes);
					writeBytes(out, bytes);
				} else if (isOpMemnonic(op)) {
					byte[] bytes = getBytes(op, arg, pc);
					pc += bytes.length;
					strBytes = toHexString(bytes);
					writeBytes(out, bytes);
				}
				System.out.println(formatLine(strPc, strBytes, lstLineNumber, parsedLineOfCode));

				srcLineNumber++;
				lstLineNumber += this.optLineNumberInc;
			}
			if (pc == UNDEFINED) {
				throw new NoOriginException();
			} else {
				segments.add(new AssemblySegment(startAddress, out.toByteArray()));
			}
		} catch (AssemblerException e) {
			throw new AssemblerException(e, srcLineNumber);
		} catch (ExpressionException e) {
			throw new ExpressionException(e, srcLineNumber);
		} finally {
			closeGracefully(out);
		}
	}

	private final static int NYBBLES_PER_LINE = 8;

	private String formatLine(String strPc, String strBytes, int lstLineNumber, LineOfCode parsedLineOfCode) {
		StringBuffer sb = new StringBuffer();

		if ((strPc.length() > 0) && (strBytes.length() > 0)) {
			if (this.optShowObject) {
				int pc = Integer.parseInt(strPc, 16);
				int numNybbles = strBytes.length();
				String nybblesChunk = "";
				boolean isFirstLine = true;

				for (int posNybbles = 0; posNybbles < numNybbles; posNybbles += nybblesChunk.length()) {
					if (posNybbles + NYBBLES_PER_LINE < numNybbles) {
						nybblesChunk = strBytes.substring(posNybbles, posNybbles + NYBBLES_PER_LINE);
					} else {
						nybblesChunk = strBytes.substring(posNybbles);
					}

					if (isFirstLine == false) {
						sb.append(CR);
					}
					sb.append(formatObjectPart(toHexString(pc), nybblesChunk));

					if (isFirstLine) {
						if (parsedLineOfCode.isEmpty() == false) {
							sb.append(formatSourcePart(lstLineNumber, parsedLineOfCode));
						}
						isFirstLine = false;
					}
					pc += nybblesChunk.length() / 2;
				}
			} else {
				sb.append(formatSourcePart(lstLineNumber, parsedLineOfCode));
			}
		} else if (strPc.isEmpty() && (strBytes.length() > 0)) {
			sb.append(formatObjectPartWithSymbol(strBytes));
			sb.append(formatSourcePart(lstLineNumber, parsedLineOfCode));
		} else if (strBytes.isEmpty())  {
			sb.append(formatObjectPart(strPc, ""));
			sb.append(formatSourcePart(lstLineNumber, parsedLineOfCode));
		}
		return sb.toString();
	}

	private String formatObjectPart(String strPc, String strBytes) {
		if (this.optShowObject) {
			return padRight(strPc, 4) + " " + padRight(strBytes, NYBBLES_PER_LINE);
		}
		return "";
	}

	private String formatObjectPartWithSymbol(String strBytes) {
		if (this.optShowObject) {
			return padRight("", 4) + "=" + padRight(strBytes, NYBBLES_PER_LINE);
		}
		return "";
	}

	private String formatSourcePart(int lineNumber, LineOfCode parsedLineOfCode) {
		StringBuffer sb = new StringBuffer();

		if (this.optShowObject) {
			sb.append(" ");
		}

		if (this.optShowLineNumbers) {
			String formatString = "%" + (this.optPadLineNumbers ? "0" : "") + this.strSrcLineNumbersDigitCount + "d";
			sb.append(String.format(formatString, lineNumber));
		}

		String formattedLineOfCode = formatLineOfCode(parsedLineOfCode);
		if (formattedLineOfCode.length() > 0) {
			if (this.optShowLineNumbers) {
				sb.append(" ");
			}
			sb.append(formattedLineOfCode);
		}
		return sb.toString();
	}

	private String formatLineOfCode(LineOfCode parsedLineOfCode) {
		if (parsedLineOfCode.isEmpty()) {
			return "";
		}

		String label = parsedLineOfCode.getLabel();
		String op = parsedLineOfCode.getOp();
		String arg = parsedLineOfCode.getArg();
		String comment = parsedLineOfCode.getComment();

		StringBuffer sb = new StringBuffer();
		if (op.length() > 0) {
			int indentPos = op.equals("=") ? this.optLabelExprPos : this.optInstructionPos;
			sb.append(padRight(label, indentPos));
			sb.append(op);
			if (arg.length() > 0) {
				sb.append(" ");
				sb.append(arg);
			}
		} else if (label.length() > 0) {
			sb.append(label);
		}

		if (comment.length() > 0) {
			int posComment = parsedLineOfCode.getCommentPos();
			if ((posComment > 0) && (this.optCommentPos > 0)) { // aligns non-zero pos comments only
				posComment = this.optCommentPos;
			}
			for (int i = sb.length(); i < posComment; i++) {
				sb.append(" ");
			}
			sb.append(comment);
		}
		return sb.toString();
	}

	private static String padRight(String str, int width) {
		StringBuffer sb = new StringBuffer(str);
		for (int i = str.length(); i < width; i++) {
			sb.append(" ");
		}
		return sb.toString();
	}

	private void writeSegments(BufferedOutputStream out, List<AssemblySegment> segments) throws IOException {
		writeWord(out, 0xFFFF);

		for (AssemblySegment segment : segments) {
			int segLength = segment.getBytes().length;
			if (segLength > 0) {
				int segStart = segment.getStartAddress();
				int segEnd = segStart + segLength - 1;

				writeWord(out, segStart);
				writeWord(out, segEnd);
				writeBytes(out, segment.getBytes());
			}
		}
	}

	private void writeByte(OutputStream out, int aByte) throws IOException {
		out.write(aByte & 0xFF);
	}

	private void writeBytes(OutputStream out, byte[] bytes) throws IOException {
		for (int aByte : bytes) {
			writeByte(out, aByte);
		}
	}

	private void writeWord(OutputStream out, int aWord) throws IOException {
		writeByte(out, aWord);
		writeByte(out, aWord >> 8);
	}

	private void printSymbols() {
		int numSymbols = 0;
		for (String symbolName : this.symbolTable.keys()) {
			if (Symbol.isLocalSymbolName(symbolName) == false) {
				numSymbols++;
			}
		}

		System.out.println();
		System.out.println(String.format("SYMBOLS (SORTED BY NAME): %d", numSymbols));
		System.out.println();

		for (String symbolName : this.symbolTable.keys()) {
			if (Symbol.isLocalSymbolName(symbolName) == false) {
				System.out.println(formatSymbol(symbolName));
			}
		}

		System.out.println();
		System.out.println(String.format("SYMBOLS (SORTED BY VALUE): %d", numSymbols));
		System.out.println();

		for (Symbol symbol : this.symbolTable.symbolsSortedByValue()) {
			String symbolName = symbol.getName();
			if (Symbol.isLocalSymbolName(symbolName) == false) {
				System.out.println(formatSymbol(symbolName));
			}
		}
	}

	private String formatSymbol(String symbolName) {
		Symbol symbol = this.symbolTable.get(symbolName);

		SymbolType type = symbol.getType();
		String isSymbol = (type == SymbolType.EXPRESSION) ? "=" : " ";
		String hexValue = toHexString(symbol.getValue());
		String isReferenced = symbol.isReferenced() ? " " : "?";

		return isSymbol + hexValue + " " + isReferenced + symbolName;
	}

	private static final int UNKNOWN = 0;
	private static final int IMPLIED = 1;
	private static final int ACCUMULATOR = 2;
	private static final int ABSOLUTE = 3;
	private static final int ZEROPAGE = 4;
	private static final int IMMEDIATE = 5;
	private static final int ABSOLUTEX = 6;
	private static final int ABSOLUTEY = 7;
	private static final int INDEXIND = 8;
	private static final int INDINDEX = 9;
	private static final int ZEROPAGEX = 10;
	private static final int RELATIVE = 11;
	private static final int INDIRECT = 12;
	private static final int ZEROPAGEY = 13;

	private static final int[] INSTRUCTION_LENGTH = {
		0, 1, 1, 3, 2, 2, 3, 3, 2, 2, 2, 2, 3, 2
	};

	private static final String[][] OP_CODES = {
		{ "ADC",   "",   "", "6D", "65", "69", "7D", "79", "61", "71", "75",   "",   "",   "" },
		{ "AND",   "",   "", "2D", "25", "29", "3D", "39", "21", "31", "36",   "",   "",   "" },
		{ "ASL",   "", "0A", "0E", "06",   "", "1E",   "",   "",   "", "16",   "",   "",   "" },
		{ "BCC",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "", "90",   "",   "" },
		{ "BCS",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "", "B0",   "",   "" },
		{ "BEQ",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "", "F0",   "",   "" },
		{ "BIT",   "",   "", "2C", "24",   "",   "",   "",   "",   "",   "",   "",   "",   "" },
		{ "BMI",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "", "30",   "",   "" },
		{ "BNE",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "", "D0",   "",   "" },
		{ "BPL",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "", "10",   "",   "" },
		{ "BRK", "00",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "" },
		{ "BVC",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "", "50",   "",   "" },
		{ "BVS",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "", "70",   "",   "" },
		{ "CLC", "18",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "" },
		{ "CLD", "D8",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "" },
		{ "CLI", "58",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "" },
		{ "CLV", "88",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "" },
		{ "CMP",   "",   "", "CD", "C5", "C9", "DD", "D9", "C1", "D1", "D5",   "",   "",   "" },
		{ "CPX",   "",   "", "EC", "E4", "E0",   "",   "",   "",   "",   "",   "",   "",   "" },
		{ "CPY",   "",   "", "CC", "C4", "C0",   "",   "",   "",   "",   "",   "",   "",   "" },
		{ "DEC",   "",   "", "CE", "C6",   "", "DE",   "",   "",   "", "D6",   "",   "",   "" },
		{ "DEX", "CA",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "" },
		{ "DEY", "88",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "" },
		{ "EOR",   "",   "", "4D", "45", "49", "5D", "59", "41", "51", "55",   "",   "",   "" },
		{ "INC",   "",   "", "EE", "E6",   "", "FE",   "",   "",   "", "F6",   "",   "",   "" },
		{ "INX", "E8",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "" },
		{ "INY", "C8",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "" },
		{ "JMP",   "",   "", "4C",   "",   "",   "",   "",   "",   "",   "",   "", "6C",   "" },
		{ "JSR",   "",   "", "20",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "" },
		{ "LDA",   "",   "", "AD", "A5", "A9", "BD", "B9", "A1", "B1", "B5",   "",   "",   "" },
		{ "LDX",   "",   "", "AE", "A6", "A2",   "", "BE",   "",   "",   "",   "",   "", "B6" },
		{ "LDY",   "",   "", "AC", "A4", "A0", "BC",   "",   "",   "", "B4",   "",   "",   "" },
		{ "LSR",   "", "4A", "4E", "46",   "", "5E",   "",   "",   "", "56",   "",   "",   "" },
		{ "NOP", "EA",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "" },
		{ "ORA",   "",   "", "0D", "05", "09", "1D", "19", "01", "11", "15",   "",   "",   "" },
		{ "PHA", "48",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "" },
		{ "PHP", "08",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "" },
		{ "PLA", "68",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "" },
		{ "PLP", "28",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "" },
		{ "ROL",   "", "2A", "2E", "26",   "", "3E",   "",   "",   "", "36",   "",   "",   "" },
		{ "ROR",   "", "6A", "6E", "66",   "", "7E",   "",   "",   "", "76",   "",   "",   "" },
		{ "RTI", "40",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "" },
		{ "RTS", "60",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "" },
		{ "SBC",   "",   "", "ED", "E5", "E9", "FD", "F9", "E1", "F1", "F5",   "",   "",   "" },
		{ "SEC", "38",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "" },
		{ "SED", "F8",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "" },
		{ "SEI", "78",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "" },
		{ "STA",   "",   "", "8D", "85",   "", "9D", "99", "81", "91", "95",   "",   "",   "" },
		{ "STX",   "",   "", "8E", "86",   "",   "",   "",   "",   "",   "",   "",   "", "96" },
		{ "STY",   "",   "", "8C", "84",   "",   "",   "",   "", "94",   "",   "",   "",   "" },
		{ "TAX", "AA",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "" },
		{ "TAY", "A8",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "" },
		{ "TSX", "BA",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "" },
		{ "TXA", "8A",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "" },
		{ "TXS", "9A",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "" },
		{ "TYA", "98",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "",   "" }
	};

	private static final Map<String /* opCode key*/, Integer /* opCode */> OP_CODES_MAP = new HashMap<String, Integer>();
	private static final Set<String /* op memnonic */> OP_MEMNONICS = new HashSet<String>();

	static {
		for (int i = 0; i < OP_CODES.length; i++) {
			String op = OP_CODES[i][0];
			OP_MEMNONICS.add(op);
			for (int opMode = 1; opMode < OP_CODES[0].length; opMode++) {
				String opCode = OP_CODES[i][opMode];
				if (opCode.length() > 0) {
					String opCodeKey = getOpCodeKey(op, opMode);
					OP_CODES_MAP.put(opCodeKey, Integer.parseInt(opCode, 16));
				}
			}
		}
	}

	private static boolean isOpMemnonic(String op) {
		return OP_MEMNONICS.contains(op);
	}

	private static boolean hasOpCode(String op, int opMode)  {
		String opCodeKey = getOpCodeKey(op, opMode);
		return OP_CODES_MAP.containsKey(opCodeKey);
	}

	private static int getOpCode(String op, int opMode) throws AssemblerException {
		String opCodeKey = getOpCodeKey(op, opMode);
		Integer opCode = OP_CODES_MAP.get(opCodeKey);
		if (opCode == null) {
			throw new IllegalInstructionException(op);
		}
		return opCode.intValue();
	}

	private static String getOpCodeKey(String op, int opMode) {
		return op + opMode;
	}

	private int getOpMode(String op, String arg) throws AssemblerException, ExpressionException {
		if (arg.startsWith("(") && arg.endsWith(",X)")) {
			return INDEXIND;
		} else if (arg.startsWith("(") && arg.endsWith("),Y")) {
			return INDINDEX;
		} else if (arg.startsWith("(") && arg.endsWith(")")) {
			return INDIRECT;
		} else if (arg.endsWith(",X") || arg.endsWith(",Y")) {
			int value;
			try {
				int posEnd = arg.indexOf(",");
				String strValue = arg.substring(0, posEnd);
				value = this.evaluator.evaluate(strValue);
			} catch (UndefinedSymbolException e) {
				value = 0xFFFF;  // assuming a non-zero page address for forward reference
			} catch (SymbolHasNoValueException e) {
				value = 0xFFFF;  // patch for pass 1
			} catch (ExpressionException e) {
				throw e;
			}
			if (arg.endsWith("X")) {
				boolean hasZeroPageMode = hasOpCode(op, ZEROPAGEX);
				return (value <= 0xFF && hasZeroPageMode) ? ZEROPAGEX : ABSOLUTEX;
			} else if (arg.endsWith("Y")) {
				boolean hasZeroPageMode = hasOpCode(op, ZEROPAGEY);
				return (value <= 0xFF && hasZeroPageMode) ? ZEROPAGEY : ABSOLUTEY;
			}
		} else if (arg.startsWith("#")) {
			return IMMEDIATE;
		} else if (arg.equals("A")) {
			return ACCUMULATOR;
		} else if (arg.equals("")) {
			return IMPLIED;
		} else if (op.startsWith("B") && (op.equals("BIT") == false) && (op.equals("BRK") == false)) {
			return RELATIVE;
		} else {
			int value;
			try {
				value = this.evaluator.evaluate(arg);
			} catch (UndefinedSymbolException e) {
				value = 0xFFFF;  // assuming a non-zero page address for forward reference
			} catch (SymbolHasNoValueException e) {
				value = 0xFFFF;  // patch for pass 1
			} catch (ExpressionException e) {
				throw e;
			}
			return (value <= 0xFF) ? ZEROPAGE : ABSOLUTE;
		}
		return UNKNOWN;
	}

	private int getNumBytes(String op, String arg) throws AssemblerException, ExpressionException {
		int numBytes = 0;
		if (op.equals(".BYTE")) {
			String[] tokens = tokenizeByteArgList(arg);
			if (tokens.length == 0) {
				throw new CannotParseByteException(arg);
			}
			for (String token : tokens) {
				if (token.startsWith("\"")) {
					numBytes += token.length() - 2;
				} else {
					numBytes++;
				}
			}
		} else if (op.equals(".WORD")) {
			StringTokenizer st = new StringTokenizer(arg, ",");
			numBytes = st.countTokens() * 2;
		} else if (isOpMemnonic(op)) {
			numBytes = INSTRUCTION_LENGTH[getOpMode(op, arg)];
		}
		return numBytes;
	}

	/*
	 * ^            | Start of string
	 * \s*+         | Match any whitespace, consumed possessively
	 * (            | Start capture group 1
	 * "            | Match opening double quote (")
	 * [^"]*?       | Match any characters that are not a double quote ("), consumed lazily
	 * "            | Match closing double quote (")
	 * |            | ..or..
	 * (?:          | Start unnamed capture group
	 * '.           | Match a single quote and any character
	 * |            | ..or..
	 * [^"',\s]     | ..match one character that is not a double quote ("), a single quote ('), a comma (,), or a whitespace
	 * )+?          | End unnamed capture group, match characters of this group one or more times, consumed lazily
	 * )            | End capture group 1
	 * \s*?         | Match any whitespace, consumed lazily
	 * (?:          | Start unnamed capture group
	 * ,            | Match comma (,)..
	 * |            | ..or..
	 * $            | ..end of string
	 * )            | End unnamed capture group
	 */
	final static private Pattern ARG_PATTERN = Pattern.compile("^\\s*+(\"[^\"]*?\"|(?:'.|[^\"',\\s])+?)\\s*?(?:,|$)");

	// public visibility for tests
	public static String[] tokenizeByteArgList(String str) {
		List<String> tokens = new ArrayList<String>();

		if (str.endsWith("'")) {
			str = str + " ";  // expand trailing single quote to indicate a space (" ") character literal
		}

		int posStart = 0;
		int posEnd = str.length();
		Matcher matcher = ARG_PATTERN.matcher(str);
		while (matcher.hitEnd() == false) {
			if (matcher.region(posStart, posEnd).lookingAt()) {
				tokens.add(matcher.group(1));
				posStart = matcher.end();
			} else {
				return new String[0];  // error, return an empty array
			}
		}
		return tokens.toArray(new String[tokens.size()]);
	}

	private byte[] getBytes(String op, String arg, int pc) throws AssemblerException, ExpressionException, IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		if (op.equals(".BYTE")) {
			String[] tokens = tokenizeByteArgList(arg);
			if (tokens.length == 0) {
				throw new CannotParseByteException(arg);
			}
			for (String token : tokens) {
				if (token.startsWith("\"")) {
					byte[] bytes = token.substring(1,  token.length() - 1).getBytes();
					writeBytes(out, bytes);
				} else {
					int value = this.evaluator.evaluate(token);
					writeByte(out, value);
				}
			}
		} else if (op.equals(".WORD")) {
			StringTokenizer st = new StringTokenizer(arg, ",");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				int value = this.evaluator.evaluate(token);
				writeWord(out, value);
			}
		} else if (isOpMemnonic(op)) {
			int opMode = getOpMode(op, arg);
			writeByte(out, getOpCode(op, opMode));

			String argExpr;
			int value;
			switch (opMode) {
				case ZEROPAGE:
				case ZEROPAGEX:
				case ZEROPAGEY:
				case INDEXIND:
				case INDINDEX:
				case IMMEDIATE:
					argExpr = getArgExpression(arg, opMode);
					value = this.evaluator.evaluate(argExpr);
					writeByte(out, value);
					break;
				case RELATIVE:
					value = this.evaluator.evaluate(arg);
					int relAddress = value - (pc + 2);
					if (relAddress > 127 || relAddress < -128) {
						throw new BranchRangeException(relAddress);
					}
					writeByte(out, relAddress);
					break;
				case ABSOLUTE:
				case ABSOLUTEX:
				case ABSOLUTEY:
				case INDIRECT:
					argExpr = getArgExpression(arg, opMode);
					value = this.evaluator.evaluate(argExpr);
					writeWord(out, value);
					break;
			}
		}
		closeGracefully(out);
		return out.toByteArray();
	}

	private String getArgExpression(String arg, int opMode) {
		int posStart = 0;
		int posEnd = arg.length();

		switch (opMode) {
			case ZEROPAGEX:
			case ABSOLUTEX:
			case ZEROPAGEY:
			case ABSOLUTEY:
				posEnd = arg.indexOf(",");
				break;
			case INDEXIND:
				posStart = arg.indexOf("(") + 1;
				posEnd = arg.indexOf(",X)");
				break;
			case INDINDEX:
				posStart = arg.indexOf("(") + 1;
				posEnd = arg.indexOf("),Y");
				break;
			case IMMEDIATE:
				posStart = arg.indexOf("#") + 1;
				break;
			case INDIRECT:
				posStart = arg.indexOf("(") + 1;
				posEnd = arg.indexOf(")");
				break;
		}
		return arg.substring(posStart, posEnd);
	}

	private static String toHexString(byte[] bytes) {
		StringBuffer sb = new StringBuffer(bytes.length * 2);
		for (int aByte : bytes) {
			String hexString = String.format("%02X", aByte & 0xFF);
			sb.append(hexString);
		}
		return sb.toString();
	}

	private static String toHexString(int value) {
		return String.format("%04X", value & 0xFFFF);
	}

	private static String formatDate(Calendar date) {
		return String.format(Locale.US, "%1$td-%1$tb-%1$tY", date);
	}
}
