package code.google.com.opentaffi.examples;

import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

/**
 * Frame containing the PinchPanel
 * 
 * @author Luc Trudeau
 * @date 07/03/2012
 */
public class PicturePincher {

	private JFrame frame;

	public PicturePincher() {
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void start() throws IOException {
		frame.setSize(1024, 768);
		frame.setVisible(true);

		PinchImagePanel panel = new PinchImagePanel(ImageIO.read(this
				.getClass().getResource("/ets.jpg")), 1024, 768, true, 0);

		frame.getContentPane().add(panel);
		panel.setSize(frame.getSize());
	}

	public static void main(String[] args) throws IOException {
		new PicturePincher().start();
	}

}
