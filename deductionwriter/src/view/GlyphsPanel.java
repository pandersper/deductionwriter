package view;

import java.awt.Color;
import java.awt.Component;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;

import control.DeductionWriter.CustomKeyboardFocusManager;
import control.Shortcut;
import control.Toolbox;
import model.description.DComposite;
import model.description.DEditableStatement;
import model.description.DPrimitive;
import model.description.abstraction.Described;
import model.independent.DoubleArray;
import model.independent.DoubleArray.Tuple;
import model.logic.Implication;
import model.logic.Implication.ImplicationType;
import model.logic.abstraction.Formal;
import view.abstraction.TraversablePanel;
import view.components.DButton;
import view.components.DisplayCanvas;
import view.components.DButton.DisplayAction;


/**
 * Panel containing the primitives written with. 
 */
public class GlyphsPanel extends TraversablePanel implements KeyListener {

	private HashMap<Formal, DButton> 	buttons  = new HashMap<Formal, DButton>();

	private static final int INSERTPRIMITIVE = 0, NEWSTATEMENT = 1, KEYBOARD = 2;
	
	
	/**
	 * The panel with all the buttons to type math with. It rereceives keyboard focus after something else 
	 * has happened on the canvas or similar. So that it can't catch keyboard key events. <br><br>
	 * 
	 * The buttons themselves fill in the canvas's cursor so this panel simple delegates a lot to them
	 * but still handles when the user accepts a formal and proceeds the theorem (space key) or accepts
	 * the preliminary statement as done (ctrl + arrow key). It also handles focus traversal while also
	 * lets space key click ordinary buttons.
	 * 
	 * @param parentcontainer	The frame containing this primitives panel.
	 * @param display	The canvas this primitives panel uses to draw on.
	 */
	public GlyphsPanel(DeductionFrame parent) {

		this.parent = parent;
		this.panel = this;
		
		this.olddispatcher = new DefaultKeyboardFocusManager();
		this.dispatcher = new CustomDispatcher();

		this.setInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, inputmap);

