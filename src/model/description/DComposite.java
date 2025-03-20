package model.description;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import control.Toolbox;
import model.description.abstraction.AbstractDComposite;
import model.description.abstraction.Described;
import model.description.abstraction.Placeholder;
import model.description.abstraction.Placeholder.Baseline;
import model.independent.CyclicList;
import model.logic.Composite;
import model.logic.Primitive;
import model.logic.abstraction.Formal;

/**
 * A description of a composite, a composite of primitives.
 */
public class DComposite extends AbstractDComposite {
	
	
	/**
	 * Instantiates a new described composite. 
	 *
	 * @param components 	The components of this composite together with their baselines and locations.
	 * @param codepoint		The codepoint assigned to this described composite. That codepoint is decide upon by 
	 * 						the user but should not conflict with the UTF codepoints.
	 * @see Placeholder
	 */
	public DComposite(List<Placeholder> components, int codepoint) {

		super.type 			= FormalType.COMPOSITE;

		super.codepoint		= codepoint;
		
		super.constituents 	= new CyclicList<Placeholder>(components);	
	
		super.name 			= makeCompositeName(this.constituents);		
		
		super.frame 		= super.constituents.get(0);
		
		Composite value 	= Composite.makeValue(toFormals(this.constituents), codepoint);
		
		super.description 	= new DRectangle(value, this.frame.baseline().length);	
		
		this.renderAndMount();	

	}

	/**
	 * Instantiates a new described composite from a composite formal.  
	 *
	 * @param formal 	The composite formal to make a default description of. If other than composite formal is 
	 * 					given as parameter it will for now just give an error message on stderr.
	 */
	public DComposite(Formal formal) {

		if (formal instanceof Composite) {

			Composite composite = (Composite) formal;
			
			super.type 			= composite.getType();
			super.codepoint		= composite.getCodepoint();

			Composite value = Composite.makeValue(Arrays.asList(composite.toArray()), this.codepoint);
			
			super.description 	= new DRectangle(value);
			
			super.constituents 	= DComposite.emptyDescription((Composite) this.description.getValue());
			
			super.name 			= makeCompositeName(this.constituents);		

			this.renderAndMount();	
			
		} else 
			System.err.println("Make described Composite from non-composite?");
	}
	

	/** {@inheritDoc} **/
	public DRectangle renderAndMount() {

		BufferedImage whole = new BufferedImage(this.description.width, this.description.height, BufferedImage.TYPE_INT_ARGB);

		Graphics2D gcopy = whole.createGraphics();

		Placeholder first = constituents.removeFirst();

		Described 		described 		= first.described();
		Baseline 		baseline 		= first.baseline();
		BufferedImage 	subglyph		= described.description().getImage();
		Point 			imageoffset		= described.description().getReference();
				
		gcopy.drawImage(subglyph, null, 0, 0);

		Point globalorigo = first.baseline().getLocalReferencepoint();		
		
		if (Toolbox.DEBUGVERBOSE) {
			System.out.println("Rendered frame baseline : (" + globalorigo.x + "," + globalorigo.y + "," + baseline.length + ")");
			System.out.println("subframe : (" + first.bounds().x + "," + first.bounds().y + ")");
			System.out.println("location : (" + 0 + "," + 0 + ")");
		}

		Point refoffset = first.described().getLocalReference();
		
		globalorigo.translate(refoffset.x, refoffset.y);
		
		int x, y;
		
		for (Placeholder component : constituents) {

			baseline 	= component.baseline();
			described 	= component.described();
			subglyph 	= described.description().getImage();			
			imageoffset = described.description().getReference();
			
			x = globalorigo.x + baseline.x - imageoffset.x;
			y = globalorigo.y + baseline.y - imageoffset.y;
			
			gcopy.drawImage(subglyph, null, x, y);

			if (Toolbox.DEBUGVERBOSE) {
				System.out.println("Rendered subframe baseline : (" + baseline.x + "," + baseline.y + "," + baseline.length + ")");
				System.out.println("subframe : (" + component.bounds().x + "," + component.bounds().y + ")");
				System.out.println("location : (" + x + "," + y + ")");
			}
		}	

		this.constituents.addFirst(first);		
		this.description.setImage(whole); 
			
		return this.description();
	}

	/** {@inheritDoc} **/
	public DComposite clone() {
				
		DComposite clone = new DComposite(super.constituents, Toolbox.nextCompositeId());
		
		return clone;
	}
	
		
	/**
	 * Constructs a list of placeholders from two text strings, the placeholder should be used for constructing a composite. 
	 * 
	 * @param codepoints 	A string of all components (utf) codepoints, integers interspaced with blanks.
	 * @param baselines 	The string of all the component's coordinates given in tripples (x,y,baseline), all integers interspaced with blanks.
	 * 
	 * @return A list of plasceholders for the components.
	 * 
	 * @see Placeholder
	 */
	public static LinkedList<Placeholder> 	parseComponents(String codepoints, String baselines) {

		LinkedList<Placeholder> components = new LinkedList<Placeholder>();

		String[] cps = codepoints.split(" ");
		String[] bls = baselines.split(" ");

		String[] bl = new String[3];

		DPrimitive primitive;
		Rectangle frame;
		Placeholder holder;
		
		int x, y, baseline, codepoint;

		for (int i = 0; i < cps.length; i++) {

			bl = bls[i].split(":");

			x = Integer.parseInt(bl[0]); y = Integer.parseInt(bl[1]); baseline = Integer.parseInt(bl[2]);
			
			codepoint = Integer.parseInt(cps[i]);

			codepoint = codepoint != -1 ? codepoint : Toolbox.DUMMY.getCodepoint(); 

			primitive   = new DPrimitive(codepoint, baseline);
			
			frame 		= primitive.description().getBounds();
			frame.translate(x, y);
			
			holder 		= new Placeholder(frame);
			
			holder.fill(primitive);
			
			components.add(holder);			
		}

		return components;
	}

	
	private static CyclicList<Placeholder> 	emptyDescription(Composite value) {

		CyclicList<Placeholder> describeds = new CyclicList<Placeholder>();
		
		for (Formal formal : value.toArray()) {
			
			Point offset = new Point(0,0);
			
			Described stddescription = null;
			
			if (formal instanceof Primitive)
				stddescription = new DPrimitive(formal);
			else
				if (formal instanceof Composite)
					stddescription = new DComposite(emptyDescription(value), formal.getCodepoint());											///(42EB)
									
			if (stddescription != null) {

				Placeholder holder = new Placeholder(stddescription.description().getBounds());

				holder.fill(stddescription);
				describeds.add(holder);
			}
			else
				System.err.println("Unknown component in composite (emptyDescription");
		}
		
		return describeds;
	}
	
	private static String 					makeCompositeName(Iterable<Placeholder> components) {

		String sequence = "";

		for (Placeholder component : components) 			
			sequence += (char) component.described().getCodepoint() + ",";

		sequence = sequence.substring(0, sequence.length()-1);

		return "[" + sequence + "]";
	}

}
