package view;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import annexes.maker.CompositeMaker;
import annexes.picker.DeductionPicker;
import annexes.trainer.DeductionTrainer;
import control.Toolbox;
import control.db.DeductionBase;
import model.description.DTheorem;
import view.abstraction.TraversablePanel;
import view.components.dialogs.CompositesLoader;
import view.components.dialogs.PrimitivesLoader;
import view.components.dialogs.TheoremStore;

/**
 * The panel that contains the functionality needed for navigating between modules, storing and loading theorems, quit and such.
 */
public class ControlPanel extends TraversablePanel implements ActionListener {
	
	private DeductionBase 	base;
	private DeductionFrame 	parent;	
	private DisplayCanvas 	canvas;

	private final TheoremStore 	lsdialog;
	
	private PrimitivesLoader 	pldialog;
	private CompositesLoader 	cldialog;
	private DeductionTrainer 	trainer;	
	private CompositeMaker 		composite;
	private DeductionPicker 	picker;
	
	
	/**
	 * Instantiates a new control panel.
	 *
	 * @param parent 	The frame and parent of this control panel.
	 * @param canvas 	The drawing canvas of the application.
	 * @param base 		The base to store theorems and their constituents in.
	 */
	public ControlPanel(DeductionFrame parent, DisplayCanvas canvas, DeductionBase base) {	
		
		this.parent = parent;
		this.canvas = canvas;		
		this.base = base;
		
		composite = new CompositeMaker(parent);
		
		lsdialog = new TheoremStore(parent, base);
		pldialog = new PrimitivesLoader(parent, base);
		cldialog = new CompositesLoader(parent, base);
			
		this.setLayout(new FlowLayout());
		
		makeAndAddButtons();
	}

	/**
	 * Hub for most of the functionality triggered by clicking buttons.
	 */
	public void actionPerformed(ActionEvent e) {

		PrimitivesPanel primitives;
		
		switch (e.getActionCommand()) {
	
			case "primitives":
				
				pldialog.initialise();

				Toolbox.switchContainer(pldialog, parent);
				break;
				
			case "storage":
				
				lsdialog.initialise();
				
				Toolbox.switchContainer(lsdialog, parent);
				break;
				
			case "storage-composites":
				
				cldialog.initialise();
				
				Toolbox.switchContainer(cldialog, parent);
				break;

			case "new":
				
				int confirm = JOptionPane.showConfirmDialog(this, "Really erase current theorem?");
				
				if (confirm == JOptionPane.YES_OPTION) {

					newTheorem();
					canvas.repaint();
					parent.getPrimitivesPanel().restoreFocus();
				}

				break;
				
			case "quit":
				
				base.closeDB();
				trainer.cancel();
				parent.cleanAndExit();
				
				break;
				
			case "trainer":
				
				primitives = parent.getPrimitivesPanel();

				this.bindings = primitives.getBindings();
				
				trainer.setPrimitives(Toolbox.describe(bindings));
				
				Toolbox.switchContainer(trainer.getFrame(), parent);
				break;
				
			case "composite":
				
				if (canvas.getDrawn() != null) {
				
					primitives = parent.getPrimitivesPanel();

					composite.setPrimitivesPanel(primitives);
					
					Toolbox.switchContainer(composite, parent);
					
					composite.initialise(canvas.getDrawn());
				}
			
				break;

			case "picker":
				
				Toolbox.switchContainer(picker, parent);
				break;
				
			default:
				
				break;		
		}		
	}

	/**
	 * Create a new theorem, and reinitialise all of the application's components with it. The old one is thrown away.
	 */
	public void newTheorem() {
		
		parent.setAndDescribeTheorem(new DTheorem("unnamed"));
		canvas.setPaintMode(false, false, false);
		canvas.repaint();
	}

	/**
	 * Fill store menu.
	 */
	public void fillStoreMenu() {

		ArrayList<String> added = base.fetchTheoremNames();

		lsdialog.insertNames(added);
	}

	/**
	 * Retreives the data base used in the main application.
	 *
	 * @return The data base of the DeductionWriter application.
	 */
	public DeductionBase getDeductionBase() { 
		return base; 
	}

	/**
	 * Sets the two annexes of the DeductionWriter application: DeductionTrainer and DeductionPicker.
	 *
	 * @param trainer 	A DeductionWriter sub application for connecting keyboard keys to primitives and practice using them.
	 * @param picker	A DeductionWriter sub application for choosing which mathematics primitives and glyphs to use when writing theorems.
	 */
	public void setAnnexes(DeductionTrainer trainer, DeductionPicker picker) {

		this.trainer = trainer;
		this.picker = picker;		
	}
	
	
	private void makeAndAddButtons() {

		JButton btnPrimitives = new JButton("Load primitives");
		btnPrimitives.addActionListener(this);
		btnPrimitives.setActionCommand("primitives");
		add(btnPrimitives);

		JButton btnLoad = new JButton("Theorems storage");
		btnLoad.addActionListener(this);
		btnLoad.setActionCommand("storage");
		add(btnLoad);
				
		JButton btnPicker = new JButton("Pick primitives");
		btnPicker.addActionListener(this);
		btnPicker.setActionCommand("picker");
		add(btnPicker);
		
		JButton btnTrainer = new JButton("Bindings");
		btnTrainer.addActionListener(this);
		btnTrainer.setActionCommand("trainer");
		add(btnTrainer);

		JButton btnComposites = new JButton("Composites storage");
		btnComposites.addActionListener(this);
		btnComposites.setActionCommand("storage-composites");
		add(btnComposites);

		JButton btnComposite = new JButton("Composites");
		btnComposite.addActionListener(this);
		btnComposite.setActionCommand("composite");
		add(btnComposite);

		JButton btnNew = new JButton("New");
		btnNew.addActionListener(this);
		btnNew.setActionCommand("new");
		add(btnNew);

		JButton btnQuit = new JButton("Quit");
		btnQuit.addActionListener(this);
		btnQuit.setActionCommand("quit");
		add(btnQuit);
	}
}
