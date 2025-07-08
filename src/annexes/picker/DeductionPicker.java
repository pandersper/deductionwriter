package annexes.picker;

import java.awt.FlowLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.SystemColor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.UIManager;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.border.TitledBorder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import control.session.Session;
import control.session.Shortcut;
import control.statics.PaintStatics;
import control.statics.Toolbox;
import control.statics.ViewStatics;
import model.description.DPrimitive;
import model.description.abstraction.Described;
import model.independent.DoubleArray;
import model.independent.DoubleArray.Tuple;
import model.logic.abstraction.Formal;
import view.DeductionFrame;
import view.abstraction.AbstractFrame;
import view.abstraction.InitiableContainer;
import view.components.DButton;

public class DeductionPicker<C extends Container & InitiableContainer> extends AbstractFrame implements ActionListener, ItemListener {

	private DeductionFrame		mainframe;
	private PickerDialog<C>		summarydialog;
	private UTFDialog			utfdialog;
	
	private HashSet<Formal> selected = new HashSet<Formal>();	

	//start_win_var_init
	private final JPanel 		pnlUpper 		= new JPanel();
	private final JPanel 		pnlControl 		= new JPanel();
	private final JPanel 		pnlCategories	= new JPanel();	
	private final JScrollPane 	scrCategories 	= new JScrollPane();
	
	private JButton btnExport, btnQuit, btnReset;

	private final JMenuBar 	mnuBar 			= new JMenuBar();
	private final JMenu 	mnuCategories	= new JMenu("Glyph categories");
	private final JMenu 	mnuCodepoint	= new JMenu("Custom UTF codes");

	private final JMenuItem itmCodepoint 	= new JMenuItem("Add by code ...");

	private final ArrayList<JCheckBoxMenuItem> 	 categories 		= new ArrayList<JCheckBoxMenuItem>();
	private final ArrayList<String> 			 categorynames 		= new ArrayList<String>();
	private final ArrayList<JPanel> 			 categorypanels 	= new ArrayList<JPanel>();
	private final ArrayList<JScrollPane>		 categoryscrollers	= new ArrayList<JScrollPane>();
	private final ArrayList<JCheckBoxMenuItem> 	 categorychkbxs 	= new ArrayList<JCheckBoxMenuItem>();

	private void makeUpper(JScrollPane scrOverview) {
		
		scrOverview.setMinimumSize(ViewStatics.pckpnlminsize);
		scrOverview.setMaximumSize(ViewStatics.pckpnlmaxsize);

		scrOverview.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrOverview.setViewportView(pnlUpper);

		pnlUpper.setMaximumSize(ViewStatics.pckpnlmaxsize); 
		pnlUpper.setMinimumSize(ViewStatics.pckpnlminsize); 		
		pnlUpper.setPreferredSize(ViewStatics.pckpnlminsize);

		TitledBorder titledBorder = new TitledBorder(UIManager.getBorder("ScrollPane.border"), "chosen", 
														TitledBorder.CENTER, TitledBorder.TOP, null, new Color(51, 51, 51)); 	
		pnlUpper.setBorder(titledBorder);
		pnlUpper.setBackground(ViewStatics.floralwhite);
	}

	private void makeLower(JPanel pnlLower) {

		pnlCategories.setLayout(new BoxLayout(pnlCategories, BoxLayout.Y_AXIS));

		scrCategories.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrCategories.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		scrCategories.setViewportView(pnlCategories);
		
		pnlControl.setLayout(new BoxLayout(pnlControl,BoxLayout.Y_AXIS));	
		pnlControl.setBackground(SystemColor.control);

		pnlLower.setLayout(new BoxLayout(pnlLower, BoxLayout.LINE_AXIS));
		pnlLower.add(scrCategories);
		pnlLower.add(pnlControl);
	}

	private void makeAndAddButtons() {

		btnReset 	= DeductionPicker.makeControlButton("reset","reset",this);		
		btnExport 	= DeductionPicker.makeControlButton("store","store-load-add",this);
		btnQuit 	= DeductionPicker.makeControlButton("quit","quit",this);		
				
		pnlControl.add(btnReset);
		pnlControl.add(btnExport);
		pnlControl.add(btnQuit);
	}

	private void makePanesAndPanels() {

		for (int i = 0; i < categories.size(); i++) {

			JScrollPane pane = makeScrollPane(i);
			categoryscrollers.add(pane);
		}
	}

