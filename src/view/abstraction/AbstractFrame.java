package view.abstraction;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JFrame;

import control.DeductionWriter.CustomKeyboardFocusManager;


/**
 * Corresponds to {@link TraversablePanel} but for JFrames instead of JPanels and also contains
 * much less functionality.
 */
public abstract class AbstractFrame extends JFrame {		

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
	 * Fetches a table (an sql view) of primitives from DeductionBase and sets it up for usage.
	 * 
	 * @param columnvalue The name of the table that contain the primitives. That is used when storing it.
	 */
	public abstract void loadPrimitives(String columnvalue);

	/**
	 * Fetches a table (an sql view) of composites from DeductionBase and sets it up for usage.
	 *
	 * @param columnvalue The name of the table that contain the composites. That is used when storing it.
	 */
	public abstract void loadComposites(String columnvalue);

	
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
