package control;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import control.db.DeductionBase;
import model.description.DComposite;
import model.description.DTheorem;
import model.description.abstraction.Described;
import model.independent.DoubleArray;
import model.logic.abstraction.Formal;
import view.DeductionFrame;
import view.GlyphsPanel;
import view.components.DisplayCanvas;

import java.awt.Rectangle;

/**
 * This class holds big parts of both control and model part of the application. In other
 * words <i>this is more of an active session than probably expected</i>. It co-manouvers 
 * {@see DisplayCanvas}, {@see GlyphsPanel} and is the interface with {@see DeductionBase}.
 * 
 * Remember also that {@see DTheorem} sits tightly within {@see DeductionCanvas} and contitutes a
 * workpiece of the application's only {@see WorkList} which in large makes up the session.
 * 
 * <i>In brief: the session is a worklist of canvases with theorems in them and some panels that it 
 * updates according to what happens on the canvases.</i>
 */
public class Session {

		public String primitivestable = "default";
		public String compositestable = "default";

		private DeductionBase 	base;		
		private WorkList 		workings;
		private DisplayCanvas 	current;
		private GlyphsPanel		glyphs;
		

		private String 			name;
		private int 			id;
		
		private HashMap<String,Rectangle> windowstates;


		/**
		 * The session must have its data base from start and ande before something can happen
		 * in the modell it must be connected with it's panel. 
		 * @param base The data base describing the theorems. The descripton parts most often generated 
		 * 				on, in and by the canvas that contains the theorem. Except from composites which
		 * 				also have some layout information in the data base.
		 * @param name
		 * @see connectPanels
		 * @see DComposite
		 * @see Composite
		 * @see DisplayCanvas
		 * @see DTheorem
		 */
		public Session(DeductionBase base, String name) {
			this.base = base;
			this.name = name;
			if (this.base.contains(name, "Sessions", "name"))
				this.workings = base.fetchWorklist(name);
			else	
				this.workings = new WorkList();
			
			this.current = this.workings.get(0);
		}


		/**
		 * Index of the current working canvas and theorem.
		 * @return The index of the current canvas and theorem in work.
		 */
		public int index() {
			return workings.indexOf(current);
		}
		
		/**
		 * The worklist of this session.
		 * @return This sessions list of canvases each containing one theorem. 
		 */
		public List<DisplayCanvas> getWorks() {		
			return workings;
		}
		
		/**
		 * Return the one and only data base of this application instance. 
		 * @see DeductionBase
		 */
		public DeductionBase getBase() {
			return base;
		}
		
		/**
		 * The name of this session.
		 * @return The name of this session.
		 */
		public String getName() {
			return name;
		}
		
		/**
		 * Creates a new work couple, a canvas with a theorem and adds it last to this sessions list of works.
		 * @param name The name of this work piece, there are no other identification and for now no uniqueness is checked.
		 * @return The canvas describing a theorem. The theorem is within the canvas.
		 * @see WorkList
		 */
		public DisplayCanvas newWork(String name) {
		
			DisplayCanvas retur = new DisplayCanvas(name);

			workings.add(workings.size(),retur);
			
			this.current = retur;
			
			return retur;
		}

		public int removeWork(int tabindex) {
			int sessionindex = workings.indexOf(current);
			
			if (sessionindex!=tabindex) System.err.println("Session-tab mafunction!");
			
			workings.remove(tabindex--);
			
			tabindex = tabindex >= 0 ? tabindex : 0;
			
			this.setWork(tabindex);
			
			return tabindex;
		}

		public String setWork(int replaceindex) {
			current = workings.get(replaceindex);
			return current.getName();
		}

		
		/**
		 * Initiates this session's administrative (non model) part, the 
		 * user interface panels it imposes changes on.
		 * @param frame
		 */
		public void connectPanels(DeductionFrame frame) {
			this.glyphs = frame.getGlyphsPanel();
		}


		
		/**
		 * Returns the canvas currently visible for the user upon which theorems are derived.
		 * @return The canvas on which mathematcs are rendered, that is painted.
		 * @see Graphics.paint(Graphics g)
		 * @ses Canvas
		 */
		public DisplayCanvas getCurrentCanvas() {
			return current;
		}

