package view.components.dialogs;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.BoxLayout;
import javax.swing.SwingConstants;

import java.util.ArrayList;
import java.util.Collections;

import control.db.DeductionBase;
import control.session.Session;
import control.statics.ViewStatics;
import view.abstraction.InitiableContainer;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * Base class for dialogs used across the application for interacing with the application's data base.
 * @see DeductionBase
 */
public abstract class DefaultDialog<C extends Container & InitiableContainer> extends JDialog implements ActionListener, InitiableContainer {
	
	/** 
	 * The elder calling container. 
	 */	
	protected C 		elder;
	/** 
	 * The base used for storage. 
	 */
	protected Session 	session;																																
	
	/** 
	 * The menu used for selecting items from the data base or for removal from the data base.
	 */
	protected DefaultListModel<String> menu  = new DefaultListModel<String>();																										
	
	/** 
	 * The list of the menu. 
	 */
	protected final JList<String> 	   list = new JList<String>(menu);
	
	private final JPanel contentpanel   = new JPanel();	
	private final JPanel buttonspanel   = new JPanel();
	
	/** 
	 * The text field used for naming new items. 
	 */
	protected final JTextField txfName  = new JTextField("name of that to store");

	/** 
	 * The button for loading selected items and adding them to the application. 
	 */
	protected final JButton 	btnLoad 	= new JButton("Load");	
	private final 	JButton 	btnStore 	= new JButton("Store");	
	private final	JButton 	btnDelete 	= new JButton("Delete");	
	private final 	JButton 	btnCancel 	= new JButton("Cancel");
	
	/** 
	 * Simple state variable for decided when som task is completed.
	 */
	protected boolean done;

	/** 
	 * Simple state variable form maintaining name of selected item. 
	 */
	protected String columnvalue;
	
	
	/**
	 * Instantiates a new default dialog.
	 *
	 * @param elder 	The elder of this dialogue to which to return to.
	 * @param session TODO
	 */
	protected DefaultDialog(C parent, Session session) {

		this.elder = parent;
		this.session = session;
			
		setTitle("Store");
		setBounds(100, 100, 240, 299);
		
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		getContentPane().add(contentpanel);
		
		contentpanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentpanel.setLayout(null);																										///(G1DF)
		
		list.addListSelectionListener(
				new ListSelectionListener() {				
					public void valueChanged(ListSelectionEvent lse) {
							String selected = list.getSelectedValue();
							txfName.setText(selected);
					}});
		
		list.setBorder(new LineBorder(new Color(0, 0, 0)));
		list.setBounds(10, 11, 196, 84);
		list.setVisibleRowCount(5);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setSelectedIndex(0);
	
		JScrollPane scroller = new JScrollPane();

		scroller.setBounds(10, 10, 210, 140);
		scroller.setViewportView(list);
		
		contentpanel.add(scroller);
		
		buttonspanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		buttonspanel.setBounds(10, 160, 210, 100);

		contentpanel.add(buttonspanel);
		
		txfName.addFocusListener(
				new FocusAdapter() {
					public void focusGained(FocusEvent e) {
							list.clearSelection();
					}});
	
		txfName.setHorizontalAlignment(SwingConstants.LEFT);
		txfName.setBounds(10, 47, 188, 20);		
		txfName.setName("input");
		txfName.setColumns(10);
		txfName.setEditable(true);
		
		makeButtons();
	}
	
	/** 
	 * Updates the menu and selects first. 
	 */
	public void 	initialise() {
		
		updateMenu();

		list.setSelectedIndex(0);
	}
		
	/** 
	 * Updates the list in the menu by looking up what tables there are in the base. 
	 * Primitive tables by default, override to change.
	 * 
	 * @return The size of the menu after update.
	 */
	protected int 	updateMenu()  {

		menu.clear();
		
		ArrayList<String> fromdb = session.getBase().fetchNames("Primitivetables");
		
		fromdb.removeAll(Collections.list(menu.elements()));

		if (! fromdb.isEmpty())
			menu.addAll(fromdb);

		return menu.size();
	}
	/** 
	 * Removes the button with name given as argument. For customisation of the default dialog extension.
	 *
	 * @param name	The name if the button to remove. 
	 */
	public void 	remove(String name) {

		for (Component c : buttonspanel.getComponents()) 
			if (name != null && c.getName().contains(name)) 
				buttonspanel.remove(c);
		
		buttonspanel.revalidate();
		buttonspanel.repaint();		
	}
	
	/**
	 * Action performed with cases for 'cancel', 'load', 'store' and 'delete' where the three last ones have corresponding
	 * abstract methods. 
	 * 
	 * @param buttonevent Originating from the dialouge's buttons.
	 * 
	 * @see #load(String)
	 * @see #store(String)
	 * @see #delete(String) 
	 */
	public void actionPerformed(ActionEvent buttonevent) {

		String txf = txfName.getText();
		String mnu = list.getSelectedValue();
		
		switch (buttonevent.getActionCommand()) {
				
			case "cancel": 	ViewStatics.switchContainer(elder, this); break;
			case "load": 	load(mnu); break;
			case "store": 	store(txf); break;
			case "delete": 	delete(mnu); break;	
								
			default: break;
		}
	}

	/**
	 * Load item from data base.
	 * @param name	The name of the item to load.
	 */
	public abstract void 	load(String name);
	
	/**
	 * Store item in data base.
	 * @param name	The name of the item to store.
	 */
	public abstract void 	store(String name);

	/**
	 * Delete item in the data base.
	 * @param name	The name of the item to delete.
	 */
	public abstract void 	delete(String name);

	
	
	private void makeButtons() {
		
		JButton[] buttons = new JButton[] {btnStore, btnLoad, btnDelete, btnCancel};

		for(JButton button : buttons) {
			button.setBackground(Color.LIGHT_GRAY);
			button.setMargin(ViewStatics.btnInset);
			button.setSize(ViewStatics.btnSize);																																			///(70D0)
			button.addActionListener(this);
		}

		btnStore.setActionCommand("store");			btnStore.setName("store");
		btnLoad.setActionCommand("load");			btnLoad.setName("load");
		btnDelete.setActionCommand("delete");		btnDelete.setName("delete");
		btnCancel.setActionCommand("cancel");		btnCancel.setName("cancel");

		btnStore.setLocation(10, 72);
		btnLoad.setLocation(10, 10);
		btnDelete.setLocation(80, 10);
		btnCancel.setLocation(138, 72);
		///(E880)
		buttonspanel.setLayout(null);

		buttonspanel.add(btnLoad);				
		buttonspanel.add(btnDelete);
		buttonspanel.add(btnCancel);	
		buttonspanel.add(btnStore);

		buttonspanel.add(txfName);

		getRootPane().setDefaultButton(btnLoad);
	}

	private void makeLabels() {
																																				///(0548)
	}	
}
