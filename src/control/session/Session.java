package control.session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.DefaultSingleSelectionModel;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import control.db.DeductionBase;
import control.statics.Toolbox;
import control.statics.ViewStatics;
import model.description.DComposite;
import model.description.DTheorem;
import model.description.abstraction.Described;
import model.independent.DoubleArray;
import model.logic.abstraction.Formal;
import view.DeductionFrame;
import view.components.DisplayCanvas;
import view.components.GlyphsPanel;
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
public class Session implements  ActionListener, ChangeListener {

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
		 * @param name
		 * @param base TODO
		 * @param base The data base describing the theorems. The descripton parts most often generated 
		 * 				on, in and by the canvas that contains the theorem. Except from composites which
		 * 				also have some layout information in the data base.
		 * @see connectPanels
		 * @see DComposite
		 * @see Composite
		 * @see DisplayCanvas
		 * @see DTheorem
		 */
		public Session(String name, DeductionBase base) {
			
			this.name 		= name;
			this.base		= base;
			this.workings 	= new WorkList();
			
			this.workings.addChangeListener(this);
			this.workings.setSelectedIndex(0);
			
			this.current 	= this.workings.getSelectedComponent();
		}
		

		private String 					getTheoremsNames() {
			
			String concatenation = "";
			
			for (DisplayCanvas canvas : workings.getCanvases()) 
				concatenation += canvas.getTheorem().getName() + ":";
			
			return concatenation.substring(0, concatenation.length()-1);
		}
		
		public LinkedList<DTheorem> 	getTheorems() {
			
			LinkedList<DTheorem> theorems = new LinkedList<DTheorem>();
			
			for (DisplayCanvas canvas : workings.getCanvases()) 
				theorems.addLast(canvas.getTheorem());
			
			return theorems;
		}

		
		public void 			fetchWorklist(String sessionname) {

			ArrayList<DisplayCanvas> 		canvases 	= base.fetchWorklist(sessionname);

			DoubleArray<Formal, Shortcut> 	primitives 	= base.fetchPrimitives(primitivestable);
			DoubleArray<Formal, Shortcut> 	composites 	= Toolbox.formals(base.fetchComposites(compositestable));
			
			DoubleArray<Formal, Shortcut> 	bindings 	= new DoubleArray<Formal, Shortcut>();
			
			bindings.addAll(primitives);
			bindings.addAll(composites);
			
			this.name = sessionname;			

			this.clearSesssion();

			workings.setBindings(bindings);
			
			for (DisplayCanvas canvas : canvases) {			
				
				workings.addTab(canvas.getTheorem().getName(), canvas);

				canvas.setSize(ViewStatics.canvasdimension);
				canvas.reset();
				canvas.describeTheorem();
			}
			
			workings.setSelectedIndex(workings.getTabCount()-1);			
		}
		/**
		 * The worklist of this session.
		 * @return This sessions list of canvases each containing one theorem. 
		 */
		public WorkList 		getWorks() {		
			
			return workings;
		}

		
		public void 			openWork(String theoremname) {

			DTheorem theorem = base.fetchTheorem(theoremname);

			newWork(theorem);
		}
		/**
		 * Creates a new work couple, a canvas with a theorem and adds it last to this sessions list of works.
		 * 
		 * @param name 		The name (String) for a new empty theorem or an existing theorem (DTheorem) for the canvas to display.
		 * 
		 * @return 	The canvas describing a theorem. The theorem is within the canvas.
		 * 
		 * @see WorkList
		 */
		public void 			newWork(DTheorem theorem) {
		
			DisplayCanvas newcanvas = new DisplayCanvas(theorem);
			
			workings.addTab(theorem.getName(), newcanvas);
						
			setupCanvasAndTheorem(newcanvas);
		}

		public DisplayCanvas 	replaceCurrentWork(DTheorem theorem) {
			
			DisplayCanvas newcanvas = new DisplayCanvas(theorem);
							
			int selected = workings.getSelectedIndex();
			
			workings.setComponentAt(selected, newcanvas);
			workings.setTitleAt(selected, theorem.getName());
						
			DisplayCanvas previous = this.current;
			
			setupCanvasAndTheorem(newcanvas);

			return previous;
		}
		 
		public int 				removeWork() {

				int selected = workings.getSelectedIndex();
				
				workings.removeTabAt(selected);
				
				selected = (selected > 0) ? selected-1 : 0;

				workings.setSelectedIndex(selected);

				DisplayCanvas newcanvas = workings.getSelectedComponent();

				setupCanvasAndTheorem(newcanvas);

				return selected;
		}

		
		private void 			setupCanvasAndTheorem(DisplayCanvas newcanvas) {

			this.current = newcanvas;
			
			workings.setSelectedComponent(this.current);

			this.current.reset();
			this.current.describeTheorem();
			
			glyphs.updateButtonsListener(this.current);					

			this.restoreFocus();
		}	
		
