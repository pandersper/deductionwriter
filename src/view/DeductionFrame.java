package view;

import java.awt.AWTKeyStroke;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import annexes.picker.DeductionPicker;
import annexes.trainer.DeductionTrainer;
import control.DeductionWriter.CustomKeyboardFocusManager;
import control.Session;
import control.db.DeductionBase;
import model.description.DTheorem;
import view.abstraction.AbstractFrame;
import view.abstraction.CustomTraversalPolicy;
import view.components.DeductionMenuBar;
import view.components.DisplayCanvas;
import view.components.ViewConstants;


/**
 * The class DeductionFrame contains the applications panels and is furthermost an intermediary betwen all other components.<br><br>
 * 
 * This design choice have been a good one and remained throughout the project. As has most of the modell-view-control
 * design pattern. The components in the view part has also worked ok allng the way with some small adjustments. One such is 
 * that {@see DisplayPanel} was not significant enough deserve an own class, hence is noe a private class in this class.
 * Making the buttons do much work was a god choice and I guess it is a quite common way to do it. This makes object oriented
 * programming do it's work at length.
 */
public class DeductionFrame extends AbstractFrame {

	 
	private DeductionMenuBar 	menuBar;
	private	MainPanel 			pnlMain;
	private SidePanel 			pnlSide;
	private GlyphsPanel			pnlGlyphs;

	private JPanel 				content;

	private CustomTraversalPolicy 		policy;

 	private ActionMap 	actionmap 	= new ActionMap();
	private InputMap 	inputmap 	= new InputMap();

	private Vector<Container> 			roots = new Vector<Container>();
	private Vector<Vector<Component>> 	nodes = new Vector<Vector<Component>>();
 	
	
	/**
	 * Instantiates a new frame containing this application's different panels.
	 *
	 * @param base 		The database for storage.
	 * @param trainer 	The DeductionTrainer module for setting keyboard key bindings to primitives.
	 * @param picker 	The DeductionPicker module for choosing primitives to use in the current theorem.
	 */
 	public DeductionFrame(DeductionTrainer trainer, DeductionPicker picker, Session session) {    	    	
		super("Deduction frame ...");

		this.session = session;
		
		setTitle("DeductionWriter");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		makePanels(this.session.getBase());		

		this.addWindowListener(pnlGlyphs);
		this.addWindowStateListener(pnlGlyphs);
		this.addWindowFocusListener(pnlGlyphs);

		trainer.setMainFrame(this);
		picker.setMainFrame(this);
		
		setNames();
	}


	public Session getSession() {
		return session;
	}

	/**
	 * Returns the theorem in use.
	 *
	 * @return The theorem.
	 */
		/**
	 * Sets the theorem of the whole application and redescribes it relative to the canvas.
	 *
	 * @param theorem The new theorem to fit into the application.
	 */
	public void setAndDescribeTheorem(DTheorem theorem) {

		// canvas layouts the theorem so have to put it right first
		DisplayCanvas canvas = session.getCurrentCanvas();

		canvas.reset();
		canvas.setAndDescribeTheorem(theorem);	
		canvas.newCursor();
		
		pnlMain.stateChanged(null);
		
		theorem.printout();
	}
	
	/**
	 * Redo the layout of this frame. It is needed because parts like the primitives panel are relocated between
	 * different frames and are not allowed to be displayed at multiple places in javax.swing. 
	 * 
	 * See {@link Container#addImpl(Component, Object, int)} used by various add-methods.
	 */
	public void redoLayout() {

		content.add(pnlGlyphs, 3);	
		pnlGlyphs.setBounds(5, 420, 600, 280);
	}

		
			
	/**
	 * Gets the primitives panel. As the primitives panel manages a central part of the program's
	 * functionality it has to be exported now and then.
	 *
	 * @return The primitives panel of this application's frame.
	 */
	public GlyphsPanel getGlyphsPanel() {
		return pnlGlyphs;
	}
	


	
	/**
	 * For now, just exits and leaves everything to vm.
	 */
	public static void cleanAndExit() {
		System.exit(0);
	}

	
	/*  focus related  */
	
	/**
	 * Sets up the traversal policy of this appliction.
	 * 
	 * @see view.abstraction.CustomTraversalPolicy
	 */
	public void initTraversalPolicy() {

		Container[]   cycleroots = this.focusCycleRoots();
		Component[][] cycles 	 = this.focusCycleNodes();

		roots.addAll(Arrays.asList(cycleroots));

		for (int i = 0; i < roots.size(); i++) {		
			nodes.add(new Vector<Component>());		
			nodes.get(i).addAll(Arrays.asList(cycles[i]));
		}

		policy.addComponents(roots, nodes);
		policy.setAllDefaults();	
		policy.setDefaultComponent(content, pnlGlyphs);		// no default component member, se to it that 
																	// primitivesPanel is it's node in policy
	}
	
