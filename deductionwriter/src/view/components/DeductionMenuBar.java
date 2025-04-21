package view.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.HeadlessException;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import annexes.maker.CompositeMaker;
import annexes.picker.DeductionPicker;
import annexes.trainer.DeductionTrainer;
import control.Session;
import control.Shortcut;
import control.Toolbox;
import control.db.DeductionBase;
import model.description.DTheorem;
import model.independent.DoubleArray;
import model.logic.abstraction.Formal;
import view.components.dialogs.CompositesStore;
import view.components.dialogs.PrimitivesLoader;
import view.components.dialogs.TheoremStore;
import view.GlyphsPanel;
import view.MainPanel;
import view.DeductionFrame;

public class DeductionMenuBar extends JMenuBar implements ActionListener {

	private DeductionFrame 	parent;	
	private Session			session;

	private final TheoremStore 		lsdialog;
	private final PrimitivesLoader 	pldialog;
	private final CompositesStore 	cldialog;
	
	private final DeductionTrainer 	trainer;	
	private final CompositeMaker 	composite;
	private final DeductionPicker 	picker;

	
	protected DoubleArray<Formal, Shortcut> 	bindings = new  DoubleArray<Formal, Shortcut>();

	
	public DeductionMenuBar(DeductionTrainer trainer, DeductionPicker picker, DeductionFrame parent) {
	
		this.trainer = trainer;
		this.picker = picker;
		this.parent = parent;		
		this.session = this.parent.getSession();
		
		this.composite = new CompositeMaker(this.parent);
		
		lsdialog = new TheoremStore(this.parent, this.session);
		pldialog = new PrimitivesLoader(this.parent, this.session);
		cldialog = new CompositesStore(this.parent, this.session);

		makeMenus();
	}

	/**
	 * Sets the one at a time session.
	 * @param session Contains all the user works.
	 */

	public Session getSession() { return session; }
	
	
	public String getPrimitivestable() {
		return session.primitivestable;
	}

