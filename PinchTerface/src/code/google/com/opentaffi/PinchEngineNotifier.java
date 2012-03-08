package code.google.com.opentaffi;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Basic observer pattern. This class runs in a separate Thread from the engine,
 * and notifies listeners of pinches. You should not access this class it should
 * be accessed via the PinchEngine.
 * 
 * @author Luc Trudeau
 * @date 07/03/2012
 * 
 */
class PinchEngineNotifier implements Runnable {

	private final List<PinchListener> listeners = new LinkedList<PinchListener>();
	private BlockingQueue<List<Pinch>> queue = new LinkedBlockingQueue<List<Pinch>>();
	private boolean running = true;

	void addListener(PinchListener listener) {
		listeners.add(listener);
	}

	void removeListener(PinchListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void run() {

		while (running) {
			try {
				List<Pinch> pinches = queue.take();
				for (PinchListener listener : listeners) {
					listener.pinch(pinches);
				}
			} catch (InterruptedException e) {
				running = false;
			}
		}

	}

	void notifyListeners(List<Pinch> pinches) {
		try {
			queue.put(pinches);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