		this.makeLayout();
	}

	
	
	public void newGlyph() {

		DisplayCanvas canvas = this.getCanvas();
		Described drawn = canvas.getDrawn();
		
		if (drawn == null) return;
		if (drawn.value() instanceof Implication) return;
		
		if (isEditing(drawn)) {

			DEditableStatement edit = canvas.getTheorem().getEditing();

			//Described old = null;

			if (edit.current().isDummy()) 

				if (drawn.isDummy()) {}					// drawn == null
					//old = edit.deleteCurrent();
				else {
					//old = edit.replaceCurrent(drawn);
					drawn.underline(true);				///(46B5)
					edit.togglePrompting();
					
				}
			else {
				//old = edit.replaceCurrent(drawn);
				drawn.underline(true);					// in chosen	
			}

			canvas.redescribeTail(edit.whole());
			canvas.setWritepoint(edit.next());			// update display
			canvas.setPaintMode(false,false,false);
			canvas.paint(canvas.getGraphics());

		} else {

			if (drawn != null)	canvas.newPrimitive();						
														// do nothing if editing or nothing to draw to insert
			canvas.setPaintMode(true,false,false);
			canvas.paint(canvas.getGraphics());
		}
	}

	
	/**
	 * Sets the map of mappings (keyboard key -> key object) and the map of mappings (key object -> action) 
	 * used to push this panels buttons. The key objects are strings. 
	 * 
	 * @param inputmap The inputmap
	 * @param actionmap The actionmap
	 * 
	 * @see InputMap
	 * @see ActionMap
	 */
   	public void setMaps(InputMap inputmap, ActionMap actionmap) {

		this.inputmap  = inputmap;
		this.actionmap = actionmap;

		this.setInputMap(JComponent.WHEN_FOCUSED, inputmap);
	}


   	public void updateButtonsListener(DisplayCanvas canvas) {
   		
   		for (DButton button : buttons.values()) {
   			button.getDisplayAction().putValue("canvas", canvas);   			
   		}
   	}
   	
   	/**
   	 * Update the binding for a particular formal. The formal is unique and is found in a unique button.
   	 * 
   	 * @param binding
   	 */
  	private void updateButtonBinding(Tuple<Formal, Shortcut> binding) {

  		Shortcut shortcut = binding.second();

  		DButton button = this.buttons.get(binding.first());

  		KeyStroke stroke = button.makeKeyStroke(binding.second());

  		String key = "" + (char) (int) shortcut.keycode + " + " + InputEvent.getModifiersExText(shortcut.modifiers);

  		DisplayAction displayaction = (DisplayAction) button.getAction();

  		inputmap.put(stroke, key);
  		actionmap.put(key, displayaction);																								///(GF05)
  	}
  	
  	/**
  	 * Add new-only bindings to the map of bindings used in the application.
  	 * 
  	 * @param bindings	The bindings that should be added if the not already are contained in the mapping.
  	 */
  	public void unionBindings(DoubleArray<Formal, Shortcut> bindings) {
	
		for (Tuple<Formal, Shortcut> pair : this.bindings) {
			
			if (bindings.getByFirst(pair.first()) != null) {
				
				this.bindings.updateByFirst(pair.first(), pair.second());

				this.updateButtonBinding(pair);
				
				bindings.removeByFirst(pair.first());
			}
		}
		
		this.bindings.addAll(bindings);
	}

  	
   	/**
   	 * Constructs and incorporates <b>primitive's buttons</b> and their actions into this panel from the bindings map. Then adds them to the buttons map. 
   	 * The buttons are displayed in <b>the primitives panel</b>.
   	 */
	public void generatePrimitiveButtons() { 	// grow from values
		
		for (Tuple<Formal, Shortcut> binding : bindings) {
			
			Formal inuse = binding.first();

			DButton found = this.buttons.get(inuse);
			
			if ((found == null) && (inuse.getType() != Formal.FormalType.COMPOSITE)) {
			
				DPrimitive fresh = new DPrimitive(inuse);
				
				this.makeButton(fresh, binding.second());
			}
		}
	}

   	/**
   	 * Constructs and incorporates the buttons and their actions for <b>composite's buttons</b>. These buttons are bound to their value in the buttons map.
   	 * The buttons are displayed in <b>the panel for composites</b>. 
   	 * 
   	 * @param composites The bijective sortable map-array containing the described composites and their short-cut key.
   	 */
	public void addCompositesButtons(DoubleArray<Described, Shortcut> composites) {		// grow from descriptions
		
		for (Tuple<Described, Shortcut> binding : composites) 			
			this.makeButton(binding.first(), binding.second());				// don't keep composites doubly, are only in buttons
	}

	
	/**
	 * Retreives a set of formals from the buttons used in this panel.
	 * 
	 * @return A set of all formals used in this panel.
	 */	
	public Set<Formal> 						getFormals() {

		return buttons.keySet();
	}

	/**
	 * Retreives a mapping between described formals and shortcuts from the buttons used in this panel.
	 * 
	 * @return A bijective and sortable mapping between formals and keyboard shortcuts.
	 */
	public DoubleArray<Described, Shortcut> getDescribed() {
		
		DoubleArray<Described, Shortcut> describeds = new DoubleArray<Described, Shortcut>();
		
		for (Formal formal : buttons.keySet()) {
			
			Described described = buttons.get(formal).getDescribed();
			
			Tuple<Formal, Shortcut> binding = bindings.getByFirst(formal);
			
			if (binding != null)
				describeds.add(new Tuple<Described, Shortcut>(described, binding.second()));
			else
				describeds.add(new Tuple<Described, Shortcut>(described, null));	
		}
		
		return describeds;
	}

	/**
	 * Gets the composites use in primitives panel.
	 *
	 * @return The composites used in primitives panel.
	 */
	public Collection<DComposite> 			getComposites() {
		return Toolbox.collectComposites(this.buttons);
	}

	/**
	 * Returns the buttons used in this panel.
	 *
	 * @return the buttons
	 */
	public Collection<DButton> 				getButtons() {
		return buttons.values();
	}

	
   	/*  event related */
	
	/**
	 * The buttons themselves fill in the canvas's cursor so this panel simple delegates a lot to them
	 * but the panel still handles some keyevents:<br>
	 * When the user accepts a formal and proceeds the theorem <i>(space key)</i> or accepts the preliminary statement 
	 * as done <i>(ctrl + arrow key)</i>. It also handles focus traversal while also lets space key click ordinary buttons.
	 * <br><br>
	 * Beware that if you alter this method in the future, it is very delicate when to consume and when to not cunsume the
	 * keyboard key event because the are sent around a lot. Sometimes they must be consumed and sometimes the may not be 
	 * consumed and sometimes it does not matter much.
	 */
	public void keyPressed(KeyEvent e) {

		int modifiers = e.getModifiersEx();
		int codepoint = e.getKeyCode();		

		int gear = isFocusModifier(modifiers) + isStatementModifier(modifiers);

		DisplayCanvas canvas = this.getCanvas();

		switch (gear) {												// ALL HERE IS PROGRAM RESERVED 

			case (INSERTPRIMITIVE): 								// new primitive gear
	
				if (codepoint == KeyEvent.VK_SPACE) {
					
					newGlyph();
					
					e.consume();									// new primitive highest priority, could not be something else, so end chain
					break;
				}
	
			break;
	
			case(NEWSTATEMENT):										// new statement gear
	
				newStatement(codepoint);
	
				e.consume();										// new statement keys are not overloaded so end chain				
				canvas.setPaintMode(false,true,false);
				break;		

			case(KEYBOARD): 										// change focus gear
	
				if (codepoint == KeyEvent.VK_SPACE) {				// space-key in focus gear should still click buttons
	
					if (e.getSource() instanceof JButton) {	
	
						((JButton) e.getSource()).doClick();
	
						this.getParent().requestFocus();
	
						e.consume(); 								// after button click end chain
						return;
					}
				}
	
				changeFocus(codepoint);
				return;
	
			default:				
				System.out.println("not reached"); 
				break;
		}		

		canvas.repaint();
		this.getTheorem().printout();
		return;
	}



	public void newStatement(int codepoint) {

		DisplayCanvas canvas = this.getCanvas();
		
		if (this.getTheorem().getPreliminary().size() > 0) {

			Described last;

			switch (codepoint) {
															// they are also reconstructed in theorem and statement constructors
				case (KeyEvent.VK_LEFT):	

					last = new DPrimitive(Implication.makeValue(ImplicationType.LEFT));	
					canvas.fillCursor(last, true, null);
					canvas.newStatement(last);						
					break;

				case (KeyEvent.VK_UP):				

					last = new DPrimitive(Implication.makeValue(ImplicationType.EQUIV));	
					canvas.fillCursor(last, true, null);
					canvas.newStatement(last);						
					break;

				case (KeyEvent.VK_RIGHT):			

					last = new DPrimitive(Implication.makeValue(ImplicationType.RIGHT));	
					canvas.fillCursor(last, true, null);
					canvas.newStatement(last);						
					break;

				default:
					break;					
			}	
		}
	}

	/**
	 * Not used. Only consumes it's event.
	 */
	public void keyTyped(KeyEvent e) {
		e.consume();
	}

	/**
	 * Not used. Only consumes it's event.
	 */
	public void keyReleased(KeyEvent e) {			
		e.consume();
	}

	
	private boolean isEditing(Described drawn) {
		return (this.parent.getSession().getCurrentCanvas().isPrompting() || this.getTheorem().isEdited()) && drawn != null;
	}

	/**
	 * Checks if a modifier is a focus modifier.
	 *
	 * @param modifiers The mask integer checked. 
	 * @return int Returns <b>2</b> yes <b>two</b> if focus modifier, otherwise 0. Used to distinguish different 
	 * 			   sums representing different combinations of modifiers.
	 */
	private static int isFocusModifier(int modifiers) {

		int onmask  = InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK;
		int offmask = InputEvent.ALT_DOWN_MASK;

		boolean focusmodifier =  ((modifiers & (onmask | offmask)) == onmask);

		return focusmodifier ? 2 : 0;
	}

	/**
	 * Checks if a modifier is a statement modifier.
	 *
	 * @param modifiers The mask integer checked. 
	 * @return int Returns <b>1</b> yes <b>one</b> if statement modifier, otherwise 0. Used to distinguish different 
	 * 			   sums representing different combinations of modifiers.
	 */
	private static int isStatementModifier(int modifiers) {

		int onmask  = InputEvent.CTRL_DOWN_MASK;
		int offmask = InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK;

		boolean statementmodifier =  ((modifiers & (onmask | offmask)) == onmask);
		return statementmodifier ? 1 : 0;
	}

	
	/*  focus traversal and windows focus */
	 
	/**
	 * Restores this components initial focus state so that it again receives keyboard key events.
	 */	
	public void restoreFocus() {
		manager.upFocusCycle(this);
	}

 	private void changeFocus(int keycode) {

		switch (keycode) {

			case (KeyEvent.VK_UP): 	 	manager.upFocusCycle(); break;
			case (KeyEvent.VK_DOWN): 	manager.downFocusCycle(); break;
			case (KeyEvent.VK_RIGHT): 	manager.focusNextComponent(); break;
			case (KeyEvent.VK_LEFT): 	manager.focusPreviousComponent(); break;
	
			default: break;
		}
	}
	
 	/** {@inheritDoc} */
	public void setFocusTraversal(CustomKeyboardFocusManager manager) {

		this.manager = manager;		

		this.setFocusable(true);
		this.setFocusCycleRoot(true);

		defaultfocus = this.getParent();	
	}
	
 	/** {@inheritDoc} */
	public Component[][] focusCycleNodes() {
		return new Component[][] { new Component[] { this.getParent() } }; 
	}

 	/** {@inheritDoc} */
	public void windowGainedFocus(WindowEvent e) {

		if (e.getSource() == parent) {

			manager.removeKeyEventDispatcher(olddispatcher);							
			manager.addKeyEventDispatcher(dispatcher);								

			parent.requestFocusInWindow();																									///(6FA2)

		} else 
			manager.removeKeyEventDispatcher(dispatcher);	

	}

 	/** {@inheritDoc} */
	public void windowLostFocus(WindowEvent e) {

		if (e.getSource() == parent) {

			manager.removeKeyEventDispatcher(dispatcher);	
			manager.addKeyEventDispatcher(olddispatcher);						
		} 
	}

	
	/*  awt & swing */
	
	/**
	 * Makes a new button for the given described formal and associates the binding to it.
	 * @param described	The described formal that this button represents.
	 * @param binding	The keyboard shortcut binding that types this button. 
	 */
	public void makeButton(Described described, Shortcut binding) {

		DButton button = new DButton(described);    			

		DisplayAction displayaction = (DisplayAction) button.getAction();
		displayaction.setCanvas(this.parent.getSession().getCurrentCanvas());
		displayaction.setFocusrestore(this);

		if (binding != null) {

			KeyStroke stroke = button.makeKeyStroke(binding);

			String key = "" + (char) (int) binding.keycode + " + " + InputEvent.getModifiersExText(binding.modifiers);

			actionmap.put(key, displayaction);																								///(0CFC)
			inputmap.put(stroke, key);
		}
		
		button.addKeyListener(this);

		if (described.getType() == Formal.FormalType.COMPOSITE) 
			pnlComposites.add(button);
		else
			pnlPrimitives.add(button);
	
		this.buttons.put(described.value(), button);
	}

	
	private void makeLayout() {

		setSize(new Dimension(327, 438));
		setLayout(new GridLayout(0, 1, 0, 0));
		
		JDesktopPane dpnGlyphs = new JDesktopPane();
		
		dpnGlyphs.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));

		this.add(dpnGlyphs);

		JInternalFrame ifrmPrimitives = new JInternalFrame("Primitives");
		JInternalFrame ifrmComposites = new JInternalFrame("Composites");

		ifrmPrimitives.getContentPane().setLayout(null);
		ifrmComposites.getContentPane().setLayout(null);
		
		ifrmPrimitives.getContentPane().add(pnlPrimitives);
		ifrmComposites.getContentPane().add(pnlComposites);

		ifrmPrimitives.addComponentListener(pnlResizer(pnlPrimitives));		
		ifrmComposites.addComponentListener(pnlResizer(pnlComposites));				

		ifrmPrimitives.setResizable(true);		ifrmPrimitives.setIconifiable(true);	ifrmPrimitives.setMaximizable(true);
		ifrmComposites.setResizable(true);		ifrmComposites.setIconifiable(true);	ifrmComposites.setMaximizable(true);

		dpnGlyphs.add(ifrmPrimitives);		
		dpnGlyphs.add(ifrmComposites);
	
		ifrmComposites.setBackground(new Color(143, 188, 143));
		ifrmPrimitives.setBackground(new Color(143, 188, 143));
		
		ifrmPrimitives.setBounds(12, 12, 149, 230);
		ifrmComposites.setBounds(12, 297, 217, 86);
		
		ifrmComposites.setVisible(true);
		ifrmPrimitives.setVisible(true);

	}

	private ComponentAdapter pnlResizer(JPanel pnl) {
		
		return (new ComponentAdapter() {			
			public void componentResized(ComponentEvent e) {
				Dimension d = e.getComponent().getSize();
				pnl.setBounds(0,0,d.width,d.height);				
			}
		});
	}

	private ActionMap 	actionmap; 
	private InputMap 	inputmap;

	private JPanel pnlPrimitives = new JPanel();
	private JPanel pnlComposites = new JPanel();

}
