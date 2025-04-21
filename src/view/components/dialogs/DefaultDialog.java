package view.components.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;

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

import control.Session;
import control.Toolbox;
import control.db.DeductionBase;

/**
 * Base class for dialogs used across the application for interacing with the application's data base.
 * @see DeductionBase
 */
public abstract class DefaultDialog extends JDialog implements ActionListener {
	
	/** The parent calling container. */	
	protected Container 		parent;
	
	/** The base used for storage. */
	protected Session 			session;																																
	
	/** The menu used for selecting items from the data base or for removal from the data base. */
	protected DefaultListModel<String> menu  = new DefaultListModel<String>();																										
	
	/** The list of the menu. */
	protected final JList<String> 	   list = new JList<String>(menu);
	
	
	private final JPanel contentpanel   = new JPanel();	
	private final JPanel buttonspanel   = new JPanel();

	/** The text field used for naming new items. */
	protected final JTextField txfName  = new JTextField("name of table ...");
	
	private final Label   	lblLoad 	= new Label("Load selected primitives table");
	private final Label   	lblStore	= new Label("Store your primitive table");	

	/** The button for loading selected items and adding them to the application. */
	protected final JButton btnLoad 	= new JButton("Load");
	
	private final JButton 	btnStore 	= new JButton("Store");	
	private final JButton 	btnDelete 	= new JButton("Delete");	
	private final JButton 	btnCancel 	= new JButton("Cancel");

	/** Simple state variable for decided when som task is completed. */
	protected boolean done;

