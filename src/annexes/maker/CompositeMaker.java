package annexes.maker;

import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Hashtable;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;

import control.statics.PaintStatics;
import control.statics.ViewStatics;
import model.description.DComposite;
import model.description.abstraction.Described;
import view.DeductionFrame;
import view.abstraction.InitiableContainer;
import view.components.DButton;
import view.components.DButton.DisplayAction;
import view.components.DisplayCanvas;
import view.components.GlyphsPanel;

/**
 * Sub application for constructing composite glyphs.
 */
public class CompositeMaker extends JFrame implements ActionListener, InitiableContainer {

	//start_win_var_int
	
	private static final Font btnfont = new Font("Dialog", Font.PLAIN, 10);

	private static final CompoundBorder borderdesktop = new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), new BevelBorder(BevelBorder.RAISED));

	private static final MatteBorder 	borderside			= new MatteBorder(1, 1, 1, 1, (Color) new Color(0, 0, 0));
	private static final Insets 		insetsbtn 			= new Insets(2, 2, 2, 2);
	private static final Color 			colorlineborder 	= new Color(107, 107, 107);
	private static final Color 			colortitledborder 	= new Color(57, 57, 57);

	private static final Dimension PRFSIDE 		= new Dimension(200, 175);
	private static final Dimension PRFZOOM 		= new Dimension(100, 150);
	private static final Dimension PRFSIZE 		= new Dimension(200, 80);
	private static final Dimension PRFGLYPHS 	= new Dimension(800, 100);
	private static final Dimension PRFINFO 		= new Dimension(400, 200);
	private static final Dimension PRFBTN 		= new Dimension(110, 30);

	private static final Insets insettxf = new Insets(2, 10, 2, 10);
	
	private JPanel contentPane;
	
	private final JPanel pnlSide 	= new JPanel();		
	private final JPanel pnlInfo 	= new JPanel();	
	private final JPanel pnlCommand = new JPanel();
	private final JPanel pnlZoom 	= new JPanel();

	private final JPanel pnlMain 	= new JPanel();
	private final JPanel pnlGlyphs 	= new JPanel();

	private final JFrame extfrmPrimitives = new JFrame();

	private final JScrollPane scrlrGlyps 	= new JScrollPane();
	
	private final JButton btnCursor = new JButton("new cursor");	private final JButton btnInsert = new JButton("insert subglyph");
	private final JButton btnRender = new JButton("render");		private final JButton btnDone 	= new JButton("done");
	private final JButton btnDelete = new JButton("delete");		private final JButton btnClear 	= new JButton("clear");
	
	private final JButton[] buttons = new JButton[] { btnCursor, btnInsert, btnRender, btnDone, btnDelete, btnClear};
	
	private final JSlider 	sldZoom = new JSlider();
	private final JSlider 	sldSize = new JSlider();

	private final JInternalFrame ifrmEdit = new JInternalFrame("Composite");
	
	private final 	DeductionFrame 	frmParent;
	private final 	CompositePanel 	pnlComposites;
	private 		GlyphsPanel 	pnlGlyps;
	private 		JButton 		btnQuit;

	private final 	JDesktopPane 	desktop	= new JDesktopPane();
	private 		DisplayCanvas 	replaced;
	private 		DComposite 		done;

	
	private void makePanels() {
		
		pnlSide.setBackground(SystemColor.control);		
		pnlSide.setBorder(borderside);
		pnlSide.setPreferredSize(new Dimension(200, 0));
		pnlSide.setLayout(new BoxLayout(pnlSide, BoxLayout.Y_AXIS));

		contentPane.add(pnlSide);
				
		makeInfoPanel();				
		pnlSide.add(pnlInfo);
				
		makeCommandPanel();
		pnlSide.add(pnlCommand);

		makeZoomPanel();
		pnlSide.add(pnlZoom);
	
		makeMainPanel();
		contentPane.add(pnlMain);
		
		setSizes();
		
		this.repaint();
	}

	private void makeMainPanel() {
		
		scrlrGlyps.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);	
		scrlrGlyps.setBorder(compoundBorder());
		scrlrGlyps.setViewportView(pnlGlyphs);
	
		pnlGlyphs.setBackground(UIManager.getColor("scrollbar"));
					
		pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
		pnlMain.add(scrlrGlyps);
	}
	
	private void makeZoomPanel() {
		
		sldZoom.setBackground(SystemColor.controlHighlight);

		sldZoom.setOrientation(SwingConstants.VERTICAL);

		sldZoom.setValue(10);			sldZoom.setMinimum(1);			sldZoom.setMaximum(30);		
		sldZoom.setPaintTicks(true);	sldZoom.setSnapToTicks(true);	sldZoom.setPaintTrack(true);	sldZoom.setPaintLabels(true); 

		sldZoom.setMajorTickSpacing(10);	
		sldZoom.setMinorTickSpacing(5);	
		sldZoom.setExtent(1);
				
		Hashtable<Integer, JComponent> labels = new Hashtable<Integer, JComponent>();
		
		labels.put(Integer.valueOf(10), new JLabel("100%"));
		labels.put(Integer.valueOf(15), new JLabel("150%"));
		labels.put(Integer.valueOf(20), new JLabel("200%"));
		
		sldZoom.setLabelTable(labels);
		
		sldZoom.setName("zoomer");
		sldZoom.addChangeListener(pnlComposites);

		pnlZoom.setForeground(SystemColor.controlShadow);
		pnlZoom.setBackground(SystemColor.control);

		pnlZoom.setBorder(titledBorder("Zoom"));
		pnlZoom.setLayout(new BoxLayout(pnlZoom, BoxLayout.X_AXIS));

		pnlZoom.add(sldZoom);
	
		sldZoom.setEnabled(false);
		
		JCheckBox zoomoff = new JCheckBox("Zoom");
		
		zoomoff.setSelected(false);
		zoomoff.setName("zoom onoff");
	
		zoomoff.setAction(new AbstractAction() {
			
			public void actionPerformed(ActionEvent ae) {

				if (sldZoom.isEnabled()) {
					sldZoom.setEnabled(false);					
					sldZoom.setValue(10);
					pnlComposites.getCanvas().setTransform(PaintStatics.IDENTITY);
				} else
					sldZoom.setEnabled(true);
			}
		});

		pnlZoom.add(zoomoff);
	}

	private void makeCommandPanel() {
		
		pnlCommand.setBackground(SystemColor.control);
		pnlCommand.setBorder(titledBorder("Command"));				
		pnlCommand.setLayout(new BoxLayout(pnlCommand, BoxLayout.Y_AXIS));
		
		// non default
		btnDone.setBackground(new Color(255, 255, 255));

		setButton(btnCursor,"cursor");
		setButton(btnInsert,"external");
		setButton(btnRender,"render");
		setButton(btnDone,"quit");

		pnlCommand.add(btnCursor);
		pnlCommand.add(btnInsert);		
		pnlCommand.add(btnRender);
		pnlCommand.add(btnDone);
	}

	private void makeInfoPanel() {
		
		pnlInfo.setPreferredSize(PRFINFO);		
		pnlInfo.setMinimumSize(PRFINFO);		
		pnlInfo.setMaximumSize(PRFINFO);

		pnlInfo.setBackground(SystemColor.control);
		pnlInfo.setBorder(titledBorder("Info"));
		pnlInfo.setLayout(new BoxLayout(pnlInfo, BoxLayout.PAGE_AXIS));
		
		makeSizeSlider();
		
		JCheckBox sizeoff = new JCheckBox("Resize baseline");
		
		sizeoff.setSelected(false);
		sizeoff.setName("size onoff");
		
		sizeoff.setAction(new AbstractAction() {
			
			public void actionPerformed(ActionEvent ae) {

				if (sldSize.isEnabled()) {
			
					sldSize.setEnabled(false);	
					pnlComposites.toggleResizing();
					sldSize.setValue(0);
				
				} else {
					sldSize.setEnabled(true);
					pnlComposites.toggleResizing();
				}
				
			}
		});
				
		pnlInfo.add(sldSize);
		pnlInfo.add(sizeoff);

		setButton(btnClear,"clear");
		setButton(btnDelete,"delete");

		Component empty = Box.createRigidArea(new Dimension(100,30));

		pnlInfo.add(empty);
		pnlInfo.add(btnClear);
		pnlInfo.add(btnDelete);		
	}

	private void makeSizeSlider() {
		
		sldSize.setOrientation(SwingConstants.HORIZONTAL);

		sldSize.setValue(0);		sldSize.setMinimum(-500);		sldSize.setMaximum(500);		
		sldSize.setMinorTickSpacing(20);
		sldSize.setMajorTickSpacing(100);

		sldSize.setSnapToTicks(true);		
		sldSize.setPaintTrack(true);
		sldSize.setPaintLabels(true);

		Hashtable<Integer, JComponent> labels = new Hashtable<Integer, JComponent>();
		
		labels.put(Integer.valueOf(-400), new JLabel("-40%"));
		labels.put(Integer.valueOf(-200), new JLabel("-20%"));
		labels.put(Integer.valueOf(0), new JLabel("100%"));
		labels.put(Integer.valueOf(200), new JLabel("+20%"));
		labels.put(Integer.valueOf(400), new JLabel("+40%"));

		sldSize.setLabelTable(labels);
		
		sldSize.setName("sizer");	
		sldSize.addChangeListener(pnlComposites);
		
		sldSize.validate();
		
		sldSize.setEnabled(false);
	}

	private void makeDesktop() {
		
		desktop.setBorder(borderdesktop);
		desktop.setBackground(UIManager.getColor("Desktop.background"));
		
		ifrmEdit.getContentPane().setBackground(UIManager.getColor("EditorPane.background"));
		ifrmEdit.setBackground(UIManager.getColor("info"));
		ifrmEdit.setBorder(new LineBorder(Color.LIGHT_GRAY, 3, true));
		
		ifrmEdit.setForeground(new Color(0, 0, 0));
		ifrmEdit.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		ifrmEdit.setSize(ViewStatics.makercanvasdimension);
		ifrmEdit.setPreferredSize(ViewStatics.makercanvasdimension);
		ifrmEdit.setLocation(new Point(20,20));
		
		ifrmEdit.setIconifiable(true);		ifrmEdit.setMaximizable(true);
		ifrmEdit.setResizable(true);		ifrmEdit.setVisible(true);
		
		pnlComposites.setButtons(buttons);
		pnlComposites.addListeners(this);
	}	

	private void setButton(JButton btn, String cmd) {
		
		btn.setFont(btnfont);
		btn.setMargin(insetsbtn);		
		btn.setAlignmentX(0.5f);
		btn.setBackground(SystemColor.controlHighlight);
		btn.setActionCommand(cmd);
	}

	private void setSizes() {

		sldSize.setSize(PRFSIZE);			
		sldSize.setMinimumSize(PRFSIZE);	
		sldSize.setMaximumSize(PRFSIZE);

		for (JButton b : buttons) {
			b.setPreferredSize(PRFBTN);		b.setMinimumSize(PRFBTN);		b.setMaximumSize(PRFBTN);
			
		}
		
		pnlCommand.setMinimumSize(ViewStatics.grow(PRFSIDE, 0, -25));
		pnlCommand.setPreferredSize(PRFSIDE);
		pnlCommand.setMaximumSize(ViewStatics.grow(PRFSIDE, 0, 25));

		pnlGlyphs.setPreferredSize(PRFGLYPHS);
						
		sldZoom.setSize(PRFZOOM);			
		sldZoom.setMinimumSize(ViewStatics.grow(PRFZOOM, -10,-50));		
		sldZoom.setMaximumSize(ViewStatics.grow(PRFZOOM, 10,10));

		pnlZoom.setSize(PRFSIDE);			
		pnlZoom.setMinimumSize(ViewStatics.grow(PRFSIDE, 0, -25));		
		pnlZoom.setMaximumSize(ViewStatics.grow(PRFSIDE, 0, 25));
		
		pnlGlyphs.setSize(PRFGLYPHS);		pnlGlyphs.setMaximumSize(PRFGLYPHS);								pnlGlyphs.setMinimumSize(PRFGLYPHS);
	}
	
	//end_win_var_init
	/**
	 * Sub application for constructing composite glyphs.
	 * @param elder The JFrame-derived elder of this JFrame-derivative that opened this
	 * 				 and to which this should return.
	 */
	public CompositeMaker(DeductionFrame parent) {

		this.frmParent = parent;
		
		pnlComposites = new CompositePanel(pnlGlyphs);

		setTitle("Design you composite");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 831, 600);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));

		this.setContentPane(contentPane);

		makePanels();
		makeDesktop();						
		
		ifrmEdit.add(pnlComposites);		
		desktop.add(ifrmEdit);
		pnlMain.add(desktop);		
	}

	/**
	 * As usual, this is the main hub of control, directed by button clicks.
	 * 
	 * @param ae The action event originating from buttons.
	 */
	public void actionPerformed(ActionEvent ae) {

		switch (ae.getActionCommand()) {
		
			case "adjust":
								
				break;
				
			case "render":
	
				CompositeCanvas canvas = pnlComposites.getCanvas();
				
				done = new DComposite(canvas.exportCursors());								
	
				pnlComposites.getCanvas().setInset(done);		
				pnlComposites.repaint();
				
				break;
	
			case "quit":
	
				if (done != null) {				
					pnlGlyps.makeButton(done, null);						
					done = null;					
				}
			
				changeActions(false);	
	
				ViewStatics.switchContainer(frmParent, this);
				
				frmParent.setGlyphsPanel(pnlGlyps);		
				frmParent.doLayout();

				break;

			case "internal": 	ViewStatics.switchContainer(this, extfrmPrimitives); break;
			case "external": 	ViewStatics.switchContainer(extfrmPrimitives, this); break;

			case "clear": 		pnlComposites.clear();						break;
			case "delete":		pnlComposites.getCanvas().deleteCurrent(); 	break;			
			case "cursor": 		pnlComposites.toggleEditing(); 				break;
			case "next":		pnlComposites.forward(); 					break;
	
			default: break;
		}
	}
	
	/**
	 * Initialises this composite maker frame.
	 * 
	 * @param described	If composite it sets it up for further editing and if primitive it works 
	 * 					as the bounding frame for a new composite.
	 */
	public void initialise(Described described) {
		
		described.setWritepoint(new Point2D.Double(0,0));
		
		pnlComposites.clearAll();	
		
		pnlComposites.setupFrame(described);
		
	}
	/**
	 * Sets the primitives pnlComposites.
	 *
	 * @param pnlComposites the new primitives pnlComposites
	 */
	public void setGlyphsPanel(GlyphsPanel panel) {

		pnlGlyps = panel;		
		pnlGlyps.doLayout();

		extfrmPrimitives.setLayout(new BorderLayout());
		extfrmPrimitives.add(this.pnlGlyps, BorderLayout.CENTER);

		btnQuit = new JButton(new AbstractAction() {

			public void actionPerformed(ActionEvent ae) {

				extfrmPrimitives.remove(btnQuit);

				ViewStatics.switchContainer(CompositeMaker.this, extfrmPrimitives);
			}
		});

		btnQuit.setText("btnQuit");
		btnQuit.setPreferredSize(new Dimension(30,25));
		btnQuit.setMinimumSize(new Dimension(30,25));

		extfrmPrimitives.add(btnQuit, BorderLayout.SOUTH);
		extfrmPrimitives.setSize(500,500);

		changeActions(true);
	}	
	/**
	 * Makes new or removes actions in all buttons used in OldCompositeMaker sub application. 
	 * The buttona are transfered from and to the main application. The same buttons are used
	 * everywhere so their actions have to be changed. 
	 *
	 * @param to Transfering to or from the main application that is leaving versus entering.
	 */
	public void changeActions(boolean to) {

		Collection<DButton> buttons = pnlGlyps.getButtons();

		if (to) {

			for (DButton button : buttons) {

				DisplayAction action = button.getDisplayAction();

				replaced = (DisplayCanvas) action.getValue("canvas");

				action.putValue("canvas", pnlComposites.getCanvas());

				button.setActionCommand("internal");
				button.addActionListener(this);
			}	
			
		} else { // from

			for (DButton button : buttons) {

				DisplayAction action = button.getDisplayAction();

				action.putValue("canvas", replaced);

				button.setActionCommand("");
				button.removeActionListener(this);
			}		
		}
	}


	private static CompoundBorder compoundBorder() {
		
		EtchedBorder 	etchedborder 	= new EtchedBorder(EtchedBorder.RAISED);	
		LineBorder 		lineborder 		= new LineBorder(colorlineborder, 3, true);	
		TitledBorder 	titledborder	= new TitledBorder(lineborder, "Subglyph", TitledBorder.CENTER, TitledBorder.TOP, null, colortitledborder);
		CompoundBorder 	compoundborder 	= new CompoundBorder(titledborder, etchedborder);

		return compoundborder;
	}

	private static TitledBorder titledBorder(String title) {
		
		LineBorder lineborder = new LineBorder(colorlineborder, 1, true);
		
		TitledBorder titledborder = new TitledBorder(lineborder, title, TitledBorder.RIGHT, TitledBorder.TOP, null, colorlineborder);

		return titledborder;		
	}

}
