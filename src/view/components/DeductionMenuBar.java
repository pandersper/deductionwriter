package view.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import java.util.ArrayList;

import annexes.maker.CompositeMaker;
import annexes.picker.DeductionPicker;
import annexes.trainer.DeductionTrainer;
import control.db.DeductionBase;
import control.session.Session;
import control.session.Shortcut;
import control.statics.Toolbox;
import control.statics.ViewStatics;
import model.description.DTheorem;
import model.independent.DoubleArray;
import model.logic.abstraction.Formal;
import view.components.dialogs.CompositesStore;
import view.components.dialogs.PrimitivesLoader;
import view.components.dialogs.SessionLoader;
import view.components.dialogs.TheoremStore;
import view.DeductionFrame;
import view.abstraction.AbstractFrame;

/**
 * Thee menu bar of this application. It has a quite extensive actionPerformed method that reaches out and does things
 * in almost all parts of the program. Therefore many parts of the program has to be introduced into the constructor.
 */
public class DeductionMenuBar extends JMenuBar implements ActionListener {

	/**
	 *  Session currently in use 
	 */
	public Session			session;

	private DeductionFrame 	parent;	

	private final SessionLoader<AbstractFrame>		ssdialog;
	private final TheoremStore<AbstractFrame> 		lsdialog;
	private final PrimitivesLoader<AbstractFrame> 	pldialog;
	private final CompositesStore<AbstractFrame> 	cldialog;
	
	private final DeductionTrainer 		trainer;	
	private final CompositeMaker 		composite;
	private final DeductionPicker<?> 	picker;

	/** 
	 * Bindings currently in use 
	 */
	protected DoubleArray<Formal, Shortcut> 	bindings = new  DoubleArray<Formal, Shortcut>();

	
	/**
	 * Constructor confining almost all sections of the application and creating all dialogs used now and then.
	 * Annexes: {@see DeductionPicker},{@see DeductionTrainer},{@see DeductionFrame}.
	 * Dialogs: {@see TheoremStore},{@see PrimitivesLoader},{@see CompositesStore},{@see SessionLoader}.
	 * 
	 * @param trainer	The annex application for mapping short cuts to keyboard keys.
	 * @param picker	The annex application for chosing glyphs to use.
	 * @param parent	The main application frame.
	 */
	public DeductionMenuBar(DeductionTrainer trainer, DeductionPicker<?> picker, DeductionFrame parent) {
	
		this.trainer = trainer;
		this.picker = picker;
		this.parent = parent;		
		this.session = this.parent.getSession();
		
		this.composite = new CompositeMaker(this.parent);
		
		lsdialog = new TheoremStore<AbstractFrame>(this.parent, this.session);
		pldialog = new PrimitivesLoader<AbstractFrame>(this.parent, this.session);
		cldialog = new CompositesStore<AbstractFrame>(this.parent, this.session);
		ssdialog = new SessionLoader<AbstractFrame>(this.parent,this.session);

		makeMenus();
	}

	
	/**
	 * Sets the one at a time session.
	 * 
	 * @param session Contains all the user works.
	 */
	public Session getSession() { return session; }
	
	/**
	 * Name of the currently used primmitives table.
	 * 
	 * @return Primitives table's name.
	 */
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