	/** {@inheritDoc} */
	public void setFocusTraversal(CustomKeyboardFocusManager manager) {

		this.manager 			= manager;
		this.policy 			= (CustomTraversalPolicy) manager.getDefaultFocusTraversalPolicy();

		HashSet<AWTKeyStroke> keys = new HashSet<AWTKeyStroke>();

		keys.add(AWTKeyStroke.getAWTKeyStroke("alt shift released U"));
		manager.setDefaultFocusTraversalKeys(KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS, keys);

		keys = new HashSet<AWTKeyStroke>();
		keys.add(AWTKeyStroke.getAWTKeyStroke("alt shift released D"));
		manager.setDefaultFocusTraversalKeys(KeyboardFocusManager.DOWN_CYCLE_TRAVERSAL_KEYS, keys);

		content.setFocusTraversalPolicy(policy);																							///(EG5E)
		content.setFocusable(true);
		content.setFocusCycleRoot(true);

		this.setFocusable(false);
		this.setFocusCycleRoot(false);	

		pnlGlyphs.setFocusTraversal(this.manager);
		pnlSide.setFocusTraversal(this.manager);		

		pnlMain.setFocusTraversal(null);																									///(BFB4)
		pnlSide.setFocusTraversal(null);
	}
	
	/** {@inheritDoc} */
	public Container[] focusCycleRoots() { 

		return new Container[] { content, pnlSide, pnlGlyphs };
	}
	
	/** {@inheritDoc} */
	public Component[][] focusCycleNodes() { 

		ArrayList<ArrayList<Component>> allnodes = new ArrayList<ArrayList<Component>>();
		ArrayList<Component> nodes;

		nodes = new ArrayList<Component>();	
		nodes.addAll(Arrays.asList(new Component[] {content, pnlSide, pnlGlyphs}));	 											///(F8G0)
		allnodes.add(nodes);

		nodes = new ArrayList<Component>();
		nodes.addAll(Arrays.asList(pnlSide.focusCycleNodes()[0]));																		///(5GC5)
		allnodes.add(nodes);

		nodes = new ArrayList<Component>();
		nodes.addAll(Arrays.asList(pnlGlyphs.focusCycleNodes()[0]));																		///(47F2)
		allnodes.add(nodes);

		Component[][] allnodesarray = new Component[allnodes.size()][];

		for (int i = 0; i < allnodesarray.length; i++) {

			Component[] nodesarray = new Component[allnodes.get(i).size()];

			for (int j = 0; j < allnodes.get(i).size(); j++)
				nodesarray[j] = allnodes.get(i).get(j);

			allnodesarray[i] = nodesarray;
		}

		return allnodesarray;
	}

	
	/**
	 * Calls super.repaint() and thereafter repaints all.
	 * 
	 * @see java.awt.Component#repaint()
	 */
	public void repaint() {
		
		session.getCurrentCanvas().setPaintMode(false, false, false);
		session.getCurrentCanvas().repaint();
		
		pnlMain.stateChanged(null);
		
		super.repaint();		
	}

	
	/*  awt and swing */
	
	private void setNames() {					// if container have null layout focusability don't work

		this.setName("main frame");
		content.setName("content pane");
		pnlMain.setName("writing surface");
		pnlGlyphs.setName("glyphs panel");		
		pnlSide.setName("side panel");
	}

	private void makePanels(DeductionBase base) {

		content = new JPanel();
		content.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(content);

		pnlMain = new MainPanel(this);

		NavigatePanel navigatePanel = new NavigatePanel(this);
		ConcludePanel concludePanel = new ConcludePanel();

		DisplayCanvas current = session.getCurrentCanvas();
		
		pnlGlyphs 	= new GlyphsPanel(this);
		pnlSide 	= new SidePanel(this,pnlGlyphs,navigatePanel,concludePanel);

		navigatePanel.registerButtons(pnlSide);
		concludePanel.registerButtons(pnlSide);

		JPanel pnlWrite = new JPanel();

		pnlGlyphs.addKeyListener(pnlGlyphs);			
		
		pnlWrite.add(pnlMain);
		JScrollPane scrWrite = new JScrollPane();
		scrWrite.setViewportView(pnlWrite);

		content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));		
		content.add(pnlSide);
		content.add(scrWrite);	// viewport is pnlMain

		navigatePanel.setMaximumSize(new Dimension(225,150));
		concludePanel.setMaximumSize(new Dimension(225,300));
		navigatePanel.setMinimumSize(new Dimension(225,150));
		concludePanel.setMinimumSize(new Dimension(225,300));

		pnlSide.add(navigatePanel);
		pnlSide.add(concludePanel);		
		pnlSide.add(pnlGlyphs);

		pnlSide.setLayout(new BoxLayout(pnlSide, BoxLayout.Y_AXIS));
		pnlWrite.setLayout(new BoxLayout(pnlWrite, BoxLayout.X_AXIS));

		pnlSide.setAlignmentX(Component.LEFT_ALIGNMENT);
		pnlWrite.setAlignmentX(Component.RIGHT_ALIGNMENT);
		
		pnlSide.setPreferredSize(new Dimension(350, 3*ViewConstants.a4height));
		pnlMain.setPreferredSize(new Dimension(3*ViewConstants.a4width, 3*ViewConstants.a4height));

		pnlGlyphs.setMaps(inputmap, actionmap);

		content.setActionMap(actionmap);
		content.setInputMap(JComponent.WHEN_FOCUSED, inputmap);
	}



}
