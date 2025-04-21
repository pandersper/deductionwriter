package annexes.trainer;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.InputEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

import control.DeductionWriter.CustomKeyboardFocusManager;
import control.Shortcut;
import model.description.abstraction.Described;
import view.abstraction.AbstractFrame;

/**
 * The frame of the sub application DeductionTrainer.
 * @see DeductionTrainer
 */
public class TrainerFrame extends AbstractFrame {

	private int counter = 0;
	private final int countermax = 6;
	private Described displayed;

	/**
	 * Instantiates a new trainer frame.
	 */
	public TrainerFrame() {
		super("Trainer frame ...");

		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setBounds(100, 100, 602, 405);

		contentPane = new JPanel();
		contentPane.setBorder(new LineBorder(new Color(72, 61, 139), 2, true));
		contentPane.setLayout(new BorderLayout(20, 20));

		setContentPane(contentPane);

		makeGlyphPanel();
		makeDataPanel();
		makeControlPanel();
		makeInfoPanel();
		makeTitle();

		setActionCommands();

		buttons = new JButton[] { btnStart, btnDone, btnStore, btnReset, btnClear };			
	}

	/**
	 * Resets then count down timer.
	 */
	public void reset() {
		counter = 0;
	}
	/**
	 * Timer for the trainer cycle.
	 *
	 * @return True when time's up. False at other countings.
	 */
	public boolean countdown() {

		if (counter == 0) 
			counter = countermax;
		else
			counter--;

		txfCountdown.setText(""+counter);

		return counter == 0;
	}
	/**
	 * Changes displayed codepoint when new binding is made and also updates the info text area.
	 *
	 * @param binding The binding to display.
	 * @param occupiedstring String to fill the information area with.
	 */
	public void changeCodepoint(Shortcut binding, String occupiedstring) {
	
		txfCodepoint.setText((char) (int) binding.keycode + InputEvent.getModifiersExText(binding.modifiers));
	
		txaInfo.setText(occupiedstring);
	
	}
	/**
	 * Sets the currently displayed described primitive.
	 *
	 * @param displayed The new described primitive.
	 */
	public void setDisplayed(Described displayed) {
		this.displayed = displayed;
		pnlGlyph.repaint();
	}
	/**
	 * Sets the information text field.
	 *
	 * @param string The new info to display.
	 */
	public void setInfo(String string) {
		txaInfo.setText(string);
	}

	/**
	 * Not in use.
	 * @param columnvalue	Not used.
	 */
	public void loadPrimitives(String columnvalue) {
		// NOT IN USE		
	}

	/**
	 * Not in use.
	 * @param columnvalue	Not used.
	 */
	public void loadComposites(String columnvalue) {
		// NOT IN USE		
	}
	/**
	 * Adds the listener.
	 *
	 * @param listener the listener
	 */
	public void addListener(DeductionTrainer listener) {

		for (JButton button : buttons) 
			button.addActionListener(listener);
	}
	/**
	 * Sets the action commands.
	 */
	private void setActionCommands() {
		btnStart.setActionCommand("start-stop");
		btnStore.setActionCommand("store");
		btnDone.setActionCommand("done");
		btnReset.setActionCommand("reset");
		btnClear.setActionCommand("clear");
	}

	/** {@inheritDoc} */
	public Container[] focusCycleRoots() {
		return new Container[] { this };
	}

	/** {@inheritDoc} */
	public Component[][] focusCycleNodes() {
		return new Component[][] { new Component[] { btnStart, btnLoad, btnStore }};
	}

	/** {@inheritDoc} */
	public void setFocusTraversal(CustomKeyboardFocusManager manager) {

		this.manager = manager;

		this.setFocusable(true);
		this.setFocusCycleRoot(true);		
	}
	
	/** {@inheritDoc} */
	public void setDefaultComponent() {
		this.defaultcomponent = contentPane;		
	}

	/** AWT AND SWING **/
	
	private class GlyphPanel extends JPanel {

		private class GlyphCanvas extends Canvas {

			private Image glyph;
			private int x, y;

			public void update(Graphics g) {

				if (displayed != null) {

					glyph = displayed.description().getImage();

					x = (this.getWidth() - glyph.getWidth(null)) / 2;
					y = (this.getHeight() - glyph.getHeight(null)) / 2;

					this.paint(g);

				}
			}

			/** {@inheritDoc} */
			public void paint(Graphics g) {

				g.drawImage(glyph, x, y, null);
			}
		}

		private GlyphCanvas canvas = new GlyphCanvas();

		/**
		 * Instantiates a new glyph panel.
		 */
		public GlyphPanel() {
			this.setBorder(new MatteBorder(2, 2, 2, 2, (Color) new Color(0, 0, 0)));
			this.setBackground(new Color(216, 216, 216));
			this.setLayout(new FlowLayout());
			this.add(canvas);
			canvas.setPreferredSize(new Dimension(100,100));
			canvas.setBackground(Color.CYAN);
		}

		/** {@inheritDoc} */
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);

