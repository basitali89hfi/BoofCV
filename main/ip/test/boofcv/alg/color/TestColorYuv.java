/*
 * Copyright (c) 2011-2013, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://boofcv.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package boofcv.alg.color;

import boofcv.alg.misc.GImageMiscOps;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.MultiSpectral;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Peter Abeles
 */
public class TestColorYuv {
	public static final double tol = 0.01;

	Random rand = new Random(234);

	double yuv_F64[] = new double[3];
	double rgb_F64[] = new double[3];

	float yuv_F32[] = new float[3];
	float rgb_F32[] = new float[3];

	@Test
	public void backAndForth_F64_and_F32() {

		check(0.5, 0.3, 0.2);
		check(0, 0, 0);
		check(1, 0, 0);
		check(0, 1, 0);
		check(0, 0, 1);
		check(0.5, 0.5, 0.5);
		check(0.25, 0.5, 0.75);
		check(0.8, 0.1, 0.75);

		for( int i = 0; i < 50; i++ ) {
			double r = rand.nextDouble();
			double g = rand.nextDouble();
			double b = rand.nextDouble();

			check(r,g,b);
		}

		// check other ranges
		for( int i = 0; i < 50; i++ ) {
			double r = rand.nextDouble()*255;
			double g = rand.nextDouble()*255;
			double b = rand.nextDouble()*255;

			check(r,g,b);
		}
	}

	private void check( double r , double g , double b ) {
		// check F64
		ColorYuv.rgbToYuv(r,g,b, yuv_F64);
		ColorYuv.yuvToRgb(yuv_F64[0], yuv_F64[1], yuv_F64[2], rgb_F64);
		check(rgb_F64,r,g,b);

		// Check F32
		float fr = (float)r, fg = (float)g, fb = (float)b;
		ColorYuv.rgbToYuv(fr,fg,fb, yuv_F32);
		ColorYuv.yuvToRgb(yuv_F32[0], yuv_F32[1], yuv_F32[2], rgb_F32);
		check(rgb_F32,fr,fg,fb);
	}

	@Test
	public void backAndForth_U8() {
		fail("Implement once format is understood");
	}

	@Test
	public void multispectral_F32() {
		MultiSpectral<ImageFloat32> rgb = new MultiSpectral<ImageFloat32>(ImageFloat32.class,10,15,3);
		MultiSpectral<ImageFloat32> hsv = new MultiSpectral<ImageFloat32>(ImageFloat32.class,10,15,3);
		MultiSpectral<ImageFloat32> found = new MultiSpectral<ImageFloat32>(ImageFloat32.class,10,15,3);

		GImageMiscOps.fillUniform(rgb, rand, 0, 1);

		ColorYuv.yuvToRgb_F32(rgb, hsv);
		ColorYuv.rgbToYuv_F32(hsv, found);

		for( int y = 0; y < rgb.height; y++ ) {
			for( int x = 0; x < rgb.width; x++ ) {
				float r = rgb.getBand(0).get(x,y);
				float g = rgb.getBand(1).get(x,y);
				float b = rgb.getBand(2).get(x,y);

				assertEquals(r,found.getBand(0).get(x,y),tol);
				assertEquals(g,found.getBand(1).get(x,y),tol);
				assertEquals(b,found.getBand(2).get(x,y),tol);
			}
		}
	}

	private static void check( double found[] , double a , double b , double c ) {
		double tol = TestColorYuv.tol * Math.max(Math.max(a,b),c);

		assertEquals(a,found[0],tol);
		assertEquals(b,found[1],tol);
		assertEquals(c, found[2], tol);
	}

	private static void check( float found[] , float a , float b , float c ) {
		double tol = TestColorYuv.tol * Math.max(Math.max(a,b),c);

		assertEquals(a,found[0],tol);
		assertEquals(b,found[1],tol);
		assertEquals(c, found[2], tol);
	}
}
