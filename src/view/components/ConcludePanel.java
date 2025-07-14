package view.components;

import javax.swing.JPanel;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import java.awt.Component;

import javax.swing.border.SoftBevelBorder;

import control.statics.ViewStatics;
import model.logic.Implication;
import view.SidePanel;

import javax.swing.border.BevelBorder;

/**
 * Panel for finalising the preliminary statement worked on.
 */
public class ConcludePanel extends JPanel {
	
	private ToggleGroup toggles 	= new ToggleGroup();
	private JButton 	btnDone 	= new JButton("done");
	private SidePanel 	executive;

	/**
	 * A toggle button dedicated to toggle between the three implications.
	 */
	public class ImplicationToggle extends JToggleButton {
		
		private Implication implication;
		
		/**
		 * Creates a toggle button for an implication.
		 * 
		 * @param implication 	One of {@see Implication.LEFT}, {@see Implication.RIGHT} or {@see Implication.EQUIV}
		 * 
		 * @see Implication
		 */
		public ImplicationToggle(Implication implication) {
			super();
			this.implication = implication;
			super.setText(""+ (char)this.implication.getCodepoint());
		}
		
		/**
		 * @return This toggle button's implication. 
		 */
		public Implication getImplication() { return implication; }
		
	}
	
	/**
	 * The three toggle button's for implications are grouped together in a button group.
	 */
 	public class ToggleGroup {
		
		private ImplicationToggle tglLeft  = new ImplicationToggle(Implication.LEFT);
		private ImplicationToggle tglEquiv  = new ImplicationToggle(Implication.EQUIV);
		private ImplicationToggle tglRight  = new ImplicationToggle(Implication.RIGHT);
		
		private ImplicationToggle[] tglbuttons = new ImplicationToggle[] {tglLeft, tglEquiv, tglRight};

		private Implication current = Implication.RIGHT;
		
		private ToggleGroup() {
			
			for (JToggleButton button : tglbuttons) 
				button.setActionCommand("toggle");			
		}
		
		private ImplicationToggle[] toggleButtons() {
			return tglbuttons;
		}
		
		/**
		 * Select which implication that is currently chosen for closing implication relation.
		 * 
		 * @param implication The implication to toggle to.
		 */
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
	 * Panel for finalising the preliminary statement worked on.
	 */
	public ConcludePanel() {

		toggles = new ToggleGroup();
			
		this.setMaximumSize(ViewStatics.pnlCnclSize);

		makeButtons();		
	}

	/**
	 * The 'done' button is the default button.
	 * @return
	 */
	public Component getDefaultComponent() {
		return btnDone;
	}

	/**
	 * Register the side panel as listener on the toggle buttons.
	 * @param listener
	 */
	public void registerToggleButtonsListener(SidePanel listener) {

		this.executive = listener;
		
		this.executive.setToggles(toggles);

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

		setLayout(new GridLayout(4, 4, 10, ViewStatics.btnVGap));

		JLabel lblStatementRelation = new JLabel("Relation");
		JLabel lblDrop = new JLabel("Drop");
		
		
		JButton btnDropStatement 	= new JButton("statement");		btnDropStatement.setActionCommand("drop statement");
		JButton btnDropPrimitive 	= new JButton("primitive");		btnDropPrimitive.setActionCommand("drop primitive");
																	btnDone.setActionCommand("done");
			
		JButton[] btns = new JButton[] {btnDropStatement, btnDropPrimitive, btnDone};
		
		for (int i = 0;i < 3; i++) {
			//btns[i].setBorder(ViewStatics.btnBorder);
			btns[i].setBackground(ViewStatics.btnBkgr);
			btns[i].setFont(ViewStatics.btnFont);
		}

		ImplicationToggle[] tgls = toggles.toggleButtons();

		for (ImplicationToggle tgl : tgls) {
			//tgl.setBorder(new BevelBorder(BevelBorder.RAISED));
			tgl.setFont(ViewStatics.btnFont);
			tgl.setBackground(ViewStatics.btnBkgr);
		}

		lblStatementRelation.setFont(ViewStatics.btnFont);
		lblStatementRelation.setVerticalAlignment(SwingConstants.BOTTOM);

		lblDrop.setFont(ViewStatics.btnFont);		
		lblDrop.setVerticalAlignment(SwingConstants.BOTTOM);

		add(lblStatementRelation);	add(lblDrop);					
		add(tgls[0]);				add(btnDropPrimitive);			
		add(tgls[1]);				add(btnDropStatement);
		add(tgls[2]);				add(btnDone);

		btnDone.setBackground(ViewStatics.btnBkgrAlarm);
		btnDone.setFont(ViewStatics.btnFontPlus);		
	}
}
