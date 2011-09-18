/*
 * Copyright (c) 2011, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://www.boofcv.org).
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

package boofcv.alg.transform.wavelet;

import boofcv.abst.wavelet.WaveletTransform;
import boofcv.alg.misc.GPixelMath;
import boofcv.core.image.ConvertBufferedImage;
import boofcv.core.image.border.BorderType;
import boofcv.factory.transform.wavelet.FactoryWaveletTransform;
import boofcv.factory.transform.wavelet.GFactoryWavelet;
import boofcv.gui.ListDisplayPanel;
import boofcv.gui.ProcessImage;
import boofcv.gui.SelectAlgorithmImagePanel;
import boofcv.gui.image.ShowImages;
import boofcv.gui.image.VisualizeImageData;
import boofcv.io.image.ImageListManager;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.wavelet.WaveletDescription;
import boofcv.struct.wavelet.WlCoef;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;


/**
 * @author Peter Abeles
 */
public class WaveletVisualizeApp
		<T extends ImageBase, W extends ImageBase, C extends WlCoef>
		extends SelectAlgorithmImagePanel implements ProcessImage
{
	int numLevels = 3;

	T image;
	T imageInv;

	Class<T> imageType;

	ListDisplayPanel panel = new ListDisplayPanel();
	boolean processedImage = false;

	public WaveletVisualizeApp(Class<T> imageType ) {
		super(1);
		this.imageType = imageType;

		addWaveletDesc("Haar",GFactoryWavelet.haar(imageType));
		addWaveletDesc("Daub 4", GFactoryWavelet.daubJ(imageType,4));
		addWaveletDesc("Bi-orthogonal 5",GFactoryWavelet.biorthogoal(imageType,5, BorderType.REFLECT));
		addWaveletDesc("Coiflet 6",GFactoryWavelet.coiflet(imageType,6));

		setMainGUI(panel);
	}

	public void process( BufferedImage input ) {
		setInputImage(input);
		
		image = ConvertBufferedImage.convertFrom(input,null,imageType);
		imageInv = (T)image._createNew(image.width,image.height);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setPreferredSize(new Dimension(image.width+50,image.height+20));
				processedImage = true;
			}});

		doRefreshAll();
	}

	@Override
	public void refreshAll(Object[] cookies) {
		setActiveAlgorithm(0,null,cookies[0]);
	}

	private void addWaveletDesc( String name , WaveletDescription desc )
	{
		if( desc != null )
			addAlgorithm(0, name,desc);
	}

	@Override
	public void setActiveAlgorithm(int indexFamily, String name, Object cookie) {
		if( image == null )
			return;

		WaveletDescription<C> desc = (WaveletDescription<C>)cookie;
		WaveletTransform<T,W,C> waveletTran = FactoryWaveletTransform.create(desc,numLevels);

		panel.reset();

		W imageWavelet = waveletTran.transform(image,null);

		waveletTran.invert(imageWavelet,imageInv);

		float maxValue = (float)GPixelMath.maxAbs(imageWavelet);
		BufferedImage buffWavelet = VisualizeImageData.grayMagnitude(imageWavelet,null,maxValue);
		BufferedImage buffInv = ConvertBufferedImage.convertTo(imageInv,null);

		panel.addImage(buffWavelet,"Transform");
		panel.addImage(buffInv,"Inverse");
	}

	@Override
	public void changeImage(String name, int index) {
		ImageListManager manager = getImageManager();

		BufferedImage image = manager.loadImage(index);
		if( image != null ) {
			process(image);
		}
	}

	@Override
	public boolean getHasProcessedImage() {
		return processedImage;
	}

	public static void main( String args[] ) {
		BufferedImage in = UtilImageIO.loadImage("data/standard/lena512.bmp");
		WaveletVisualizeApp app = new WaveletVisualizeApp(ImageFloat32.class);
//		WaveletVisualizeApp app = new WaveletVisualizeApp(ImageUInt8.class);

		ImageListManager manager = new ImageListManager();
		manager.add("lena","data/standard/lena512.bmp");
		manager.add("boat","data/standard/boat.png");
		manager.add("fingerprint","data/standard/fingerprint.png");

		app.setImageManager(manager);

		// wait for it to process one image so that the size isn't all screwed up
		while( !app.getHasProcessedImage() ) {
			Thread.yield();
		}

		ShowImages.showWindow(app,"Wavelet Transforms");
	}
}