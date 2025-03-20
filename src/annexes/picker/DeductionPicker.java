package annexes.picker;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import control.Shortcut;
import control.Toolbox;
import control.db.DeductionBase;
import model.description.DPrimitive;
import model.description.abstraction.Described;
import model.independent.DoubleArray;
import model.independent.DoubleArray.Tuple;
import model.logic.abstraction.Formal;
import view.DeductionFrame;
import view.abstraction.AbstractFrame;
import view.components.DButton;

/**
 * The class DeductionPicker is a sub application for putting together glyphs for writing
 * mathematics and other deductions.
 */
public class DeductionPicker extends AbstractFrame implements ActionListener, ItemListener {
	
	private ArrayList<String>	categories 	= new ArrayList<String>();
	private HashSet<Formal> 	selected 	= new HashSet<Formal>();
	private DeductionBase 		base;
	private DeductionFrame		mainframe;
	private PickerDialog 		dialog;

	
	/**
	 * A DeductionWriter sub application for choosing which mathematics primitives and glyphs to use when writing theorems.
	 *
	 * @param base The data base containing theorems and their constituents.
	 */	
	public DeductionPicker(DeductionBase base)  {
		super("Pick primitives ...");
		
		this.base = base;		
		this.dialog = new PickerDialog(this, base);

		
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setBounds(100, 100, 1090, 800);
		
		contentPane.setBorder(new TitledBorder("Pick your primitives for further use in deductions"));
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		setContentPane(contentPane);		
		
		makeScrollPane(-1);				// overview panel
		
		mnuBar.add(overview);
		mnuBar.add(mnuCategories);
		contentPane.add(mnuBar);
						
		categories.add("default");
		categories.add("lowercase");
		categories.add("uppercase");
		categories.add("algebra");
		categories.add("logic");
		categories.add("fundamental_sets");	
						
		makeAndAddCheckBoxes();
		makeAndAddButtons();
		makePanesAndPanels();
		
		fillPaneByPrimitivesView("default", 0);
		fillPaneByPrimitivesView("lowercase", 1);
		fillPaneByPrimitivesView("uppercase", 2); 
		fillPaneByPrimitivesView("algebra", 3);
		fillPaneByPrimitivesView("logic", 4);
		fillPaneByPrimitivesView("fundamental_sets", 5); 
		
		checkboxes.get(0).setSelected(true);
		checkboxes.get(1).setSelected(false);
		checkboxes.get(2).setSelected(true);		

		contentPane.doLayout();
		contentPane.revalidate();
	}

	
	/**
	 * Store selected primitives in the data base.
	 *
	 * @param viewprefix What name to add as prefix when naming the table of primitives.
	 */
	public void storeInBase(String viewprefix) {		
		base.insertPrimitivesTable(selected, viewprefix);
	}
	
	/**
	 * Sets the sibling main frame of this sub application. Used in initialisation.
	 *
	 * @param mainframe The main DeductionWriter frame.
	 */
	public void setMainFrame(DeductionFrame mainframe) {
		this.mainframe = mainframe;
	}
	
	/**
	 * Update the overview panel and all structures it depends on.
	 */
	public void updateOverview() {
		
		overview.removeAll();
		
		for (Formal formal : selected) {
			
			DButton button = new DButton(new DPrimitive(formal));
			
			button.addActionListener(this);
			button.setSelected(true);
			button.setBorderPainted(true);

			overview.add(button);
		}
		
		overview.revalidate();
		overview.repaint();
	}
	
	/**
	 * Adds a database table of primitives to the currently selected primitives.
	 *
	 * @param viewname The name the table (sql view) of primitives to fetch from the data base.
	 */
 	public void addToSelected(String viewname) {
		
		DoubleArray<Described, Shortcut> bindings = Toolbox.describe(base.fetchPrimitives(viewname));
		
		ArrayList<Formal> view = new ArrayList<Formal>();

		for (Tuple<Described, Shortcut> pair : bindings)
			view.add(pair.first().value());
		
		this.addToSelected(view);
	}
	
	/**
	 * Clear selected primitives and everything it depends on.
	 */
	public void clearSelected() {

		selected.clear();
		
		overview.removeAll();
		overview.revalidate();
		overview.repaint();
	}

