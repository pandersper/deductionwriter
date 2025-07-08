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
 * cursor among all the cursors and the buttons fills it int the same way as in the main application. 
 * It does not keep it's own set of constituents but instead gets it from the elder composite panel. 
 * It shows the rendering in an inset in the upper left corner and has a side panel displaying and 
 * choosing the described primitives in use. <br>
 * 
 * It keeps it's own static drawing helpers but probably they will be merged with other similar in 
 * {@link control.statics.PaintStatics} in the future.
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
					
		DButton button = new DButton(adjusted, current.id);
		
		parent.addButton(button);			
	}		
	
	
	public Placeholder 		addPlaceholder(DCursor subrectangle) {

		Placeholder holder 	= new Placeholder(subrectangle);

		this.handle 		= holder.handle();
		this.handle.depth 	= depth;
		
		depth++;
		
		cursors.put(this.handle, holder);
		handles.add(this.handle);
		
		return holder;
	}
	
	public void 		addPlaceholder(Placeholder replace) {

		this.handle 		= replace.handle();
		
		cursors.put(this.handle, replace);
		handles.add(this.handle);
	}

	public void 		addPlaceholder(Rectangle2D.Double subrectangle) {
		addPlaceholder(new DCursor(subrectangle));		
	}
	
	/**
	 * Sets the described primitive component currently edited.
	 *
	 * @param current The placeholder containing the sub component to choose for editing.
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

	public void 							setupCursors(CyclicMap<Handle, Placeholder> cursors) {

		this.cursors.clear();
		this.handles.clear();
		
		this.cursors.putAll(cursors);		
		this.handles.addAll(cursors.keySet());
		
		this.depth = cursors.size() - 1;		
	}
	
	
	public void 			clear() {
		
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
	
	public void				clearAll() {

		cursors.clear();
		handles.clear();
		
		depth = 0;
		
		handle = null;

		setTransform(null);
		setInset(null);
		
		this.recomputeInteraction();	
		this.repaint();
	}
	
	public void 			setOrigo(Point2D.Double p) {
		origo = p;
	}
	

	public void 		recomputeInteraction() {

		interacting.reset();

		for (Placeholder adp : cursors.values()) 
			interacting.add(new Area(adp.handle()));
		
		handles = new PriorityQueue<Handle>();
		
		handles.addAll(cursors.keySet());		
	}

	public Area 		updateInteraction(Placeholder added) {

		interacting.add(new Area(added.handle()));
		
		handles.add(added.handle());

		return interacting;
	}
	
	public Area 		getInteractingArea() {

		return interacting;
	}
	
	
	@Override
	public Described 	getDrawn() {
		// TODO Auto-generated method stub	
		return null;
	}
	@Override
	public void 		proceedCursor() {
		// TODO Auto-generated method stub	
	}
	@Override
	public void 		emptyCursor() {
		// TODO Auto-generated method stub	
	}
}	
