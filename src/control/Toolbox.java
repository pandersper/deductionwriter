package control;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.RGBImageFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import model.description.DComposite;
import model.description.DPrimitive;
import model.description.DStatement;
import model.description.DTheorem;
import model.description.abstraction.Described;
import model.independent.DoubleArray;
import model.independent.DoubleArray.Tuple;
import model.logic.Composite;
import model.logic.Primitive;
import model.logic.abstraction.Formal;
import model.logic.abstraction.Formal.FormalType;
import view.components.DButton;

/**
 * A box of tools used across the application.
 */
public class Toolbox {

	/** Verbosity of debugging - verbose level low on/off. */
	public static final boolean DEBUGVERBOSE = false;
	/** Verbosity of debugging - minimal level low on/off. */
	public static final boolean DEBUGMINIMAL = false;
	
	private static int 					COMPOSITEID 	= Integer.MAX_VALUE - (int)(Math.random() * 10000);
	
	private static final Dimension 		GLYPHBOUNDS  	= new Dimension(200, 400); 
	private static final BufferedImage 	ANYPAINTABLE 	= new BufferedImage(GLYPHBOUNDS.width, GLYPHBOUNDS.height, BufferedImage.TYPE_INT_ARGB);
	private static final Font 			FONT 			= new Font("Monospaced", Font.PLAIN, 60);
	
	/** Commonly used font metrics. */
	public static final FontMetrics		FONTMETRICS	  	= ANYPAINTABLE.createGraphics().getFontMetrics(FONT);
	
	private static final char 			DUMMYCHAR 		= (char) 0x03A3;
	/** An empty primitive for replacement. */
	public static final Primitive 		DUMMY 			= Primitive.makeValue(DUMMYCHAR);
	/** Default description of it. */
	public static final DPrimitive 		DUMMYCURSOR 	= new DPrimitive(DUMMY);
	
	/* Default baseline parameters. */
	private static final int			BL_LENGTH 		= defaultBaselineLength();											 
	private static final int 			BL_OFFSET_Y 	=  DUMMYCURSOR.description().height - FONTMETRICS.getDescent();		

	private static final int[]			MARGINS			= { 5, 7, 10, 20 };
	private static final int 			LEFT = 0, TOP = 1, RIGHT = 2, BOTTOM = 3; 

	private static final int			MAXROWS 		= 20;
	
	/** The start of a page. */
	public static final Point 			PAGESTART 		= new Point(MARGINS[RIGHT], 2*BL_OFFSET_Y);
	/** The end of a page. */
	public static final Point 			PAGEEND 		= new Point(MARGINS[LEFT], MAXROWS*BL_OFFSET_Y + MARGINS[BOTTOM]);

	
	/** The global collection of character to primitive bindings.  */
	public static final HashMap<Character, DPrimitive>  GLYPHDICTIONARY = new HashMap<Character, DPrimitive>();	

	
	/**
	 * Make a glyph corresponding to the codepoint given scaled to fit the baseline.
	 *
	 * @param codepoint The codepoint for the symbol.
	 * @param baseline The length of the baseline upon which it should fit.
	 * @return The rendering of th glyph.
	 */
	public static BufferedImage		makeGlyph(int codepoint, int baseline) {
 
		float scale  = ((float) baseline) / FONTMETRICS.charWidth((char)codepoint);

		int width  = baseline;
		int height = (int) scale * (FONTMETRICS.getHeight() - FONTMETRICS.getDescent());
				 
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g = image.createGraphics();

		g.setColor(Color.white);		
		g.fillRect(0 , 0, width, height);
		
		g.setFont(Toolbox.FONT);

		g.setColor(Color.black);				
		
		// characters are written upside down relative device coordinates
		g.drawString("" + (char) codepoint, FONTMETRICS.getLeading(), height - scale * FONTMETRICS.getDescent());
		
		return image;
	}
	