	/**
	 * Fill store menu.
	 */
	public void fillStoreMenu() {

		ArrayList<String> added = session.getBase().fetchTheoremNames();

		lsdialog.insertNames(added);
	}

	
	public void actionPerformed(ActionEvent e) {
		
		GlyphsPanel 	glyphs;
		
		switch (e.getActionCommand()) {

				case "new theorem":
		
					int confirm = JOptionPane.showConfirmDialog(this, "Really erase current theorem?");
							
					if (confirm == JOptionPane.YES_OPTION) {
		
						DisplayCanvas canvas = session.getCurrentCanvas();

						canvas.setAndDescribeTheorem(new DTheorem("empty"));
						canvas.setPaintMode(false, false, false);
						canvas.repaint();

						parent.getGlyphsPanel().restoreFocus();
						
					}
					
					parent.repaint();
					
					break;

				case "store theorem":
					
					DTheorem theorem = session.getCurrentCanvas().getTheorem();
					DeductionBase base = session.getBase();

					String originalname = theorem.getName();
					String name = originalname;
					
					while (theorem.getName().equals("emptyD") | theorem.getName() == "") {	// another isOkName
						name = JOptionPane.showInputDialog("Name of theorem to store?");
						theorem.setName(name);
					}
					
					int confirmation;
					int inserted = -1;
										
					if (base.contains(theorem.getName() , "Theorems", "name")) {
						
						confirmation = JOptionPane.showConfirmDialog(this,"Name of theorem exists. Overwrite or other name?");						
					
						if (confirmation == JOptionPane.YES_OPTION) inserted = base.insert(theorem, true, session.primitivestable, session.compositestable);
						else {						
							if (confirmation == JOptionPane.NO_OPTION) {
							
								while (base.contains(name, "Theorems", "name")) {
								
									try {
										name = JOptionPane.showInputDialog("Name exists. Another name for the theorem?");
										theorem.setName(name);
									} catch (HeadlessException he) {
										name = "";
										if (Toolbox.DEBUGMINIMAL) 
											System.err.println("Error: input dialog for new name of " + theorem.getName() + " throwed exception."); 
										break;
									}
								}
								
								if (name != "")	
									inserted = base.insert(theorem, false, session.primitivestable, session.compositestable);
							
							} else {	// JOptionPane.CANCEL_OPTION
								theorem.setName(originalname.substring(0,originalname.length()-1));	// must not keep adding "D"
								return;							
							}
						}
					} else 
						inserted = base.insert(theorem, false, session.primitivestable, session.compositestable);
					
					if (inserted < 1) { if (Toolbox.DEBUGMINIMAL) System.err.println("Error: no insertion or theorem stored with 0 statements?"); }
	
					parent.repaint();

					break;

				case "open theorem":
					
					lsdialog.initialise();
					
					Toolbox.switchContainer(lsdialog, parent);
					
					parent.repaint();
					
					break;

				case "store session":
		
					session.saveSession();
		
					break;
		
				case "set session":
					
					session.saveSession();
		
					break;

				case "session quit":
		
					session.saveSession();
		
				case "quit":
		
					session.closeSession();
					trainer.cancel();
					DeductionFrame.cleanAndExit();
		
					break;
		
				case "picker":
					
					Toolbox.switchContainer(picker, parent);
					break;

				case "storage primitives":
					
					pldialog.initialise();

					Toolbox.switchContainer(pldialog, parent);
					break;	
				
				case "storage composites":
					
					cldialog.initialise();
					
					Toolbox.switchContainer(cldialog, parent);
					break;
					
				case "composer":
					
					DisplayCanvas canvas = session.getCurrentCanvas();

					if (canvas.getDrawn() != null) {
						
						glyphs = parent.getGlyphsPanel();

						composite.setGlyphsPanel(glyphs);
						
						Toolbox.switchContainer(composite, parent);
						
						composite.initialise(canvas.getDrawn());
					}
				
					break;

				case "trainer":
					
					glyphs = parent.getGlyphsPanel();

					this.bindings = glyphs.getBindings();
					
					trainer.setPrimitives(Toolbox.describe(bindings));
					
					Toolbox.switchContainer(trainer.getFrame(), parent);
					break;
										
				default:
		
					break;
				}		
		}

	
	private void makeMenus() {
		
		JMenu mnTheorem = new JMenu("Theorem");
		
		JMenuItem mntmNewTheorem = new JMenuItem("New theorem");					mntmNewTheorem.setActionCommand("new theorem");
		mntmNewTheorem.addActionListener(this);
		mntmNewTheorem.setAccelerator(KeyStroke.getKeyStroke('N',InputEvent.CTRL_DOWN_MASK));

		JMenuItem mntmStoreTheorem = new JMenuItem("Store theorem");				mntmStoreTheorem.setActionCommand("store theorem");
		mntmStoreTheorem.addActionListener(this);
		mntmStoreTheorem.setAccelerator(KeyStroke.getKeyStroke('S',InputEvent.CTRL_DOWN_MASK));
		
		JMenuItem mntmOpenTheorem = new JMenuItem("Open theorem");					mntmOpenTheorem.setActionCommand("open theorem");
		mntmOpenTheorem.addActionListener(this);
		mntmOpenTheorem.setAccelerator(KeyStroke.getKeyStroke('O',InputEvent.CTRL_DOWN_MASK));

		JMenuItem mntmSaveSession = new JMenuItem("Save current session");			mntmSaveSession.setActionCommand("store session");
		mntmSaveSession.addActionListener(this);
		
		JMenuItem mntmSetSession = new JMenuItem("Reset to old session");			mntmSetSession.setActionCommand("set session");
		mntmSetSession.addActionListener(this);

		JMenuItem mntmSaveSessionAndQuit = new JMenuItem("Save session and quit");	mntmSaveSessionAndQuit.setActionCommand("session quit");
		mntmSaveSessionAndQuit.addActionListener(this);
		
		JMenuItem mntmQuitWithoutSaving = new JMenuItem("Quit without saving");		mntmQuitWithoutSaving.setActionCommand("quit");
		mntmQuitWithoutSaving.addActionListener(this);
		mntmQuitWithoutSaving.setAccelerator(KeyStroke.getKeyStroke('Q',InputEvent.CTRL_DOWN_MASK));

		JMenu mnGlyps = new JMenu("Glyphs");
		
		JMenuItem mntmSelectPrimitives = new JMenuItem("Pick new primitives");		mntmSelectPrimitives.setActionCommand("picker");
		mntmSelectPrimitives.addActionListener(this);
		
		JMenuItem mntmLoadPrimitives = new JMenuItem("Load primitives");			mntmLoadPrimitives.setActionCommand("storage primitives");
		mntmLoadPrimitives.addActionListener(this);									
		
		JMenuItem mntmLoadComposites = new JMenuItem("Load composites");			mntmLoadComposites.setActionCommand("storage composites");
		mntmLoadComposites.addActionListener(this);
		
		JMenuItem mntmEditComposite = new JMenuItem("Edit composite");				mntmEditComposite.setActionCommand("composer");
		mntmEditComposite.addActionListener(this);
		
		JMenuItem mntmProgramBindings = new JMenuItem("Program bindings");			mntmProgramBindings.setActionCommand("trainer");
		mntmProgramBindings.addActionListener(this);
		
		this.add(mnTheorem);
		this.add(mnGlyps);

		mnTheorem.add(mntmNewTheorem);
		mnTheorem.add(mntmStoreTheorem);
		mnTheorem.add(mntmOpenTheorem);
		mnTheorem.add(mntmSaveSession);
		mnTheorem.add(mntmSetSession);
		mnTheorem.add(mntmSaveSessionAndQuit);
		mnTheorem.add(mntmQuitWithoutSaving);

		mnGlyps.add(mntmSelectPrimitives);
		mnGlyps.add(mntmLoadPrimitives);
		mnGlyps.add(mntmLoadComposites);
		mnGlyps.add(mntmEditComposite);
		mnGlyps.add(mntmProgramBindings);

	}

}
