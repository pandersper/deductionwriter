package model.description;

import control.Toolbox;
import model.description.abstraction.AbstractDescribed;
import model.logic.Implication;
import model.logic.Implication.ImplicationType;
import model.logic.Primitive;
import model.logic.abstraction.Formal;

/**
 * A description object of a primitive mathematical object. This class relies except for constructors and clone 
 * totally on it's abstract base class.
 * 
 * @see Primitive
 * @see AbstractDescribed
 */
public class DPrimitive extends AbstractDescribed {

	
	/**
	 * Creates a description object to the mathematical value corresponding to a specific UTF codepoint.
	 * For example it could be an ordinary literal used as a variable but could also be an integral 
	 * operator corresponding to the character for the integral sign. 
	 *  
	 * @param codepoint		The UTF codepoint corresponding to this described mathematics.
	 */
	public DPrimitive(int codepoint) {
		
		commonConstructor(codepoint);

		super.description 	= new DRectangle(Primitive.makeValue(codepoint));	
		super.scale 		= 1.0f;		
	} 	
	
	/**
	 * Creates a description object to the mathematical value given by a formal object. That formal object is
	 * always in correspondance to an UTF codepoint.
	 *
	 * @param formal 		The object representing a formal piece of mathematics.
	 */
	public DPrimitive(Formal formal) {
		
		int codepoint = formal.getCodepoint();
		
		if (formal instanceof Implication) {

			ImplicationType type = ((Implication) formal).getImplicationType();

			commonConstructor(codepoint);
			
			super.description = new DRectangle(Implication.makeValue(type));
			super.scale 		= 1.0f;				
		}
		
		if (formal instanceof Primitive) {
			
			this.commonConstructor(codepoint);
			super.description 	= new DRectangle(Primitive.makeValue(codepoint));	
			super.scale 		= 1.0f;				
		}
	}
	
	/**
	 * Creates a description object of a mathematical value and the description are scaled to fit on the
	 * font baseline length given. The mathematical entity corresponds to an UTF codpoint. See 
	 * {@see DPrimitive(int)}
	 *  
	 * @param codepoint 	The UTF codepoint corresponding to this described mathematics.
	 * @param baseline		The baseline length that this mathematical description (symbol) should fit onto.
	 */
	public DPrimitive(int codepoint, int baseline) {
		
		commonConstructor(codepoint);
		
		super.description 	= new DRectangle(Primitive.makeValue(codepoint), baseline);			
		super.scale 	  	= this.description.height / (float) Toolbox.FONTMETRICS.getHeight();		

		assert(baseline == this.description.getAdvance());
	} 	

	
	private void commonConstructor(int codepoint) {

		super.codepoint 	= codepoint;
		super.type			= Toolbox.lookupType(codepoint);
		super.name 			= Character.isUnicodeIdentifierPart(codepoint) ? Character.getName(codepoint) : "<"+codepoint+">";
	}


	/** {@inheritDoc} */
	public DPrimitive clone() {
		
		DPrimitive clone = new DPrimitive(this.getCodepoint(), this.description.getAdvance());

		return clone;
	}																																		
}
