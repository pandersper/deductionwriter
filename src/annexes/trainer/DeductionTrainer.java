package annexes.trainer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;

import javax.swing.JButton;
import javax.swing.KeyStroke;

import annexes.trainer.BindingsViewDialog.BindingContainer;
import control.Shortcut;
import control.Toolbox;
import control.db.DeductionBase;
import model.description.abstraction.Described;
import model.independent.DoubleArray;
import model.independent.DoubleArray.Tuple;
import model.logic.abstraction.Formal;
import view.DeductionFrame;
import view.abstraction.AbstractFrame;

/** 
 * A DeductionWriter sub application for connecting keyboard keys to primitives and practice using them.	 
 */
public class DeductionTrainer extends TimerTask implements KeyListener, ActionListener {

	
	private static final int INTERVAL	= 80;																								// ms
	private static final int DELAY 		= 20;																								

	private TrainerFrame 		frame;
	private BindingsViewDialog 	mapdialog;
	private DeductionFrame 		mainframe;
	
	private DeductionBase 		base;	
	private Timer 				timer = new Timer();
	
	private ArrayBlockingQueue<Described> 	keyqueue = new ArrayBlockingQueue<Described>(300);

	private boolean 	stopped = true;
	
	private Described  	first, displayed;

	private KeyStroke 	stroke;

	private boolean catching;
	private BindingContainer catched;
	
	/**
	 *  
	 * A DeductionWriter sub application for connecting keyboard keys to primitives and practice using them.
	 *
	 * @param base The base of theorems and their constituents.
	 */
 	public DeductionTrainer(DeductionBase base) {
		super();

		this.base = base;
		
		frame = new TrainerFrame();
		frame.addListener(this);
		frame.addKeyListener(this);
		
		mapdialog	= new BindingsViewDialog(this);
			
		timer.schedule(this, DELAY, INTERVAL);				
	}

 	
	private void rewindQueue() {
		
		while (keyqueue.peek() != first)	
			keyqueue.offer(keyqueue.poll());
	
	}
	
	private void updateQueue() {
				
		DoubleArray<Described, Shortcut> bindings = mapdialog.getBindings();
		
		keyqueue.clear();
		
		this.first = bindings.get(0).first();
		this.displayed = this.first;
		
		bindings.sortByFirst();
		
		for (Tuple<Described, Shortcut> pair: bindings) {			
			keyqueue.offer(pair.first());
		}						
	}

	private void updateInfo() {

		frame.setInfo(mapdialog.occupiedString());
		frame.revalidate();
	}

	private void storeBindings() {
  		
		DoubleArray<Formal, Shortcut> bindings = Toolbox.formals(mapdialog.getBindings());

  		int inserted = base.insertBindings(bindings, mainframe.currentTheorem());					
		  		
  		mainframe.getPrimitivesPanel().unionBindings(bindings);
	}

	/** 
	 * Loads currently used primitives table into this trainer.  
	 */
 	public void loadPrimitives() {
		
		DoubleArray<Described, Shortcut> mapping = mainframe.getPrimitivesPanel().getDescribed();
						
		mapdialog.updateBindings(mapping);
		
		this.updateQueue();
		frame.changeCodepoint(mapping.firstElement().second(), mapdialog.occupiedString());
		frame.setDisplayed(keyqueue.peek());
	} 
 	
