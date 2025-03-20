/**
 * This package provides the ground work for inheritance on the value (logic) side of the model.<br><br>
 *  
 * The purpose is to off-load the concreete classes of as much as possible so that the constructor is 
 * what's missing for them to work properly. One should not have to use the abstract classes but
 * instead could reimplement the {@see Formal} interface. How far this works is not thought 
 * through yet.
 * 
 * @see Formal
 * @see AbstractFormal
 * @see AbstractComposite
 * 
 * @author Anders Persson (perssonandersper@gmail.com)
 * @version 0.2	
 * @since 2024-07-07
 */
package model.logic.abstraction;
