/**
 * The logic part of the model. This contains only the abstract part of the entities worked with in the application.
 * This is what should meet the pen-and-paper mathematician when deriving theorems. <br><br>
 * 
 * The main point with this structure is to separate it from <i>everything else</i> as far as possible so
 * the first division of semantics is that between the <i>value</i> and it's <i>description</i>. The mathematics as
 * it is and that which is used for perceiving it.<br><br>
 * 
 * The most foundational entities are called <b>primitives</b> and they should rely fully on established mathematics which 
 * in turn are rigidly described by the UTF standardisation. So they are described only by their UTF codepoint.<br><br>
 * 
 * Next in increasing complexity is entities that consist of primitives and they are called <b>composites</b>. They are 
 * constructed by the user today but will be thoroughly structured later: there will be fractions, functions, sums and
 * products etc.<br><br>
 * 
 * These two then make upp statements and theorems.
 * 
 * @see Primitive
 * @see Composite
 * @see Statement
 * @see Theorem
 * 
 * @author Anders Persson (perssonandersper@gmail.com)
 * @version 0.2	
 * @since 2024-07-07
 */
package model.logic;
