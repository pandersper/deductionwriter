package annexes.maker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import control.Toolbox;
import model.description.DComposite;
import model.description.DPrimitive;
import model.description.DRectangle;
import model.description.abstraction.Described;
import model.description.abstraction.Placeholder;
import model.independent.CyclicList;
import view.DeductionFrame;
import view.components.DButton;

/**
 * CompositePanel is the intermediary between the canvas and the topmost component, the frame.
 * It handles mouse events and puts together and exports the end product, a described composite, amongst other things.
 * 
 * When complete and rendered the described composite is exported via {@link #requestComposite()}.
 * 
 * The first component of the composite is decided to be the bounding frame of it and has to be given at start
 * via {@link #setupComposite(DComposite)}. 
 */
public class CompositePanel extends JPanel implements MouseListener, MouseMotionListener, ActionListener {


	private CyclicList<Placeholder> constituents = new CyclicList<Placeholder>();

	private Point 				first = null, second = null;
	private Point 				dragged;
	private Placeholder			current;
	private Placeholder			frameholder;
	private Rectangle 			bounds;
	private Ellipse2D.Double 	handle;

	private Area 		interacting = new Area();

	private boolean 	editing = false;

	
	/**
	 * Instantiates a new composite panel.
	 *
	 * @param parent The parent frame to return control to.
	 */
	public CompositePanel(DeductionFrame parent) {

		setLayout(new BorderLayout(10, 10));

		this.canvas = new CompositeCanvas(this);	

		this.canvas.addMouseListener(this);
		this.canvas.addMouseMotionListener(this);

		makeNorthPane();	makeEastPane();
		makeSouthPane();	makeWestPane();
		makeCenterPane();

		add(pnlNorth, BorderLayout.NORTH);		add(pnlEast, BorderLayout.EAST);		
		add(pnlSouth, BorderLayout.SOUTH);		add(pnlWest, BorderLayout.WEST);
		add(canvas, BorderLayout.CENTER);

		pnlNorth.setBackground(Color.gray);		pnlEast.setBackground(Color.blue);
		pnlSouth.setBackground(Color.green);	pnlWest.setBackground(Color.lightGray);

		this.setDoubleBuffered(true);
	}

	
	/**
	 * Sets the bounding backdrop of the current composite. It is often only a blank dummy rectangle
	 * but can be also some glyph giving this composite a structure.
	 * 
	 * @param primitive The described primitive.
	 * 
	 * @see CompositeCanvas#setCurrent(Placeholder)
	 */
	public void setBoundingCursor(Described primitive) {

		Rectangle frame = primitive.description().getBounds();
		
		canvas.setOrigo(frame);

		frameholder = new Placeholder(frame);		// is filled by canvas
													// anchored at origo for now	
		canvas.setCurrent(frameholder);
		canvas.fillCursor(primitive, true, null);
		
		constituents.addFirst(frameholder);

		this.makeInteractingArea();		
	}

	/**
	 * Returns the bounding back drop primitive for this composite. 
	 *
	 * @return The bounding primitive, together with it's placeholder.
	 */
	public Placeholder getBoundingCursor() {
		return frameholder;
	}

	/**
	 * Sets up a given described composite for further editing of it. 
	 * 
	 * @param composite	The described composite to continue editing.
	 */
	public void setupComposite(DComposite composite) {

		constituents.clear();
						
		Placeholder frameholder = composite.getFrame();
		
		this.setBoundingCursor(frameholder.described());			// first is bounding
		
		for (Placeholder oldholder : composite.getConstituents()) {
			
			if (oldholder == frameholder) continue;
			
			Described constituent = oldholder.described();
			
			Placeholder newholder = this.addAndFillCursor(constituent.description(), constituent);
			
			newholder.setFrame(oldholder.bounds());
			
			this.updateButtons(new DButton(constituent));
		}
	}
	
	/**
	 * Puts together a new composite out of the current state of this sub application, from the sub components and their layout.
	 *
	 * @return The designed described composite formal.
	 */
	public DComposite requestComposite() {

		DComposite done = null;

		if (constituents.size() > 0) 
			done = new DComposite(constituents, Toolbox.nextCompositeId()); 		// decision: subprimitives should be all, we are done with canvas

		return done;
	}

	/**
	 * Returns the sub components that this composite consists of currently.
	 *
	 * @return The sub primitives of this composite.
	 */
	public CyclicList<Placeholder> getConstituents() { 
		return constituents; 
	}

	
	/**
	 * Iterates the selected component to the next one.
	 */
	public void forward() {		
			canvas.setCurrent(constituents.next());
	}
	
