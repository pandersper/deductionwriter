package view;

import java.awt.AWTKeyStroke;
import java.awt.Component;
import java.awt.Container;
import java.awt.KeyboardFocusManager;
import java.awt.event.WindowEvent;

import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Vector;

import annexes.picker.DeductionPicker;
import annexes.trainer.DeductionTrainer;
import control.DeductionWriter.CustomKeyboardFocusManager;
import control.session.Session;
import model.description.DTheorem;
import view.abstraction.AbstractFrame;
import view.abstraction.CustomTraversalPolicy;
import view.components.ConcludePanel;
import view.components.DeductionMenuBar;
import view.components.DisplayCanvas;
import view.components.GlyphsPanel;
import view.components.NavigatePanel;


/**
 * The class DeductionFrame contains the application's panels and is furthermost an intermediary betwen all other components.<br><br>
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
	
	private DeductionTrainer 	thread;
	
	private CustomTraversalPolicy 		policy;

 	private ActionMap 	actionmap 	= new ActionMap();
	private InputMap 	inputmap 	= new InputMap();

	private Vector<Container> 			roots = new Vector<Container>();
	private Vector<Vector<Component>> 	nodes = new Vector<Vector<Component>>(); 	
	
	
	/**
	 * Instantiates a new frame containing this application's different panels.
	 *
	 * @param trainer 	The DeductionTrainer module for setting keyboard key bindings to primitives.
	 * @param picker 	The DeductionPickerOld module for choosing primitives to use in the current theorem.
	 */
 	public DeductionFrame(DeductionTrainer trainer, DeductionPicker<?> picker, Session session) {    	    	
		super("Deduction frame ...");

		setTitle("DeductionWriter");				
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.session = session;
		this.thread = trainer;

		pnlGlyphs = this.session.makeGlypsPanel(this); 
				
		makePanels();		

		connectToAnnexes(trainer, picker);
		setupListeners();		
		setNames();		
	}
 	
 	
	/**
	 * Sets the theorem of the whole application and redescribes it relative to the canvas.
	 *
	 * @param theorem The new theorem to fit into the application.
	 */
	public void 		setAndDescribeTheorem(DTheorem theorem) {

		DisplayCanvas canvas = session.getCurrentCanvas();		// canvas layouts the theorem so have to put it right first

		canvas.reset();
		canvas.setTheorem(theorem);	
		canvas.describeTheorem();	
		canvas.newCursor();
				
		theorem.printout();
	}

	/**
	 * The panel of glyphs is transfered to break-out applications neatly but all buttons's 
	 * actions need to be reset as well.
 	 * 
	 * @return The primitives panel of this application's frame.
	 */
	public GlyphsPanel 	getGlyphsPanel() {
		return pnlGlyphs;
	}
 	/**
 	 * The panel of glyphs is transfered to break-out applications neatly. This method can reset it
 	 * but all buttons actions need to be reset as well.
 	 *   
 	 * @param glyphspanel
 	 */
	public void 		setGlyphsPanel(GlyphsPanel glyphspanel) {
		this.pnlGlyphs = glyphspanel;
		this.pnlSide.add(this.pnlGlyphs);
	}

 	/**
 	 * Return the session currently in use. The session serves as a centraliser and capsule of work 
 	 * but is still needed at quiet a lot of places. This frame distributes the session to its panels. 
 	 * 
 	 * @return
 	 */
	public Session 		getSession() {
		return session;
	}
	/**
	 * Clears and reuses the current session.
	 */
	public void 		newSession() {

		this.session.clearSesssion();
	}
	/**
	 * Stores the current session.
	 */
	public boolean 		storeSession(String name) {
		
		session.changeSessionName(name);
		
		return session.saveSession(); 
	}
	/**
	 * For now, just exits and leaves everything to vm.
	 */
	public void 		cleanAndExit() {
		
		session.closeSession();
		
		DeductionFrame.this.dispose();
	}	
		
	/* * * *  event related  * * * */
	
 	/** 
 	 * Delegates to windowadapter. 
 	 */
	public void windowClosing(WindowEvent e) {
		cleanAndExit();
	}
 	/** 
 	 * Delegates to windowadapter. 
 	 */
	public void windowClosed(WindowEvent e) {

		thread.halt();

		System.exit(0);
	}
	
	/* * * *  focus related  * * * */
	
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

		this.manager = manager;
		this.policy = (CustomTraversalPolicy) manager.getDefaultFocusTraversalPolicy();

		HashSet<AWTKeyStroke> keys = new HashSet<AWTKeyStroke>();

		keys.add(AWTKeyStroke.getAWTKeyStroke("alt shift released U"));
		manager.setDefaultFocusTraversalKeys(KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS, keys);

		keys = new HashSet<AWTKeyStroke>();

		keys.add(AWTKeyStroke.getAWTKeyStroke("alt shift released D"));
		manager.setDefaultFocusTraversalKeys(KeyboardFocusManager.DOWN_CYCLE_TRAVERSAL_KEYS, keys);

		content.setFocusTraversalPolicy(policy);														///(EG5E)
		content.setFocusable(true);
		content.setFocusCycleRoot(true);

		this.setFocusable(false);
		this.setFocusCycleRoot(false);	

		pnlGlyphs.setFocusTraversal(this.manager);
		pnlSide.setFocusTraversal(this.manager);		

		pnlMain.setFocusTraversal(null);																///(BFB4)
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
		nodes.addAll(Arrays.asList(new Container[] { content, pnlSide, pnlGlyphs }));	 					///(F8G0)
		allnodes.add(nodes);

		nodes = new ArrayList<Component>();
		nodes.addAll(Arrays.asList(pnlSide.focusCycleNodes()[0]));										///(5GC5)
		allnodes.add(nodes);

		nodes = new ArrayList<Component>();
		nodes.addAll(Arrays.asList(pnlGlyphs.focusCycleNodes()[0]));									///(47F2)
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

	/* * * * awt and swing * * * */
	
	private void setNames() {		
					
		// if container have null layout focusability don't work

		this.setName("main frame");
		content.setName("content pane");
		pnlMain.setName("writing surface");
		pnlGlyphs.setName("glyphs panel");		
		pnlSide.setName("side panel");
	}

	private void setupListeners() {
		
		this.addWindowListener(this);
		this.addWindowStateListener(this);
		this.addWindowFocusListener(pnlGlyphs);
	}	

	private void connectToAnnexes(DeductionTrainer trainer, DeductionPicker<?> picker) {
		trainer.setMainFrame(this);
		picker.setMainFrame(this);
	}

	private void makePanels() {

		content = new JPanel();

		content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));		
		content.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(content);
		
		NavigatePanel navigatePanel = new NavigatePanel(this);
		ConcludePanel concludePanel = new ConcludePanel();

		pnlSide 	= new SidePanel(this,pnlGlyphs,navigatePanel,concludePanel);
		pnlMain 	= new MainPanel(this);

		JPanel 		pnlWrite = new JPanel();
		
		pnlWrite.setLayout(new BoxLayout(pnlWrite, BoxLayout.X_AXIS));
		pnlWrite.setAlignmentX(Component.RIGHT_ALIGNMENT);
		pnlWrite.add(pnlMain);		
		
		JScrollPane scrWrite = new JScrollPane();
		scrWrite.setViewportView(pnlWrite);

		content.add(pnlSide);
		content.add(scrWrite);			// viewport is pnlMain
		pnlSide.add(navigatePanel);
		pnlSide.add(concludePanel);		
		pnlSide.add(pnlGlyphs);
		
		pnlGlyphs.setMaps(inputmap, actionmap);

		content.setActionMap(actionmap);
		content.setInputMap(JComponent.WHEN_FOCUSED, inputmap);
	}
	
}
