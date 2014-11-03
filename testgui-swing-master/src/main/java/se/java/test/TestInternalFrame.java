package se.java.test;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

class TestInternalFrame extends InternalFrameAdapter {

	private JDesktopPane parent;

	private final JPanel contentPane;

	public TestInternalFrame(JPanel contentPane) {
		this.contentPane = contentPane;
	}

	public JInternalFrame createWindow() {
		JInternalFrame window = new JInternalFrame();
		window.putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);
		window.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
		window.setClosable(true);
		window.setResizable(true);
		window.addInternalFrameListener(this);
		window.setSize(contentPane.getPreferredSize());
		window.setLocation(0, 0);
		window.setVisible(true);
		window.add(contentPane);
		return window;
	}

	@Override
	public void internalFrameClosing(InternalFrameEvent e) {
		if (parent == null) {
			parent = (JDesktopPane) e.getInternalFrame().getParent();
		}
	}

	@Override
	public void internalFrameClosed(InternalFrameEvent e) {
		if (parent != null && parent.getComponentCount() == 0) {
			parent.add(createWindow());
		}
	}
}
