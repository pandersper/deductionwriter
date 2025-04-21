package view.abstraction;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JFrame;

import control.DeductionWriter.CustomKeyboardFocusManager;
import control.Session;


/**
 * Corresponds to {@link TraversablePanel} but for JFrames instead of JPanels and also contains
 * much less functionality.
 */
public abstract class AbstractFrame extends JFrame {		

	protected Session session;
	
	/** The defaultcomponent to focus. */
	protected Component defaultcomponent;	
	
	/** Manages focus traversal by keyboard. */
	protected CustomKeyboardFocusManager manager;

	/**
	 * Instantiates a new traversable frame.
	 *
	 * @param string The title of this frame.
	 */
	public AbstractFrame(String string) {
		super(string);
	}

	
	/**
	 * Sets the session that AbstractFrames uses.
	 * @param session
	 */
	public void setSession(Session session) {
		this.session = session;
	}	
	
	/**
	 * Sets up a new focus traversal manager.
	 *
	 * @param manager The new focus traversal manager.
	 * 
	 * @see control.DeductionWriter.CustomKeyboardFocusManager
	 */
	public void setFocusTraversal(CustomKeyboardFocusManager manager) {
		this.manager = manager;	
		this.setFocusable(false);
		this.setFocusCycleRoot(false);
	}
	
	/**
	 * Retreives the focus cycle roots of this frame.
	 *
	 * @return Array of the containers that are roots in this frame's focus traversal tree structure.
	 * 
	 * @see Container
	 */
	public Container[] focusCycleRoots() { return null; }
																																				
	/**
	 * Retreives the focus cycle nodes of all the focus cycle roots of this frame.
	 *
	 * @return Array of arrays of components to change focus between.
	 * 
	 * @see AbstractFrame
	 * @see Component
	 */
	public Component[][] focusCycleNodes() { return null; }
		
}
