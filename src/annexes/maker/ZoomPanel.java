package annexes.maker;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.Map.Entry;
import java.util.PriorityQueue;

import javax.swing.JPanel;

import control.statics.Arithmetic;
import model.description.DComposite;
import model.description.abstraction.Placeholder;
import model.description.abstraction.Placeholder.Handle;
import model.independent.CyclicMap;

/**
 * A simple panel, more like a canvas but with a zooming feature. 
 * 
 * Remark: as I comprehend, to make benefit of the simplicity of a ordinary {@link Canvas} one has to improve on double
 * buffering, which is not in the scope of this application. That is way {@link JPanel} is used although it is a simple canvas.
 */
public class ZoomPanel extends JPanel {

	/**
	 * The placeholders of the designed composite.
	 */
 	protected CyclicMap<Handle, Placeholder> 	cursors	= new CyclicMap<>();
 	/**
 	 *  The handles of the placeholders.
 	 */
	protected PriorityQueue<Handle> 					handles	= new PriorityQueue<Handle>();
	/**
	 * The zero coordinates of the canvas.
	 */
	protected Point2D.Double 								origo;
	/**
	 * The handle of the currently active placeholder. 
	 */
	protected Handle 												handle;

	private AffineTransform 		map = null;
	private DComposite			inset = null;
	
	
	/**
	 * Sets the zooming transform.
	 * 
	 * @param map	A scaling transform.
	 * 
	 * @see AffineTransform#getScaleInstance(double, double)
	 */
	public void 						setTransform(AffineTransform map) {
		this.map = map;
	}
	/**
	 * Returns the current zooming transform.
	 * 
	 * @return The transform paint uses to draw current works in a zoomed state.
	 */
	public AffineTransform 	getTransform() {
		 return map;
	}
	
	/**
	 * Change of coordinates to local coordinate system. Basis translation to local basis.
	 * 
	 * @param R		The point to have a change of basis.
	 * @return			The point's cooridinates in local basis.
	 */
	public Point2D.Double 	getLocalCoordinates(Point2D.Double R) {	
		return Arithmetic.add(R, Arithmetic.neg(origo));
	}
	/**
	 * The inverted coordinates of the zoomed ones.
	 * 
	 * @return	Inverted zoomed localal coordinates, the real local coordinates, not the ones that paint uses.
	 */
	public Point2D 			getInvertedCoordinates(Point2D.Double Z) {

		if (map == null) return Z;
		
		AffineTransform unzoom = this.getInverseTransform();
		
		Point2D R = new Point2D.Double(0,0);
		
		R = unzoom.transform(Z, R);
		
		return R;
	}
	/**
	 * The inverted vectorial change of a zoomed vectorial change in the current zooming.
	 * 
	 * @return	Inverted vectorial change cooresponding to non-zoomen local coordinates.
	 */
	public Point2D 			getInvertedDeltaCoordinates(Point2D.Double dZ) {

		if (map == null) return dZ;

		AffineTransform unzoom = this.getInverseTransform();
		
		Point2D dR = new Point2D.Double(0,0);
		
		dR = unzoom.deltaTransform(dZ, dR);
		
		return dR;
	}

	/**
	 * Activates the placeholder a point points at.
	 * 
	 * @param p		The point that possibly is contained in some placeholder handle.
	 * 
	 * @return			The place holder or null if the point isn't contained in any placeholder.
	 */
	public Placeholder 		activateObject(Point2D.Double p) {

		Placeholder active = cursors.get(this.handle);

		if (active != null && this.handle.contains(p))		// no action 
				return active; 				

		Handle handle;

		for (Entry<Handle, Placeholder> e : cursors.entrySet()) {

			active = e.getValue();
			handle = e.getKey();

			if (handle.contains(p)) {

				this.handle = handle;

				return active; 		// new activated only
			}
		}

		return null; 				// moving 0
	}
	/**
	 * Moves the currently active place holder to a point.
	 * 
	 * @param r	The point to move it to.
	 */
	public void 			moveActivated(Point2D.Double r) {

		Placeholder moved = cursors.get(handle);

		moved.moveTo(r);
	}
		
	/**
	 * Sets the inset image that displays the current all-components rendering.
	 *
	 * @param done The rendered composite mathematics symbol.
	 */
	public void 			setInset(DComposite done) {
		
		inset = done;
	}
	
	
	public void paint(Graphics g) {

		Graphics2D g2dc = (Graphics2D) g.create();

		drawFraming(g2dc);
		
		g2dc.translate(origo.x, origo.y);		
		
		if (map != null) {

			AffineTransform at = g2dc.getTransform();

			at.concatenate(map);
			
			g2dc.setTransform(at);
		}

		Placeholder holder;

		if (handle != null) {
			
			holder = cursors.get(handle);
			
			holder.draw(g2dc, true);
		}		
		
		for (Handle h : handles)  {			
			
			Placeholder drawn = cursors.get(h);
			drawn.draw(g2dc, false);
		}

		if (inset != null) drawInset(g2dc);
		
		g2dc.dispose();
	}

	
	private AffineTransform 	getInverseTransform() {

		if (map == null) return null;

		try {
			if (this.map.getDeterminant() != 0)
				return this.map.createInverse();	
			else
				return null;
			
		} catch (NoninvertibleTransformException nite) {

			nite.printStackTrace();
			
			return null;
		}
	}

	private void drawInset(Graphics2D g2d) {
		
		g2d.translate(-origo.x, -origo.y);

		inset.draw(g2d);	
	}

	private void drawFraming(Graphics2D g2d) {
		
		Rectangle bounds = g2d.getClipBounds();

		g2d.setColor(this.getBackground());		

		g2d.fillRect(0, 0, bounds.width, bounds.height);
		
		g2d.setColor(this.getForeground());

		bounds.grow(-3,-3);
		
		g2d.setColor(Color.blue);
		
		g2d.draw(bounds);
		
		g2d.fillArc((int) origo.x-3, (int)  origo.y-3, 6, 6, 0, 360);
	}
}