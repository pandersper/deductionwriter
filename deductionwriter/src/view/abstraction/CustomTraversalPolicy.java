package view.abstraction;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.util.Vector;

import view.components.DButton;

/**
 * A tree like traversal policy that is easily set up by giving all roots and all roots's cycle of nodes to traverse. 
 * @see Container
 * @see Component
 */
public class CustomTraversalPolicy extends FocusTraversalPolicy {

	private Vector<Vector<Component>> 	nodes = new Vector<Vector<Component>>();
	private Vector<Container> 			roots = new Vector<Container>();
	
	private int[] defaults;	

	
	/**
	 * Adds all roots and each root's focus traversal cycle. Remark: the length of roots vector must be the same of the nodes vector. 
	 * <i>null roots and empty cycles are not allowed</i>.
	 * @param roots The root of each cycle.
	 * @param nodes	The components to traverse in each cycle. 
	 */
	public void addComponents(Vector<Container> roots, Vector<Vector<Component>> nodes) {

		try { checkArguments(roots, nodes); } catch (IllegalArgumentException iae) { iae.printStackTrace(); }

		this.roots.addAll(roots);

		for (int i = 0; i < this.roots.size(); i++) {		
			this.nodes.add(i, new Vector<Component>());			
			this.nodes.get(i).addAll(nodes.get(i));				
		}

		defaults = new int[roots.size()];

		for (int i = 0; i < this.roots.size(); i++) this.defaults[i] = 0; 

	}

	
	/** {@inheritDoc} */
	public Component getComponentAfter(Container root, Component node) {
		return getComponent(root, node, false); 
	}
	/** {@inheritDoc} */
	public Component getComponentBefore(Container root, Component node) {
		return getComponent(root, node, true); 
	}
	/** {@inheritDoc} */
	public Component getFirstComponent(Container root) {

		int rootindex = roots.indexOf(root);

		if (rootindex == -1)
			throw new IllegalArgumentException("no such cycle root");

		Component first = nodes.get(rootindex).get(0);								// empty cycles allowed

		return (first != null) ? first : root;
	}
	/** {@inheritDoc} */
	public Component getLastComponent(Container root) {

		int rootindex = roots.indexOf(root);

		if (rootindex == -1)
			throw new IllegalArgumentException("no such cycle root");

		int cyclesize = nodes.get(rootindex).size();

		Component last = nodes.get(rootindex).get(cyclesize-1);																				///(25D3)

		return (last != null) ? last : root;
	}
	/** {@inheritDoc} */
	public Component getDefaultComponent(Container root) {

		int rootindex = roots.indexOf(root);			

		if (rootindex > -1) 
			return nodes.get(rootindex).get(defaults[rootindex]);
		else
			throw new IllegalArgumentException("no such cycle (root) for default node");
	}
	/** 
	 * Sets default focus component a focus root container.
	 * 
	 *  @param root				The focus root who's default component should be set.
	 *  @param defaultcomponent	The component to make the default one for the root container.
	 *  
	 *  @return The index of the node in the nodes vector.
	 */
	public int setDefaultComponent(Container root, Component defaultcomponent) { 	

		int rootindex = roots.indexOf(root);

		if (rootindex > -1) {

			int nodeindex = nodes.get(rootindex).indexOf(defaultcomponent);

			if (nodeindex > -1) {

				defaults[rootindex] = nodeindex;
				return defaults[rootindex];

			} else 
				throw new IllegalArgumentException("No such node in cycle to set as default");					
		} else 
			throw new IllegalArgumentException("No such cycle for default node");
	}

	private void setDefaultComponent(TraversablePanel root) {
		this.setDefaultComponent(root, root.defaultfocus);
	}
	/** 
	 * Sets default focus component for all the focus roots in the roots vector. 
	 */
	public void setAllDefaults() {

		for (Container root : roots) {

			if (root instanceof TraversablePanel)
				((TraversablePanel)root).setDefaultComponent();
		}
	}


	private Component getComponent(Container root, Component node, boolean forwardbackward) {

		if (node instanceof DButton)
			return null;

		int rootindex, nodeindex, direction;

		direction = forwardbackward ? 1 : -1;
		rootindex = roots.indexOf(root);			

		if (nodes.get(rootindex).size() == 0) return root;

		if (rootindex > -1) {

			int current = nodes.get(rootindex).indexOf(node);

			if (current == -1)
				throw new IllegalArgumentException("No such node in cycle");
			else
				nodeindex = (current + direction) % nodes.get(rootindex).size();																///(FG3C)

			if (nodeindex == -1) nodeindex = nodes.get(rootindex).size()-1; 

			return nodes.get(rootindex).get(nodeindex);

		} else 
			throw new IllegalArgumentException("No such root");
	}

	private void checkArguments(Vector<Container> roots, Vector<Vector<Component>> nodes) {

		if (roots.size() != nodes.size()) 
			throw new IllegalArgumentException("Cycle roots without cycle");

		for (int i = 0; i < roots.size(); i++) {																								///(G3C4)

			Vector<Component> check = nodes.get(i);

			for (int j = 0; j < check.size(); j++) 

				for (int k = j; k < check.size(); k++) 

					if (check.get(j) == check.get(k) && j != k)
						throw new IllegalArgumentException("Traversal cycles are not disjoint");
		}
	} 	
}
