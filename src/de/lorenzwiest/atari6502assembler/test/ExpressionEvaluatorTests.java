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

package de.lorenzwiest.atari6502assembler.test;

import org.junit.Test;

import de.lorenzwiest.atari6502assembler.exception.ExpressionException;
import de.lorenzwiest.atari6502assembler.expression.ExpressionEvaluator;

public class ExpressionEvaluatorTests {

	final private static int ERROR = -1;

	@Test
	public void testEvaluate() {
		test("1", 1);
		test("1+1", 2);
		test("1 + 1", 2);
		test(" 1 + 1 ", 2);

		test("1-1", 0);
		test("1 1", ERROR);
		test("',100", ERROR);

		test("2*3", 6);
		test("4/2", 2);
		test("4/3", 1);
		test("1/0", ERROR);

		test("2*3+4*5", 26);
		test("2*[3+4]*5", 70);
		test("-2*[3+4]*5", 65466);
		test("-2*[3+4]*-5", 70);
		test("-[-3-1]-3", 1);
		test("-2--2--5+3", 8);

		test(">$1234", 0x12);
		test("<$1234", 0x34);
		test(">$1034+<$1210", 0x20);

		test("%1", 1);
		test("%1111", 15);

		test("3!2", 3);
		test("6&3", 2);

		test("$1", 1);
		test("$01", 1);
		test("$001", 1);
		test("$0001", 1);

		test("$FF", 255);
		test("$FFFF", 65535);
		test("$FFFFFF", ERROR);
		test("65536+2", 2);
		test("65538", 2);

		test("'A", 65);
		test("'A+1", 66);
		test("1+'A", 66);
		test("'", ERROR);
		test("1+'", ERROR);
		test("' ", 32);
		test("' +1", 33);
		test("'  + 1", 33);

		test("1[", ERROR);
		test("+1", ERROR);
		test("++1", ERROR);
		test("1+[1", ERROR);
		test("1++1", ERROR);

		test("-", ERROR);
		test(">", ERROR);
		test("<", ERROR);
		test("!", ERROR);
		test("+", ERROR);
		test("*", ERROR);
		test("[]", ERROR);

		test("$FF00+4096", 0x0F00);
		test("$FF00&$00FF", 0x0000);
		test("$03!$0A", 0x000B);
		test("$003F^$011F", 0x0120);
		test("-2", 0xFFFE);
		test("<$3456", 0x56);
		test(">$3456", 0x34);
		test("<[<$AA55^-1]+1", 0xAB);
		test(">$45FE+3", 0x48);
		test(">[$45FE+3]", 0x46);
		test(" 'A", 65);

		test("1+SAMPLE", ERROR);
		test("1!", ERROR);
		test("1+2/", ERROR);
	}

	private void test(String actualInput, int expected) {
		int actual = ERROR;
		try {
			actual = new ExpressionEvaluator(null).evaluate(actualInput);
		} catch (ExpressionException e) {
			// empty
		}
		org.junit.Assert.assertEquals(expected, actual);
	}
}
