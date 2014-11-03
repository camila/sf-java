package se.java.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

@SuppressWarnings("serial")
public class TestGUI extends JFrame {

	private static final Dimension CONTENT_SIZE = new Dimension(400, 400);
	private static final String TITLE = "JPanel viewer";
	private static final String CLASS_NAME = "Qualified class name:";
	private static final String TAB = "   ";

	private final JPanel pnlContent;
	private final JLabel lblStatus;

	private UIConsoleWindow console;

	private Container contentPane;

	public TestGUI() {
		super(TITLE);
		setIconImage(loadIcon("/javax/swing/plaf/metal/icons/Inform.gif")
				.getImage());
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pnlContent = new JPanel();
		lblStatus = new JLabel(TAB);
		add(createStatusBar(), BorderLayout.SOUTH);
		setJMenuBar(createMenuBar());
		setSize(800, 600);
		setLocationRelativeTo(null);
		registerKeyListeners(getTitle() + ".closeAction");
	}

	private void createConsole() {
		console = new UIConsoleWindow(this) {
			@Override
			public void actionPerformed(ActionEvent e) {
				printDebug();
			}
		};
	}

	private void configurePnlContent(boolean internalFrame) {
		if (internalFrame) {
			pnlContent.setPreferredSize(CONTENT_SIZE);
			contentPane = new JDesktopPane();
			contentPane.add(new TestInternalFrame(pnlContent).createWindow());
		} else {
			contentPane = new JScrollPane(pnlContent);
			((JScrollPane) contentPane).setBorder(null);
		}
		setContentPane(contentPane);
	}