		/**
		 * The name of this session.
		 * @return The name of this session.
		 */
		public String 			getName() {
			
			return name;
		}
		/**
		 * Returns the canvas currently visible for the user upon which theorems are derived.
		 * @return The canvas on which mathematcs are rendered, that is painted.
		 * @see Graphics.paint(Graphics g)
		 * @ses Canvas
		 */
		public DisplayCanvas 	getCurrentCanvas() {
			
			return current;
		}
		/**
		 * Return the one and only data base of this application instance. 
		 * @see DeductionBase
		 */
		public DeductionBase 	getBase() {
			
			return base;
		}

		
		public void 	changeSessionName(String newname) {
			
			this.name = newname; 
		}
		/**
		 * Stores session in data base so that it can be fully restored later.
		 * @see DeductionBase
		 * @see WorkList
		 */
		public boolean 	saveSession() {
			
			return base.insert(this);	
		}
		/**
		 * Closes all and checks that current session is consistent with the database description of it. Exits anyhow, with 
		 * error message.
		 */
		public void 	closeSession() {
			
			base.closeDB();
		}

		public void 	clearSesssion() {
			
			workings.removeAll();
			
			id = -1;
			name = "empty";
			current = null;

			primitivestable = "default";
			compositestable = "default";
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
			
			//composites = null;																													///(DGGF)
		}
		/**
		 * Stores the composites currently in use in DeductionBase. 
		 * 
		 * @param compositestable	The name to store them under.
		 */
		public void storeComposites(String compositestable) {

			Collection<DComposite> composites = glyphs.getComposites();
			
			base.insert(composites, compositestable);
		}

		public void deleteComposites(String compositestable) {
			
			if (base.contains(compositestable, "Composites", "glyphtablename")) 
				base.delete(compositestable, "Composites", "glyphtablename");
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

		
		public String getSQLInsertString() {
			
			String sql = "";
			
			sql += "'" + name + "',";
			
			sql += "'" + this.getTheoremsNames() + "',";
			
			sql += "'" + this.primitivestable + "',";
			
			sql += "'" + this.compositestable + "',";
			
			sql += "'" + this.getWorks().getDescription() + "'";	
			
			return "(" + sql + ")";			
		}

		public String getSQLUpdateString() {
			
			String sql = "";
			
			sql += "'theorems'='" + this.getTheoremsNames() + "', ";
			sql += "'primitivestable'='" + this.primitivestable + "', ";
			sql += "'compositestable'='" + this.compositestable + "', ";
			sql += "'description'='" + this.workings.getDescription() + "'";
			
			
			return sql;
		}

		
		public void restoreFocus() {
			this.current.repaint();
			if (glyphs != null) glyphs.restoreFocus();
		}

		
		public void actionPerformed(ActionEvent e) {
			
			String command = e.getActionCommand();
			
			switch (command) {
			
				case "remove":
					
					if (workings.getTabCount() > 1 && workings.isSelected()) {

						int confirmation = JOptionPane.showConfirmDialog(glyphs,"Ok to drop theorem?",
																						"Pieces of art",JOptionPane.OK_CANCEL_OPTION);
						
						if (confirmation == JOptionPane.CANCEL_OPTION) return;

						this.removeWork();
						
					} 
					
					break;
					
				case "new":
					
					String name = JOptionPane.showInputDialog("Name of new theorem?");

					name = (name == "" || name == null) ? "empty" : name;
					
					this.newWork(new DTheorem(name));
					
					break;

				default:
					
					System.err.println("Unreachable default in Session.");
					
					break;
			}
		}
		
		/**
		 * Updates this panel when any change has occured the motivates updating. It is mostly called automatically
		 * by the selection model of the tabs component but also directly in some methods for now.
		 * @see DefaultSingleSelectionModel
		 * @see Component
		 * @param ce
		 */
		public void stateChanged(ChangeEvent ce) {
			
			if (workings.isSelected()) {		

				this.current = workings.getSelectedComponent();
				
				glyphs.updateButtonsListener(this.current);
				glyphs.restoreFocus();
				
				this.current.repaint();
			}		
			
			workings.invalidate();
		}


		public GlyphsPanel makeGlypsPanel(DeductionFrame parent) {

			this.glyphs = new GlyphsPanel(parent);
			
			return this.glyphs;
		}
}
