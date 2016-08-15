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

package de.lorenzwiest.atari6502assembler.tests;

import org.junit.Test;

import de.lorenzwiest.atari6502assembler.Atari6502Assembler;
import de.lorenzwiest.atari6502assembler.Atari6502Assembler.LineOfCode;

public class ParseLineOfCodeTests {

	@Test
	public void testParseLineOfCode() {
		test1("1000 ; comment", new String[]{"1000", "", "", "", "; comment"});
		test1("1000 LABEL ; comment", new String[]{"1000", "LABEL", "", "", "; comment"});
		test1("1000 LABEL CLC ; comment", new String[]{"1000", "LABEL", "CLC", "", "; comment"});
		test1("1000 LABEL LDA #3 ; comment", new String[]{"1000", "LABEL", "LDA", "#3", "; comment"});
		test1("1000  CLC ; comment", new String[]{"1000", "", "CLC", "", "; comment"});
		test1("1000  LDA #3 ; comment", new String[]{"1000", "", "LDA", "#3", "; comment"});
		test1("1000  CLC", new String[]{"1000", "", "CLC", "", ""});
		test1("1000  LDA #3", new String[]{"1000", "", "LDA", "#3", ""});
		test1("1000  CLC ", new String[]{"1000", "", "CLC", "", ""});
		test1("1000  LDA #3 ", new String[]{"1000", "", "LDA", "#3", ""});
		test1("1000 LABEL = $FFFF", new String[]{"1000", "LABEL", "=", "$FFFF", ""});
		test1("1000 LABEL =$FFFF", new String[]{"1000", "LABEL", "=", "$FFFF", ""});
		test1("1000 LABEL *= $FFFF", new String[]{"1000", "LABEL", "*=", "$FFFF", ""});
		test1("1000 LABEL *=$FFFF", new String[]{"1000", "LABEL", "*=", "$FFFF", ""});

		test1("1000   LDA #3 ", new String[]{"1000", "", "LDA", "#3", ""});
		test1(" 1000   LDA #3 ", new String[]{"1000", "", "LDA", "#3", ""});

		test1("; comment", new String[]{"", "", "", "", "; comment"});
		test1("LABEL ; comment", new String[]{"", "LABEL", "", "", "; comment"});
		test1("LABEL CLC ; comment", new String[]{"", "LABEL", "CLC", "", "; comment"});
		test1("LABEL LDA #3 ; comment", new String[]{"", "LABEL", "LDA", "#3", "; comment"});
		test1(" CLC ; comment", new String[]{"", "", "CLC", "", "; comment"});
		test1(" LDA #3 ; comment", new String[]{"", "", "LDA", "#3", "; comment"});
		test1(" CLC", new String[]{"", "", "CLC", "", ""});
		test1(" LDA #3", new String[]{"", "", "LDA", "#3", ""});
		test1(" CLC ", new String[]{"", "", "CLC", "", ""});
		test1(" LDA #3 ", new String[]{"", "", "LDA", "#3", ""});
		test1("LABEL = $FFFF", new String[]{"", "LABEL", "=", "$FFFF", ""});
		test1("LABEL =$FFFF", new String[]{"", "LABEL", "=", "$FFFF", ""});
		test1("LABEL *= $FFFF", new String[]{"", "LABEL", "*=", "$FFFF", ""});
		test1("LABEL *=$FFFF", new String[]{"", "LABEL", "*=", "$FFFF", ""});

		test1("   LDA #3 ", new String[]{"", "", "LDA", "#3", ""});
		test1(" = 1", new String[]{"", "", "=", "1", ""});

		test1(" .BYTE ';+$20 ; comment", new String[]{"", "", ".BYTE", "';+$20", "; comment"});
		test1(" .BYTE ';+$20   ", new String[]{"", "", ".BYTE", "';+$20", ""});
		test1(" .BYTE \"HEL;LO\",';+$20   ", new String[]{"", "", ".BYTE", "\"HEL;LO\",';+$20", ""});
		test1(" .BYTE 12, '\", \"Hello;world\", ';+$20, \";\" '; ;comment", new String[]{"", "", ".BYTE", "12, '\", \"Hello;world\", ';+$20, \";\" ';", ";comment"});
		test1(" .BYTE 12, '\", \"Hello;world\", ';+$20, \";\" '; comment", new String[]{"", "", ".BYTE", "12, '\", \"Hello;world\", ';+$20, \";\" '; comment", ""});

		test1("100", new String[]{"100", "", "", "", ""});
	}

	private void test1(String actualInput, String[] expected) {
		LineOfCode lineOfCode = Atari6502Assembler.LineOfCode.parse(actualInput);
		org.junit.Assert.assertEquals(expected[0], lineOfCode.getLineNumber());
		org.junit.Assert.assertEquals(expected[1], lineOfCode.getLabel());
		org.junit.Assert.assertEquals(expected[2], lineOfCode.getOp());
		org.junit.Assert.assertEquals(expected[3], lineOfCode.getArg());
		org.junit.Assert.assertEquals(expected[4], lineOfCode.getComment());
	}

	@Test
	public void testCommentPos() {
		test2("xxx 'A xxx ; Comment", 11);
		test2("xxx '' xxx ; Comment", 11);
		test2("xxx '\" xxx ; Comment", 11);
		test2("xxx '; xxx ; Comment", 11);

		test2("xxx \"' ; Comment", -1);
		test2("xxx \"'; Comment", -1);

		test2("xxx \"'\" ; Comment", 8);
		test2("xxx \";\" ; Comment", 8);
		test2("xxx \";'\" ; Comment", 9);
		test2("xxx \";\", \";\" ; Comment", 13);
		test2("xxx       ; Comment", 10);
		test2("xxx       ; ; Comment", 10);
		test2("xxx ''; Comment", 6);
	}

	private void test2(String actualInput, int expected) {
		int actual = Atari6502Assembler.LineOfCode.parse(actualInput).getCommentPos();
		org.junit.Assert.assertEquals(expected, actual);
	}
}