	private JScrollPane makeScrollPane(int index) {
		
		JScrollPane scroller = new JScrollPane();
		JPanel 		panel 	 = new JPanel();

		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);		
		String name = categorynames.get(index);
	
		scroller.setName("scr" + name);	
		panel.setName("pnl" + name);
			
		panel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), 
										 name, TitledBorder.CENTER, TitledBorder.TOP, 
										 null, new Color(0, 0, 0)));
		panel.setBackground(ViewStatics.floralwhite);
		panel.setLayout(new FlowLayout());
		

		panel.setPreferredSize(new Dimension(400,240));
		
		categorypanels.add(panel);

		scroller.setPreferredSize(new Dimension(400,160));
		scroller.setMinimumSize(new Dimension(50,80));

		scroller.setViewportView(panel);		
		
		return scroller;
	}

	private void makeAndAddCheckBoxes(ArrayList<String> names) {

		String name;
		JCheckBoxMenuItem item;
		
		for (int i = 0; i < names.size(); i++) {

			name = names.get(i);
			item = new JCheckBoxMenuItem(name);
			
			item.setActionCommand(name);
			item.setName(name);
			item.addItemListener(this);
			
			categories.add(item);
			mnuCategories.add(item);
			categorychkbxs.add(item);
		}
	}

	//end_win_var_init

	/**
	 * Sub application for choosing glyphs out of the UTF-8 character set to
	 * use in the main application DeductionWriter.
	 */
	public DeductionPicker(Session session) {
		super("Pick primitives ...");
		
		this.session = session;
		this.categorynames.addAll(ViewStatics.CATEGORIES);	
		this.summarydialog  = new PickerDialog<C>(this,this.session);
		this.utfdialog = new UTFDialog(this);
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setBounds(100, 100, 640, 560);	

		
		JPanel 		pnlLower 	= new JPanel();
		JPanel 		contentPane = new JPanel();		
		JMenuBar 	menuBar 	= new JMenuBar();
		JScrollPane scrOverview = new JScrollPane();

		this.setJMenuBar(menuBar);
		menuBar.add(mnuCategories);
		menuBar.add(mnuCodepoint);

		itmCodepoint.addActionListener(this);
		itmCodepoint.setActionCommand("custom");
		
		mnuCodepoint.add(itmCodepoint);
		
		makeAndAddCheckBoxes(categorynames);		
		makeUpper(scrOverview);				
		makeLower(pnlLower);
		makeAndAddButtons();
		
		this.setContentPane(contentPane);		
		
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));	
		contentPane.add(scrOverview);	
		contentPane.add(pnlLower);
		contentPane.setBorder(new EmptyBorder(2, 2, 2, 2));

		this.makePanesAndPanels();
		this.setupCategoryPanes();		
	}

	
	public void actionPerformed(ActionEvent e) {
		Object sender = e.getSource();

		if (sender instanceof DButton) {

			DButton button = (DButton) e.getSource();

			Formal value = button.getDescribed().value();

			if (selected.contains(value))
				selected.remove(value);
			else

			toggleInPanels(value);
			selected.add(value);

			updateOverview();

		} else {

			switch (e.getActionCommand()) {

				case "store-load-add":
					summarydialog.initialise();
					ViewStatics.switchContainer(summarydialog, this);
					break;
	
				case "custom":
						ViewStatics.switchContainer(utfdialog, this);
						break;
				case "reset":
					this.setSelectedAll(false);
					this.clearSelected();
					break;
	
				case "quit":
					ViewStatics.switchContainer(mainframe, this);
					break;
	
				default:
					break;
			}
		}
	}
	
	public void itemStateChanged(ItemEvent e) {

		JMenuItem item = (JMenuItem) e.getItem();
		
		String command = item.getActionCommand();
		
		int index = indexOf(command, categorynames);

		if (item.isSelected()) 			
			pnlCategories.add(categoryscrollers.get(index));
		else
			pnlCategories.remove(categoryscrollers.get(index));

		pnlCategories.doLayout();
		pnlCategories.revalidate();
	}	

	

	public void addCustomGLyph(int codepoint) {

		DPrimitive fresh = new DPrimitive(codepoint);
		DPrimitive old;
		
		if (!PaintStatics.GLYPHDICTIONARY.containsValue(fresh))
			old = PaintStatics.GLYPHDICTIONARY.put((char) codepoint, fresh);
		else 
			old = null;
				
		
		DButton button = new DButton(fresh);

		button.addActionListener(this);
		button.setBorderPainted(false);
					
		int index = categorynames.indexOf("custom");

		categorypanels.get(index).add(button);			
	}
	/**
	 * Update the overview panel and all structures it depends on.
	 */
	public void updateOverview() {
		
		pnlUpper.removeAll();
		
		for (Formal formal : selected) {
			
			DButton button = new DButton(new DPrimitive(formal));
			
			button.addActionListener(this);
			button.setSelected(true);
			button.setBorderPainted(true);

			pnlUpper.add(button);
		}
		
		pnlUpper.revalidate();
		pnlUpper.repaint();
	}
	
	/**
	 * Store selected primitives in the data base.
	 *
	 * @param viewprefix What name to add as prefix when naming the table of primitives.
	 */
	public void storeInBase(String viewprefix) {		
		session.getBase().insert(new ArrayList<Formal>(selected), viewprefix);
	}
	/**
	 * Adds a database table of primitives to the currently selected primitives.
	 *
	 * @param viewname The name the table (sql view) of primitives to fetch from the data base.
	 */
 	public void addToSelected(String viewname) {
		
		DoubleArray<Described, Shortcut> bindings = Toolbox.describe(session.getBase().fetchPrimitives(viewname));
		
		ArrayList<Formal> view = new ArrayList<Formal>();

		for (Tuple<Described, Shortcut> pair : bindings)
			view.add(pair.first().value());
		
		this.addToSelected(view);
	}
	/**
	 * Sets the sibling main frame of this sub application. Used in initialisation.
	 *
	 * @param mainframe The main DeductionWriter frame.
	 */
	public void setMainFrame(DeductionFrame mainframe) {
		this.mainframe = mainframe;
	}
		
	private void setupCategoryPanes() {
		
		int i = 0;

		for (String category : categorynames)
			fillPaneByPrimitivesView(category, i++);
		
		categorychkbxs.get(0).setSelected(true);
		categorychkbxs.get(1).setSelected(false);
		categorychkbxs.get(2).setSelected(true);	
	}
	/**
	 * Fill a pane with primitives from a particular sql view.
	 *
	 * @param viewname 	The view (table) of primitives.
	 * @param paneindex Index of the pane to fill.
	 */

	private void fillPaneByPrimitivesView(String viewname, int paneindex) {

		DPrimitive fresh, old;
	
		ArrayList<Integer> codepoints = session.getBase().fetchCategory(viewname);
		
		for (int codepoint : codepoints) {
						
			fresh = new DPrimitive(codepoint);									// DOES NOT SET TYPE
						
			if (!PaintStatics.GLYPHDICTIONARY.containsValue(fresh))
				old = PaintStatics.GLYPHDICTIONARY.put((char) codepoint, fresh);
			else 
				old = null;
				
			DButton button = new DButton(fresh);

			button.addActionListener(this);
			button.setBorderPainted(false);
						
			categorypanels.get(paneindex).add(button);			
		}			

	}
	
	/**
	 * Clear selected primitives and everything it depends on.
	 */
	private void clearSelected() {

		selected.clear();
		
		pnlUpper.removeAll();
		pnlUpper.revalidate();
		pnlUpper.repaint();
	}	 	
	/**
	 * Adds a collection of formals to the set of currently selected.
	 *
	 * @param addition 	The primitives to add to the selection.
	 */
	private void addToSelected(Collection<Formal> addition) {
		
		for (JScrollPane sp : categoryscrollers) {
			
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
		
		for (JPanel p : categorypanels) {
			
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
	 * Toggle a particular primitive's button in all categorypanels in this sub application.
	 *
	 * @param primitive The primitive who's button to toggle everywhere it occurs.
	 */
	private void toggleInPanels(Formal primitive) {
		
		for (JScrollPane sp : categoryscrollers) {
			
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
		this.defaultcomponent = pnlUpper;		
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


	public static JButton makeControlButton(String name, String command, ActionListener listener) {
		
		JButton button = new JButton(name);		

		button.setMargin(ViewStatics.btnInset);
		//button.setBorder(new BevelBorder(BevelBorder.RAISED));
		button.setBackground(ViewStatics.btnBkgr);
		button.setFont(ViewStatics.btnFontBold);
		
		button.setActionCommand(command);
		button.addActionListener(listener);
		
		return button;		
	}
}