	/**
	 * Adds a new placeholder to the edited composite, to be filled with a formal later. 
	 * 
	 * @param cursor	The bounding rectangle of the new placeholders's frame.
	 * 
	 * @return 			The new placeholder.
	 */
 	public Placeholder addCursor(Rectangle cursor) {
 						
		Placeholder placeholder = new Placeholder(cursor);

		constituents.add(placeholder);
		
		canvas.setCurrent(placeholder);

		this.makeInteractingArea();
		
		return placeholder;
 	}

 	/**
 	 * Adds a new placeholder and also fills it with a new described formal. 
 	 * 
 	 * @param cursor	The bounding rectangle of the new placeholders's frame.
 	 * @param formal	A new formal to fill the new placeholder with.
 	 * 
 	 * @return			The new filled placeholder.
 	 */
 	public Placeholder addAndFillCursor(Rectangle cursor, Described formal) {
 				
 		Placeholder placeholder = addCursor(cursor);
 		
 		placeholder.fill(formal);
 		
 		return placeholder;
 	}
 	
	/**
	 * Deletes current sub component and it's place holder.
	 */
	public void deleteCurrent() {

		Placeholder current = canvas.getCurrent();

		if (current != constituents.get(0)) {

			constituents.remove(current);	
			updateButtons(null);

			current = constituents.get(0);
			canvas.setCurrent(current);

			correctTextFields();		
		}
	}

	/**
	 * Clears the workpiece, the described composite and resets this panel.
	 */
	public void clearAll() {

		this.constituents.clear();

		makeInteractingArea();
		updateButtons(null);

	}

	
	/**
	 * Trims a glyph bounds to fit it tightly.
	 */
	public void boundsToGlyph() {

		canvas.getCurrent().trimBounds();

		correctTextFields();			

		canvas.repaint();
	}  
	
	/**
	 * Scale glyph so that it's baseline fits onto it's bounding rectangle's baseline.
	 * 
	 * CHECK IMPLEMENTATION!
	 */
	public void glyphToBounds() {

		Placeholder current = canvas.getCurrent();

		if (current != this.frameholder) {

			Rectangle newbounds = parseTextFields();			

			current.setFrame(newbounds);

			int codepoint 	= current.described().value().getCodepoint();
			int baseline 	= CompositeCanvas.computeBaseline(current.bounds(), codepoint);

			Described adjusted = new DPrimitive(codepoint, baseline);

			current.fill(adjusted); 
			
			canvas.repaint();
		}		
	}
		
	/**
	 * Sees to it that the panel of primitives correspond to the collection of primitives used 
	 * in the application, consists of the same primitives. First adds the button given as parameter if
	 * not null.
	 * 
	 * @param button	The button to add.
	 */
	public void updateButtons(DButton button) {

		if (button != null) {
			button.setActionCommand("button");
			button.addActionListener(this);
			pnlButtons.add(button);
		}

		LinkedList<Component> toremove = new LinkedList<Component>();

		for (Component component : pnlButtons.getComponents()) 
			if (component instanceof DButton) 
				if (findComponent(((DButton) component).getDescribed(), constituents) == null) 
					toremove.add(component);

		for (Component c : toremove) 
			pnlButtons.remove(c);

		pnlButtons.revalidate(); pnlButtons.repaint();
	}
	
