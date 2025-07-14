# deductionwriter

---- DeductionWriter - Math notetaking tool ----

 This application is now coming to form as a working prototype, most of the desired 
 functionality is there although almost everything needs tweaking and polishing.

 --- Comments on progress
 The first protype had an awful gui so it has been replaced all over. Another all
 over revision was when changing graphical objects to have double precision. Why 
 didn't I think of that from start. The zooming feature took a whole lot of time
 due to some bad and complicated anzatses but in the end it was really simple and 
 robust due to the position of the affine transform in java graphics. When changing
 underlying types the data base and it's interaction also has to be rewritten. This 
 brought up some worries but I have been able to continuosly expand it quite easy. 


 --- What to
 The application is for writing things with many symbols, primarily mathematics 
 but since there are no export/import functionality it is not so easy to put it
 into any context yet. My goal is anyway to solve the problem of clean writing 
 lecture notes, but another purpose is to develop a larger software and learn the 
 tools for that in real mode. 

 --- How to 
 The application should be easy to understand but any way...

  -- Main mode
   - point and click glyphs to use in a statement.
   - finalise a statement with an implication.
   - these two things can be carried out by keyboard key shortcuts and every glyph
     can be mapped to a key plus modifier. 
   - SPACE is reserved for the command new glyph.
   - CTRL + SHIFT + ARROW + {LEFT,UP,RIGHT} is reserved for finalising a statement.

  -- In the menus one can access other parts of the application
   - store/load theorems, sessions, primitive glyphs and composite glyphs.

  -- Side applications accesed via the menus
   - select and make menu of new glyphs.
   - put together composite glyphs.
   - map keyboard keys to glyphs and practices using them.

