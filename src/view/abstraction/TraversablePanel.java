package view.abstraction;

import java.awt.Component;
import java.awt.Container;
import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;
import java.awt.event.WindowFocusListener;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import control.DeductionWriter.CustomKeyboardFocusManager;
import control.session.Shortcut;
import model.description.DTheorem;
import model.independent.DoubleArray;
import model.independent.DoubleArray.Tuple;
import model.logic.abstraction.Formal;
import view.DeductionFrame;
import view.components.DisplayCanvas;
import view.components.GlyphsPanel;

/**
 * A JPanel object that handles the most common window events and windows listening. An important distinction to make 
 * is that between this panel's components focus traversal and this panel's elder frame's focus traversal.
 *
 * @see WindowFocusListener
 * @see java.awt.event.FocusListener 
 */
public abstract class TraversablePanel extends JPanel implements InitiableContainer  {		

	
	/** 
	 * Filters key events to those that concerns the bindings in use in the application and then dispatches them again. 
	 */
	public class CustomDispatcher implements KeyEventDispatcher {

		/**
		 * Overriden method to handle direct commands via keyboard key short cuts. After handling events and redispatching
		 * them to the keyboard focus manager it consumes them so that they wont flood event handling as the easily do.
		 * <br>
		 * SPACE key is for accepting a formal<br>
		 * SHIFT, ALT is for focus traversal<br>
		 * ELSE istreated as key strokes.
		 */
		public boolean dispatchKeyEvent(KeyEvent e) {

			KeyStroke stroke = KeyStroke.getKeyStrokeForEvent(e);

			if (e.isControlDown() || stroke.getKeyCode() == KeyEvent.VK_SPACE) {

				manager.redispatchEvent(panel, e);
				e.consume();
				return true;				

			} else {

				if (e.isShiftDown() || e.isAltDown() || e.getModifiersEx() == 0){ 

					Tuple<Formal, Shortcut> binding = findByKeystroke(bindings, stroke);

					if (binding != null) {

						manager.dispatchKeyEvent(e);								
						e.consume();
						return true;	

					} else {

						e.consume(); 							
						return false;
					}

				} else {

					System.out.println("Other strange modifiers.");
					e.consume(); 													
					return false;
				}
			}
		}

		private Tuple<Formal, Shortcut> findByKeystroke(DoubleArray<Formal, Shortcut> bindings, KeyStroke stroke) {

			for (Tuple<Formal, Shortcut> binding : bindings) 
				if (binding.second()!= null && binding.second().keycode == stroke.getKeyCode() && binding.second().modifiers == stroke.getModifiers())
					return binding;
			
			
			return null;
		}	
	}

	
	/** 
	 * The bindings used in the application. Keyboard events that is not in this collection are filtered away. 
	 */
	protected DoubleArray<Formal, Shortcut> 	bindings = new  DoubleArray<Formal, Shortcut>() ;

	
	/** 
	 * Often the elder or grand elder container of this panel. 
	 */
	protected DeductionFrame 					parent;

	/** 
	 * The panel containing the buttons for typing primitives. 
	 */																																			
	protected GlyphsPanel						panel;

	/** 
	 * Handles focus traversal by keyboard. 
	 */
	protected CustomKeyboardFocusManager 		manager;

	/** 
	 * The default component to focus 
	 * 
	 * @see java.awt.KeyboardFocusManager#upFocusCycle(Component) 
	 */
	protected Component 						defaultfocus;

	
	/** 
	 * When changing to and from this panel, a key event dispatcher change is needed. Then this variable holds the old
	 * one temporarily. 
	 */
	protected KeyEventDispatcher 	olddispatcher;				

	/** 
	 * The dispatcher that filter key events to only thos that are relevant, that are bound to primitives. 
	 */
	protected CustomDispatcher 		dispatcher;
	
	/**
	 * Exports the theorem in use.
	 * 
	 * @return	The theorem in the tab currently selected.
	 */
 	public DTheorem getTheorem() {
 		return this.parent.getSession().getCurrentCanvas().getTheorem();
 	}
 	
 	/**
 	 * The display canvas currently selected.
 	 */
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
	
}
