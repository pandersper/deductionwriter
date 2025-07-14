package annexes.maker;

import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.PriorityQueue;

import model.description.DCursor;
import model.description.DPrimitive;
import model.description.DRectangle;
import model.description.abstraction.Described;
import model.description.abstraction.Placeholder;
import model.description.abstraction.Placeholder.Handle;
import model.independent.CyclicMap;
import view.abstraction.CursoredCanvas;
import view.components.DButton;

/**
 * The canvas of the composite maker which draws everything while they are edited. I keeps a current 
 * cursor among all the cursors and implements the {@link CursoredCanvas} interface. 
 * It does not keep it's own set of constituents but instead gets it from the elder composite panel. 
 * It shows the rendering in an inset in the upper left corner and has a side panel displaying and 
 * choosing the described primitives in use.S
 */
public class CompositeCanvas extends ZoomPanel implements CursoredCanvas {
	
	private Area 				interacting = new Area();
	private CompositePanel 		parent;
	
	private int depth = 0;
	
	/**
	 * Instantiates a new composite canvas.
	 *
	 * @param parentcontainer The elder panel which keeps the constituents and corresponding buttons.
	 */
	public CompositeCanvas(CompositePanel parent) {
		super();
		
		this.parent = parent;
		
		this.clearAll();
	}
	
	/** {@inheritDoc} */
	public void 		fillCursor(Described fullsize, Described erased, boolean paint) {
	
		Placeholder current = this.getCurrent();
	
		int codepoint 	= fullsize.getCodepoint();
		double baseline = current.getAdvance();
		
		Described adjusted = (fullsize.getAdvance() != baseline) ? new DPrimitive(codepoint, baseline, current.handle().depth != 0) : fullsize.clone();
		
		current.insert(adjusted);
					
		DButton button = new DButton(adjusted);		// must pass in current.id for distinguishing later
		
		parent.addButton(button);			
	}		
	
	/**
	 * Constructs and adds a new place holder based on a cursor object.
	 * 
	 * @param cursor	The cursor that determines the shape and reference point of the placeholder. 
	 * @return			The place holder constructed and added.
	 */
	public Placeholder 	addPlaceholder(DCursor cursor) {


		Placeholder holder 	= new Placeholder(cursor);

		this.handle 		= holder.handle();
		this.handle.depth 	= depth;
		
		depth++;
		
		cursors.put(this.handle, holder);
		handles.add(this.handle);
		
		return holder;
	}
	
	/**
	 * Adds a new place holder.
	 * 
	 * @param cursor	The new place holder. 
	 */
	public void 		addPlaceholder(Placeholder replace) {

		this.handle 		= replace.handle();
		
		cursors.put(this.handle, replace);
		handles.add(this.handle);
	}

	/**
	 * Constructs and adds a new place holder based on a rectangular bounding area.
	 * 
	 * @param cursor	The rectangle that bounds the area to create a place holder for. Reference point and handle 
	 * 					is derived by an average glyph.
	 */
	public void 		addPlaceholder(Rectangle2D.Double subrectangle) {
		addPlaceholder(new DCursor(subrectangle));		
	}
	
	/**
	 * Sets the described primitive component currently edited.
	 *
	 * @param current 	The placeholder containing the sub component to choose for editing.
	 */
	public void 		setCurrent(Placeholder current) {

		super.handle = current.handle();	
	}	
	/**
	 * Returns the sub component currently edited. 
	 *
	 * @return The currently edited sub component, in it's placeholder.
	 */
	public Placeholder 	getCurrent() {

		return cursors.get(handle);
	}		
	/**
	 * Deletes current sub component and it's place holder.
	 */
	public void 		deleteCurrent() {
		
		Placeholder current = this.getCurrent();

		Placeholder frameholder = parent.getFrameholder();
		
		if (current != frameholder) {

			cursors.remove(current.handle());
			handles.remove(current.handle());
			
			this.setCurrent(frameholder);

			parent.removeButton(current);
		}
	}
	
