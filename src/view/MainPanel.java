package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.border.LineBorder;
import javax.swing.BoxLayout;

import control.session.Session;
import control.session.WorkList;
import control.statics.ViewStatics;
import view.abstraction.TraversablePanel;


public class MainPanel extends TraversablePanel  {
	
	/**
	 * The main center pane where the theorems are drawn.
	 * 
	 * @param elder The outermst fram of the application.
	 */
	public MainPanel(DeductionFrame parent) {
		
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setBorder(new LineBorder(new Color(0, 0, 0)));
		this.setPreferredSize(new Dimension(3*ViewStatics.a4width, 3*ViewStatics.a4height));
		
		makeSubpanels(parent);
	}		
	
	private void makeSubpanels(DeductionFrame parent) {
				
		Session session = parent.getSession();
		WorkList works 	= session.getWorks();
		
		Rectangle tabs = new Rectangle();
		
		tabs.setSize(ViewStatics.canvasdimension);
		tabs.grow(0,15);
		
		works.setSize(tabs.getSize());
		works.setMaximumSize(tabs.getSize());
						
		add(works);
		
		JPanel boxAdm = new JPanel();
		add(boxAdm);
		
		JButton btnDel = new JButton("-");		
		btnDel.setBackground(Color.LIGHT_GRAY);
		
		JButton btnNew = new JButton("+");		
		btnNew.setBackground(Color.LIGHT_GRAY);
		
		boxAdm.setLayout(new BoxLayout(boxAdm, BoxLayout.X_AXIS));		
		boxAdm.add(btnDel);		
		boxAdm.add(btnNew);
				
		btnDel.addActionListener(session);
		btnDel.setActionCommand("remove");
		btnNew.addActionListener(session);
		btnNew.setActionCommand("new");
	}
}