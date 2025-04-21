/**
 * The description part of the model. D as in Described. Should function like a descriptive cloak hung on to the value
 * semantics in the logic package. Hung on to it by inheritance so DPrimitive extends Primitive and so on.<br><br>
 * 
 * This is descriptions in a broad sense and the graphical part is only one aspect. The graphical part is to high extent 
 * carried out by the {@link DRectangle} class which is a rectangle frame with an image in it and is stuffed with other 
 * functionality needed to display it and shuffle it around the application.<br><br>
 * 
 * Descriptions of composites does not follow the same implementation structure since primitives and composites is very
 * different in their semantics and therefore splits the inheritance. Compare {@link AbstractDescribed}
 * and {@link AbstractDComposite} in the {@link model.description.abstraction} package.
 * So the {@link Described} interface is redundantly implemented twice.<br><br>
 * 
 * @see DPrimitive
 * @see DComposite
 * @see DStatement
 * @see DTheorem
 * 
 * @see DRectangle
 * 
 * @author Anders Persson (perssonandersper@gmail.com)
 * @version 0.2	
 * @since 2024-07-07
 */
package model.description;
import model.description.abstraction.*;
