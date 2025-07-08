package annexes.trainer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.Thread.State;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.KeyStroke;
import annexes.trainer.BindingsViewDialog.BindingContainer;
import control.session.Session;
import control.session.Shortcut;
import control.statics.DebugStatics;
import control.statics.Toolbox;
import control.statics.ViewStatics;
import model.description.abstraction.Described;
import model.independent.DoubleArray;
import model.independent.DoubleArray.Tuple;
import model.logic.abstraction.Formal;
import view.DeductionFrame;
import view.abstraction.AbstractFrame;

/** 
 * A DeductionWriter sub application for connecting keyboard keys to primitives and practice using them.	 
 */
public class DeductionTrainer implements Runnable, KeyListener, ActionListener {

	private static final int DELAY 		= 140 ;

	private TrainerFrame 		frame;
	private BindingsViewDialog 	mapdialog;
	private DeductionFrame 		mainframe;
	
	private Session 			session;	
		
	private Described  	displayed;
	private KeyStroke 	stroke;

	private boolean 		 catching;
	private BindingContainer catched;
	
	private Thread 	timer;
	private boolean stopped = true;
	private boolean halting = false;
	
/**
 *  
 * A DeductionWriter sub application for connecting keyboard keys to primitives and practice using them.
 * 
 * @param session TODO
 * @param base The base of theorems and their constituents.
	 */
 	public DeductionTrainer(Session session) {
		
 		this.session = session;
 		
		mapdialog	= new BindingsViewDialog(this);

		frame = new TrainerFrame(mapdialog);

		frame.addListener(this);
		frame.addKeyListener(this);
	}
 	
	/** {@inheritDoc} */
	public void keyTyped(KeyEvent e) {
		
		DebugStatics.output(true, "", dumpKeyEvent(e), "");
		
		if (!catching && !stopped && displayed != null) {
						
			Described primitive = displayed;
			
			DoubleArray<Described, Shortcut> 	bijection 	= mapdialog.getBindings();
			Tuple<Described, Shortcut> 			binding 	= bijection.getByFirst(primitive);
									
			Shortcut newshortcut = new Shortcut(stroke);				
			Shortcut oldshortcut = (binding != null) ? binding.second() : null;
			
			Tuple<Described, Shortcut> newbinding = new Tuple<Described, Shortcut>(displayed, newshortcut);
			
			if (oldshortcut == null) {														// new primitive

				Tuple<Described, Shortcut> remove = bijection.getBySecond(newshortcut);
				
				if (remove == null) 														// both primitive and key is new					
					bijection.add(newbinding);					
				else 																		// must drop stroke's old primitive	
					bijection.remove(remove);							
				
			} else {																		// primitive is bound, update primitive's binding and remove old stroke's primitive, if any.
				
				bijection.updateByFirst(primitive, newshortcut);
				
				Tuple<Described, Shortcut> remove = bijection.getBySecond(newshortcut);		// try remove
				
				if (remove != null) 														// old stroke existed elsewhere, drop its primitive 
					bijection.remove(remove);							
			}																				// input done 

			frame.setArrayAndInfo();
		}		
		e.consume();
	}
	
	/** {@inheritDoc} */
	public void keyPressed(KeyEvent e) {

		DebugStatics.output(true, "", dumpKeyEvent(e), "");
		
		stroke = KeyStroke.getKeyStrokeForEvent(e);
		
		if (catching) 
			catched.shortcut = new Shortcut(stroke.getKeyChar(), stroke.getModifiers());
	}

	/** {@inheritDoc} */
	public void keyReleased(KeyEvent e) {

		DebugStatics.output(true, "", dumpKeyEvent(e), "");

		e.consume();
		
		stroke = null;
		
		if (catching) {

			catching = false;

			catched.updateFields();
			
			catched = null;

			mapdialog.setBackground(null);
			mapdialog.revalidate();
			mapdialog.repaint();	// ghost graphics artifacts may be left
		}
	}


	public void startTimer() {
		
		timer = new Thread(this);
		
		timer.setDaemon(true);
		timer.setName("DeductionWriter timer");
		timer.start();
	}
	
