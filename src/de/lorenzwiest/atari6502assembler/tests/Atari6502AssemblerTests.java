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

public class Atari6502AssemblerTests {

	private final static String[] EMPTY_ARRAY = new String[0];

	@Test
	public void testTokenizeByteArgList() {
		test("',-100", new String[] {"',-100"});

		test("100", new String[] {"100"});
		test("100,200", new String[] {"100", "200"});
		test("100,200,300", new String[] {"100", "200", "300"});
		test(" 100,200,300", new String[] {"100", "200", "300"});
		test(" 100 , 200 , 300 ", new String[] {"100", "200", "300"});

		test("100,", EMPTY_ARRAY);
		test(",100", EMPTY_ARRAY);
		test(" 100 , ", EMPTY_ARRAY);
		test(" , 100 , ", EMPTY_ARRAY);
		test("100,,", EMPTY_ARRAY);

		test("\"XXX\"", new String[] {"\"XXX\""});
		test("\"XX,X\"", new String[] {"\"XX,X\""});
		test("\"XXX\",", EMPTY_ARRAY);
		test(",\"XXX\"", EMPTY_ARRAY);
		test(" \"XXX\" , ", EMPTY_ARRAY);
		test(" , \"XXX\" , ", EMPTY_ARRAY);
		test("\"XXX\",,", EMPTY_ARRAY);

		test("'A", new String[] {"'A"});
		test("'A,'B", new String[] {"'A", "'B"});
		test("'A,'B,'C", new String[] {"'A", "'B", "'C"});
		test(" 'A,'B,'C", new String[] {"'A", "'B", "'C"});
		test(" 'A , 'B , 'C ", new String[] {"'A", "'B", "'C"});

		test("'A,", EMPTY_ARRAY);
		test(",'A", EMPTY_ARRAY);
		test(" 'A   , ", EMPTY_ARRAY);
		test(" , 'A , ", EMPTY_ARRAY);
		test("'A,,", EMPTY_ARRAY);

		test("'A,100,\"XXX\"", new String[] {"'A", "100", "\"XXX\""});
		test("100,'A,\"XXX\"", new String[] {"100", "'A", "\"XXX\""});
		test("\"XXX\", 100,'A", new String[] {"\"XXX\"", "100", "'A"});

		test("''", new String[] {"''"});
		test("'',100", new String[] {"''", "100"});
		test("'' , 100", new String[] {"''", "100"});

		test("',", new String[] {"',"});
		test("'\"", new String[] {"'\""});
		test("' ", new String[] {"' "});

		test("',,100", new String[] {"',", "100"});
		test("' ,100", new String[] {"' ", "100"});

		test("1+1", new String[] {"1+1"});
		test("1+'1", new String[] {"1+'1"});
		test("'1+1", new String[] {"'1+1"});
		test("',100", new String[] {"',100"}); // Lexing OK, but evaluation-wise wrong

		test("NEG!'A,100", new String[] {"NEG!'A", "100"});
		test("'A+$20 , 100", new String[] {"'A+$20", "100"});

		test("',\"xxx\"", EMPTY_ARRAY);
		test(",\"xxx\"", EMPTY_ARRAY);
		test("\",100", EMPTY_ARRAY);

		test("'", new String[] {"' "});
		test("\"'", EMPTY_ARRAY);
		test("\",", EMPTY_ARRAY);

	}

	private void test(String actualInput, String[] expected) {
		String[] actual = Atari6502Assembler.tokenizeByteArgList(actualInput);
		if (actual.length == 0) {
			actual = EMPTY_ARRAY;
		}
		org.junit.Assert.assertEquals(expected, actual);
	}
}
