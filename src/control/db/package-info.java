/**
 * The applications data base which stores the following tables: primitives, composites, statments and theorems.<br><br>
 * 
 * The three latter makes a lot of references to the first one by utf codepoint integers and those are well standardised. 
 * Theorems also reference statements with generated integer identification numbers. Composites are constructed by the user
 * and is therefore bound to have their own representation form. They are stored as codepoints together with their baselines 
 * consisting of coordinates to the beginning of it, the referencepoint, together with it's length.
 *  
 * The database also maintains views for easier access. for example to all theorem names.<br><br>
 * 
 * @see java.awt.FontMetrics
 * 
 * 
 * @author Anders Persson (perssonandersper@gmail.com)
 * @version 0.2	
 * @since 2024-07-07
 */
package control.db;