	/**
	 *  
	 * Set a particular set of primitives and their bindings to be used in this trainer.
	 *
	 * @param bindings A mapping of described primitives to their corresponding binding. 
	 */
	public void setPrimitives(DoubleArray<Described, Shortcut> bindings) {
		
		mapdialog.resetBindings(bindings);
		
		this.updateQueue();
		frame.changeCodepoint(bindings.firstElement().second(), mapdialog.occupiedString());
		frame.setDisplayed(keyqueue.peek());
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

	
	/**
	 * The driving loop of the timer task.
	 */
	public void run() {
		
		if (!stopped) {

			if (frame.countdown()) {
			
				displayed = keyqueue.poll();		

				keyqueue.offer(displayed);

				frame.setDisplayed(displayed);				
				frame.repaint();				
				frame.requestFocusInWindow();
			}
		}
	}
	
	
	/**
	 * The main hub of functionality executed by the buttons clicked in the application.
	 */
  	public void actionPerformed(ActionEvent e) {
  		
  		JButton button;
  		BindingContainer container;
  		
  		Tuple<Described, Shortcut> binding;
  		
  		switch (e.getActionCommand()) {

			case "start-stop":				
	
				stopped = !stopped;				
				
				break;
				
			case "store":
				
				rewindQueue();
				mapdialog.open(frame);				
				
				break;

			case "reset":
				
				stopped = true;		

				DoubleArray<Described, Shortcut> described = Toolbox.describe(mainframe.getPrimitivesPanel().getBindings());

				mapdialog.resetBindings(described);

				updateInfo();
				rewindQueue();				
				frame.reset();
				
				break;

			case "clear":

				stopped = true;	
				
				mapdialog.clearShortcuts();

				updateInfo();
				updateQueue();
				frame.reset();
				
				break;

			case "done":	

				storeBindings(); Toolbox.switchContainer(mainframe, frame); break;
			
			case "insert-bindings":

				storeBindings(); rewindQueue(); updateInfo(); Toolbox.switchContainer(mapdialog, frame); break;

			case "return-to-writer":

				storeBindings(); rewindQueue(); Toolbox.switchContainer(mainframe, mapdialog); break;
	
			case "return-to-trainer":	
				
				storeBindings(); rewindQueue();	updateInfo(); Toolbox.switchContainer(frame, mapdialog); break;

			case "binding": 														
				
				container = (BindingContainer) e.getSource();

				mapdialog.getBindings().updateByFirst(container.formal, container.shortcut);
				
				break;
				
			case "delete":
				
				button = (JButton) e.getSource();
				
				container = (BindingContainer) button.getAction().getValue("container");
				
				binding = mapdialog.removeBinding(container);
				
				mapdialog.revalidate();
				mapdialog.repaint();													// ghost graphics artifacts may be left
				
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

  	
	/** {@inheritDoc} */
	public void keyTyped(KeyEvent e) {
		
		if (Toolbox.DEBUGVERBOSE) dumpKeyEvent(e);
		
		if (!catching && !stopped && displayed != null) {
						
			Described 	primitive 	 = displayed;
			
			DoubleArray<Described, Shortcut> bijection = mapdialog.getBindings();

			Tuple<Described, Shortcut> binding = bijection.getByFirst(primitive);
			
			Shortcut newbinding = new Shortcut(stroke);			
			Shortcut oldbinding = (binding != null) ? binding.second() : null;
			
			if (oldbinding == null) {														// new primitive

				Tuple<Described, Shortcut> remove = bijection.getBySecond(newbinding);
				
				if (remove == null) {														// both primitive and key is new
					
					bijection.add(new Tuple<Described, Shortcut>(displayed, newbinding));					
				} else { 																	// must drop stroke's old primitive
					
					bijection.remove(remove);							
				}
				
			} else {																		// primitive is bound, update primitive's binding and remove old stroke's primitive, if any.
				
				bijection.updateByFirst(primitive, newbinding);
				
				Tuple<Described, Shortcut> remove = bijection.getBySecond(newbinding);		// try remove
				
				if (remove != null) {														// old stroke existed elsewhere, drop its primitive 
					
					bijection.remove(remove);							
				}
			}																				// input done 

			frame.changeCodepoint(newbinding, mapdialog.occupiedString());
		}
				
		e.consume();
	}
	
	/** {@inheritDoc} */
	public void keyPressed(KeyEvent e) {

		if (Toolbox.DEBUGVERBOSE) dumpKeyEvent(e);
		
		stroke = KeyStroke.getKeyStrokeForEvent(e);
		
		if (catching) {
			catched.shortcut = new Shortcut(stroke.getKeyChar(), stroke.getModifiers());
		}
	}

	/** {@inheritDoc} */
	public void keyReleased(KeyEvent e) {

		if (Toolbox.DEBUGVERBOSE) dumpKeyEvent(e);

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

	
	private static void dumpKeyEvent(KeyEvent e) {	

		KeyStroke stroke = KeyStroke.getKeyStrokeForEvent(e);

		char 		bindingchar 	 = stroke.getKeyChar();
		int 		bindingcode 	 = stroke.getKeyCode();
		int 		bindingmodifiers  = stroke.getModifiers();

		System.out.println("stroke modifiers: " + InputEvent.getModifiersExText(bindingmodifiers) + " event: " + InputEvent.getModifiersExText(e.getModifiersEx()));
		System.out.println("KeyEvent:" + e.getKeyChar() + " : " + e.getKeyCode() + " : " + e.getExtendedKeyCode() + " : " + e.getKeyLocation()+ " : " + e.getModifiersEx());
		System.out.println("KeyStroke:" + bindingchar + " : " + bindingcode + " : " + stroke.getKeyEventType()+ " : " + bindingmodifiers);
	}
	
}