	/** 
	 * Extensive action performed hub handling all menu item's actions. 
	 */
	public void actionPerformed(ActionEvent e) {
		
		GlyphsPanel 	glyphs;
		
		switch (e.getActionCommand()) {

				case "new theorem":
		
					int confirm = JOptionPane.showConfirmDialog(this, "Really erase current theorem?");
							
					if (confirm == JOptionPane.YES_OPTION) {

						session.replaceCurrentWork(new DTheorem("empty"));
						
						parent.getGlyphsPanel().restoreFocus();					
					}
					
					parent.repaint();
					
					break;

				case "store theorem":
					
					DTheorem theorem 	= session.getCurrentCanvas().getTheorem();
					DeductionBase base 	= session.getBase();

					String originalname = theorem.getName();
					String name = originalname;

					int confirmation;
					int inserted = -1;

					boolean _OVERWRITE = false;

					while (name.equals("empty") | name.equals("")) {
						
						name = JOptionPane.showInputDialog("Name of theorem to store?");
						
						if (base.contains(name,"Theorems","name")) {
							
							confirmation = JOptionPane.showConfirmDialog(this,"Name of theorem exists. Overwrite or other name? (no)");	
							
							switch (confirmation) {
							
								case (JOptionPane.NO_OPTION):
									
									while (base.contains(name,"Theorems","name")) 										
										name = JOptionPane.showInputDialog("Name exists. Another name for the theorem?");
									continue;
									
								case (JOptionPane.YES_OPTION): 	
									
									_OVERWRITE = true;				
									break;	
									
								case (JOptionPane.CANCEL_OPTION): 	
									
									theorem.setName(originalname); 	
									return;
									
								default: System.err.println("Unknow JOptionPane choice."); break; 
							}
						}
					}

					theorem.setName(name);
					inserted = base.insert(theorem,_OVERWRITE);
					
					if (inserted < 1) { System.err.println("Error: 0 or less statements in insertion."); }
	
					parent.repaint();

					break;

				case "open theorem":
					
					lsdialog.initialise();
					
					ViewStatics.switchContainer(lsdialog, parent);
					
					parent.repaint();
					
					break;

				case "new session":
										
					parent.newSession();
					
					break;
					
				case "rename session":

					name = JOptionPane.showInputDialog("Name of session to store?");
					
					break;
					
				case "store session":

					ssdialog.initialise();
					
					ViewStatics.switchContainer(ssdialog, parent);
					
					parent.repaint();
							
					break;
		
				case "open session":

					ssdialog.initialise();
					
					ViewStatics.switchContainer(ssdialog, parent);
					
					parent.repaint();
							
					break;

				case "session quit":
		
					session.saveSession();
		
				case "quit":
		
					parent.cleanAndExit();
		
					break;
		
				case "picker":
					
					ViewStatics.switchContainer(picker, parent);
					
					break;

				case "storage primitives":
					
					pldialog.initialise();

					ViewStatics.switchContainer(pldialog, parent);
					
					break;	
				
				case "storage composites":
					
					cldialog.initialise();
					
					ViewStatics.switchContainer(cldialog, parent);
					
					break;
					
				case "composer":
					
					DisplayCanvas canvas = session.getCurrentCanvas();

					if (canvas.getDrawn() != null) {
						
						glyphs = parent.getGlyphsPanel();

						glyphs.removeButton(canvas.getDrawn());
						
						composite.setGlyphsPanel(glyphs);
						
						ViewStatics.switchContainer(composite, parent);
						
						composite.initialise(canvas.getDrawn().clone());
					}
				
					break;

				case "trainer":
					
					glyphs = parent.getGlyphsPanel();

					this.bindings = glyphs.getBindings();
					
					trainer.setPrimitives(Toolbox.describe(bindings));
					
					ViewStatics.switchContainer(trainer.getFrame(), parent);
					
					break;
										
				default:
		
					break;
				}		
	}

	private void makeMenus() {
		
		JMenu mnTheorem = new JMenu("Theorem");
		
		JMenuItem mntmNewTheorem = new JMenuItem("New theorem");					mntmNewTheorem.setActionCommand("new theorem");
		mntmNewTheorem.addActionListener(this);

		JMenuItem mntmStoreTheorem = new JMenuItem("Store theorem");				mntmStoreTheorem.setActionCommand("store theorem");
		mntmStoreTheorem.addActionListener(this);
				
		JMenuItem mntmOpenTheorem = new JMenuItem("Open theorem");					mntmOpenTheorem.setActionCommand("open theorem");
		mntmOpenTheorem.addActionListener(this);
		
		JMenuItem mntmSaveSession = new JMenuItem("Save current session");			mntmSaveSession.setActionCommand("store session");
		mntmSaveSession.addActionListener(this);
		
		JMenuItem mntmSetSession = new JMenuItem("Open old session");				mntmSetSession.setActionCommand("open session");
		mntmSetSession.addActionListener(this);

		JMenuItem mntmSaveSessionAndQuit = new JMenuItem("Save session and quit");	mntmSaveSessionAndQuit.setActionCommand("session quit");
		mntmSaveSessionAndQuit.addActionListener(this);
		
		JMenuItem mntmQuitWithoutSaving = new JMenuItem("Quit without saving");		mntmQuitWithoutSaving.setActionCommand("quit");
		mntmQuitWithoutSaving.addActionListener(this);
		
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
		
		mntmNewTheorem.setAccelerator(KeyStroke.getKeyStroke('N',InputEvent.CTRL_DOWN_MASK));
		mntmStoreTheorem.setAccelerator(KeyStroke.getKeyStroke('S',InputEvent.CTRL_DOWN_MASK));
		mntmOpenTheorem.setAccelerator(KeyStroke.getKeyStroke('O',InputEvent.CTRL_DOWN_MASK));
		mntmQuitWithoutSaving.setAccelerator(KeyStroke.getKeyStroke('Q',InputEvent.CTRL_DOWN_MASK));
		
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
