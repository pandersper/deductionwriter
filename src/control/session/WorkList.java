package control.session;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;

import javax.swing.DefaultSingleSelectionModel;
import javax.swing.JTabbedPane;
import javax.swing.SingleSelectionModel;
import javax.swing.border.LineBorder;

import control.statics.Toolbox;
import control.statics.ViewStatics;
import model.description.DTheorem;
import model.description.abstraction.Described;
import model.independent.DoubleArray;
import model.logic.abstraction.Formal;
import view.components.DisplayCanvas;

/**
 * List containg canvases with a theorem each being the list of user works of
 * a session. 
 * @see DisplayCanvas	
 * @see Session
 */
public class WorkList extends JTabbedPane implements SingleSelectionModel {
	
	private DoubleArray<Formal, Shortcut> bindings  = new DoubleArray<Formal, Shortcut>();
	private String description;
	
	private DefaultSingleSelectionModel model = new DefaultSingleSelectionModel();
	
	/**
	 * A new empty list of only one empty canvas with an empty theorem together
	 * with an empty {@see DoubleArray} of bindings between described mathematic
	 * primitives and keyboard keys.		
	 *  
	 * @see DisplayCanvas	
	 * @see DTheorem
	 */
	public WorkList() {
		super();
		
		setBackground(ViewStatics.floralwhite);
		setBorder(new LineBorder(Color.black, 1));
		setDoubleBuffered(true);

		this.setModel(model);
		
		super.addTab("empty", new DisplayCanvas(new DTheorem("empty")));
		
		model.setSelectedIndex(0);

		this.bindings = new DoubleArray<Formal,Shortcut>();
	}
	/**
	 * A new empty list of only one empty canvas with an empty theorem. It initiates
	 * this list of work with keybard key to mathematics bindings.
	 * 
	 * @param bindings 	Bijective list of keyboard key to mathematics bindings.
	 * 
	 * @see DisplayCanvas	
	 * @see DTheorem
	 */
	public WorkList(DoubleArray<Formal, Shortcut> bindings) {
		this();
		this.bindings = bindings;
	}	
	/**
	 * A new work list initiated with both a set of canvases and the bindings for this session
	 * 
	 * @param bindings 	Bijective list of keyboard key to mathematics bindings.
	 * @param canvases		List of canvases to be worked on in this session.
	 */
	public WorkList(DoubleArray<Formal, Shortcut> bindings, ArrayList<DisplayCanvas> canvases) {
		super();
		
		for (DisplayCanvas canvas : canvases)
			this.addTab(canvas.getTheorem().getName(), canvas);

		model.setSelectedIndex(canvases.size()-1);

		this.bindings = bindings;
	}
	/**
	 * A new work list initiated with both a set of canvases and the bindings for this session
	 * 
	 * @param bindings 	Bijective list of keyboard key to mathematics bindings.
	 * @param canvases		List of canvases to be worked on in this session.
	 */
	public WorkList(ArrayList<DisplayCanvas> canvases,  DoubleArray<Formal, Shortcut> primitives, 
														DoubleArray<Described, Shortcut> composites, String description) {

		for (DisplayCanvas canvas : canvases)
			this.addTab(canvas.getTheorem().getName(), canvas);

		model.setSelectedIndex(canvases.size()-1);

		bindings.addAll(primitives);
		bindings.addAll(Toolbox.formals(composites));
		
		this.description = description;
	}

	
	/**
	 * The current bindings of this worklist session.
	 * @return The bindings.
	 */
	public DoubleArray<Formal, Shortcut> 	getBindings() {
			return bindings;
	}
	/**

	 * Sets the current bindings of this worklist session.
	 * @param The bindings.
	 */
	public void 							setBindings(DoubleArray<Formal, Shortcut> bindings) {
			this.bindings = bindings;
	}

	/**
	 * Returns the description string, which might contain line delimiters and other special characters.
	 * 
	 * @return	The full mmulti line description of this list of ongoing works. 
	 */
	public String 			getDescription() {
		return description;
	}
	/**
	 * Appends a string to the description of this worklist.
	 * 
	 * @param string	Addition to the description.
	 */
	public void 			appendToDescription(String string) {	
		description += string;
	}

	/**
	 * Return the canvas currently worked on.
	 */
	public DisplayCanvas 	getSelectedComponent() {
		return (DisplayCanvas) super.getSelectedComponent();
		
	}
	/**
	 * Just delegates, continues on to underlying model.
	 * @see DefaultSingleSelectionModel#getSelectedIndex()
	 */
	public int 				getSelectedIndex() {
		return model.getSelectedIndex();
	}
	/**
	 * Just delegates, continues on to underlying model.
	 * @see DefaultSingleSelectionModel#setSelectedIndex()
	 */
	public void 			setSelectedIndex(int index) {
		model.setSelectedIndex(index);
	}
	/**
	 * Just delegates, continues on to underlying model.
	 * @see DefaultSingleSelectionModel#isSelected()
	 */
	public boolean 			isSelected() {
		return model.isSelected();
	}
	/**
	 * Just delegates, continues on to underlying model.
	 * @see DefaultSingleSelectionModel#clearSelection()
	 */	
	public void clearSelection() {
		model.clearSelection();
		
	}	
	/**
	 * Exports all the canvases in the work list.
	 * @return
	 */
	public DisplayCanvas[] 	getCanvases() {
		
		int n = this.getTabCount();
		
		DisplayCanvas[] canvases = new DisplayCanvas[n];

		Component[] cs = this.getComponents();
		
		for (int i = 0; i < n; i++) 
			canvases[i] = (DisplayCanvas) cs[i];
	
		return canvases;
	}
}