	private JPanel createStatusBar() {
		JPanel status = new JPanel(new FlowLayout(0, 0, 0));
		status.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0,
				Color.LIGHT_GRAY));
		status.add(lblStatus);
		return status;
	}

	private JMenuBar createMenuBar() {
		JMenuBar mb = new JMenuBar();
		mb.setBackground(Color.DARK_GRAY);
		mb.setBorder(null);
		final JMenu m = createMenu("File", KeyEvent.VK_F);
		m.add(createLoadClassItem());
		m.add(createUnloadItem());
		m.addSeparator();
		m.add(createSettingsItem(m));
		m.addSeparator();
		m.add(createExitItem());
		mb.add(m);

		mb.add(Box.createHorizontalGlue());
		JMenu s = createMenu("Help", KeyEvent.VK_H);
		s.add(createConsoleItem());
		mb.add(s);
		return mb;
	}

	private AbstractAction createExitItem() {
		return new MenuItemAction("Exit", true, KeyEvent.VK_X) {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		};
	}

	private AbstractAction createSettingsItem(final JMenu m) {
		return new MenuItemAction("Settings", true, KeyEvent.VK_S) {

			@Override
			public void actionPerformed(ActionEvent e) {
				int showOptionDialog = JOptionPane.showConfirmDialog(
						TestGUI.this, "Create internal frame?",
						"Internal frame test",
						JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if (showOptionDialog != JOptionPane.CANCEL_OPTION) {
					configurePnlContent(showOptionDialog == 0);
					enableMenuItems(m, true);
					setEnabled(false);
				}
			}

			private void enableMenuItems(JMenu m, boolean enabled) {
				for (Component c : m.getMenuComponents()) {
					c.setEnabled(enabled);
				}
			}
		};
	}

	private JMenu createMenu(String title, int keyEvent) {
		JMenu s = new JMenu(title);
		s.setMnemonic(keyEvent);
		s.setForeground(Color.LIGHT_GRAY);
		return s;
	}

	private Action createConsoleItem() {
		return new MenuItemAction("Console", true, KeyEvent.VK_C,
				InputEvent.CTRL_DOWN_MASK) {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (console == null) {
					createConsole();
				}
				printDebug();
			}
		};
	}

	private Action createUnloadItem() {
		return new MenuItemAction("Unload", false, KeyEvent.VK_U) {
			{
				enabled = false;
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				addAndReload(null);
			}
		};
	}

	private Action createLoadClassItem() {
		return new MenuItemAction("Load", false) {
			@Override
			public void actionPerformed(ActionEvent e) {
				Component testComp = null;
				String className = JOptionPane.showInputDialog(CLASS_NAME,
						(pnlContent.getComponentCount() == 1 ? pnlContent
								.getComponent(0).getName() : ""));
				if (className == null || className.isEmpty()) {
					return;
				}
				testComp = loadTestComponent(className);
				if (testComp != null) {
					testComp.setName(className);
					addAndReload(testComp);
				}
			}
		};
	}

	@SuppressWarnings("unchecked")
	private Class<? extends Component> loadClass(File classFile,
			String className) {
		try {
			ClassLoader loader = new URLClassLoader(new URL[] { classFile
					.toURI().toURL() });
			return (Class<? extends Component>) loader.loadClass(className);
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	private void addAndReload(Component c) {
		pnlContent.removeAll();
		if (c == null) {
			setTitle(TITLE);
			lblStatus.setText(TAB);
		} else {
			pnlContent.add(c);
			lblStatus.setText(asString(c) + " loaded.");
			setTitle(TITLE + " -> " + asString(c));
		}
		invalidate();
		validate();
		repaint();
	}

	@SuppressWarnings("deprecation")
	private void printDebug() {
		Cursor c = getCursor();
		setCursor(Cursor.WAIT_CURSOR);
		console.setVisible(true);
		int total = debug("> ", this, getComponents());
		System.out.println("Component count = " + total);
		console.flush();
		setCursor(c);
	}

	protected Component loadTestComponent(String className) {
		try {
			return (Component) Class.forName(className).newInstance();
		} catch (Exception e) {
			JFileChooser fc = new JFileChooser();
			fc.setFileFilter(new FileNameExtensionFilter(".class", "class"));
			int returnVal = fc.showOpenDialog(TestGUI.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				loadClass(fc.getSelectedFile(), className);
			}
			return loadTestComponent(JOptionPane.showInputDialog(CLASS_NAME,
					className));
		}
	}

	private int debug(String path, Component parent, Component... components) {
		int i = 1;
		System.out.println(path + asString(parent));
		if (components.length > 0) {
			for (Component c : components) {
				if (c instanceof JComponent) {
					if (c instanceof JViewport) {
						c = ((JViewport) c).getView();
					}
					i += debug(TAB + path, c, ((JComponent) c).getComponents());
				} else {
					i++;
					System.out.println(c);
				}
			}
		}
		return i;
	}

	private String asString(Component c) {
		return (c.getName() == null || c.getName().isEmpty()) ? c.toString()
				: c.getName();
	}

	private ImageIcon loadIcon(String path) {
		try {
			BufferedImage image = ImageIO.read(getClass().getResourceAsStream(
					path));
			return new ImageIcon(image);
		} catch (IOException e) {
			return null;
		}
	}

	public void setFullScreen() {
		Dimension maxSize = Toolkit.getDefaultToolkit().getScreenSize();
		setSize(maxSize);
		setPreferredSize(maxSize);
		setMinimumSize(maxSize);
		setMaximumSize(maxSize);
		setBounds(0, 0, maxSize.width, maxSize.height);
		setResizable(false);
		setUndecorated(true);
		setAlwaysOnTop(true);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
	}

	private void registerKeyListeners(String closeAction) {
		rootPane.getActionMap().put(closeAction, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(-1);
			}
		});
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,
						KeyEvent.CTRL_DOWN_MASK), closeAction);
	}

}

@SuppressWarnings("serial")
abstract class MenuItemAction extends AbstractAction {
	public MenuItemAction(String title, boolean enabled, int... keyEvent) {
		super(title);
		this.enabled = enabled;
		if (keyEvent.length == 2) {
			putValue(ACCELERATOR_KEY,
					KeyStroke.getKeyStroke(keyEvent[0], keyEvent[1]));
		} else {
			putValue(MNEMONIC_KEY, keyEvent.length == 0 ? null : keyEvent[0]);
		}
	}
}