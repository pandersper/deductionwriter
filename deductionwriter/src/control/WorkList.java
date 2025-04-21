package control;

import java.util.ArrayList;

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
public class WorkList extends ArrayList<DisplayCanvas> {

	
	private DoubleArray<Formal, Shortcut> bindings  = new DoubleArray<Formal, Shortcut>();
	private String description;

	/**
	 * A new empty list of only one empty canvas with an empty theorem together
	 * with an empty {@see DoubleArray} of bindings between described mathematic
	 * primitives and keyboard keys.		 
	 * @see DisplayCanvas	
	 * @see DTheorem
	 */
	public WorkList() {
		super();
		this.add(new DisplayCanvas());
		this.bindings = new DoubleArray<Formal,Shortcut>();
	}

	/**
	 * A new empty list of only one empty canvas with an empty theorem. It initiates
	 * this list of work with keybard key to mathematics bindings.
	 * @param bindings 	Bijective list of keyboard key to mathematics bindings.
	 * @see DisplayCanvas	
	 * @see DTheorem
	 */
	public WorkList(DoubleArray<Formal, Shortcut> bindings) {
		super();
		this.bindings = bindings;
	}
	
	/**
	 * A new work list initiated with both a set of canvases and the bindings for this session
	 * @param bindings 	Bijective list of keyboard key to mathematics bindings.
	 * @param tabs		List of canvases to be worked on in this session.
	 */
	public WorkList(DoubleArray<Formal, Shortcut> bindings, ArrayList<DisplayCanvas> tabs) {
		super(tabs);
		this.bindings = bindings;
	}

	public WorkList(ArrayList<DisplayCanvas> canvases,  DoubleArray<Formal, Shortcut> primitives, 
														DoubleArray<Described, Shortcut> composites, String description) {
		this.addAll(canvases);

		bindings.addAll(primitives);
		bindings.addAll(Toolbox.formals(composites));
		
		this.description = description;
	
	}

	
	/**
	 * The current bindings of this worklist session.
	 * @return The bindings.
	 */
	public DoubleArray<Formal, Shortcut> getBindings() {
			return bindings;
	}

	public String getDescription() {
		return description;
	}
}
