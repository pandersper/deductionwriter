package model.description;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import control.statics.PaintStatics;
import control.statics.PaintStatics.Size2D;
import control.statics.Toolbox;
import model.description.abstraction.AbstractDComposite;
import model.description.abstraction.Described;
import model.description.abstraction.Placeholder;
import model.description.abstraction.Placeholder.Handle;
import model.independent.CyclicMap;
import model.logic.Composite;
import model.logic.Primitive;
import model.logic.abstraction.Formal;


/**
 * A description of a composite, a composition of primitives. In future perhaps composites containing composites 
 * will be implemented.
 */
public class DComposite extends AbstractDComposite {
	
	
	/**
	 * Instantiates a new described composite. 
	 *
	 * @param holders 		The components of this composite together with their baselines and locations amongst
	 * 						other things in placeholder objects.
	 * @param codepoint		The codepoint assigned to this described composite. That codepoint is decide upon by 
	 * 						the user but should not conflict with the UTF codepoints.
	 * @see Placeholder
	 */	
	public DComposite(CyclicMap<Handle, Placeholder> cursors) {

		super.constituents 		= cursors;

		super.codepoint			= Toolbox.nextCompositeId();	
		super.type 				= FormalType.COMPOSITE;
		
		super.frameholder 		= Toolbox.findFrame(this.constituents);
		super.name 				= Composite.makeCompositeName(this.constituents.values());		

		Composite instance 			= Composite.makeValue(toFormals(this.constituents.values()), this.codepoint);
		BufferedImage iconimage		= PaintStatics.makeCompositeGlyph(this.constituents);
		
		this.composition = instance.getComposition();
										
									// REMOVE instance parameter.
		super.description 			= new DRectangle(instance, this.frameholder, iconimage);	
		super.description.reference = this.frameholder.getLocalReference();	
		super.current 				= this.frameholder;				
 	}
	
	/**
	 * Instantiates a new described composite. 
	 *
	 * @param components	The components of this composite together primarily with their baselines and locations
	 * 						contained by a placeholder.
	 * @param codepoint		The codepoint assigned to this described composite. That codepoint is decide upon by 
	 * 						the user but should not conflict with the UTF codepoints.
	 * @see Placeholder
	 */	
	public DComposite(CyclicMap<Handle, Placeholder> components, int codepoint) {
		this(components);
		
		this.codepoint = codepoint;
	}
	
	/**
	 * The progress on the line that drawing this glyph gives.
	 */
	public double getAdvance() {
		return description.advance;
	}
	
	
	/**
	 * Cloning does not work correctly yet. See todo list.
	 */
	public DComposite clone() {
						
		CyclicMap<Handle, Placeholder> clonedconstituents = new CyclicMap<Handle, Placeholder>();
		
		for (Placeholder holder : this.constituents.sortedValues()) {
			
			Placeholder holderclone = holder.clone();
			
			clonedconstituents.put(holderclone.handle(), holderclone);
		}
		
		DComposite clone = new DComposite(clonedconstituents);
		
		clone.description = this.description.clone();
				
		clone.setWritepoint(this.getWritepoint());
		
		return this;		
	}
	
	/**
	 * Not implemented yet.
	 */
	public Described scaledClone(double baseline, boolean transparent) {
		// Not implemented yet
		System.err.println("Not implemented yet");
		return null;
	}


	/**
	 * Constructs a list of placeholders from two text strings, the placeholders should be passed on composite 
	 * constructors. 
	 * 
	 * @param codepoints 	A string of all components (utf) codepoints, integers interspaced with blanks.
	 * @param baselines 	The string of all the component's coordinates given in tripples (x,y,baseline), all 
	 * 						integers interspaced with blanks.
	 * 
	 * @return A list of plasceholders for the components.
	 * 
	 * @see Placeholder
	 */
	public static CyclicMap<Handle, Placeholder> 	parseComponents(String codepoints, String baselines) {

		ArrayList<Placeholder> components = new ArrayList<Placeholder>();

		String[] cps = codepoints.split(" ");
		String[] bls = baselines.split(" ");

		Placeholder holder;
		
		for (int i = 0; i < cps.length; i++) {

			holder = parsePlaceholder(cps[i], bls[i]);
			
			components.add(holder);			
		}

		return Toolbox.cyclicMap(components);
	}

	private static Placeholder parsePlaceholder(String codepointsstring, String baselinestring) {
		
		String[] bl;
		DPrimitive primitive;
		Rectangle2D.Double frame;
		Placeholder holder;

		int x, y, baseline, codepoint;
		
		bl = baselinestring.split(":");

		x = Integer.parseInt(bl[0]); 	y = Integer.parseInt(bl[1]); 	baseline = Integer.parseInt(bl[2]);
		
		codepoint = Integer.parseInt(codepointsstring);

		primitive = (codepoint != -1) ? new DPrimitive(codepoint, baseline, false) : DPrimitive.DUMMY.scaledClone(baseline,true);
		
		frame 		= primitive.getBounds();
		
		frame.setFrame(new Point2D.Double(frame.x,frame.y), new Size2D(frame));
		
		holder 		= new Placeholder(new DCursor(frame));
		
		holder.insert(primitive);

		return holder;
	}
	
	private static CyclicMap<Handle, Placeholder> 	emptyDescription(Composite value) {

		
		CyclicMap<Handle, Placeholder> describeds = new CyclicMap<Handle, Placeholder>();
	
		for (Formal formal : value.getComposition()) {
						
			Described stddescription = null;
			
			if (formal instanceof Primitive)
				stddescription = new DPrimitive(formal);
			else
				if (formal instanceof Composite) {
					
					CyclicMap<Handle, Placeholder> empty = emptyDescription(value);
					
					Placeholder emptyframe = Toolbox.findFrame(empty);	
					
					stddescription = new DComposite(empty);				
				}
			
			if (stddescription != null) {

				Placeholder holder = new Placeholder(stddescription.description());

				holder.insert(stddescription);
				describeds.insertElement(holder.handle().depth, holder.handle(),holder);
			
			} else
				System.err.println("Unknown component in composite (emptyDescription");
		}	
		return describeds;
	}
}