	/**
	 * Toggle editing mode.
	 */
	public void toggleEditing() {

		if (!editing) {

			txfLowerLeftX.setText("" + 0);  	txfLowerLeftY.setText("" + 0);
			txfUpperRightX.setText("" + 0); 	txfUpperRightY.setText("" + 0);

			canvas.setBackground(Color.pink);

		} else 
			canvas.setBackground(new Color(228, 222, 160));

		editing = !editing;

		first = null; second = null;
	}

	
	/**
	 * Only receives and acts on events from buttons in the east panel of sub primitives.
	 * 
	 * @param e Only events originating from buttons is handled.
	 */	
	public void actionPerformed(ActionEvent e) {

		switch (e.getActionCommand()) {

		case "button":

			Described source = ((DButton) e.getSource()).getDescribed();

			Placeholder chosen = findComponent(source, constituents);

			canvas.setCurrent(chosen);					

			this.makeInteractingArea();		
			this.correctTextFields();

			break;

		default:
			break;		
		}
	}

	
	/**
	 * Registers first and second point clicked on and sets the sub components movable-variable.
	 * 
	 * @param e	The mouse button pressed event.
	 */
	public void mousePressed(MouseEvent e) {

		Point local = e.getPoint();

		local.translate(-canvas.origo.x, -canvas.origo.y);					// translate to local frame

		if (editing) {

			if (first != null) {

				second = local;

				txfUpperRightX.setText(second.x + ""); 
				txfUpperRightY.setText(second.y + "");

				boolean ok = tryMakeSubRectangle(first, second);

				blink(ok);

				correctTextFields();

				toggleEditing();

			} else {

				first = local;

				txfLowerLeftX.setText(first.x + ""); 
				txfLowerLeftY.setText(first.y + "");
			}

		} else {

			int currentindex = getHandleIndex(local);

			if (interacting.contains(local) && currentindex != -1) {

				canvas.moving = (currentindex == 0);
				canvas.movingpart = (currentindex > 0);

				current = constituents.get(currentindex);
				
				canvas.setCurrent(current);

				correctTextFields();
			}
		}

		e.consume();
	}
	
	/**
	 * Releases movability and updates gui information.
	 * 
	 * @param e The mouse button released event.
	 */
	public void mouseReleased(MouseEvent e) {

		canvas.moving = false;
		canvas.movingpart = false;

		this.correctTextFields();
		this.makeInteractingArea();

		canvas.repaint();

		e.consume();
	}
	
	/**
	 * Handles dragging, translation, while buttons is held down. Either the whole work piece or a sub component is translated.
	 * 
	 * @param e Mouse dragged event. How and at what pace they are genereated can probably be read in {@see MouseMotionListener}.
	 */
	public void mouseDragged(MouseEvent e) {

		if (canvas.moving) {

			dragged = e.getPoint();
			dragged.translate(-canvas.origo.x, -canvas.origo.y);		

			bounds = frameholder.bounds();
			handle = frameholder.handle();
			
			dragged.translate((int)-handle.x-canvas.DR, (int)-handle.y-canvas.DR);
			
			canvas.origo.translate(dragged.x, dragged.y);

			canvas.repaint();		

		} else {

			if (canvas.movingpart) {				

				bounds = current.bounds();
				
				dragged = e.getPoint();
				dragged.translate(-canvas.origo.x, -canvas.origo.y);		
				dragged.translate(-bounds.x, -bounds.y);

				current.translate(dragged);

				this.makeInteractingArea();

				canvas.repaint();		
			}
		}
	}


	private boolean tryMakeSubRectangle(Point first, Point second) {	
		// |relative canvas origo| - devices coordinates
		Rectangle bounds = frameholder.bounds();						

		if (bounds.contains(first) && bounds.contains(second)) {			

			bounds.grow(1,1);

			int w = Math.abs(second.x - first.x);
			int h = Math.abs(second.y - first.y);

			Rectangle subrectangle = new Rectangle(w, h);

			if (first.x < second.x) 									// determines mouse click order
				if (first.y < second.y)
					subrectangle.setLocation(first.x, first.y);
				else 
					subrectangle.setLocation(first.x, second.y);				
			else 
				if (first.y < second.y) 
					subrectangle.setLocation(second.x, first.y);
				else 
					subrectangle.setLocation(second.x, second.y);				

			this.addCursor(subrectangle);

			bounds.grow(-1,-1);

			return true;
		}

		return false;
	}

	private int getHandleIndex(Point localcoordinate) {

		if (interacting.contains(localcoordinate)) 			
			for (int i = 0; i < constituents.size(); i++) 
				if (constituents.get(i).handle().contains(localcoordinate))
					return i;

		return -1;
	}

	private void makeInteractingArea() {

		interacting.reset();

		for (Placeholder adp : constituents) 
			interacting.add(new Area(adp.handle()));
	}

	private Rectangle parseTextFields() {

		int x1, y1, x2, y2;

		x1 = (int) Double.parseDouble(txfLowerLeftX.getText());
		y1 = (int) Double.parseDouble(txfLowerLeftY.getText());
		x2 = (int) Double.parseDouble(txfUpperRightX.getText());
		y2 = (int) Double.parseDouble(txfUpperRightY.getText());

		int dx = Math.abs(x2-x1);
		int dy = Math.abs(y2-y1);

		return new Rectangle(x1, y1, dx, dy);
	}

