package annexes.maker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import control.statics.PaintStatics.Size2D;
import control.statics.Toolbox;
import control.statics.ViewStatics;
import model.description.DComposite;
import model.description.abstraction.Described;
import model.description.abstraction.Placeholder;
import model.description.abstraction.Placeholder.Handle;
import model.independent.CyclicMap;
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
public class CompositePanel extends JPanel implements MouseListener, MouseMotionListener, ChangeListener, ActionListener {


	private Point2D.Double 		first = null, second = null, dragged;

	private Placeholder			current, frameholder;

	private boolean 			moving0, moving1;	
	private boolean 			editing = false, zooming  = false;

	private double 				scale = 1, oldbaseline = -1;
		
	/**
	 * Instantiates a new composite panel.
	 * @param elder The elder frame to return control to.
	 */
	public CompositePanel(JPanel buttons) {

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		this.buttons = buttons;

		canvas = new CompositeCanvas(this);	
		
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);

		canvas.setOrigo(new Point2D.Double(ViewStatics.makercanvasdimension.width / 2.0, 
										   ViewStatics.makercanvasdimension.height / 2.0));

		this.setName("composites panel");
		
		add(canvas, BorderLayout.CENTER);
	}
	
	/** 
	 * Retreives the canvas of the composite maker.
	 * 
	 * @return	The canvas displaying the editing.
	 */
	public CompositeCanvas 	getCanvas() {
		return canvas;
	}
	
	public Placeholder 		getFrameholder() {
		return frameholder;
	}
	
	/**
	 * Sets the bounding backdrop of the current composite. It is often only a blank dummy rectangle
	 * but can be also some glyph giving this composite a structure.
	 * 
	 * @param primitive The described primitive.
	 * 
	 * @see CompositeCanvas#setCurrent(Placeholder)
	 */
	public void 		setupFrame(Described init) {
		
		canvas.clearAll();

		if (init instanceof DComposite) {

			DComposite composite = (DComposite) init;

			CyclicMap<Handle, Placeholder> constituents = composite.getConstituents();

			for (Handle key : constituents.sortedKeys()) {

				Placeholder old = constituents.get(key);
				
				Described adjusted = old.described();
				
				canvas.addPlaceholder(old);
				
				Placeholder current = canvas.getCurrent();

				current.insert(adjusted);
				
				this.addButton(new DButton(adjusted));
			}

			frameholder = Toolbox.findFrame(canvas.cursors);
			frameholder.moveTo(new Point2D.Double(0,0));		

			canvas.setCurrent(frameholder);
			
			canvas.recomputeInteraction();
			
		} else {
					
			frameholder = canvas.addPlaceholder(init.description());
			
			frameholder.moveTo(new Point2D.Double(0,0));		

			frameholder.insert(init);
			
			canvas.updateInteraction(frameholder);
			
			this.addButton(new DButton(init));
		}
	}
	/**
	 * Sets up a given described composite for further editing of it. 
	 * 
	 * @param composite	The described composite to continue editing.
	 */
	public void 		setupComposite(DComposite composite) {

		Placeholder frame = composite.getFrame();		

		this.setupFrame(frame.described());					assert(!frame.isEmpty());

		CyclicMap<Handle, Placeholder> shapes = new CyclicMap<Handle, Placeholder>();
		
		Collection<Placeholder> holders = composite.getConstituents().sortedValues();
		
		for (Placeholder holder : holders)
			shapes.put(holder.handle(), holder);
		
		shapes.remove(frame.handle());					// remove frame from other constituents
		
		for (Placeholder holder : holders) 			
			this.addButton(new DButton(holder.described()));		
		
		canvas.setupCursors(shapes);
	}
		/**
	 * Puts together a new composite out of the current state of this sub application, from the sub components and their layout.
	 *
	 * @return The designed described composite formal.
	 */
	
	public void 		removeButton(Placeholder current) {

		for (Component component : buttons.getComponents()) {
		
			if (component instanceof DButton) {

				if (((DButton)component).id == current.id) {

					buttons.remove(component);
					
					canvas.repaint();
					buttons.repaint();
					
					return;
				}
			}
		}
	}
	/**
	 * Adds a button to the buttons panel.
	 * 
	 * @param button	The button to add.
	 */
	public void 		addButton(DButton button) {

		if (button != null) {
			
			button.setActionCommand("button");
			
			button.addActionListener(this);
			
			buttons.add(button);
		}			
		
		buttons.revalidate();
	}

	public void clear() {

		Described frame = frameholder.described();
		
		canvas.clear();
		
		buttons.removeAll();
		
		this.setupFrame(frame);
		
		this.addButton(new DButton(frame));

		
		buttons.revalidate();
	}

	/**
	 * Clears the workpiece, the described composite and resets this panel.
	 */
	public void clearAll() {
		
		canvas.clearAll();
		
		buttons.removeAll();		
		buttons.revalidate();
	}
	
		/**
	
	/**
	 * Toggle editing mode.
	 */
	public void toggleEditing() {

		if (!editing) 
			canvas.setBackground(Color.pink);
		else 
			canvas.setBackground(Color.white);

		editing = !editing;

		first = null; second = null;
		
		canvas.repaint();
	}

	public void toggleResizing() {

		oldbaseline = (oldbaseline == -1) ? canvas.getCurrent().getAdvance() : -1;
	}

	/**
	 * Only receives and acts on events from buttons in the east panel of sub primitives.
	 * 
	 * @param e Only events originating from buttons is handled.
	 */	
	public void actionPerformed(ActionEvent e) {

		switch (e.getActionCommand()) {

			case "button":
	
				DButton source = (DButton) e.getSource();
	
				Placeholder chosen = null;
				
				Collection<Placeholder> constituents = canvas.exportCursors().values();

				for (Placeholder holder : constituents) 
					if (holder.id == source.id) {
						chosen = holder;
						break;
					}		
				
				canvas.setCurrent(chosen);		
				canvas.repaint();
	
				canvas.recomputeInteraction();		
	
				break;
					
			default:
				
				break;						
		}
		
		canvas.repaint();
	}
	
	public void stateChanged(ChangeEvent e) {
	
		JSlider slider = (JSlider) e.getSource();
		
		int value = slider.getValue();

		if (slider.getName() == "zoomer") {
			
			double scale = value / 10.0;
			
			canvas.setTransform(AffineTransform.getScaleInstance(scale, scale));
			
			canvas.recomputeInteraction();								
		} 		
		
		if (slider.getName() == "sizer" && oldbaseline != -1) {	// could be inbetween events

			double scale = 1 + (value / 1000.0);

			Placeholder current = canvas.getCurrent();
			
			double newbaseline = scale * oldbaseline;
			
			if (current != frameholder) 
				current.resizeAllToBaseline(newbaseline);				
		} 		

		canvas.repaint();
	}
	
	/**
	 * Registers first and second point clicked on and sets the sub components movable-variable.
	 * 
	 * @param e	The mouse button pressed event.
	 */
	public void mousePressed(MouseEvent e) {

		Point r = e.getPoint();

		Point2D.Double r2dglobal = new Point2D.Double(r.x,r.y);
		Point2D.Double r2dlocal = canvas.getLocalCoordinates(r2dglobal);

		r2dlocal = (canvas.getTransform() != null) ? (Point2D.Double) canvas.getInvertedCoordinates(r2dlocal) : r2dlocal;
		
		current = canvas.activateObject(r2dlocal);
		
		moving0 = current == null || current == frameholder;
		moving1 = !moving0;
		
		canvas.repaint();		
		
		if (editing) alterComposite(r2dlocal);
		else 
			moveComposite(r2dlocal);

		e.consume();
	}
	/**
	 * Releases movability and updates gui information.
	 * 
	 * @param e The mouse button released event.
	 */
	public void mouseReleased(MouseEvent e) {

		moving0 = false;		moving1 = false;

		canvas.recomputeInteraction();
		canvas.repaint();
		
		e.consume();
	}
	/**
	 * Handles dragging, translation, while buttons is held down. Either the whole work piece or a sub component is translated.
	 * 
	 * @param e Mouse dragged event. How and at what pace they are genereated can probably be read in {@see MouseMotionListener}.
	 */
	public void mouseDragged(MouseEvent e) {
		
		Point  r = e.getPoint();
		
		Point2D.Double r2dglobal = new Point2D.Double(r.x, r.y); 
		
		Point2D.Double r2dlocal = canvas.getLocalCoordinates(r2dglobal);

		r2dlocal = (canvas.getTransform() != null) ? (Point2D.Double) canvas.getInvertedCoordinates(r2dlocal) : r2dlocal;

		if (moving0) canvas.setOrigo(r2dglobal);	
		else
			if (moving1) canvas.moveActivated(r2dlocal);	
			else
				//if (moving2) canvas.moveSubhandle(r2dlocal);					
		
		canvas.recomputeInteraction();
		canvas.repaint();		
	}

	
	private boolean 	tryMakeSubRectangle(Point2D.Double first, Point2D.Double second) {	

		Rectangle2D.Double bounds = frameholder.frame();		// |relative canvas origo| - devices coordinates

		if (bounds.contains(first) && bounds.contains(second)) {			

			double w = Math.abs(second.x - first.x);
			double h = Math.abs(second.y - first.y);

			Rectangle2D.Double subrectangle = new Rectangle2D.Double(0, 0, w, h);
 
			Point2D.Double location = null;
			
			if (first.x < second.x) 				// determines mouse click order
				if (first.y < second.y)
					location = new Point2D.Double(first.x, first.y);
				else 
					location = new Point2D.Double(first.x, second.y);
			else 
				if (first.y < second.y) 
					location = new Point2D.Double(second.x, first.y);
				else 
					location = new Point2D.Double(second.x, second.y);

			subrectangle.setFrame(location, new Size2D(subrectangle));
			
			canvas.addPlaceholder(subrectangle);

			return true;
		}		
		return false;
	}

	
	private void 		moveComposite(Point2D.Double local) {

		Area interacting = canvas.getInteractingArea();
		
		if (interacting.contains(local)) {
			
			current = canvas.getCurrent();
			
			canvas.setCurrent(current);
		}
	}

	private void 		alterComposite(Point2D.Double local) {
	
		if (first != null) {

			second = local;

			boolean ok = tryMakeSubRectangle(first, second);

		//	blink(ok);

			toggleEditing();

		} else {

			first = local;
		}
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

	/**
	 * Add a action listener to all buttons.
	 * 
	 * @param al The action listener distributed to all components that it should listen on.
	 * 
	 * @see ActionListener
	 */
	public void addListeners(ActionListener al) {

		btnCursor.addActionListener(al);
		btnInsert.addActionListener(al);
		btnRender.addActionListener(al);
		btnDone.addActionListener(al);

		btnDelete.addActionListener(al);
		btnClear.addActionListener(al);
	}

	public void setButtons(JButton[] btns) {
		
		btnCursor 	= btns[0];
		btnInsert 	= btns[1];
		btnRender 	= btns[2];
		btnDone   	= btns[3];

		btnDelete 	= btns[4];
		btnClear  	= btns[5];
	}		

	//start_win_var_init
	
	private JPanel 			content = new JPanel();
	private JLayeredPane 	layered = new JLayeredPane();
	
	private CompositeCanvas 	canvas;

	private JInternalFrame 		viewport;
	private JPanel 				buttons;

	private JButton btnNext = new JButton("step");
	
	private JButton btnCursor, btnInsert, btnRender, btnDone, btnDelete, btnClear;
	
	private GridBagLayout 		grdbgWest 	= new GridBagLayout();
	private GridBagConstraints 	cnstrWest	= new GridBagConstraints();
	
	//end_win_var_init
	
	/**
	 * Iterates the selected component to the next one.
	 */
	public void forward() {		
		System.out.println("Not implemented (CompsitePanel:forward()");
	}
}