	/**
	 * The number of pixels to advance when typing a character, relative the current font metrics.  
	 *
	 * @param c The character.
	 * @return The number of pixels to advance.
	 */
 	public static int 				advance(char c) {
		return FONTMETRICS.charWidth(c);
	}		

 	
	/**
	 * Switch from one container to another in the gui of the application.
	 *
	 * @param to The container switched to, going to.
	 * @param from The container switched from, comming from.
	 */
	public static void 				switchContainer(Container to, Container from) {
		from.setVisible(false);
		from.repaint();
		to.setVisible(true);
		to.repaint();
	}
	
	
	/**
	 * Generate a new integer id for a new DComposite.
	 *
	 * @return int A new id.
	 */
	public static int 				nextCompositeId() {
		return COMPOSITEID--;
	}

	
	/**
	 * Find the statement containing a particular described primitive.
	 *
	 * @param find The described primtive to find.
	 * @param theorem The theorem to search.
	 * @return The described statement containing the described primitive or null if none found.
	 */
	public static DStatement 		findStatement(Described find, DTheorem theorem) {

 		boolean done = false;

 		DStatement s; Described d;
 		
 		theorem.reset();
 		
		s = theorem.current();

 		while (!theorem.currentIsLast()) {
 		 		
 			d = s.current();
 			
 			while (!s.currentIsLast()) {

 				if (d == find) return s;
 				d = s.next();
 			}

 			// is last primitive
 			d = s.current();
			if (d == find) return s;
 			
 		}

 		// is last statement
 		d = s.current();

 		while (!s.currentIsLast()) {

 			if (d == find) return s;
 			d = s.next();
 		}
 		
 		// is last primitive in last statement
 		d = s.current();
 		if (d == find) return s;

 		return null;		
 	}

	/**
	 * Ask the world which kind of mathemtaics a codepoint correspoonds to.
	 * 
	 * @param codepoint		utf codepoint of the mathematical glyph.
	 * 
	 * @return				The type this application categorises the glyph as.
	 */
	public static FormalType 		lookupType(int codepoint) {
		return FormalType.VARIABLE;		// for now
	}

	
	/**
	 * Filter for drawing selected items.
	 */
	public final static class 		SelectedFilter extends RGBImageFilter {

		/** {@inheritDoc} */
		public int filterRGB(int x, int y, int rgb) {
			
			return (rgb & 0x8888ff88);	// greenish
		}
	}
	
	/**
	 * Filter for drawing pressed items.
	 */
	public final static class 		PressedFilter  extends RGBImageFilter {

		/** {@inheritDoc} */
		public int filterRGB(int x, int y, int rgb) {
			
			return (rgb & 0x888888ff);	// blueish
		}
	}
	
	/**
	 * Filter for drawing composites.
	 */
	public final static class 		CompositeFilter  extends RGBImageFilter {

		/** {@inheritDoc} */
		public int filterRGB(int x, int y, int rgb) {
			
			return (rgb & 0xffaaffaa);
		}
	}
	
	
	/**
	 * Creates a text string representation of the described statement for storage in the data base.
	 *
	 * @param description The described statement.
	 * @return The text representation of the described statement.
	 */
	public static String 			parseToString(DStatement description) {

		String charsequence = "";
		
		Described implication = (description.isClosed()) ? description.removeLast() : null;
		
		for (Described described : description) 
			charsequence += (char) described.getCodepoint();
				
		if (description.isClosed()) description.addLast(implication);
		
		return charsequence;
	}
	
	/**
	 * Is name a sensible name to use for file naming. NOT PROPERLY IMPLEMENTED YET. 
	 *
	 * @param name The name of the file.
	 * @return Sensible or not.
	 */
	public static boolean 			isOkName(String name) {
			return name.length() > 3;
	}

	
	/**
	 * Describe individually a collection of primitives.
	 *
	 * @param formals The formals to make descriptions for.
	 * @return A new similar collection but where the primitives have descriptions. 
	 */
	public static DoubleArray<Described, Shortcut> 		describe(DoubleArray<Formal, Shortcut> formals) {

		DoubleArray<Described, Shortcut> describeds = new DoubleArray<Described, Shortcut>();
		
		for (Tuple<Formal, Shortcut> fpair : formals) {
						
			Described described = new DPrimitive(fpair.first()); 
			
			Tuple<Described, Shortcut> dpair = new Tuple<Described, Shortcut>(described, fpair.second());

			describeds.add(dpair);			
		}
		
		return describeds;
	}
	