	/**
	 * The driving loop of the timer task
	 * 
	 * Excessive thread think fo learning purose. Remove soon and do some other application.
	 */
	public void run() {

		boolean running = true, emergency = false, returning = false, slept = false;

		try { // run
			while (running) {
				if (!stopped) 										// do work
					if (frame.periodEnded())
						displayed = frame.displayNext();
				try { 												// do waiting
					timer.sleep(DELAY); 							// have to call by instance for ownership
					slept = true;
				} catch (InterruptedException ie) {
					System.err.println("Interrupted from waiting: " + ie.getMessage() + ". Have slept:" + slept + " Trying again");
					
					if (halting) break;									// interrupted for halting so stop running
					else
						continue;										// interrupted with other purpose - ok go on though unchecked sleep
				} finally {
					if (!slept) {
						running = false;							// dont go on
						System.err.println("Finally. Something is wrong. Trying to exit. Slept: " + slept);
						emergency = true;							// bad has happened
					} else {
						slept = false;								// new cycle
						continue;									// ok go on as well - have slept
					}
				};
				if (emergency)										// do no more
					break;
				// something could be done				
			}
		} catch (Exception outer) {
			System.err.println("Exiting thread (run) in: " + this.toString() + " thread: " + Thread.currentThread().getName() + " error: " + outer.getMessage());
			if (emergency) {
																	// handle bad and/or exit
				System.err.println("Emergency call.");
			} else {
																	// still running (probably not) - could running be restored
				if (running) { 
					if (slept) { 
																	// parachuted out in some miraculous way
					} else { 
																	// not slept, must compensate if timing is critical, but no more cycles
					} 				
				} else {
																	// should have returned - is perhaps late.
				}
			}										
			System.err.println("Running: " + running + ". Bad has happened: " + emergency);
		} finally {
			returning = true;
			System.err.println("Exiting to elder process with unknown exception. Emergency: " + emergency);
			if (timer.getState() == State.RUNNABLE) 
				System.err.println("Runnable, so seems ok. Probably manually shut down (interrupted). Halting: " + halting);
			else 
				System.err.println("Not runnable, so something happened without intention (os interaction or java failure).");				
		};
		Thread.onSpinWait();										// tell os i might be spinning
		System.err.println("On spin wait return.");
		while (returning)											// perhaps not all of outer catch was done
			return;													// force controlled return
		System.err.println("Uncontrolled return.");					// could not possiby be reached
	}

	public void halt() {
		
		halting = true;
		
		while (timer.isAlive()) {

			State state = timer.getState();
			
			timer.interrupt();
			
			state = timer.getState();
			
			while (state != State.TERMINATED) {
				
				if (state == State.TIMED_WAITING) {
				
					state = timer.getState();
					
					timer.yield();
				
				} else {
					
					state = timer.getState();

					if (state == State.WAITING) {
											
						try {
							
							timer.join();
						
						} catch (InterruptedException e) {
						
							e.printStackTrace();
						}
					}					
				}
				
				state = timer.getState();
				
				if (state == State.BLOCKED)
					while (Thread.activeCount() > 1)
						timer.interrupt();
			}		
		}
	}
	
