/*
 * Copyright 2011 Peter Abeles
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package gecv.struct;

import jgrl.struct.point.Point2D_I32;
import org.junit.Test;

import static org.junit.Assert.*;


/**
 * @author Peter Abeles
 */
public class TestFastArray {
	@Test
	public void get_pop() {
		FastArray<Point2D_I32> alg = new FastArray<Point2D_I32>(Point2D_I32.class);

		// test a failure case
		try {
			alg.get(0);
			fail("Didn't fail");
		} catch( IllegalArgumentException e ) {}

		alg.pop();
		alg.get(0);
	}

	@Test
	public void size() {
		FastArray<Point2D_I32> alg = new FastArray<Point2D_I32>(Point2D_I32.class);
		assertEquals(0,alg.size);
		alg.pop();
		assertEquals(1,alg.size);
	}

	/**
	 * Checks to see if pop automatically grows correctly
	 */
	@Test
	public void pop_grow() {
		FastArray<Point2D_I32> alg = new FastArray<Point2D_I32>(1,Point2D_I32.class);

		int before = alg.getInternalArraySize();
		for( int i = 0; i < 20; i++ ) {
			alg.pop();
		}
		alg.get(19);
		int after = alg.getInternalArraySize();
		assertTrue(after>before);
	}

	@Test
	public void growArray() {
		FastArray<Point2D_I32> alg = new FastArray<Point2D_I32>(1,Point2D_I32.class);

		alg.pop().set(10,12);
		int before = alg.getInternalArraySize();
		alg.growArray(before+5);
		assertEquals(10,alg.get(0).getX());
	}
}