		public void setCurrentCanvas(DisplayCanvas canvas) {
			this.current = canvas;
		}

				
		public void changeSessionName(String newname) {
			this.name = newname; 
		}

		
		/**
		 * Stores session in data base so that it can be fully restored later.
		 * @see DeductionBase
		 * @see WorkList
		 */
		public boolean saveSession() {
			return base.saveSession(this,workings.getDescription());	
		}
		
		/**
		 * Closes all and checks that current session is consistent with the database description of it. Exits anyhow, with 
		 * error message.
		 */
		public void closeSession() {

			base.closeDB();
			System.exit(0);

			// base.isConsistent is sketched only
//			if (base.isConsistent(this)) {	
//			} else {
//				// quit without saving dialog
//				System.err.println("DB is not consistent with session on leaving. Probably somone forgot saving. Quiting anyhow.");
//				System.exit(-1);
//			}
		}

		
		/** Adds primitives to this session and to the panel with glyphs.
		 * @param primitivestable The name of the table of primitives to fetch
		 * @see DeductionBase
		 * @see Primitive
		 */	
		public void loadPrimitives(String primitivestable) {

			this.primitivestable = primitivestable;
			
			DoubleArray<Formal, Shortcut> bindings = base.fetchPrimitives(primitivestable);

			this.unionPrimitiveBindings(bindings, primitivestable);
		}

		/** Adds composites to this session and to the panel with glyphs.
		 * @param compsitestable The name of the table of composites to fetch
		 * @see DeductionBase
		 * @see Composite
		 */	
		public void loadComposites(String compositestable) {
			
			this.compositestable = compositestable;

			DoubleArray<Described, Shortcut> composites = base.fetchComposites(compositestable);

			this.unionCompositeBindings(composites, compositestable);	
			
			composites = null;																													///(DGGF)
		}

		/**
		 * Stores the composites currently in use in DeductionBase. 
		 * 
		 * @param tablename	The name to store them under.
		 */
		public void storeComposites(String tablename) {

			Collection<DComposite> composites = glyphs.getComposites();
			
			base.insertCompositesTable(composites, tablename);
			
		}

		
		/**
		 * Add new bindings to the set of primitives in use. Only adds primitives not already there. It also sets 
		 * a new name for the table of primitives stored.  
		 * 
		 * @param bindings	The bindings to update with.
		 * @param viewname	The new name of the set (table) of primitives in use.
		 */
		public void unionPrimitiveBindings(DoubleArray<Formal, Shortcut> bindings, String viewname) {

			this.primitivestable = viewname;

			glyphs.unionBindings(bindings);	
			glyphs.generatePrimitiveButtons();
		}
		
		/**
		 * Add new bindings to the set of composites in use. Only adds composites not already there. It also sets 
		 * a new name for the table of composites stored.  
		 * 
		 * @param bindings	The bindings to update with.
		 * @param viewname	The new name of the set (table) of primitives in use.
		 */
		public void unionCompositeBindings(DoubleArray<Described, Shortcut> bindings, String viewname) {

			this.compositestable = viewname;

			glyphs.unionBindings(Toolbox.formals(bindings));		// values (everywhere)
			glyphs.addCompositesButtons(bindings);					// descriptions (permanent)
		}



		
		public String getTheoremsNames() {
			
			String concatenation = "";
			
			for (DisplayCanvas canvas : workings) 
				concatenation += canvas.getTheorem().getName() + ":";
			
			return concatenation;
		}
		
		public LinkedList<DTheorem> getTheorems() {
			
			LinkedList<DTheorem> theorems = new LinkedList<DTheorem>();
			
			for (DisplayCanvas canvas : workings) 
				theorems.addLast(canvas.getTheorem());
			
			return theorems;
		}
		
		public long consistencyNumber() {

			long sum = 0;
			
			for (DisplayCanvas canvas : workings) 
				sum += canvas.getTheorem().consistencyNumber();
			
			return sum;
		}
				
}