	private void correctTextFields() {

		if (canvas.getCurrent() != frameholder) {		

			Rectangle correct = canvas.getCurrent().bounds();

			txfLowerLeftX.setText((int) correct.x + "");
			txfLowerLeftY.setText((int) correct.y + "");

			txfUpperRightX.setText((int) (correct.x + correct.width) + "");
			txfUpperRightY.setText((int) (correct.y + correct.height) + "");
		}		
	}

	private void blink(boolean ok) {

		Color color = ok ? Color.green : Color.red;

		Color restore = canvas.getBackground();

		canvas.setBackground(color);
		canvas.update(canvas.getGraphics());

		try { Thread.sleep(200); } catch (InterruptedException ie) {}

		canvas.setBackground(restore);		
		canvas.update(canvas.getGraphics());
	}


	/**
	 * Add a action listener to all buttons.
	 * 
	 * @param al The action listener distributed to all components that it should listen on.
	 * 
	 * @see ActionListener
	 */
	public void addListeners(ActionListener al) {

		btnNew.addActionListener(al);
		btnPrimitives.addActionListener(al);
		btnRender.addActionListener(al);
		btnQuit.addActionListener(al);

		btnBounds.addActionListener(al);
		btnGlyph.addActionListener(al);
		btnDelete.addActionListener(al);
		btnClear.addActionListener(al);

		btnNext.addActionListener(al);
	}

	/** 
	 * Retreives the canvas of the composite maker.
	 * 
	 * @return	The canvas displaying the editing.
	 */
	public CompositeCanvas getCanvas() {
		return canvas;
	}


	/**
	 * Find a particular described formal in the composite currently worked on.
	 *
	 * @param find The chosen
	 * @param components the components
	 * @return the primitive placeholder
	 */
	public static Placeholder	findComponent(Described find, Collection<Placeholder> components) {

		for (Placeholder component : components) 
			if (component.described().getCodepoint() == find.getCodepoint())
				return component;

		return null;
	}

	/**
	 * Finds a button for a primitive given a description of that primitive.
	 *
	 * @param description The descritption to look for.
	 * @param buttons The array buttons of search.
	 * @return The button having the primitive looked for or null if no such was found.
	 */
	public static DButton 		findButton(DRectangle description, Component[] buttons) {

		for (Component component : buttons) 	{		

			if (component instanceof DButton) {

				DButton button = (DButton) component;

				String utfname = button.getDescribed().getName();

				if (utfname.equals(description.getValue().getName()))
					return button;
			}
		}

		return null;
	}


	private void makeNorthPane() {

		JLabel lblTitle = new JLabel("Make your composite");

		pnlNorth.add(lblTitle);
	}