	/**
	 * Action performed. Receives buttons by their events, adds and removes them and also handles all other 
	 * button-triggered functionality. 
	 * 
	 * @param e	The action event.
	 */
	public void actionPerformed(ActionEvent e) {

		Object sender = e.getSource();
		
		if (sender instanceof DButton) {
			
			DButton button = (DButton) e.getSource();
			
			Formal value = button.getDescribed().value();
										 
			boolean removed = false; 

			if (selected.contains(value))  	
				removed = selected.remove(value);
			else 			
				selected.add(value);

			toggleInPanels(value);

			updateOverview();		
			
		} else {

			switch (e.getActionCommand()) {
			
				case "load-store":
					dialog.initialise();
					Toolbox.switchContainer(dialog, this);
					break;
		
				case "reset":
					this.setSelectedAll(false);
					this.clearSelected();
					break;
		
				case "quit":
					Toolbox.switchContainer(mainframe, this);
					break;
	
				default:					
					break;
			}
		}
		
	}
	
	/**
	 * Adds a collection of formals to the set of currently selected.
	 *
	 * @param addition 	The primitives to add to the selection.
	 */
	private void addToSelected(Collection<Formal> addition) {
		
		for (JScrollPane sp : scrollers) {
			
			Component component = sp.getViewport().getView();
			
			if (component instanceof JPanel) {
				
				JPanel jpanel = (JPanel) component;
			
				for (Component button : jpanel.getComponents()) {
					
					if (button instanceof DButton) {

						DButton dbutton = (DButton) button;
						
						Formal primitive = dbutton.getDescribed().value();

						if (addition.contains(primitive)) {
							
							this.selected.add(dbutton.getDescribed().value());

							dbutton.setSelected(true);
							dbutton.setBorderPainted(true);
						}
					} 
				}
				
			} else 		
				System.err.println("Unknown component in JScrollPane");	
		}		
	}

	/**
	 * Set all buttons selected state.
	 *
	 * @param selected 	If all button's state should be selected or unselected.
	 */
	private void setSelectedAll(boolean selected) {
		
		for (JPanel p : panels) {
			
			Component[] buttons = p.getComponents();
			
			for (Component c : buttons) {
				
				if (c instanceof DButton) {
					
					DButton b = (DButton) c;
					b.setSelected(selected);
					b.setBorderPainted(selected);
					
				} else {
					System.err.println("Unknown component.");
				}
			}	
		}
	}
	
	/**
	 * Toggle a particular primitive's button in all panels in this sub application.
	 *
	 * @param primitive The primitive who's button to toggle everywhere it occurs.
	 */
	private void toggleInPanels(Formal primitive) {
		
		for (JScrollPane sp : scrollers) {
			
			Component component = sp.getViewport().getView();
			
			if (component instanceof JPanel) {
				
				JPanel jpanel = (JPanel) component;
			
				for (Component c : jpanel.getComponents()) {
					
					if (c instanceof DButton) {

						DButton button = (DButton) c;
						
						Formal f = button.getDescribed().value();

						if (f.equals(primitive)) {
							button.setSelected(!button.isSelected());
							button.setBorderPainted(!button.isBorderPainted());					
						}
					} 
				}	
			} else {				
				System.err.println("Unknown component in JScrollPane");
			}			
		}
	}
	
	/**
	 * Fill a pane with primitives from a particular sql view.
	 *
	 * @param viewname 	The view (table) of primitives.
	 * @param paneindex Index of the pane to fill.
	 */
	private void fillPaneByPrimitivesView(String viewname, int paneindex) {

		DPrimitive fresh, old;
	
		ArrayList<Integer> codepoints = base.fetchCategory(viewname);
		
		for (int codepoint : codepoints) {
						
			fresh = new DPrimitive(codepoint);									// DOES NOT SET TYPE
						
			if (!Toolbox.GLYPHDICTIONARY.containsValue(fresh))
				old = Toolbox.GLYPHDICTIONARY.put((char) codepoint, fresh);
			else 
				old = null;
			
			
			if (old != null) {
				if (Toolbox.DEBUGVERBOSE) System.out.println("Replaced " + old.getCodepoint() + " with " + fresh.getCodepoint() + " in glyphdictionary.");
			}
			
			DButton button = new DButton(fresh);

			button.addActionListener(this);
			button.setBorderPainted(false);
						
			panels.get(paneindex).add(button);			
		}			
	}

	
	/* * * * * * * * * * AWT & SWING * * * * * * * * * */
	