	/**
	 * Transfers and all placeholders and their handles to a new cyclic map and returns it. The placeholder map
	 * in this canvas is left empty.
	 * 
	 * @return	A map of handles (keys) to placeholders (values).
	 */
	public CyclicMap<Handle, Placeholder> 	exportCursors() {
		
		CyclicMap<Handle, Placeholder> export = new CyclicMap<Handle, Placeholder>();
		
		//alignAllFrames(cursors);
		
		export.putAll(cursors);
		
		return export;
	}
	
	private static void alignAllFrames(CyclicMap<Handle, Placeholder> cursors) {
		
		for (Handle key : cursors.keySet()) {

			Placeholder holder = cursors.get(key);
			
			Described primitive = holder.described();
			
			DRectangle description = primitive.description();
			
			Rectangle2D.Double d1 = primitive.getBounds();
			Rectangle2D.Double d2 = description;
			Rectangle2D.Double d3 = holder.frame();
			
			int x = 2;
		}
	}
	/**
	 * Sets up the cursors that are sub glyphs of the composite glyph that should be edited.
	 * 
	 * @param cursors	The cycli map of sub glyph cursors.
	 */
	public void 							setupCursors(CyclicMap<Handle, Placeholder> cursors) {

		this.cursors.clear();
		this.handles.clear();
		
		this.cursors.putAll(cursors);		
		this.handles.addAll(cursors.keySet());
		
		this.depth = cursors.size() - 1;		
	}
	
	/**
	 * Clears the list of placeholders holding sub glyphs and the assisting ordered list of handles (keys). 
	 */
	public void 		clear() {
		
		cursors.clear();
		handles.clear();

		Placeholder frame = parent.getFrameholder();
		
		cursors.put(frame.handle(), frame);
		
		depth = 0;
		
		handle = frame.handle();

		setTransform(null);
		setInset(null);
		
		this.recomputeInteraction();	
		this.repaint();
		
	}
	
	/**
	 * Clears everything in this canvas for a complete restart.
	 */
	public void			clearAll() {

		cursors.clear();
		handles.clear();
		
		depth = 0;
		
		handle = null;

		setTransform(null);
		setInset(null);
		
		this.recomputeInteraction();	
		this.repaint();
	}
	
	/**
	 * Set the point being the origo of the glyph where the main frame has it's write point.
	 * 
	 * @param p		The new origo.
	 */
	public void 		setOrigo(Point2D.Double p) {
		origo = p;
	}
	
	/**
	 * Determines the area of handles that interact with the user by mouse point and clicks.
	 */
	public void 		recomputeInteraction() {

		interacting.reset();

		for (Placeholder adp : cursors.values()) 
			interacting.add(new Area(adp.handle()));
		
		handles = new PriorityQueue<Handle>();
		
		handles.addAll(cursors.keySet());		
	}

	/**
	 * Updates the area of interaction with the handle of a new place holder.
	 * 
	 * @param added	The new place holder whos handle shoul be interacted with.
	 * 
	 * @return	The interactive area.
	 */
	public Area 		updateInteraction(Placeholder added) {

		interacting.add(new Area(added.handle()));
		
		handles.add(added.handle());

		return interacting;
	}
	
	/**
	 * Retruns the area to interact with by mouse point and clicks.
	 * 
	 * @return	The interactive area.
	 */
	public Area 		getInteractingArea() {

		return interacting;
	}
	
	/** {@inheritDoc} */
	public Described 	getDrawn() {	
		return (super.handle !=null) ? super.cursors.get(super.handle).described() : null;
	}
	/** {@inheritDoc} */
	public void 		proceedCursor() {
		System.err.println("Not implemented (CompositeCanvas:proceedCursor())");	// TODO Auto-generated method stub	
	}
	/** {@inheritDoc} */
	public void 		emptyCursor() {
		System.err.println("Not implemented (CompositeCanvas:emptyCursor())");		// TODO Auto-generated method stub	
	}
}	
