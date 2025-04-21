package control;

import java.awt.Color;
import java.awt.Component;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.HashSet;
import java.util.List;

import annexes.picker.DeductionPicker;
import annexes.trainer.DeductionTrainer;
import control.db.DeductionBase;
import view.DeductionFrame;
import view.abstraction.CustomTraversalPolicy;
import view.components.DeductionMenuBar;


/**
 * The application DeductionWriter - A note taking tool for mathematics and other glyph intensive writing. 
 */
public class DeductionWriter implements FocusListener {
	
	/**
	 * Manages the focus traversal of the application. For now identical with it's base class.
	 */
	public class CustomKeyboardFocusManager extends DefaultKeyboardFocusManager {
		
		/** {@inheritDoc} */
		public Component getGlobalFocusOwner() throws SecurityException {
			return super.getGlobalFocusOwner();
		}
				
		/** {@inheritDoc} */
		public synchronized List<KeyEventDispatcher> getKeyEventDispatchers() {
			return super.getKeyEventDispatchers();
		}
	}

	private final DeductionBase 	 	base 	= new DeductionBase(false); 	
	private final DeductionMenuBar		menu;

	private Session 					session = new Session(base,"empty");;
	
	// to be anonymous
	private final DeductionTrainer 		trainer = new DeductionTrainer();	
	private final DeductionPicker 		picker 	= new DeductionPicker(session); 
	private final DeductionFrame 		frame 	= new DeductionFrame(trainer,picker,session); 			
	
	private CustomKeyboardFocusManager 	manager;
	private CustomTraversalPolicy 		policy;


	/**
	 * Instantiates a new DeductionWriter application.
	 */
	public DeductionWriter() {		
				
		session.connectPanels(frame);

		menu 	= new DeductionMenuBar(trainer, picker, frame);
		menu.fillStoreMenu();		

		frame.setJMenuBar(menu);

		session.loadPrimitives("default");	
		
						
		initManager();	 		/* default policy only sticks to new components - must be outside constructor */
		initComponents();
		setListeners();		
	}


	private void initManager() { 

		manager = new CustomKeyboardFocusManager();
		KeyboardFocusManager.setCurrentKeyboardFocusManager(manager);  

		policy = new CustomTraversalPolicy();		
		manager.setDefaultFocusTraversalPolicy(policy);	
	}

	private void initComponents() {		

		frame.setFocusTraversal(manager);
		frame.initTraversalPolicy();

	}

	private void setListeners() {

		HashSet<Component> set = new HashSet<Component>();

		for (Component c : frame.focusCycleRoots()) {
			if (c != null) set.add(c);
		}

		for (Component[] cs : frame.focusCycleNodes()) 
			for (Component c : cs) 
				if (c != null) set.add(c);

		for (Component unique : set)
			unique.addFocusListener(this);
	}

	/** {@inheritDoc} */
	public void focusGained(FocusEvent e) {

		Component c = e.getComponent();	
		c.setBackground(Color.white);		
	}
	/** {@inheritDoc} */
	public void focusLost(FocusEvent e) {

		Component c = e.getComponent();
		c.setBackground(Color.lightGray);
	}
	

	private void info(String intro) {
		System.out.println("\t<<< " + intro + ">>>");
		System.out.println("Focusmanager:\t\t" + 
		KeyboardFocusManager.getCurrentKeyboardFocusManager().toString());
		System.out.println("Default policy:\t\t" + 
		KeyboardFocusManager.getCurrentKeyboardFocusManager().getDefaultFocusTraversalPolicy().toString());
		System.out.println("Frame policy:\t\t" + frame.getFocusTraversalPolicy());
		System.out.println("-------------------------------------------------------------------");
	}

	/** {@inheritDoc} */	
	public static void main(String[] args) {

		DeductionWriter application = new DeductionWriter();
		application.frame.setVisible(true);		
		application.frame.pack();		
	} 

}
