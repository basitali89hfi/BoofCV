/*
 * Copyright (c) 2011-2014, Peter Abeles. All Rights Reserved.
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

package boofcv.abst.segmentation;

import boofcv.alg.segmentation.ImageSegmentationOps;
import boofcv.alg.segmentation.fh04.SegmentFelzenszwalbHuttenlocher04;
import boofcv.struct.ConnectRule;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageSInt32;

/**
 * Wrapper around {@link SegmentFelzenszwalbHuttenlocher04} for {@link ImageSegmentation}.
 *
 * @author Peter Abeles
 */
public class Fh04_to_ImageSegmentation<T extends ImageBase> implements ImageSegmentation<T> {

	SegmentFelzenszwalbHuttenlocher04<T> alg;
	ConnectRule rule;

	ImageSInt32 pixelToSegment = new ImageSInt32(1,1);

	public Fh04_to_ImageSegmentation(SegmentFelzenszwalbHuttenlocher04<T> alg, ConnectRule rule) {
		this.alg = alg;
		this.rule = rule;
	}

	@Override
	public void segment(T input, ImageSInt32 output) {

		pixelToSegment.reshape(input.width, input.height);

		alg.process(input,pixelToSegment);

		ImageSegmentationOps.regionPixelId_to_Compact(pixelToSegment, alg.getRegionId(), output);
	}

	@Override
	public int getTotalSegments() {
		return alg.getRegionSizes().size();
	}

	@Override
	public ConnectRule getRule() {
		return rule;
	}
}
