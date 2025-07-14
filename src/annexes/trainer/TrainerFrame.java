package annexes.trainer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.JSlider;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import java.awt.SystemColor;

import control.DeductionWriter.CustomKeyboardFocusManager;
import control.session.Shortcut;
import control.statics.ViewStatics;
import model.description.DPrimitive;
import model.description.abstraction.Described;
import model.independent.CyclicAccessList;
import model.independent.DoubleArray;
import model.independent.DoubleArray.Tuple;
import view.abstraction.AbstractFrame;
import view.components.DButton;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

/**
 * The frame of the sub application DeductionTrainer.
 * 
 * @see DeductionTrainer
 */
public class TrainerFrame extends AbstractFrame implements ChangeListener {

	private int counter = 0;
	private final int countermax = 6;
	private Described displayed;
	private DeductionTrainer parent;
	
	private CyclicAccessList<Described> keyqueue;
	private BindingsViewDialog dialog;
	
	
	/**
	 * Instantiates a new trainer frame.
	 */
	public TrainerFrame(BindingsViewDialog dialog) {
		super("Trainer frame ...");

		this.keyqueue = new CyclicAccessList<Described>();		

		this.dialog = dialog;
		
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 450);

		contentPane = new JPanel();
		contentPane.setBorder(new LineBorder(SystemColor.windowBorder, 1, true));
		contentPane.setLayout(new BorderLayout(20, 20));

		setContentPane(contentPane);
		
		btnStart = new JButton("start/stop");	btnDone = new JButton("quit");	
		btnReset = new JButton("reset");		btnClear = new JButton("clear");
		btnStore = new JButton("store");

		buttons = new JButton[] { btnStart, btnDone, btnReset, btnClear, btnStore };			

		makeGlyphPanel();
		makeControlPanel();
		makeInfoPanel();
		makeTitle();