	/**
	 * The main hub of functionality executed by the buttons clicked in the application.
	 */
  	public void actionPerformed(ActionEvent e) {
  		
  		JButton 			button;
  		BindingContainer 	container;
  		 		
  		switch (e.getActionCommand()) {

			case "start-stop":				

				stopped = !stopped;	
								
				break;
				
			case "store":
				
				//rewindQueue();
				mapdialog.open(frame);				
				
				break;

			case "reset":
				
				stopped = true;		

				DoubleArray<Described, Shortcut> bindings = Toolbox.describe(mainframe.getGlyphsPanel().getBindings());

				this.setPrimitives(bindings);
				
				break;

			case "clear":

				stopped = true;	
				
				mapdialog.clearShortcuts();

				break;

			case "done":	

				storeBindings(); ViewStatics.switchContainer(mainframe, frame, null, null); break;
			
			case "insert-bindings":

				storeBindings(); ViewStatics.switchContainer(mapdialog, frame); break;

			case "return-to-writer":

				storeBindings(); ViewStatics.switchContainer(mainframe, mapdialog); break;
	
			case "return-to-trainer":	
				
				storeBindings(); ViewStatics.switchContainer(frame, mapdialog); break;

			case "binding": 														
				
				container = (BindingContainer) e.getSource();

				mapdialog.getBindings().updateByFirst(container.formal, container.shortcut);
				
				break;
				
			case "delete":
				
				button = (JButton) e.getSource();
				
				container = (BindingContainer) button.getAction().getValue("container");
				
				mapdialog.removeBinding(container);
				mapdialog.revalidate();
				mapdialog.repaint();									
				
				break;

			case "catch":
				
				button = (JButton) e.getSource();
	
				catched = (BindingContainer) button.getAction().getValue("container");
				
				catching = true;

				mapdialog.setCatching();
				
				catched.requestFocus();
				catched.requestFocusInWindow();

				mainframe.setVisible(true);
				mainframe.transferFocus();
				
				break;
			
			default:
				
				System.out.println("Strange error in default.");
				
				break;
		}
  		
	}

  	
	private void rewindQueue() {
		this.setPrimitives(mapdialog.getBindings());
	}

	private void storeBindings() {
  		
		DoubleArray<Formal, Shortcut> bindings = Toolbox.formals(mapdialog.getBindings());

  		int inserted = session.getBase().insert(bindings, session.primitivestable, session.compositestable);					
		  		
  		mainframe.getGlyphsPanel().unionBindings(bindings);
	}
	/** 
	 * Loads currently used primitives table into this trainer.  
	 */
 	public void loadPrimitives() {
		
		DoubleArray<Described, Shortcut> mapping = mainframe.getGlyphsPanel().getDescribed();
						
		mapdialog.updateBindings(mapping);
		
		frame.setArrayAndInfo();				
	} 
 	
	/**
	 *  
	 * Set a particular set of primitives and their bindings to be used in this trainer.
	 *
	 * @param bindings A mapping of described primitives to their corresponding binding. 
	 */
	public void setPrimitives(DoubleArray<Described, Shortcut> bindings) {
		
		mapdialog.resetBindings(bindings);
		
		ArrayList<Described> display = new ArrayList<Described>();
		
		for (Tuple<Described,Shortcut> binding : bindings) display.add(binding.first());
		
		frame.getKeyqueue().clear();
		frame.getKeyqueue().addAll(display);
		
		frame.setArrayAndInfo();
	}
	/**
	 * Sets the main sibling application frame for hading over control.
	 * @param mainframe The DeductionWriter frame.
	 */
	public void setMainFrame(DeductionFrame mainframe) {
		this.mainframe = mainframe;
	}
	/**
	 * Returns the outer frame of this sub application.
	 *
	 * @return the frame
	 */
	public AbstractFrame getFrame() { 
		return frame; 
	}
	
	
	private static String dumpKeyEvent(KeyEvent e) {	

		KeyStroke stroke = KeyStroke.getKeyStrokeForEvent(e);

		char 		bindingchar 	 = stroke.getKeyChar();
		int 		bindingcode 	 = stroke.getKeyCode();
		int 		bindingmodifiers  = stroke.getModifiers();

		String output = "";
		
		output += "stroke modifiers: " + InputEvent.getModifiersExText(bindingmodifiers); 
		output += " event: " + InputEvent.getModifiersExText(e.getModifiersEx());
		
		output += "KeyEvent:" + e.getKeyChar() + " : " + e.getKeyCode() + " : ";
		output += e.getExtendedKeyCode() + " : " + e.getKeyLocation()+ " : " + e.getModifiersEx();
		
		output += "KeyStroke:" + bindingchar + " : " + bindingcode + " : " + stroke.getKeyEventType();
		output += " : " + bindingmodifiers;

		return output;
	}
}

