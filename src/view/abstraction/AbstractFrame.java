package view.abstraction;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import javax.swing.JFrame;

import control.DeductionWriter.CustomKeyboardFocusManager;
import control.session.Session;

/**
 * Corresponds to {@link TraversablePanel} but for JFrames instead of JPanels and also contains
 * much less functionality.
 */
public abstract class AbstractFrame extends JFrame implements InitiableContainer, WindowListener, WindowStateListener {		

	
	/** Still just a plain WindowAdapter and nothing else. */
	public class CustomWindowAdapter extends WindowAdapter { }
	
	
	protected Session session;
	
	/** The defaultcomponent to focus. */
	protected Component defaultcomponent;	
	
	/** Manages focus traversal by keyboard. */
	protected CustomKeyboardFocusManager manager;
	
	/** Still just a plain window windowadapter. {@link WindowAdapter}. */
	protected CustomWindowAdapter			windowadapter = new CustomWindowAdapter();


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
		

	/** 
	 * Calls windowActivated. 
	 * @see #windowActivated(WindowEvent)
	 */
	public void windowOpened(WindowEvent e) {
		session.restoreFocus();
		windowadapter.windowOpened(e);
	}	
	/** Delegates to windowadapter. */
	public void windowClosing(WindowEvent e) {
		windowadapter.windowClosing(e);
	}
	
	/** Delegates to windowadapter. */
	public void windowClosed(WindowEvent e) {
		windowadapter.windowClosed(e);
	}
	
	/** Delegates to windowadapter. */
	public void windowIconified(WindowEvent e) {
		windowadapter.windowIconified(e);
	}
	
	/** Delegates to windowadapter. */
	public void windowDeiconified(WindowEvent e) {
		session.restoreFocus();
		windowadapter.windowDeiconified(e);
	}
	
	
	/** 
	 * Repaints all and restores focus to the panel of buttons. 
	 * 
	 *	@see view.components.DisplayCanvas#setPaintMode(boolean, boolean, boolean) 
	 */
	public void windowActivated(WindowEvent e) {
		session.restoreFocus();
		windowadapter.windowActivated(e);
	}
	/** Delegates to windowadapter. */
	public void windowDeactivated(WindowEvent e) {
		windowadapter.windowDeactivated(e);
	}

	/** Delegates to windowadapter. */
	public void windowStateChanged(WindowEvent e) {	
		session.restoreFocus();
		windowadapter.windowStateChanged(e);
	}
	
	
	/** Delegates to windowadapter. */
	public void windowGainedFocus(WindowEvent e) {
		session.restoreFocus();
		windowadapter.windowGainedFocus(e);
	}

	/** Delegates to windowadapter. */
	public void windowLostFocus(WindowEvent e) {
		windowadapter.windowLostFocus(e);
	}
}
