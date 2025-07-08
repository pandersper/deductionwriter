package control.statics;

import java.awt.Component;
import java.io.PrintStream;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import control.session.Shortcut;
import model.description.DComposite;
import model.description.DPrimitive;
import model.description.DRectangle;
import model.description.DStatement;
import model.description.DTheorem;
import model.description.abstraction.Described;
import model.description.abstraction.Placeholder;
import model.description.abstraction.Placeholder.Handle;
import model.independent.CyclicMap;
import model.independent.DoubleArray;
import model.independent.DoubleArray.Tuple;
import model.logic.Composite;
import model.logic.abstraction.Formal;
import model.logic.abstraction.Formal.FormalType;
import view.components.DButton;

/**
 * A box of tools used across the application.
 */
public class Toolbox {

	private static int 		COMPOSITEID 	= Integer.MAX_VALUE - (int)(Math.random() * 10000);

	private static double	TOLERANCE		= 0.01f; 	// percent of a pixel is ok

	
	public static boolean 	tolerance(double baseline1, double baseline2) {
		return Math.abs(baseline1 - baseline2) <= TOLERANCE;
	}

	/**
	 * Finds a button for a primitive given a description of that primitive.
	 *
	 * @param description The descritption to look for.
	 * @param buttons The array buttons of search.
	 * @return The button having the primitive looked for or null if no such was found.
	 */
	public static DButton 		findButton(DRectangle description, Component[] buttons) { 				/* NOT USED (YET)*/
	
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
	/**
	 * Find a particular described formal in the composite currently worked on.
	 *
	 * @param find The chosen
	 * @param components the components
	 * @return the primitive placeholder
	 */
	public static Placeholder	findConstituent(Described find, Collection<Placeholder> components) {  	/* NOT USED (YET) */
	
		for (Placeholder component : components) 
			if (component.described().getCodepoint() == find.getCodepoint())
				return component;
	
		return null;
	}	
	/**
	 * Find the statement containing a particular described primitive.
	 *
	 * @param find 		The described primtive to find.
	 * @param theorem 	The theorem to search.
	 * 
	 * @return 			The described statement containing the described primitive or null if none found.
	 */
	public static DStatement 	findStatement(Described find, DTheorem theorem) {


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
	 * @param codepoint		UTF codepoint of the mathematical glyph.
	 * 
	 * @return				The type this application categorises the glyph as.
	 */
	public static FormalType 	lookupType(int codepoint) {
		return FormalType.VARIABLE;		// for now
	}
	/**
	 * Generate a new integer id for a new DComposite.
	 *
	 * @return int 	A new id.
	 */
	public static int 			nextCompositeId() {
		return COMPOSITEID--;
	}
	/**
	 * Creates a text string representation of the described statement for storage in the data base.
	 *
	 * @param description 	The described statement.
	 * 
	 * @return 				The text representation of the described statement.
	 */
	public static String 		parseToString(DStatement description) {

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
	 * @param name 	The name of the file.
	 * 
	 * @return 		Sensible or not.
	 */
	public static boolean 		isOkName(String name) {
			return name.length() > 3;
	}
	
	/**
	 * Describe individually a collection of primitives.
	 *
	 * @param formals 	The formals to make descriptions for.
	 * 
	 * @return A new similar collection but where the primitives have descriptions. 
	 */
	public static DoubleArray<Described, Shortcut> 	describe(DoubleArray<Formal, Shortcut> formals) {

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
	 * @param described 	The collection of described primitives.
	 * 
	 * @return 				A new similar collection but with only the primitives.
	 */
	public static DoubleArray<Formal, Shortcut> 	formals(DoubleArray<Described, Shortcut> described) {

		DoubleArray<Formal, Shortcut> formals = new DoubleArray<Formal, Shortcut>();
		
		for (Tuple<Described, Shortcut> dpair : described) {
						
			Tuple<Formal, Shortcut> fpair = new Tuple<Formal, Shortcut>(dpair.first().value(), dpair.second());

			formals.add(fpair);			
		}
		
		return formals;
	}


	public static ArrayList<Described> 			glyphSequence(DTheorem theorem) {						 /* NOT USED (YET) */
		
		ArrayList<Described> all = new ArrayList<Described>();
		
		for (DStatement statement : theorem)
			for (Described described : statement) 
				if (described instanceof DPrimitive) 
					all.add(described);
				else 
					if (described instanceof DComposite) 
						for (Placeholder holder : ((DComposite) described).getConstituents().values()) 
							all.add(holder.described());		
		return all;
	}
	
	public static HashMap<Integer, Described> 	glyphMap(DTheorem theorem) {							 /* NOT USED (YET) */
		
		HashMap<Integer,Described> all = new HashMap<Integer,Described>();
		
		for (DStatement statement : theorem)
			for (Described described : statement) 
				if (described instanceof DPrimitive) 
					all.put(described.getCodepoint(),described);
				else 
					if (described instanceof DComposite) 
						for (Placeholder holder : ((DComposite) described).getConstituents().values()) 
							all.put(holder.described().getCodepoint(), holder.described());		
		return all;
	}
	
	/**
	 * Retreive all formals that have a certain type from a map of bindings of formal values to keyboard short-cuts.
	 * 
	 * @param bijection		A bijective sortable mapping of bindings between formals and keyboard short-cuts.	
	 * @param type			The type of formals that should be filtered out.
	 * 
	 * @return 	A bijective sortable array of all such bindings having that type.
	 */
	public static DoubleArray<Formal, Shortcut> 	filterBindings(DoubleArray<Formal, Shortcut> bijection, Formal.FormalType type) {
		
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
	public static List<DComposite> 					collectComposites(HashMap<Formal, DButton> buttons) {

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
	public static Collection<Described> 			collectType(HashMap<Formal, DButton> buttons, Formal.FormalType type) {		/* NOT USED (YET) */

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
	public static int 		decreasePGE(int decreased, int upperbound) {

		int index = decreased;
		
		index = (index >= upperbound) ? ((index > 0) ? index-- : index) : index;
	
		return index;
	}
	
	public static void 		sysinfodump() {

		
		/* System properties */
		System.out.println(" \n JAVA SYSTEM PROPERTIES \n");

		Properties properties = System.getProperties();
		
		properties.list(new PrintStream(System.out));
		
		/* Operating environment */
		System.out.println(" \n OPERATING SYSTEM ENVIRONMENT \n");

		Map<String,String> env = System.getenv();
		
		for (Map.Entry<String,String> kv : env.entrySet()) {
			System.out.println(kv.getKey() + ":\n\t" + kv.getValue());
		}

		
		/* Filesystem */		
		System.out.println(" \n FILESYSTEM INFO \n");

		
		LinkedList<FileSystemProvider> providers = new LinkedList<FileSystemProvider>(FileSystemProvider.installedProviders());
		
		for (FileSystemProvider fsp : providers) {	
			System.out.println("Scheme: " + fsp.getScheme() + "\t provided by \t" + fsp.toString());
		}		
	}

	public static CyclicMap<Handle, Placeholder> 		cyclicMap(Collection<Placeholder> holders) {

		CyclicMap<Handle, Placeholder> mapping = new CyclicMap<Handle, Placeholder>();
				
		for (Placeholder holder : holders)
			mapping.put(holder.handle(), holder);
				
		return mapping;
	}

	public static Placeholder 							findFrame(CyclicMap<Handle, Placeholder> components) {
		
		for (Handle key : components.keySet())
			if (key.depth == 0)
				return components.get(key);
		
		System.err.println("No frame found - map not properly constructed.");
		return null;	
	}		
}