	private JPanel 					contentPane = new JPanel();		
			
	private 		JPanel						 overview;
	private final	ArrayList<JPanel> 			 panels 		= new ArrayList<JPanel>();
	private final	ArrayList<JScrollPane>		 scrollers		= new ArrayList<JScrollPane>();
	private final 	ArrayList<JCheckBoxMenuItem> checkboxes 	= new ArrayList<JCheckBoxMenuItem>();
 
	private final JMenuBar 	mnuBar 			= new JMenuBar();	
	private final JMenu 	mnuCategories 	= new JMenu("Categories");

	private JButton btnExport, btnQuit, btnReset;
	

	/**
	 * Responds to changes in the menu.
	 * 
	 * @param e	Event generated by menu when changed.
	 */
	public void itemStateChanged(ItemEvent e) {

		JMenuItem item = (JMenuItem) e.getItem();

		int index = indexOf(item.getActionCommand(), categories);

		if (item.isSelected())			
			contentPane.add(scrollers.get(index));
		else
			contentPane.remove(scrollers.get(index));

		contentPane.doLayout();
		contentPane.revalidate();
	}

	
	private JScrollPane makeScrollPane(int index) {
		
		JScrollPane scroller = new JScrollPane();
		
		String name;
		
		if (index != -1) 
			name = categories.get(index);
		else
			name = "selected";
		
		int width  = contentPane.getWidth();
		int height = this.getHeight() / 4;
	
		scroller.setName("scr" + name);
		
		JPanel panel = new JPanel();
		panel.setName("pnl" + name);
	
		EtchedBorder etchedBorder = new EtchedBorder(EtchedBorder.LOWERED, null, null);
		
		panel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), 
										 name, TitledBorder.CENTER, TitledBorder.TOP, 
										 null, new Color(0, 0, 0)));
		panel.setBackground(Color.WHITE);
		panel.setLayout(new FlowLayout());
		panel.setPreferredSize(new Dimension(width - 40, 4 * height));
		
		if (index != -1)
			panels.add(panel);
		else
			this.overview = panel;
			
		scroller.setPreferredSize(new Dimension(width - 20, height));
		scroller.setViewportView(panel);		
		
		return scroller;
	}

	private void makeAndAddButtons() {

		btnExport = new JButton("Add or store");		
		btnExport.setActionCommand("load-store");
		btnExport.addActionListener(this);
		mnuBar.add(btnExport);

		btnReset = new JButton("Reset selection");		
		btnReset.setActionCommand("reset");
		btnReset.addActionListener(this);
		mnuBar.add(btnReset);

		btnQuit = new JButton("Quit");		
		btnQuit.setActionCommand("quit");
		btnQuit.addActionListener(this);
		mnuBar.add(btnQuit);
	}

	private void makePanesAndPanels() {

		for (int i = 0; i < categories.size(); i++) {

			JScrollPane pane = makeScrollPane(i);
			scrollers.add(pane);
		}
	}

	private void makeAndAddCheckBoxes() {

		for (int i = 0; i < categories.size(); i++) {

			JCheckBoxMenuItem item = new JCheckBoxMenuItem(categories.get(i));
			item.addItemListener(this);
			item.setActionCommand(categories.get(i));
			item.setName(categories.get(i));
			mnuCategories.add(item);
			checkboxes.add(item);
		}
	}


	private static int indexOf(String string, ArrayList<String> strings) {

		for (int i = 0; i < strings.size(); i++) {
			if (strings.get(i) == string)
				return i;
		}

		return -1;
	}

	
	/* * * * * * * * * * FOCUS TRAVERSAL * * * * * * * * * */
	
	/** {@inheritDoc} */
	public Container[] focusCycleRoots() {
		return new Container[] { this };
	}

	/** {@inheritDoc} */
	public Component[][] focusCycleNodes() {
		return new Component[][] { new Component[] { btnExport, btnReset }};
	}
	
	/** {@inheritDoc} */
	public void setDefaultComponent() {
		this.defaultcomponent = overview;		
	}

	/**
	 * Not in use.
	 * 
	 * @param columnvalue Not in use.
	 */
	public void loadPrimitives(String columnvalue) {
		// NOT IN USE		
	}

	/**
	 * Not in use.
	 * 
	 * @param columnvalue Not in use.
	 */
	public void loadComposites(String columnvalue) {
		// NOT IN USE		
	}

}
