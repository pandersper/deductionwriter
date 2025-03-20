package view;

import java.awt.AWTKeyStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.KeyboardFocusManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.border.TitledBorder;

import annexes.picker.DeductionPicker;
import annexes.trainer.DeductionTrainer;
import control.DeductionWriter.CustomKeyboardFocusManager;
import control.Shortcut;
import control.Toolbox;
import control.db.DeductionBase;
import model.description.DComposite;
import model.description.DTheorem;
import model.description.abstraction.Described;
import model.independent.DoubleArray;
import model.logic.abstraction.Formal;
import view.abstraction.AbstractFrame;
import view.abstraction.CustomTraversalPolicy;
import view.abstraction.TraversablePanel;

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

	/**
	 * Container for the main canvas.
	 * 
	 * @see DisplayCanvas
	 */
	private class DisplayPanel extends TraversablePanel {
		
		private DisplayCanvas canvas;		
		
		/**
		 * Creates a new display panel and it's layout.
		 */
		public DisplayPanel() {
	        makeAndAddLayout(); 
		}
		
		/**
		 * The canvas for drawing theorems.
		 *
		 * @return The canvas in this panel that is the application's main canvas for showing theorems.
		 */
		public DisplayCanvas getCanvas() {
			return canvas;
		}
		
		private void makeAndAddLayout() {

			this.canvas = new DisplayCanvas();
			canvas.setBackground(new Color(255, 255, 255));

			SpringLayout springLayout = new SpringLayout();
			this.setLayout(springLayout);
					
			springLayout.putConstraint(SpringLayout.NORTH, canvas, 1,  SpringLayout.NORTH, this);
			springLayout.putConstraint(SpringLayout.WEST,  canvas, 1,  SpringLayout.WEST,  this);
			springLayout.putConstraint(SpringLayout.SOUTH, canvas, -1, SpringLayout.SOUTH, this);
			springLayout.putConstraint(SpringLayout.EAST,  canvas, -1, SpringLayout.EAST,  this);

			this.add(canvas);			
		}
	}
	 
	private	DisplayPanel 		pnlDisplay;
	private PrimitivesPanel 	pnlPrimitives; 
	private DeductionPanel 		pnlDeduction;
	private ControlPanel 		pnlControl;
	private DisplayCanvas  		canvas;
	private JPanel 				pnlOuter;

	private CustomTraversalPolicy 		policy;

 	private ActionMap 	actionmap 	= new ActionMap();
	private InputMap 	inputmap 	= new InputMap();

	private Vector<Container> 			roots = new Vector<Container>();
	private Vector<Vector<Component>> 	nodes = new Vector<Vector<Component>>();
 
	private DTheorem 	theorem;
	
	
	/**
	 * Instantiates a new frame containing this application's different panels.
	 *
	 * @param base 		The database for storage.
	 * @param trainer 	The DeductionTrainer module for setting keyboard key bindings to primitives.
	 * @param picker 	The DeductionPicker module for choosing primitives to use in the current theorem.
	 */
 	public DeductionFrame(DeductionBase base, DeductionTrainer trainer, DeductionPicker picker) {    	    	
		super("Deduction frame ...");

		makePanels(base);		

		this.addWindowListener(pnlPrimitives);
		this.addWindowStateListener(pnlPrimitives);
		this.addWindowFocusListener(pnlPrimitives);

		canvas = pnlDisplay.getCanvas();

		pnlControl.newTheorem();
		pnlControl.fillStoreMenu();
		pnlControl.setAnnexes(trainer, picker);

		trainer.setMainFrame(this);
		picker.setMainFrame(this);

		this.loadPrimitives(theorem.primitivestable);	
		this.setResizable(false);
	}

 	
	/**
	 * Returns the theorem in use.
	 *
	 * @return The theorem.
	 */
	public DTheorem currentTheorem() {
		return theorem;
	}
	
	
	/**
	 * Sets the theorem of the whole application and redescribes it relative to the canvas.
	 *
	 * @param theorem The new theorem to fit into the application.
	 */
	public void setAndDescribeTheorem(DTheorem theorem) {

		this.theorem = theorem;
		
		// canvas layouts the theorem so have to put it right first
		DisplayCanvas canvas = pnlDisplay.getCanvas();

		canvas.reset();
		canvas.setAndDescribeTheorem(this.theorem);	
		canvas.newCursor();
		
		pnlPrimitives.setTheorem(this.theorem);
		pnlDeduction.setTheorem(this.theorem);

		this.theorem.printout();
	}
	
	/**
	 * Redo the layout of this frame. It is needed because parts like the primitives panel are relocated between
	 * different frames and are not allowed to be displayed at multiple places in javax.swing. 
	 * 
	 * See {@link Container#addImpl(Component, Object, int)} used by various add-methods.
	 */
	public void redoLayout() {

		pnlOuter.add(pnlPrimitives, 3);	
		pnlPrimitives.setBounds(5, 420, 600, 280);
	}

	
	/**
	 * Add new bindings to the set of primitives in use. Only adds primitives not already there. It also sets 
	 * a new name for the table of primitives stored.  
	 * 
	 * @param bindings	The bindings to update with.
	 * @param viewname	The new name of the set (table) of primitives in use.
	 */
	public void unionPrimitiveBindings(DoubleArray<Formal, Shortcut> bindings, String viewname) {

		theorem.primitivestable = viewname;

		pnlPrimitives.unionBindings(bindings);	
		pnlPrimitives.generatePrimitiveButtons();
	}
	
	/**
	 * Add new bindings to the set of composites in use. Only adds composites not already there. It also sets 
	 * a new name for the table of composites stored.  
	 * 
	 * @param bindings	The bindings to update with.
	 * @param viewname	The new name of the set (table) of primitives in use.
	 */
	public void unionCompositeBindings(DoubleArray<Described, Shortcut> bindings, String viewname) {

		theorem.compositestable = viewname;

		pnlPrimitives.unionBindings(Toolbox.formals(bindings));		// values (evanescent)
		pnlPrimitives.addCompositesButtons(bindings);				// descriptions (permanent)
	}
	
	
	/** {@inheritDoc} */
	public void loadPrimitives(String primitivestable) {

		theorem.primitivestable = primitivestable;
		
		DoubleArray<Formal, Shortcut> bindings = pnlControl.getDeductionBase().fetchPrimitives(theorem.primitivestable);

		this.unionPrimitiveBindings(bindings, primitivestable);
	}

	/** {@inheritDoc} */
	public void loadComposites(String compositestable) {
		
		theorem.compositestable = compositestable;

		DoubleArray<Described, Shortcut> composites = pnlControl.getDeductionBase().fetchComposites(theorem.compositestable);

		this.unionCompositeBindings(composites, compositestable);	
		
		composites = null;																													///(DGGF)
	}

	/**
	 * Stores the composites currently in use in DeductionBase. 
	 * 
	 * @param tablename	The name to store them under.
	 */
	public void storeComposites(String tablename) {

		DeductionBase base = pnlControl.getDeductionBase();
		
		Collection<DComposite> composites = pnlPrimitives.getComposites();
		
		base.insertCompositesTable(composites, tablename);
		
		int x = 0;
	}
	
		
	/**
	 * Gets the primitives panel. As the primitives panel manages a central part of the program's
	 * functionality it has to be exported now and then.
	 *
	 * @return The primitives panel of this application's frame.
	 */
	public PrimitivesPanel getPrimitivesPanel() {
		return pnlPrimitives;
	}
		
	/**
	 * Gets the canvas that draws the theorem. 
	 *
	 * @return The drawing canvas of this frame, and this application.
	 */
	public DisplayCanvas getCanvas() {
		return canvas;
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
		policy.setDefaultComponent(pnlOuter, pnlPrimitives);		// no default component member, se to it that 
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

		pnlOuter.setFocusTraversalPolicy(policy);																							///(EG5E)
		pnlOuter.setFocusable(true);
		pnlOuter.setFocusCycleRoot(true);

		this.setFocusable(false);
		this.setFocusCycleRoot(false);	

		pnlPrimitives.setFocusTraversal(this.manager);
		pnlDeduction.setFocusTraversal(this.manager);		

		pnlDisplay.setFocusTraversal(null);																									///(BFB4)
		pnlControl.setFocusTraversal(null);
	}
	
	/** {@inheritDoc} */
	public Container[] focusCycleRoots() { 

		return new Container[] { pnlOuter, pnlDeduction, pnlPrimitives };
	}
	
	/** {@inheritDoc} */
	public Component[][] focusCycleNodes() { 

		ArrayList<ArrayList<Component>> allnodes = new ArrayList<ArrayList<Component>>();
		ArrayList<Component> nodes;

		nodes = new ArrayList<Component>();	
		nodes.addAll(Arrays.asList(new Component[] {pnlOuter, pnlDeduction, pnlPrimitives}));	 											///(F8G0)
		allnodes.add(nodes);

		nodes = new ArrayList<Component>();
		nodes.addAll(Arrays.asList(pnlDeduction.focusCycleNodes()[0]));																		///(5GC5)
		allnodes.add(nodes);

		nodes = new ArrayList<Component>();
		nodes.addAll(Arrays.asList(pnlPrimitives.focusCycleNodes()[0]));																		///(47F2)
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
		super.repaint();		
		canvas.setPaintMode(false, false, false);
		canvas.repaint();
	}

	
	/*  awt and swing */
	
	private void setNames() {					// if container have null layout focusability don't work

		this.setName("main frame");
		pnlOuter.setName("outer panel");
		pnlControl.setName("control panel");
		pnlDisplay.setName("display panel");
		pnlPrimitives.setName("primitives panel");		
		pnlDeduction.setName("deduction panel");
	}

	private void makePanels(DeductionBase base) {

		pnlOuter = new JPanel();
		pnlOuter.setLayout(null);				// null layout may disturb focus subsystem
		pnlOuter.setSize(630, 810);

		pnlDisplay = new DisplayPanel();
		pnlDisplay.addKeyListener(pnlPrimitives);
		pnlDisplay.setBounds(5, 205, 600, 200);
		pnlDisplay.setBorder(new TitledBorder("Review ..."));
		pnlOuter.add(pnlDisplay);

		pnlControl = new ControlPanel(this, pnlDisplay.getCanvas(), base);
		pnlControl.addKeyListener(pnlPrimitives);
		pnlControl.setBounds(40, 710, 490, 90);
		pnlControl.setBorder(new TitledBorder("Application ..."));
		pnlOuter.add(pnlControl);		

		pnlPrimitives = new PrimitivesPanel(this, pnlDisplay.getCanvas());
		pnlOuter.addKeyListener(pnlPrimitives);
		pnlPrimitives.addKeyListener(pnlPrimitives);			// ?
		pnlPrimitives.setBounds(5, 420, 600, 280);
		pnlPrimitives.setMaps(inputmap, actionmap);

		pnlOuter.add(pnlPrimitives);
		pnlOuter.setActionMap(actionmap);
		pnlOuter.setInputMap(JComponent.WHEN_FOCUSED, inputmap);

		pnlDeduction = new DeductionPanel(pnlDisplay.getCanvas(), pnlPrimitives);
		pnlDeduction.addKeyListener(pnlPrimitives);
		pnlDeduction.setBounds(50, 5, 500, 195);
		pnlDeduction.setBorder(new TitledBorder("Deduce ..."));
		pnlOuter.add(pnlDeduction);
		setNames();

		this.setLayout(null);
		this.setContentPane(pnlOuter);
		this.setSize(630, 950);
	}
}
