package view.abstraction;

import java.awt.Component;
import java.awt.Container;
import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;

import javax.swing.JPanel;
import javax.swing.KeyStroke;

import control.DeductionWriter.CustomKeyboardFocusManager;
import control.Shortcut;
import control.Toolbox;
import model.description.DTheorem;
import model.independent.DoubleArray;
import model.independent.DoubleArray.Tuple;
import model.logic.abstraction.Formal;
import view.DeductionFrame;
import view.GlyphsPanel;
import view.components.DisplayCanvas;

/**
 * A JPanel object that handles the most common window events and window listening. An important distinction to make is that between this
 * panel's components focus traversal and this panel's parent frame's focus traversal.
 *
 * @see WindowFocusListener
 * @see java.awt.event.FocusListener 
 */
public abstract class TraversablePanel extends JPanel implements WindowListener, WindowStateListener, WindowFocusListener  {		

	
	/** Still just a plain WindowAdapter and nothing else. */
	public class CustomAdapter extends WindowAdapter { }



	/** Filters key events to those that concerns the bindings in use in the application and then dispatches them again. */
	public class CustomDispatcher implements KeyEventDispatcher {
																																				/**(251A)**/
		public boolean dispatchKeyEvent(KeyEvent e) {

			KeyStroke stroke = KeyStroke.getKeyStrokeForEvent(e);

			if (e.isControlDown() || stroke.getKeyCode() == KeyEvent.VK_SPACE) {																///(E82E)

				manager.redispatchEvent(panel, e);
				e.consume();
				return true;				

			} else {

				if (e.isShiftDown() || e.isAltDown() || e.getModifiersEx() == 0){ 															///(FAE4)

					Tuple<Formal, Shortcut> binding = findByKeystroke(bindings, stroke);

					if (binding != null) {																									///(FCA6)

						manager.dispatchKeyEvent(e);								
						e.consume();
						return true;	

					} else {																													///(8AEB)

						e.consume(); 							
						return false;
					}

				} else {																														///(E3F2)

					e.consume(); 													System.out.println("Other strange modifiers.");
					return false;
				}
			}
		}

		private Tuple<Formal, Shortcut> findByKeystroke(DoubleArray<Formal, Shortcut> bindings, KeyStroke stroke) {

			for (Tuple<Formal, Shortcut> binding : bindings) 
				if (binding.second().keycode == stroke.getKeyCode() && binding.second().modifiers == stroke.getModifiers())
					return binding;

			return null;
		}	
	}

	
	/** The bindings used in the application. Keyboard events that is not in this collection are filtered away. */
	protected DoubleArray<Formal, Shortcut> 	bindings = new  DoubleArray<Formal, Shortcut>() ;

	
	/** Often the parent or grand parent container of this panel. */
	protected DeductionFrame 					parent;

	/** The panel containing the buttons for typing primitives. */																																			
	protected GlyphsPanel						panel;

	/** Handles focus traversal by keyboard. */
	protected CustomKeyboardFocusManager 		manager;

	/** 
	 * The default component to focus 
	 * 
	 * @see java.awt.KeyboardFocusManager#upFocusCycle(Component) 
	 * */
	protected Component 						defaultfocus;

	
	/** When changing to and from this panel, a key event dispatcher change is needed. Then this variable holds the old one temporarily. */
	protected KeyEventDispatcher 	olddispatcher;				

	/** The dispatcher that filter key events to only thos that are relevant, that are bound to primitives. */
	protected CustomDispatcher 		dispatcher;

	/** Still just a plain window adapter. {@link WindowAdapter}. */
	protected CustomAdapter			adapter = new CustomAdapter();

	

 	public DTheorem getTheorem() {
 		return this.parent.getSession().getCurrentCanvas().getTheorem();
 	}
 	
 	public DisplayCanvas getCanvas() {
 		return this.parent.getSession().getCurrentCanvas(); 		
 	}
	/**
	 * The bindings used across the application. Maps formal mathematics atoms to keyboard short-cuts.
	 * 
	 * @return A bijective, sortable structure of bindings.
	 * 
	 * @see model.independent.DoubleArray
	 */
	public DoubleArray<Formal, Shortcut> getBindings() {
		return this.bindings;
	}

	public DeductionFrame getFrame() { return parent; }
	
	/**
	 * Sets up essential focus traversal components.
	 * 
	 * @param manager 	Managing focus control.
	 */
	public void setFocusTraversal(CustomKeyboardFocusManager manager) {
		this.manager = manager;	
		this.setFocusable(false);
		this.setFocusCycleRoot(false);		
	}
	/**
	 * Sets the default component to focus.
	 */
	public void setDefaultComponent() {}
	
	/**
	 * Returns the containers serving as focus cycle roots in this panels focus cycle.
	 *
	 * @return The roots in the focus cycle.
	 */
	public Container[] focusCycleRoots() { return null; }
	/**
	 * Returns an array of arrays containing the components that are traversed in each root's focus travesal cycle.
	 * The root itself can but does not have to be included in it's cycle and array.
	 *
	 * @return An array of all the cycle roots's focus cycles.
	 */
	public Component[][] focusCycleNodes() { return null; }
	
	/** 
	 * Calls windowActivated. 
	 * @see #windowActivated(WindowEvent)
	 */
	public void windowOpened(WindowEvent e) {

		if (Toolbox.DEBUGVERBOSE) System.out.println("window opened");

		this.parent.getSession().getCurrentCanvas().setPaintMode(false, false, false);
		panel.restoreFocus();

	}
	/** Delegates to adapter. */
	public void windowClosing(WindowEvent e) {
		adapter.windowClosing(e);
	}
	/** Delegates to adapter. */
	public void windowClosed(WindowEvent e) {
		adapter.windowClosed(e);
	}
	/** Delegates to adapter. */
	public void windowIconified(WindowEvent e) {
		adapter.windowIconified(e);
	}
	/** Delegates to adapter. */
	public void windowDeiconified(WindowEvent e) {
		adapter.windowDeiconified(e);
	}
	/** 
	 * Repaints all and restores focus to the panel of buttons. 
	 * 
	 *	@see view.components.DisplayCanvas#setPaintMode(boolean, boolean, boolean) 
	 */
	public void windowActivated(WindowEvent e) {

		if (Toolbox.DEBUGVERBOSE) System.out.println("window activated - repainting all");

		this.parent.getSession().getCurrentCanvas().setPaintMode(false, false, false);
		
		panel.restoreFocus();
	}
	/** Delegates to adapter. */
	public void windowDeactivated(WindowEvent e) {
		adapter.windowDeactivated(e);
	}
	/** Delegates to adapter. */
	public void windowStateChanged(WindowEvent e) {
		adapter.windowStateChanged(e);
	}
	/** Delegates to adapter. */
	public void windowGainedFocus(WindowEvent e) {
		System.out.println("window gained focus");
		adapter.windowGainedFocus(e);
	}
	/** Delegates to adapter. */
	public void windowLostFocus(WindowEvent e) {
		adapter.windowLostFocus(e);
	}
	
}
