package se.java.test;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

@SuppressWarnings("serial")
public abstract class UIConsoleWindow extends JDialog implements ActionListener {

	private static final String EMPTY = "";

	private final ConsoleArea consoleArea;

	public UIConsoleWindow(Window owner) {
		super(owner, "Console");
		consoleArea = new ConsoleArea();
		JTextArea component = consoleArea.component();
		createMenu(component);
		add(new JScrollPane(component));
		setSize(400, 300);
		setLocationRelativeTo(owner);
	}

	private void createMenu(final JTextArea component) {
		JPopupMenu m = new JPopupMenu("Console");
		m.add(new AbstractAction("Reload") {
			@Override
			public void actionPerformed(ActionEvent e) {
				UIConsoleWindow.this.actionPerformed(e);
			}
		});
		m.add(new AbstractAction("Clear") {
			@Override
			public void actionPerformed(ActionEvent e) {
				component.setText(EMPTY);
			}
		});
		component.setComponentPopupMenu(m);
	}

	public void flush() {
		consoleArea.flush();
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (consoleArea == null) {
			return;
		}
		if (visible) {
			consoleArea.register();
		} else {
			consoleArea.unregister();
		}
	}

}

class ConsoleArea extends OutputStream {

	private final JTextArea console;

	private final PrintStream out;

	private final PrintStream newOut;

	public ConsoleArea() {
		console = new JTextArea();
		console.setEditable(false);
		out = System.out;
		newOut = new PrintStream(this);
	}

	public JTextArea component() {
		return console;
	}

	@Override
	public void write(int b) throws IOException {
		console.append(new String(new byte[] { (byte) b }));
	}

	@Override
	public void flush() {
		console.repaint();
		console.setCaretPosition(0);
	}

	public void register() {
		System.setOut(newOut);
	}

	public void unregister() {
		System.setOut(out);
	}
}