			canvas.repaint();
		}

	}

	private JPanel 			contentPane;
	private JPanel 			pnlGlyph, pnlData, pnlControl, pnlInfo;
	private JTextField 		txfTitle, txfCountdown, txfCodepoint;
	private JTextArea 		txaInfo;
	private JButton 		btnStart, btnDone, btnLoad, btnStore, btnReset, btnClear;

	private JButton[] 		buttons;

	private void makeTitle() {
		txfTitle = new JTextField();
		txfTitle.setBorder(new MatteBorder(2, 2, 2, 2, (Color) new Color(0, 0, 0)));
		txfTitle.setBackground(new Color(100, 149, 237));
		txfTitle.setFont(new Font("Rachana", Font.BOLD, 24));
		txfTitle.setHorizontalAlignment(SwingConstants.CENTER);
		txfTitle.setText("Map short cut keys to primitives");
		txfTitle.setColumns(5);		
		txfTitle.setEditable(false);
		contentPane.add(txfTitle, BorderLayout.NORTH);
	}
	
	private void makeGlyphPanel() {
		pnlGlyph = new GlyphPanel();
		contentPane.add(pnlGlyph, BorderLayout.CENTER);
	}
	
	private void makeDataPanel() {
		pnlData = new JPanel();
		pnlData.setPreferredSize(new Dimension(160, 600));
		pnlData.setSize(new Dimension(160, 600));
		pnlData.setBorder(new MatteBorder(2, 2, 2, 2, (Color) new Color(0, 0, 0)));
		pnlData.setBackground(new Color(176, 180, 185));
		pnlData.setLayout(null);
		contentPane.add(pnlData, BorderLayout.WEST);

		btnStore = new JButton("store mapping");
		btnStore.setBounds(12, 12, 138, 25);
		pnlData.add(btnStore);
	}
	
	private void makeControlPanel() {
		pnlControl = new JPanel();
		pnlControl.setPreferredSize(new Dimension(135, 600));
		pnlControl.setSize(new Dimension(200, 600));
		pnlControl.setBorder(new MatteBorder(2, 2, 2, 2, (Color) new Color(0, 0, 0)));
		pnlControl.setBackground(new Color(176, 180, 185));
		contentPane.add(pnlControl, BorderLayout.EAST);
		pnlControl.setLayout(null);

		btnStart = new JButton("start/stop");
		btnDone = new JButton("quit");
		btnReset = new JButton("reset");
		btnClear = new JButton("clear");
		btnStart.setBounds(13, 12, 110, 25);		
		btnDone.setBounds(13, 100, 110, 25);
		btnReset.setBounds(13, 43, 110, 25);
		btnClear.setBounds(13, 75, 110, 25);
		pnlControl.add(btnStart);
		pnlControl.add(btnReset);
		pnlControl.add(btnClear);
		pnlControl.add(btnDone);		
	}

	private void makeInfoPanel() {
		pnlInfo = new JPanel();
		pnlInfo.setBorder(new MatteBorder(2, 2, 2, 2, (Color) new Color(0, 0, 0)));
		contentPane.add(pnlInfo, BorderLayout.SOUTH);
		pnlInfo.setLayout(new BoxLayout(pnlInfo, BoxLayout.X_AXIS));

		txfCodepoint = new JTextField();
		txfCodepoint.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		txfCodepoint.setText("UTF-8");
		txfCodepoint.setSize(new Dimension(35, 35));
		txfCodepoint.setPreferredSize(new Dimension(35, 35));
		txfCodepoint.setMaximumSize(new Dimension(35, 35));
		txfCodepoint.setMargin(new Insets(5, 0, 0, 0));
		txfCodepoint.setHorizontalAlignment(SwingConstants.CENTER);
		txfCodepoint.setFont(new Font("Rachana", Font.BOLD, 22));
		txfCodepoint.setEditable(false);
		txfCodepoint.setColumns(8);
		txfCodepoint.setBackground(new Color(70, 130, 180));
		txfCodepoint.setAutoscrolls(false);

		txaInfo = new JTextArea();
		txaInfo.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		txaInfo.setRows(5);
		txaInfo.setColumns(50);

		txfCountdown = new JTextField();
		txfCountdown.setAutoscrolls(false);
		txfCountdown.setMargin(new Insets(5, 0, 0, 0));
		txfCountdown.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		txfCountdown.setEditable(false);
		txfCountdown.setHorizontalAlignment(SwingConstants.CENTER);
		txfCountdown.setMaximumSize(new Dimension(35, 35));
		txfCountdown.setPreferredSize(new Dimension(35, 35));
		txfCountdown.setSize(new Dimension(35, 35));
		txfCountdown.setFont(new Font("Rachana", Font.BOLD, 22));
		txfCountdown.setBackground(new Color(70, 130, 180));
		txfCountdown.setText("0");
		txfCountdown.setColumns(3);		

		pnlInfo.add(txfCodepoint);
		pnlInfo.add(txaInfo);
		pnlInfo.add(txfCountdown);
	}
}
