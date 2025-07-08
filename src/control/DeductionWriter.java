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

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import annexes.picker.DeductionPicker;
import annexes.trainer.DeductionTrainer;
import control.db.DeductionBase;
import control.session.Session;
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

	private final DeductionBase 	 	base; 	
	private final DeductionTrainer 		trainer; 	
	private final DeductionPicker<?> 	picker; 	
	private final DeductionFrame 		frame;
	private final DeductionMenuBar		menu;
	
	private Session 					session; 

	private CustomKeyboardFocusManager 	manager;
	private CustomTraversalPolicy 		policy;

	/**
	 * Instantiates a new DeductionWriter application.
	 */
	public DeductionWriter() {		
						
		setLookAndFeel();

		base 	= new DeductionBase(false);
		session = new Session("empty", base);		
		trainer = new DeductionTrainer(session);	
		picker 	= new DeductionPicker<>(session); 
		frame 	= new DeductionFrame(trainer,picker,session); 		// frame henceforth distributes session			
		menu 	= new DeductionMenuBar(trainer, picker, frame);
		
		this.initManager();			/* default policy only sticks to new components - must be outside constructor */
		this.initComponents();
		this.setListeners();	
	}


	private void setLookAndFeel() {
		
		LookAndFeelInfo[] lfi = UIManager.getInstalledLookAndFeels();	

		try { UIManager.setLookAndFeel(lfi[1].getClassName()); } 
		
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException| UnsupportedLookAndFeelException e) { e.printStackTrace(); }
	}


	private void initManager() { 

		manager = new CustomKeyboardFocusManager();
		KeyboardFocusManager.setCurrentKeyboardFocusManager(manager);  

		policy = new CustomTraversalPolicy();		
		manager.setDefaultFocusTraversalPolicy(policy);	

		frame.setFocusTraversal(manager);
		frame.initTraversalPolicy();
	}

	private void initComponents() {		

		frame.setJMenuBar(menu);
		menu.fillStoreMenu();		
		session.loadPrimitives("default");	
		trainer.startTimer();
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
	}
	/** {@inheritDoc} */
	public void focusLost(FocusEvent e) {

		Component c = e.getComponent();
		
		c.setBackground(Color.lightGray);
	}
	
	/** {@inheritDoc} */	
	public static void main(String[] args) {

		DeductionWriter application = new DeductionWriter();
				
		application.frame.setAllVisible();		
		application.frame.pack();		

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

}