	/** Simple state variable form maintaining name of selected item. */
	protected String columnvalue;
	
	
	/**
	 * Instantiates a new default dialog.
	 *
	 * @param parent 	The parent of this dialogue to which to return to.
	 * @param session TODO
	 */
	protected DefaultDialog(Container parent, Session session) {

		this.parent = parent;
		this.session = session;
			
		setTitle("Load and store your primitives table");
		setBounds(100, 100, 500, 300);
	
		getContentPane().setLayout(new BorderLayout());		
		getContentPane().add(contentpanel, BorderLayout.CENTER);
		
		contentpanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentpanel.setLayout(null);																										///(G1DF)
		
		list.setBorder(new LineBorder(new Color(0, 0, 0)));
		list.setBounds(10, 11, 296, 84);
		list.setVisibleRowCount(5);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setSelectedIndex(0);
	
		JScrollPane scroller = new JScrollPane();

		scroller.setBounds(10, 11, 468, 140);
		scroller.setViewportView(list);
		
		contentpanel.add(scroller);
		
		buttonspanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		buttonspanel.setBounds(12, 163, 468, 95);

		contentpanel.add(buttonspanel);
		
		GridBagLayout gbl_buttonspanel = new GridBagLayout();
		
		gbl_buttonspanel.columnWeights = new double[]{ 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		gbl_buttonspanel.rowWeights = new double[]{ 0.0, 0.0, 0.0};
		
		buttonspanel.setLayout(gbl_buttonspanel);
			
		makeLabels();
																																				///(8608)
		GridBagConstraints gbc_txfName = new GridBagConstraints();
		gbc_txfName.gridwidth = 2;
		gbc_txfName.anchor = GridBagConstraints.SOUTH;
		gbc_txfName.weighty = 1.0;
		gbc_txfName.weightx = 1.0;
		gbc_txfName.ipadx = 5;
		gbc_txfName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txfName.insets = new Insets(5, 0, 5, 5);
		gbc_txfName.gridx = 1;
		gbc_txfName.gridy = 1;
		buttonspanel.add(txfName, gbc_txfName);
		txfName.setName("input");
		txfName.setColumns(10);
		txfName.setEditable(true);
		
		makeButtons();
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
				
			case "cancel": 	Toolbox.switchContainer(parent, this); break;
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
	public abstract void load(String name);
	
	/**
	 * Store item in data base.
	 * @param name	The name of the item to store.
	 */
	public abstract void store(String name);

	/**
	 * Delete item in the data base.
	 * @param name	The name of the item to delete.
	 */
	public abstract void delete(String name);

	/** Updates the menu and selects first. */
	public void initialise() {
		
		updateMenu();

		list.setSelectedIndex(0);
	}
	
	/** 
	 * Removes the button with name given as argument. For customisation of the default dialog extension.
	 *
	 * @param name	The name if the button to remove. 
	 */
	public void remove(String name) {

		for (Component c : buttonspanel.getComponents()) 
			if (name != null && c.getName().contains(name)) 
				buttonspanel.remove(c);
		
		buttonspanel.revalidate();
		buttonspanel.repaint();		
	}
	
	/** 
	 * Updates the list in the menu by looking up what tables there are in the base. 
	 * Primitive tables by default, override to change.
	 * 
	 * @return The size of the menu after update.
	 */
	protected int updateMenu()  {

		menu.clear();
		
		ArrayList<String> fromdb = session.getBase().fetchNames("Primitivetables");
		
		fromdb.removeAll(Collections.list(menu.elements()));

		if (! fromdb.isEmpty())
			menu.addAll(fromdb);

		return menu.size();
	}

	
	private void makeButtons() {
																																				///(BBC8)
		btnLoad.setActionCommand("load");
		btnLoad.setName("load");
		btnLoad.addActionListener(this);
		GridBagConstraints gbc_btnLoad = new GridBagConstraints();
		gbc_btnLoad.insets 		= new Insets(0, 0, 5, 5);
		gbc_btnLoad.weightx 	= 1.0;
		gbc_btnLoad.anchor 		= GridBagConstraints.SOUTHWEST;
		gbc_btnLoad.gridx 		= 4;							gbc_btnLoad.gridy 		= 1;
		buttonspanel.add(btnLoad, gbc_btnLoad);
		getRootPane().setDefaultButton(btnLoad);
																																				///(70D0)
		btnStore.setActionCommand("store");
		btnStore.setName("store");
		btnStore.addActionListener(this);
		GridBagConstraints gbc_btnStore = new GridBagConstraints();
		gbc_btnStore.insets 	= new Insets(0, 0, 10, 5);
		gbc_btnStore.weightx 	= 1.0;
		gbc_btnStore.anchor 	= GridBagConstraints.EAST;
		gbc_btnStore.gridx 		= 1;							gbc_btnStore.gridy 		= 2;
		buttonspanel.add(btnStore, gbc_btnStore);
																																				///(F63C)
		btnDelete.setActionCommand("delete");
		btnDelete.setName("delete");
		btnDelete.addActionListener(this);
		GridBagConstraints gbc_btnDelete = new GridBagConstraints();
		gbc_btnDelete.insets 	= new Insets(0, 0, 10, 5);
		gbc_btnDelete.weightx 	= 1.0;
		gbc_btnDelete.anchor 	= GridBagConstraints.SOUTHWEST;
		gbc_btnDelete.gridx 	= 1;							gbc_btnDelete.gridy 	= 2;
		buttonspanel.add(btnDelete, gbc_btnDelete);
																																				///(E880)
		btnCancel.setActionCommand("cancel");
		btnCancel.setName("cancel");
		btnCancel.addActionListener(this);
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets 	= new Insets(0, 0, 10, 0);
		gbc_btnCancel.weightx 	= 1.0;
		gbc_btnCancel.anchor 	= GridBagConstraints.SOUTHWEST;
		gbc_btnCancel.gridwidth = 2;
		gbc_btnCancel.gridx 	= 4;							gbc_btnCancel.gridy 	= 2;
		
		buttonspanel.add(btnCancel, gbc_btnCancel);	
	}

	private void makeLabels() {
																																				///(0548)
		GridBagConstraints gbc_lblWrite = new GridBagConstraints();
		gbc_lblWrite.insets 	= new Insets(0, 0, 0, 5);
		gbc_lblWrite.gridwidth 	= 2;
		gbc_lblWrite.anchor 	= GridBagConstraints.SOUTHWEST;
		gbc_lblWrite.gridx 		= 1;							gbc_lblWrite.gridy 		= 0;
		lblStore.setName("storelabel");
		buttonspanel.add(lblStore, gbc_lblWrite);
																																				///(F850)
		GridBagConstraints gbc_lblRead = new GridBagConstraints();
		gbc_lblRead.insets 		= new Insets(0, 0, 0, 5);
		gbc_lblRead.anchor 		= GridBagConstraints.SOUTHWEST;
		gbc_lblRead.gridx 		= 4;							gbc_lblRead.gridy 		= 0;
		lblLoad.setName("loadlabel");		
		buttonspanel.add(lblLoad, gbc_lblRead);
	}	
}
