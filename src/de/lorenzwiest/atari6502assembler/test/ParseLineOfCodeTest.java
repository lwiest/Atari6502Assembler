package de.lorenzwiest.atari6502assembler.test;

import org.junit.Test;

import de.lorenzwiest.atari6502assembler.Atari6502Assembler;
import de.lorenzwiest.atari6502assembler.Atari6502Assembler.LineOfCode;

public class ParseLineOfCodeTest {

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

		test2("xxx \"' ; Comment", 7);
		test2("xxx \"'; Comment", 6);

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
