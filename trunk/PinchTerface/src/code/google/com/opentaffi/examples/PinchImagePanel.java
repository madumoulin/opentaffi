package code.google.com.opentaffi.examples;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JPanel;

import code.google.com.opentaffi.Pinch;
import code.google.com.opentaffi.PinchEngine;
import code.google.com.opentaffi.PinchListener;


/**
 * This JPanel displays an image that can be translated, rotated and scaled base
 * the output of the PinchEngine.
 * 
 * @author Luc Trudeau
 * @date 07/03/2012
 * 
 */
public class PinchImagePanel extends JPanel implements PinchListener {

	private static final long serialVersionUID = 1L;

	private final BufferedImage image;
	private final PinchEngine engine;

	/**
	 * Old values are used to measure deltas
	 */
	private int oldX;
	/**
	 * Old values are used to measure deltas
	 */
	private int oldY;
	/**
	 * Old values are used to measure deltas
	 */
	private double oldS;

	/**
	 * Position of the image along x axis
	 */
	private int x;
	/**
	 * Position of the image along y axis
	 */
	private int y;

	/**
	 * Scaling factor of the image
	 */
	private double scale;

	/**
	 * Rotation angle of the image in radiant
	 */
	private double angle;

	/**
	 * Indicates if it's the first pinch
	 */
	private boolean first;

	/**
	 * Indicates if it's the first double pinch
	 */
	private boolean firstDouble;

	/**
	 * Precompute half width and half height
	 */
	private final int halfWidth;

	/**
	 * Precompute half height
	 */
	private final int halfHeight;

	/**
	 * 
	 * @param image
	 *            Image that will be displayed
	 * @param width
	 *            Width of the panel (needed to center image)
	 * @param height
	 *            Height of the panel (needed to center image)
	 * @param showHands
	 *            Show the hands seen by the pinch engine
	 * @param deviceId
	 *            Id of your camera (default = 0);
	 */
	public PinchImagePanel(BufferedImage image, int width, int height,
			boolean showHands, int deviceId) {
		setSize(width, height);
		halfWidth = image.getWidth() / 2;
		halfHeight = image.getHeight() / 2;
		this.image = image;
		engine = new PinchEngine(deviceId);

		if (showHands) {
			engine.show();
		}

		engine.addPinchListener(this);
		setDoubleBuffered(true);
		setBackground(Color.WHITE);
		new Thread(engine).start();

		reset();
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;

		AffineTransform xform = new AffineTransform();
		xform.translate(x, y);
		xform.rotate(angle, halfWidth, halfHeight);
		xform.scale(scale, scale);
		g2d.drawImage(image, xform, this);
	}

	@Override
	public void pinch(List<Pinch> pinches) {
		switch (pinches.size()) {
		case 0:
			reset();
			break;
		case 1:
			// Reset double pinch
			firstDouble = true;
			int newX = pinches.get(0).x;
			int newY = pinches.get(0).y;

			if (first) {
				first = false;
			} else {
				x += newX - oldX;
				y += newY - oldY;
			}
			oldX = newX;
			oldY = newY;
			break;
		case 2:
			// Reset pinch
			first = true;

			int newX1 = pinches.get(0).x;
			int newX2 = pinches.get(1).x;
			int newY1 = pinches.get(0).y;
			int newY2 = pinches.get(1).y;

			double newS = 0;
			if (newX2 >= newX1) {
				angle = Math.atan2(newY2 - newY1, newX2 - newX1);
				newS = (newX2 - newX1) / 1000.0;
			} else {
				angle = Math.atan2(newY1 - newY2, newX1 - newX2);
				newS = (newX1 - newX2) / 1000.0;
			}

			if (firstDouble) {
				firstDouble = false;
			} else {
				scale += newS - oldS;
			}

			oldS = newS;
			break;
		}
		this.repaint();
	}

	private void reset() {
		x = getWidth() / 2 - halfWidth;
		y = getHeight() / 2 - halfHeight;
		angle = 0;
		first = true;
		firstDouble = true;
		scale = 1;
	}
}