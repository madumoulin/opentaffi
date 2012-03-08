package code.google.com.opentaffi;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvEllipseBox;
import static com.googlecode.javacv.cpp.opencv_core.cvFlip;
import static com.googlecode.javacv.cpp.opencv_core.cvReleaseImage;
import static com.googlecode.javacv.cpp.opencv_core.cvSub;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_CHAIN_APPROX_SIMPLE;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_GAUSSIAN;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_GRAY2RGB;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_RETR_TREE;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_THRESH_BINARY;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvFindContours;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvMinAreaRect2;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvSmooth;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvThreshold;

import java.io.Closeable;
import java.util.LinkedList;
import java.util.List;


import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.FrameGrabber.Exception;
import com.googlecode.javacv.FrameGrabber.ImageMode;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.CvBox2D;
import com.googlecode.javacv.cpp.opencv_core.CvContour;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;


/**
 * The PinchEngine scans camera input and finds pinching gestures.
 * 
 * @author Luc Trudeau
 * @date 07/03/2012
 */
public class PinchEngine implements Runnable, Closeable {

	private static final int DEFAULT_WIDTH = 800;
	private static final int DEFAULT_HEIGHT = 600;
	private static final double THRESHOLD = 1000;

	private CanvasFrame canvasFrame = null;
	private OpenCVFrameGrabber grabber;
	private boolean running = true;
	private final List<Pinch> pinches = new LinkedList<Pinch>();
	private final PinchEngineNotifier notifier = new PinchEngineNotifier();

	/**
	 * Default constructor, default values are
	 * DeviceId = 0
	 * Width = 800
	 * Height = 600
	 */
	public PinchEngine() {
		this(0);
	}

	/**
	 * 
	 * @param deviceId Id of the camera to use
	 */
	public PinchEngine(int deviceId) {
		this(deviceId, DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	/**
	 * 
	 * @param deviceId id of the camera to use
	 * @param width Width of the camera resolution
	 * @param height Height of the camera resolution
	 */
	public PinchEngine(int deviceId, int width, int height) {
		grabber = new OpenCVFrameGrabber(deviceId);
		grabber.setImageMode(ImageMode.GRAY);
		grabber.setBitsPerPixel(IPL_DEPTH_8U);
		grabber.setImageHeight(height);
		grabber.setImageWidth(width);
	}

	/**
	 * Displays the hands seen by the engine in a new frame.
	 */
	public void show() {
		canvasFrame = new CanvasFrame("Pinch Viewer");
		canvasFrame.setCanvasSize(grabber.getImageWidth(),
				grabber.getImageHeight());
	}

	/**
	 * @param listener Will be notified of pinches
	 */
	public void addPinchListener(PinchListener listener) {
		notifier.addListener(listener);
	}

	/**
	 * @param listener Will no longer be notified of pinches
	 */
	public void removePinchListener(PinchListener listener) {
		notifier.removeListener(listener);
	}

	@Override
	public void close() {
		if (running) {
			running = false;
		} else {
			dispose();
		}

	}

	private void dispose() {
		if (canvasFrame != null) {
			canvasFrame.dispose();
		}
	}

	@Override
	public void run() {

		Thread t = new Thread(notifier);
		t.start();

		IplImage frame = null;
		IplImage bg = null;
		IplImage diff = null;
		IplImage image = null;

		try {
			grabber.start();
			frame = grabber.grab();

			// First frame serves as background for background extraction.
			bg = frame.clone();
			// Smoothing removes noise and improves background extraction.
			cvSmooth(bg, bg, CV_GAUSSIAN, 9, 9, 2, 2);

			diff = IplImage.create(bg.width(), bg.height(), IPL_DEPTH_8U, 1);
			image = IplImage.create(bg.width(), bg.height(), IPL_DEPTH_8U, 3);
			CvMemStorage storage = CvMemStorage.create();

			while (running && (frame = grabber.grab()) != null) {
				if (canvasFrame != null && !canvasFrame.isVisible()) {
					close();
					break;
				}

				cvFlip(frame, frame, -1);
				cvSmooth(frame, frame, CV_GAUSSIAN, 9, 9, 2, 2);
				cvSub(frame, bg, diff, null);
				cvThreshold(diff, diff, 20, 255, CV_THRESH_BINARY);

				CvSeq contour = new CvSeq(null);
				cvFindContours(diff, storage, contour,
						Loader.sizeof(CvContour.class), CV_RETR_TREE,
						CV_CHAIN_APPROX_SIMPLE);

				if (canvasFrame != null) {
					// Display ellipses in color
					cvCvtColor(frame, image, CV_GRAY2RGB);
				}

				pinches.clear();

				findLeaf(contour, storage, image, 0);

				notifier.notifyListeners(pinches);

				if (canvasFrame != null) {
					canvasFrame.showImage(image);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				grabber.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
			t.interrupt();
			dispose();
			cvReleaseImage(bg);
			cvReleaseImage(diff);
			cvReleaseImage(frame);
			cvReleaseImage(image);
		}

	}

	private void findLeaf(CvSeq contour, CvMemStorage storage, IplImage image,
			int depth) {
		while (contour != null && !contour.isNull()) {
			if (contour.elem_size() > 0) {
				if (contour.v_next() != null) {
					findLeaf(contour.v_next(), storage, image, depth + 1);
				}

				drawBoundBox(contour, storage, image, depth);
			}
			contour = contour.h_next();
		}
	}

	private void drawBoundBox(CvSeq contour, CvMemStorage storage,
			IplImage image, int depth) {
		CvScalar color = CvScalar.BLUE;
		CvBox2D box = cvMinAreaRect2(contour, storage);
		if (box != null && box.size().height() * box.size().width() > THRESHOLD) {

			int x = Math.round(box.center().x() - (box.size().width() / 2));
			int y = Math.round(box.center().y() - (box.size().height() / 2));

			if (depth > 0) {
				color = CvScalar.RED;
				pinches.add(new Pinch(x, y, Math.round(box.size().width()),
						Math.round(box.size().height()), box.angle()));
			}

			if (canvasFrame != null) {
				cvEllipseBox(image, box, color, 1, 8, 0);
			}

		}
	}
}
