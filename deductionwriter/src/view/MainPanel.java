package view;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.Box;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import java.awt.Component;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import control.Session;
import view.abstraction.TraversablePanel;
import view.components.DisplayCanvas;
import view.components.ViewConstants;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.BoxLayout;
import javax.swing.DefaultSingleSelectionModel;

public class MainPanel extends TraversablePanel implements ChangeListener {
	
	private JTabbedPane tabs;
	/**
	 * @param parent 
	 * 
	 */
	public MainPanel(DeductionFrame parent) {
		
		this.parent = parent;
		
		makeSubpanels();
		
		tabs.addTab(this.getTheorem().getName(), this.getCanvas());

		DefaultSingleSelectionModel model = new DefaultSingleSelectionModel();
		
		model.addChangeListener(this);
				
		tabs.setModel(model);
	}
		
	private void makeSubpanels() {
		
		setBorder(new LineBorder(new Color(0, 0, 0)));
		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		add(panel);
		
		tabs = new JTabbedPane(JTabbedPane.TOP);
		panel.add(tabs);

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
								
		Box boxAdm = Box.createHorizontalBox();
		panel.add(boxAdm);
		
		JButton btnDel = new JButton("-");		
		JButton btnNew = new JButton("+");
		JButton btnStore = new JButton("store");

		btnDel.addActionListener(tabRemover(tabs));
		btnNew.addActionListener(tabAdder(tabs));
		
		Component strutleft 	= Box.createHorizontalStrut(50);
		Component strutmiddle	= Box.createHorizontalStrut(100);

		boxAdm.add(strutleft);		boxAdm.add(btnDel);		boxAdm.add(btnNew);		
		boxAdm.add(strutmiddle);	boxAdm.add(btnStore);
		
		boxAdm.setMaximumSize(new Dimension(ViewConstants.a4width,200));
		boxAdm.setMinimumSize(new Dimension(ViewConstants.a4width,200));

		panel.setMaximumSize(new Dimension(3*ViewConstants.a4width,3*ViewConstants.a4height));
		panel.setMinimumSize(new Dimension(3*ViewConstants.a4width,3*ViewConstants.a4height));
	}

	
	private	ActionListener tabRemover(JTabbedPane pane){
						return(new ActionListener() {
									public void actionPerformed(ActionEvent e) {

										if (tabs.getTabCount() > 1) {

											Session session = parent.getSession();										
											
											int tabindex = tabs.getSelectedIndex();
											
											tabs.remove(tabindex);
											
											tabindex = session.removeWork(tabindex);																					
											
											tabs.setSelectedIndex(tabindex);
										} 
									}
						});
	}
	
	private ActionListener tabAdder(JTabbedPane pane) {
						return(new ActionListener() {
									public void actionPerformed(ActionEvent e) {

										Session session = parent.getSession();
										
										String name = JOptionPane.showInputDialog("Name of new theorem?");
										
										pane.addTab(name, session.newWork(name));
										pane.setSelectedIndex(session.index());										
										pane.setTitleAt(session.index(), name);
										
										parent.getGlyphsPanel().updateButtonsListener(session.getCurrentCanvas());
									}
						});
	}


	public void stateChanged(ChangeEvent ce) {

		int selected = tabs.getModel().getSelectedIndex();
		
		if (selected == -1) {
			tabs.setSelectedIndex(0);
			selected = 0;			
		}
		
		parent.getSession().setWork(selected);
			
		DisplayCanvas currentcanvas = parent.getSession().getCurrentCanvas();
			
		parent.getGlyphsPanel().updateButtonsListener(currentcanvas);
	
		currentcanvas.newCursor();
			
		currentcanvas.setPaintMode(false, false, false);		
			
		tabs.setTitleAt(selected, currentcanvas.getTheorem().getName());	
	}

}