	/**
	 * Removes individually the descriptions from the primitives in the collection.
	 *
	 * @param described The collection of described primitives.
	 * @return A new similar collection but with only the primitives.
	 */
	public static DoubleArray<Formal, Shortcut> 		formals(DoubleArray<Described, Shortcut> described) {

		DoubleArray<Formal, Shortcut> formals = new DoubleArray<Formal, Shortcut>();
		
		for (Tuple<Described, Shortcut> dpair : described) {
						
			Tuple<Formal, Shortcut> fpair = new Tuple<Formal, Shortcut>(dpair.first().value(), dpair.second());

			formals.add(fpair);			
		}
		
		return formals;
	}

	/**
	 * Retreive all formals that have a certain type from a map of bindings of formal values to keyboard short-cuts.
	 * 
	 * @param bijection		A bijective sortable mapping of bindings between formals and keyboard short-cuts.	
	 * @param type			The type of formals that should be filtered out.
	 * 
	 * @return A bijective sortable array of all such bindings having that type.
	 */
	public static DoubleArray<Formal, Shortcut> 		filterBindings(DoubleArray<Formal, Shortcut> bijection, Formal.FormalType type) {
		
		DoubleArray<Formal, Shortcut> selection = new DoubleArray<Formal, Shortcut>();
		
		for (Tuple<Formal, Shortcut> pair : bijection) 
			if (pair.first().getType() == type) selection.add(pair);

		return selection.size() > 0 ? selection : null;
	}

	/**
	 * Retreive all described formals that have composite value from a map of bindings of formal values to buttons.
	 * 
	 * @param buttons	The map of bindings between formal values and buttons.
	 * 
	 * @return			The collection of all composites represented by the buttons. 
	 */
	public static Collection<DComposite> 				collectComposites(HashMap<Formal, DButton> buttons) {

		ArrayList<DComposite> list = new ArrayList<DComposite>();

		for (Formal formal : buttons.keySet()) 
			if (formal instanceof Composite) 
				list.add((DComposite) buttons.get(formal).getDescribed());
		
		return list.size() > 0 ? list : null;
	}

	/**
	 * Retreive all described formals of a certain type from a map of bindings of formal values to buttons.
	 * 
	 * @param buttons	The map of bindings between formal values and buttons.
	 * @param type		The type of formals.
	 * 
	 * @return			The collection of all formals represented by the buttons having the given type. 
	 */
	public static Collection<Described> 				collectType(HashMap<Formal, DButton> buttons, Formal.FormalType type) {

		ArrayList<Described> list = new ArrayList<Described>();

		for (Formal formal : buttons.keySet()) 
			if (formal.getType() == type) 
				list.add(buttons.get(formal).getDescribed());
		
		return list.size() > 0 ? list : null;
	}

	
	/**
	 * Decrease an integer variable if it is positive and less than or equal to the upper bound.
	 * 
	 * @param decreased		The value to decrease if: 0 &lt;= value &lt;= upperbound.
	 * @param upperbound	The upper bound.
	 * 
	 * @return 				'value-1' if 'value' is within the bounds otherwise not changed, that is returns 'value'.
	 */
	public static int 				decreasePGE(int decreased, int upperbound) {

		int index = decreased;
		
		index = (index >= upperbound) ? ((index > 0) ? index-- : index) : index;
	
		return index;
	}

	/** 
	 * Returns the vector in opposite direction of the given vector. Vectors are represented by points here.
	 * 
	 * @param	vector	Vector to negate.
	 * 
	 * @return 	Vector in opposite direction.
	 */
	public static Point 			negate(Point vector) {
		return new Point(-vector.x, -vector.y);
	}

	
	private static int 				defaultBaselineLength() {
		
		int[] widths = FONTMETRICS.getWidths();
		
		int sum = 0;
		
		for (int i = 0; i < widths.length; i++) sum += widths[i];
		
		return (int) (sum / (float) widths.length);
	}
}