	private void makeWestPane() {

		JLabel lblFirst = new JLabel("upper left (x,y)");	
		txfLowerLeftX = new JTextField("X", 3); 	txfLowerLeftY = new JTextField("Y", 3);

		JLabel lblSecond = new JLabel("lower right (x,y)"); 
		txfUpperRightX = new JTextField("X", 3); 	txfUpperRightY = new JTextField("Y", 3);

		Action txfAction = new AbstractAction() {

			public void actionPerformed(ActionEvent e) {

				Rectangle bounds = parseTextFields();

				canvas.getCurrent().bounds().setBounds(bounds);

				canvas.repaint();
			}			
		};

		txfLowerLeftX.setAction(txfAction);		txfLowerLeftY.setAction(txfAction);
		txfUpperRightX.setAction(txfAction);	txfUpperRightY.setAction(txfAction);

		JLabel lblZoom = new JLabel("zoom"); btnMinus = new JButton("-"); btnPlus = new JButton("+");

		pnlWest.setLayout(grdbgWest);
		pnlWest.setPreferredSize(new Dimension(200, 600));

		grdbgWest.columnWeights = new double[]{ 1, 0, 1};
		grdbgWest.rowWeights = new double[]{ };

		cnstrWest.insets = new Insets(2, 2, 2, 2);
		cnstrWest.anchor = GridBagConstraints.WEST;

		cnstrWest.gridy = 0;	cnstrWest.gridx = 1;	pnlWest.add(lblFirst, cnstrWest);
		cnstrWest.gridy = 2;	cnstrWest.gridx = 1;	pnlWest.add(lblSecond, cnstrWest);
		cnstrWest.gridy = 1;	cnstrWest.gridx = 1;	pnlWest.add(txfLowerLeftX, cnstrWest);		
		cnstrWest.gridy = 1;	cnstrWest.gridx = 2;	pnlWest.add(txfLowerLeftY, cnstrWest);
		cnstrWest.gridy = 3;	cnstrWest.gridx = 1;	pnlWest.add(txfUpperRightX, cnstrWest);
		cnstrWest.gridy = 3;	cnstrWest.gridx = 2;	pnlWest.add(txfUpperRightY, cnstrWest);		

		cnstrWest.insets = new Insets(20, 2, 2, 2);
		cnstrWest.fill = GridBagConstraints.HORIZONTAL;
		cnstrWest.gridy = 6;	cnstrWest.gridx = 1;	pnlWest.add(btnBounds, cnstrWest); 		

		cnstrWest.insets = new Insets(2, 2, 2, 2);
		cnstrWest.gridy = 7;	cnstrWest.gridx = 1;	pnlWest.add(btnGlyph, cnstrWest);
		cnstrWest.gridy = 8;	cnstrWest.gridx = 1;	pnlWest.add(btnDelete, cnstrWest);
		cnstrWest.gridy = 9;	cnstrWest.gridx = 1;	pnlWest.add(btnClear, cnstrWest);
		
		cnstrWest.fill = GridBagConstraints.NONE;
		cnstrWest.insets = new Insets(20, 2, 2, 2);
		cnstrWest.gridy = 11;	cnstrWest.gridx = 1;	pnlWest.add(lblZoom, cnstrWest);		

		cnstrWest.insets = new Insets(2, 2, 2, 2);
		cnstrWest.gridy = 12;	cnstrWest.gridx = 1;	pnlWest.add(btnMinus, cnstrWest);
		cnstrWest.gridy = 12;	cnstrWest.gridx = 2;	pnlWest.add(btnPlus, cnstrWest);
		cnstrWest.gridy = 14;	cnstrWest.gridx = 2;	pnlWest.add(btnNext, cnstrWest);

		btnBounds.setActionCommand("bounds"); 		btnGlyph.setActionCommand("glyph"); 
		btnDelete.setActionCommand("delete"); 		btnClear.setActionCommand("clear");
		btnMinus.setActionCommand("zoom-minus");	btnPlus.setActionCommand("zoom-plus");		
		btnNext.setActionCommand("next");
	}

	private void makeSouthPane() {

		btnNew.setActionCommand("new");
		btnPrimitives.setActionCommand("to primitives");
		btnRender.setActionCommand("render");
		btnQuit.setActionCommand("quit");

		pnlSouth.add(btnNew); pnlSouth.add(btnPrimitives);
		pnlSouth.add(btnRender); pnlSouth.add(btnQuit);
	}

	private void makeEastPane() {

		pnlEast.add(pnlButtons);
		pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.PAGE_AXIS));
	}

	private void makeCenterPane() {

		this.canvas.setBackground(new Color(228, 222, 160));
	}

	
	/**
	 * Does nothing yet.
	 */
	public void mouseMoved(MouseEvent e) {		
	}
	
	/**
	 * Changes cursor
	 */
	public void mouseEntered(MouseEvent e) {
		this.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
		e.consume();
	}
	
	/**
	 * Changes cursor back.
	 */
	public void mouseExited(MouseEvent e) {
		this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		e.consume();
	}
	
	/**
	 * Does nothing but consumes the event.
	 */
	public void mouseClicked(MouseEvent e) {
		e.consume();
	}


	private CompositeCanvas 	canvas;

	private JButton btnNext 		= new JButton("step");
	private JButton btnNew 			= new JButton("new cursor");
	private JButton btnPrimitives 	= new JButton("insert");
	private JButton btnRender 		= new JButton("render");
	private JButton btnQuit 		= new JButton("quit");

	private JButton btnPlus, btnMinus;

	private JTextField txfLowerLeftX, txfLowerLeftY, txfUpperRightX, txfUpperRightY;

	private Panel pnlNorth = new Panel();
	private Panel pnlEast  = new Panel();
	private Panel pnlSouth = new Panel();
	private Panel pnlWest  = new Panel();

	private GridBagLayout 		grdbgWest 	= new GridBagLayout();
	private GridBagConstraints 	cnstrWest	= new GridBagConstraints();

	private JPanel pnlButtons = new JPanel();

	private JButton btnBounds 	= new JButton("> bounds");
	private JButton btnGlyph 	= new JButton("> glyph");
	private JButton btnDelete 	= new JButton("delete");
	private JButton btnClear 	= new JButton("clear all");

}
