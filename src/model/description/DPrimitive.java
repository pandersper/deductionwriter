package model.description;

import control.statics.PaintStatics;
import control.statics.Toolbox;
import model.description.abstraction.AbstractDescribed;
import model.logic.Implication;
import model.logic.Implication.ImplicationType;
import model.logic.Primitive;
import model.logic.abstraction.Formal;

/**
 * A description object of a primitive mathematical object. This class relies except for constructors and clone 
 * very much on it's abstract base class.
 * 
 * @see Primitive
 * @see AbstractDescribed
 */
public class DPrimitive extends AbstractDescribed {

	/**
	 * To be used when empty positions are temporarily needed when editing.
	 */
	public final static DPrimitive DUMMY = dummy();
	
	private boolean			tpbg = false;

	
	/**
	 * Creates a description object for the mathematical value corresponding to a specific UTF codepoint.
	 * For example it could be an ordinary literal used as a variable but could also be an integral 
	 * operator corresponding to the character for the integral sign. 
	 *  
	 * @param onecharacter		String beginning with the specific character.
	 */
	public DPrimitive(String onecharacter) {
		this(Primitive.makeValue(onecharacter.codePointAt(0)));
	}

	/**
	 * Creates a description object for the mathematical value corresponding to a specific UTF codepoint.
	 * For example it could be an ordinary literal used as a variable but could also be an integral 
	 * operator corresponding to the character for the integral sign. 
	 *  
	 * @param codepoint		The UTF codepoint corresponding to this described mathematics.
	 */
	public DPrimitive(int codepoint) {
		this(Primitive.makeValue(codepoint));
	}
	
	/**
	 * Creates a description object of a mathematical value and the description are scaled to fit on the
	 * font baseline length given. The mathematical entity corresponds to an UTF codpoint. See 
	 * {@see DPrimitive(int)}
	 *  
	 * @param codepoint 	The UTF codepoint corresponding to this described mathematics.
	 * @param baseline		The baseline length that this mathematical description (symbol) should fit onto.
	 * @param transparent	If the descriptions background should be rendered transparent or not.
	 */
	public DPrimitive(int codepoint, double baseline, boolean transparent) {
		
		this.tpbg = transparent;
		
		super.description 	= new DRectangle(Primitive.makeValue(codepoint), baseline, this.tpbg);			

		commonConstructor(codepoint);
	}
	
	/**
	 * Creates a standard size description object to the mathematical formal value given. That formal corresponds 
	 * to an UTF codepoint.
	 *
	 * @param formal 		The object representing a formal piece of mathematics that should be described.
	 */
	public DPrimitive(Formal formal) {
		
		int codepoint = formal.getCodepoint();
		
		if (formal instanceof Implication) {

			ImplicationType type = ((Implication) formal).getImplicationType();

			super.description = new DRectangle(Implication.makeValue(type));
		}
		
		if (formal instanceof Primitive) 
			super.description = new DRectangle(Primitive.makeValue(codepoint));				
		
		this.commonConstructor(codepoint);
	}
	
	private void commonConstructor(int codepoint) {

		super.codepoint 	= codepoint;
		super.type			= Toolbox.lookupType(codepoint);
		super.name 			= super.description.value.getName();
	}

	
	/**
	 * Convenience method to reach the description glyphs baseline advance when drawn (written).
	 */
	public double getAdvance() {
		return description.advance;
	}
	
	
	/** 
	 * Quite deep clone meaning that is also clones it's description leaving only the image.
	 */
	public DPrimitive clone() {
		
		DPrimitive clone = new DPrimitive(this.getCodepoint(), this.getAdvance(), this.tpbg);	

		clone.description = this.description.clone();
		
		clone.setWritepoint(this.getWritepoint());
				
		return clone;
	}
	
	/** 
	 * Returns a clone that is scaled and has background transparency reset.
	 * 
	 * @param baseline		The length of the new base line.
	 * @param transparant	If background should be transparant or not.
	 */
	public DPrimitive scaledClone(double baseline, boolean transparent) {
				
		DPrimitive clone =  new DPrimitive(this.getCodepoint(), baseline, transparent);

		clone.setWritepoint(this.getWritepoint());
	
		return clone;
	}

	
	private static final DPrimitive dummy() {

		DPrimitive dummy = new DPrimitive(-1, (int) PaintStatics.AVERAGEADVANCE, false);
		
		dummy.description = DRectangle.DUMMYRECTANGLE.clone();
		
		dummy.description.value = Primitive.DUMMYFORMAL;
		
		dummy.type = FormalType.OTHER;
		
		dummy.name = "DUMMY";
		
		return dummy;
	}
}
