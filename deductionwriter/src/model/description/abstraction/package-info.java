/**
 * This package provides the ground work for inheritance on the description side of the model.<br><br>
 * 
 * As on the logic side of the modell the purpose is to off-load the concreete classes of as much as 
 * possible so the constructor is what's missing for them to work properly. One should not have to use the 
 * abstract classes but instead could reimplement the {@see Describedl} interface. How far this works is not 
 * thought through yet.<br><br>
 * 
 * The class {@see Placeholder} is also put in this package instead of in the {@see model.independent} package
 * since it is so tighly knit to the other classes and the describing things.
 * 
 * @see Described
 * @see AbstractDescribed
 * @see AbstractDComposite
 * 
 * @see Placeholder
 * 
 * @author Anders Persson (perssonandersper@gmail.com)
 * @version 0.2	
 * @since 2024-07-07
 */
package model.description.abstraction;
