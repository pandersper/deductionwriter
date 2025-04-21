package view;

import javax.swing.JPanel;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import java.awt.Color;
import java.awt.Component;

import javax.swing.border.SoftBevelBorder;

import model.logic.Implication;
import view.components.ViewConstants;

import javax.swing.border.BevelBorder;

public class ConcludePanel extends JPanel {
	
	private ToggleGroup toggles 	= new ToggleGroup();
	private JButton 	btnDone 	= new JButton("Done");
	private SidePanel 	executive;

	public class ImplicationToggle extends JToggleButton {
		
		private Implication implication;
		
		public ImplicationToggle(Implication implication) {
			super();
			this.implication = implication;
			super.setText(""+ (char)this.implication.getCodepoint());
		}
		
		public Implication getImplication() { return implication; }
		
	}
	
 	public class ToggleGroup {
		
		private ImplicationToggle tglLeft  = new ImplicationToggle(Implication.LEFT);
		private ImplicationToggle tglEquiv  = new ImplicationToggle(Implication.EQUIV);
		private ImplicationToggle tglRight  = new ImplicationToggle(Implication.RIGHT);
		
		private ImplicationToggle[] tglbuttons = new ImplicationToggle[] {tglLeft, tglEquiv, tglRight};

		private Implication current = Implication.RIGHT;
		
		public ToggleGroup() {
			
			for (JToggleButton button : tglbuttons) 
				button.setActionCommand("toggle");			
		}
		
		public ImplicationToggle[] toggleButtons() {
			return tglbuttons;
		}
		
		public Implication chooseToggle(Implication implication) {
			
			switch (implication.getImplicationType()) {
			
				case RIGHT:
					tglRight.setSelected(true);
					tglEquiv.setSelected(false);
					tglLeft.setSelected(false);					
					current = Implication.RIGHT;
					break;
					
				case EQUIV:
					tglRight.setSelected(false);
					tglEquiv.setSelected(true);
					tglLeft.setSelected(false);										
					current = Implication.EQUIV;
					break;
					
				case LEFT:
					tglRight.setSelected(false);
					tglEquiv.setSelected(false);
					tglLeft.setSelected(true);										
					current = Implication.LEFT;
					break;
				default:
					return null;
			}
						
			return current;
		}
 	}	

 	
 	/**
	 * Panel for navigation in deductions.
	 */
	public ConcludePanel() {

		toggles = new ToggleGroup();
			
		makeButtons();		
	}


	public Component getDefaultComponent() {
		return btnDone;
	}

	public void registerButtons(SidePanel listener) {

		this.executive = listener;
		
		this.executive.shareToggles(toggles);

		Component[] components = this.getComponents();

		for (Component button : components)
			if (button instanceof JButton)
				((JButton)button).addActionListener(listener);
			else 
				if (button instanceof ImplicationToggle)
					((ImplicationToggle) button).addActionListener(listener);
	}

	private void makeButtons() {
		
		setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));

		setLayout(new GridLayout(4, 4, 10, ViewConstants.btnvgap));

		JLabel lblStatementRelation = new JLabel("Relation");
		JLabel lblDrop = new JLabel("Drop");
		
		
		JButton btnDropStatement 	= new JButton("statement");		btnDropStatement.setActionCommand("drop statement");
		JButton btnDropPrimitive 	= new JButton("primitive");		btnDropPrimitive.setActionCommand("drop primitive");
																	btnDone.setActionCommand("done");
			
		JButton[] btns = new JButton[] {btnDropStatement, btnDropPrimitive, btnDone};
		
		for (int i = 0;i < 3; i++) {
			btns[i].setBorder(ViewConstants.btnBorder);
			btns[i].setBackground(ViewConstants.btnBkgr);
			btns[i].setFont(ViewConstants.btnFont);
		}

		ImplicationToggle[] tgls = toggles.toggleButtons();

		for (ImplicationToggle tgl : tgls) {
			tgl.setBorder(new BevelBorder(BevelBorder.RAISED));
			tgl.setFont(ViewConstants.btnFont);
			tgl.setBackground(ViewConstants.btnBkgr);
		}

		lblStatementRelation.setFont(ViewConstants.btnFont);
		lblStatementRelation.setVerticalAlignment(SwingConstants.BOTTOM);
		lblDrop.setFont(ViewConstants.btnFont);		
		lblDrop.setVerticalAlignment(SwingConstants.BOTTOM);

		add(lblStatementRelation);	add(lblDrop);					
		add(tgls[0]);				add(btnDropPrimitive);			
		add(tgls[1]);				add(btnDropStatement);
		add(tgls[2]);				add(btnDone);

		btnDone.setBackground(ViewConstants.btnBkgrAlarm);
		btnDone.setFont(ViewConstants.btnFontPlus);		
	}
	
}