		setActionCommands();
	}

	
	public Described displayNext() {
		//long t = 0; System.out.println(t - System.currentTimeMillis());
		displayed = keyqueue.moveMiddle(1);

		this.setArrayAndInfo();					
		this.requestFocusInWindow();
		//t = System.currentTimeMillis();
		return displayed;
	}

	/**
	 * Resets then count down timer.
	 */
	private void reset() {
		counter = 0;
	}
	/**
	 * Timer for the trainer cycle.
	 *
	 * @return True when time's up. False at other countings.
	 */
	public boolean periodEnded() {

		if (counter == 0) 
			counter = countermax;
		else
			counter--;

		return counter == 0;
	}

	public CyclicAccessList<Described> getKeyqueue() {
		return keyqueue;
	}
	
	public void setArrayAndInfo() {
	
		DoubleArray<Described, Shortcut> bindings = dialog.getBindings();
		
		String occupied 	= dialog.occupiedString();
		String unoccupied 	= dialog.unoccupiedString();

		ArrayList<Described> 		array = keyqueue.getCenterArray(2);
		
		Tuple<Described,Shortcut> 	binding = bindings.getByFirst(array.get(2));

		Shortcut middle;
		String codepoint = "?";		// no binding for the glyph
		
		if (binding != null) {		// binding exists
		
			middle =  binding.second();

			codepoint = ViewStatics.bindingString(middle);		
		}
		
		setGlyphArray(array,bindings);	

		// update fields and repaint
		txfCodepoint.setText(codepoint);
		txaSet.setText(occupied);
		txaNotSet.setText(unoccupied);
		pnlGlyph.repaint();
	}

	
	public void stateChanged(ChangeEvent e) {

		if (!keyqueue.isEmpty()) {

			int value = slider.getValue();
	
			int newmiddle = (int) ((double) value) / (keyqueue.size());
	
			if (slider.getValueIsAdjusting() && newmiddle != 0) keyqueue.moveMiddle(newmiddle); 
			else 							
				slider.setValue(0);
						
			setArrayAndInfo();
		}
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


	/** {@inheritDoc} */
	public Container[] focusCycleRoots() {
		return new Container[] { this };
	}
	/** {@inheritDoc} */
	public Component[][] focusCycleNodes() {
		return new Component[][] { new Component[] { btnStart, btnStore }};
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

	
	private void setGlyphArray(ArrayList<Described> array, DoubleArray<Described, Shortcut> bindings) {
		
		for (int i = 0; i < buttonarray.length; i++) 
			buttonarray[i].setIcon(ViewStatics.describedIcon(array.get(i)));		
		
		String codepoint; 
		Shortcut bound;
		Tuple<Described,Shortcut> binding;
		
		for (int i = 0; i < labelarray.length; i++) {

			binding = bindings.getByFirst(array.get(i));

			if (binding != null) {
				bound = binding.second();			
				codepoint = ViewStatics.bindingString(bound);
			} else
				codepoint = "?";
			
			labelarray[i].setText(codepoint);		
		}

	}

	private void setActionCommands() {
		btnStart.setActionCommand("start-stop");
		btnStore.setActionCommand("store");
		btnDone.setActionCommand("done");
		btnReset.setActionCommand("reset");
		btnClear.setActionCommand("clear");
	}

	//start_win_var_init

	private static final 	EtchedBorder 	BORDERTXA 	= new EtchedBorder(EtchedBorder.LOWERED, null, null);
	private static final 	Dimension 		SIZETXF 	= new Dimension(35, 35);

	private JPanel 			contentPane;
	
	private JPanel 			pnlGlyph, pnlSequence, pnlBindings, pnlControl, pnlInfo;

	private JSlider 		slider;	

	private JTextArea 		txaNotSet, txaSet;
	private JTextField 		txfTitle, txfCodepoint;
	
	private JButton 		btnStart, btnDone, btnStore, btnReset, btnClear;
	private JButton[] 		buttons;
	
	private DButton 		btnBefore2, btnBefore1, btnNow, btnAfter1, btnAfter2;
	private DButton[]		buttonarray;

	private DButton 		lbtnBefore2, lbtnBefore1, lbtnNow, lbtnAfter1, lbtnAfter2;
	private DButton[]		labelarray;
	
	
	private void makeTitle() {
		
		txfTitle = new JTextField();
		txfTitle.setEditable(false);
		
		txfTitle.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		txfTitle.setBorder(new EmptyBorder(10, 0, 0, 10));
		txfTitle.setBackground(UIManager.getColor("windowBorder"));
		txfTitle.setFont(new Font("Dialog", Font.PLAIN, 24));
		txfTitle.setHorizontalAlignment(SwingConstants.RIGHT);
		txfTitle.setText("Map short cut keys to primitives");
		
		contentPane.add(txfTitle, BorderLayout.NORTH);
	}
	
	private void makeGlyphPanel() {

		pnlGlyph 	= new JPanel();
		pnlSequence = new JPanel();
		pnlBindings = new JPanel();
		pnlBindings.setVerifyInputWhenFocusTarget(false);
		pnlBindings.setRequestFocusEnabled(false);
		pnlBindings.setOpaque(false);
		pnlBindings.setFocusable(false);
		pnlBindings.setDoubleBuffered(false);
		slider 		= new JSlider();
		slider.setBackground(UIManager.getColor("Slider.background"));
		slider.setPaintTrack(false);
		slider.addChangeListener(this);
		
		pnlGlyph.setBorder(new LineBorder(SystemColor.windowBorder, 1, true));
		pnlGlyph.setLayout(new BoxLayout(pnlGlyph, BoxLayout.Y_AXIS));

		contentPane.add(pnlGlyph, BorderLayout.CENTER);
		
		slider.setMinimum(-25);		slider.setMaximum(25);	slider.setValue(0);
		pnlGlyph.add(slider);
		
		pnlSequence.setBorder(new EmptyBorder(20, 50, 20, 50));
		pnlSequence.setLayout(new BoxLayout(pnlSequence, BoxLayout.X_AXIS));

		pnlBindings.setBorder(null);
		pnlBindings.setLayout(new BoxLayout(pnlBindings, BoxLayout.X_AXIS));

		pnlGlyph.add(pnlSequence);
		pnlGlyph.add(pnlBindings);
		
		btnBefore2 	= new DButton(new DPrimitive("Z"));	btnBefore1 	= new DButton(new DPrimitive("Å"));
		btnNow 		= new DButton(new DPrimitive("Ä"));			
		btnAfter1 	= new DButton(new DPrimitive("Ö"));	btnAfter2 	= new DButton(new DPrimitive("!"));

		lbtnBefore2 	= new DButton(new DPrimitive("z"));	lbtnBefore1 = new DButton(new DPrimitive("å"));
		lbtnNow 		= new DButton(new DPrimitive("ä"));			
		lbtnAfter1 		= new DButton(new DPrimitive("ö"));	lbtnAfter2 	= new DButton(new DPrimitive("?"));
				
		buttonarray = new DButton[] {btnBefore2, btnBefore1, btnNow, btnAfter1, btnAfter2};
		labelarray  = new DButton[] {lbtnBefore2, lbtnBefore1, lbtnNow, lbtnAfter1, lbtnAfter2};

		for (DButton button : buttonarray) {
			button.setBorder(new LineBorder(Color.gray,2));
			button.setEnabled(false);
		}
		
		Dimension lblSize = new Dimension(60,18);
		
		for (DButton button : labelarray) {
			button.setIcon(null);
			button.setBackground(ViewStatics.floralwhite);
			button.setMinimumSize(lblSize);
			button.setMaximumSize(lblSize);
			button.setPreferredSize(lblSize);
			button.setSize(lblSize);
			button.setFocusable(false);
			button.setBorderPainted(false);
			button.setRolloverEnabled(false);
			button.setPressedIcon(null);
			button.setSelectedIcon(null);
			button.setRolloverSelectedIcon(null);
		}

		pnlSequence.add(btnBefore2);	pnlSequence.add(btnBefore1);			
		pnlSequence.add(btnNow);
		pnlSequence.add(btnAfter1);		pnlSequence.add(btnAfter2);

		pnlBindings.add(lbtnBefore2);	pnlBindings.add(lbtnBefore1);
		pnlBindings.add(lbtnNow);
		pnlBindings.add(lbtnAfter1);	pnlBindings.add(lbtnAfter2);
		
		btnNow.setBorder(new LineBorder(Color.red,3));
		btnNow.setPressedIcon(null);
		btnNow.setSelectedIcon(null);
		btnNow.setRolloverSelectedIcon(null);
		btnNow.setEnabled(true);
	}
		
	private void makeControlPanel() {

		pnlControl = new JPanel();
		
		pnlControl.setPreferredSize(new Dimension(135, 600));
		pnlControl.setSize(new Dimension(200, 600));
		pnlControl.setBorder(new LineBorder(SystemColor.windowBorder, 1, true));
		pnlControl.setBackground(UIManager.getColor("Panel.background"));
		pnlControl.setLayout(null);

		contentPane.add(pnlControl, BorderLayout.EAST);

		for (JButton button : buttons) {
			button.setForeground(UIManager.getColor("Button.foreground"));
			button.setBackground(SystemColor.control);
		}
		
		btnStart.setBounds(13, 12, 110, 25);		
		btnDone.setBounds(13, 163, 110, 25);
		btnReset.setBounds(13, 43, 110, 25);
		btnClear.setBounds(13, 75, 110, 25);
		btnStore.setBounds(13, 108, 110, 25);
		
		pnlControl.add(btnStart);
		pnlControl.add(btnReset);
		pnlControl.add(btnClear);
		pnlControl.add(btnDone);				
		pnlControl.add(btnStore);
	}

	private void makeInfoPanel() {
		
		pnlInfo = new JPanel();
		txaSet = new JTextArea();
		txaNotSet = new JTextArea();
		txfCodepoint = new JTextField();
		
		pnlInfo.setBackground(SystemColor.window);
		pnlInfo.setBorder(new LineBorder(UIManager.getColor("Table.gridColor"), 1, true));
		pnlInfo.setLayout(new BoxLayout(pnlInfo, BoxLayout.X_AXIS));

		contentPane.add(pnlInfo, BorderLayout.SOUTH);
		
		JLabel lblSet = new JLabel("Set: ");
		lblSet.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		lblSet.setLabelFor(txaSet);
		
		JLabel lblNotSet = new JLabel("Not set: ");
		lblNotSet.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		lblNotSet.setLabelFor(txaNotSet);

		txaSet.setBorder(BORDERTXA);		txaSet.setRows(5);			txaSet.setColumns(50);
		txaNotSet.setBorder(BORDERTXA);		txaNotSet.setRows(5);		txaNotSet.setColumns(50);

		txfCodepoint.setBorder(new EmptyBorder(3, 5, 3, 5));
		txfCodepoint.setText("UTF-8");
		txfCodepoint.setSize(SIZETXF);		txfCodepoint.setPreferredSize(SIZETXF);		txfCodepoint.setMaximumSize(SIZETXF);
		txfCodepoint.setMargin(new Insets(5, 0, 0, 0));
		txfCodepoint.setHorizontalAlignment(SwingConstants.CENTER);
		txfCodepoint.setFont(new Font("Dialog", Font.ITALIC, 22));
		txfCodepoint.setColumns(8);
		txfCodepoint.setBackground(SystemColor.info);
		txfCodepoint.setAutoscrolls(false);	

		pnlInfo.add(lblSet);				
		pnlInfo.add(txaSet);
		pnlInfo.add(lblNotSet);
		pnlInfo.add(txaNotSet);		
		pnlInfo.add(txfCodepoint);
	}

	//end_win_var_init

}
