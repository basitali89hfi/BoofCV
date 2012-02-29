/*
 * Copyright (c) 2011-2012, Peter Abeles. All Rights Reserved.
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

package boofcv.alg.geo.calibration;

import boofcv.alg.feature.detect.chess.DetectChessCalibrationPoints;
import boofcv.alg.feature.detect.quadblob.DetectQuadBlobsBinary;
import boofcv.core.image.ConvertBufferedImage;
import boofcv.core.image.GeneralizedImageOps;
import boofcv.gui.ProcessInput;
import boofcv.gui.SelectImagePanel;
import boofcv.gui.binary.VisualizeBinaryData;
import boofcv.gui.feature.VisualizeFeatures;
import boofcv.gui.image.ImageZoomPanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.ConfigureReaderInterface;
import boofcv.io.SimpleNumberSequenceReader;
import boofcv.io.image.ImageListManager;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSingleBand;
import georegression.struct.point.Point2D_F64;
import georegression.struct.point.Point2D_I32;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

/**
 * @author Peter Abeles
 */
public class DetectCalibrationChessApp<T extends ImageSingleBand, D extends ImageSingleBand>
		extends SelectImagePanel implements ProcessInput, GridCalibPanel.Listener ,
		ConfigureReaderInterface
		
{
	Class<T> imageType;
	DetectChessCalibrationPoints<T,D> alg;

	GridCalibPanel calibGUI;
	ImageZoomPanel gui = new ImageZoomPanel();

	// work buffer that stuff is drawn inside of and displayed
	BufferedImage workImage;
	// original untainted image
	BufferedImage input;
	// gray scale image that targets are detected inside of
	T gray;

	boolean foundTarget;
	boolean processed = false;
	
	public DetectCalibrationChessApp(Class<T> imageType) {
		this.imageType = imageType;

		gray = GeneralizedImageOps.createSingleBand(imageType,1,1);

		JPanel panel = new JPanel();
		panel.setLayout( new BorderLayout());

		calibGUI = new GridCalibPanel(false);
		calibGUI.setListener( this );
		calibGUI.setMinimumSize(calibGUI.getPreferredSize());
		
		panel.add(gui,BorderLayout.CENTER);
		panel.add(calibGUI,BorderLayout.WEST);
		
		setMainGUI(panel);
	}

	public void configure( int numCols , int numRows ) {
		alg = new DetectChessCalibrationPoints<T,D>(numCols,numRows,5,20,255,imageType);
	}

	@Override
	public void configure(Reader input) {
		SimpleNumberSequenceReader reader = new SimpleNumberSequenceReader('#');
		try {
			List<Double> s = reader.read(input);

			int numCols = s.get(0).intValue();
			int numRows = s.get(1).intValue();

			configure(numCols,numRows);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public synchronized void process( final BufferedImage input ) {
		this.input = input;
		workImage = new BufferedImage(input.getWidth(),input.getHeight(),BufferedImage.TYPE_INT_RGB);

		gray.reshape(input.getWidth(),input.getHeight());
		ConvertBufferedImage.convertFrom(input, gray);

		detectTarget();

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				gui.setPreferredSize(new Dimension(input.getWidth(), input.getHeight()));
				renderOutput();
			}
		});
	}

	private void detectTarget() {
		foundTarget = alg.process(gray);
		calibGUI.setThreshold((int)alg.getSelectedThreshold());
	}

	private void renderOutput() {
		switch( calibGUI.getSelectedView() ) {
			case 0:
				workImage.createGraphics().drawImage(input,null,null);
				break;

			case 1:
				VisualizeBinaryData.renderBinary(alg.getBinary(), workImage);
				break;

			case 2:
				DetectQuadBlobsBinary detectBlobs = alg.getFindBound().getDetectBlobs();

				int numLabels = detectBlobs.getNumLabels();
				VisualizeBinaryData.renderLabeled(detectBlobs.getLabeledImage(),numLabels,workImage);
				break;

			default:
				throw new RuntimeException("Unknown mode");
		}
		Graphics2D g2 = workImage.createGraphics();
		if( foundTarget ) {
			if( calibGUI.isShowPoints() ) {
				List<Point2D_F64> candidates =  alg.getPoints();
				for( Point2D_F64 c : candidates ) {
					VisualizeFeatures.drawPoint(g2, (int)c.x, (int)c.y, 2, Color.RED);
				}
			}

			if( calibGUI.isShowBound() ) {
				List<Point2D_I32> boundary =  alg.getFindBound().getBoundingQuad();
				DetectCalibrationSquaresApp.drawBounds(g2,boundary);
			}
			
			if( calibGUI.doShowGraph ) {
				DetectCalibrationSquaresApp.drawGraph(g2,alg.getFindBound().getGraphBlobs());
			}
			
			if( calibGUI.isShowNumbers() ) {
				drawNumbers(g2, alg.getPoints());
			}
		}

		gui.setBufferedImage(workImage);
		gui.setScale(calibGUI.getScale());
		gui.repaint();

		processed = true;
	}

	public static void drawNumbers( Graphics2D g2 , java.util.List<Point2D_F64> foundTarget ) {

		Font regular = new Font("Serif", Font.PLAIN, 16);
		g2.setFont(regular);

		AffineTransform origTran = g2.getTransform();
		for( int i = 0; i < foundTarget.size(); i++ ) {
			Point2D_F64 p = foundTarget.get(i);
			String text = String.format("%2d",i);

			int x = (int)p.x;
			int y = (int)p.y;

			g2.setColor(Color.BLACK);
			g2.drawString(text,x-1,y);
			g2.drawString(text,x+1,y);
			g2.drawString(text,x,y-1);
			g2.drawString(text,x,y+1);
			g2.setTransform(origTran);
			g2.setColor(Color.GREEN);
			g2.drawString(text,x,y);
		}
	}

	@Override
	public boolean getHasProcessedImage() {
		return processed;
	}

	@Override
	public void changeImage(String name, int index) {
		ImageListManager manager = getInputManager();

		BufferedImage image = manager.loadImage(index);
		if (image != null) {
			process(image);
		}
	}

	@Override
	public void calibEventGUI() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				renderOutput();
			}
		});
	}

	@Override
	public void calibEventProcess() {
		detectTarget();

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				gui.setPreferredSize(new Dimension(input.getWidth(), input.getHeight()));
				renderOutput();
			}
		});
	}

	public static void main(String args[]) throws FileNotFoundException {

		DetectCalibrationChessApp app = new DetectCalibrationChessApp(ImageFloat32.class);

		String prefix = "../data/evaluation/calibration/mono/Sony_DSC-HX5V_Chess/";

		app.configure(new FileReader(prefix+"info.txt"));

		ImageListManager manager = new ImageListManager();
		manager.add("View 01",prefix+"frame01.jpg");
		manager.add("View 02",prefix+"frame02.jpg");
		manager.add("View 03",prefix+"frame03.jpg");
		manager.add("View 04",prefix+"frame04.jpg");
		manager.add("View 05",prefix+"frame05.jpg");
		manager.add("View 06",prefix+"frame06.jpg");
		manager.add("View 07",prefix+"frame07.jpg");
		manager.add("View 08",prefix+"frame08.jpg");

		app.setInputManager(manager);

		// wait for it to process one image so that the size isn't all screwed up
		while( !app.getHasProcessedImage() ) {
			Thread.yield();
		}

		ShowImages.showWindow(app, "Calibration Target Detection");
	}
}
