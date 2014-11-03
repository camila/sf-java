package se.java.test;

import java.awt.EventQueue;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App {
	public static void main(String[] args) {
		resetLoggers();
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				new TestGUI().setVisible(true);
			}
		});
	}

	private static void resetLoggers() {
		System.clearProperty("sun.awt.exception.handler");
		Logger logger = Logger.getLogger("");
		logger.addHandler(new ConsoleHandler());
		logger.setLevel(Level.CONFIG);
	}
}
