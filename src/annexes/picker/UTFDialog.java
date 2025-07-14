package annexes.picker;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JLabel;
import java.awt.Color;
import javax.swing.border.TitledBorder;

import control.statics.ViewStatics;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HexFormat;
import javax.swing.JTextArea;

/**
 * Small dialog for UTF code point iput.
 */
public class UTFDialog extends JDialog implements ActionListener {

	
	private final class UTFKeyAdapter extends KeyAdapter {
	
		public void keyReleased(KeyEvent ke) {

			if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
				
				ke.consume();
				
				JTextField txf = (JTextField) ke.getSource();
				
				glyphset = parseFields(txf);
				
				if (glyphset) fillFields();
				else 
					highlightError();
			}
		}
	}

	private DeductionPicker<?> parent;
	 
	private final JPanel contentPanel = new JPanel();

	private JTextArea txaName;
	private JTextField txfDecimal, txfHex, txfChar;

	private int codepoint;
	private boolean glyphset;
	
	private String character, decimalstring, hexstring, utfname;
	
	
	/**
	 * Dialog for importing UTF-16 glyphs one by one.
	 * 
	 * @param The deduction picker component that opens this dialog.
	 */
	public UTFDialog(DeductionPicker<?> parent) {
	
		this.parent = parent;
		
		setBounds(100, 100, 450, 220);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new TitledBorder(null, "Find UTF-8 character by code", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		txfDecimal 	= new JTextField();		txfHex 		= new JTextField();
		txaName 	= new JTextArea();		txfChar 	= new JTextField();
		
		txaName.setRows(3);
		txaName.setLineWrap(true);
		txaName.setBackground(new Color(211, 211, 211));
		

		txfHex.setName("hex");
		txfDecimal.setName("decimal");
		
		txfDecimal.addKeyListener(new UTFKeyAdapter());
		txfHex.addKeyListener(new UTFKeyAdapter());

		txfDecimal.setColumns(10);
		txfHex.setColumns(10);		
		txaName.setColumns(10);
		txfChar.setColumns(10);

		txfChar.setBackground(new Color(248, 248, 255));

		txaName.setEditable(false);
		txfChar.setEditable(false);
		
		txfHex.setText("0373");
		txaName.setText("SMALL LETTER ARCHAIC SAMPI");

		JLabel lblDecimal = new JLabel("Decimal");		JLabel lblHex = new JLabel("Hex");
		JLabel lblUtfName = new JLabel("UTF Name");		JLabel lblCharacter = new JLabel("Character");

		contentPanel.add(txfDecimal);		contentPanel.add(txfHex);		contentPanel.add(txaName);		contentPanel.add(txfChar);
		contentPanel.add(lblDecimal);		contentPanel.add(lblHex);		contentPanel.add(lblUtfName);

		lblDecimal.setBounds(12, 43, 61, 15);		lblHex.setBounds(84, 43, 70, 15);
		lblUtfName.setBounds(144, 43, 70, 15);		lblCharacter.setBounds(355, 43, 70, 15);
		
		txfDecimal.setBounds(12, 70, 45, 28);		txfHex.setBounds(84, 70, 45, 28);
		txaName.setBounds(141, 70, 202, 62);		txfChar.setBounds(355, 70, 73, 28);
		
		contentPanel.add(lblCharacter);

		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			
			{
				JButton okButton = new JButton("Add");
				okButton.addActionListener(UTFDialog.this);
				okButton.setActionCommand("add");
				buttonPane.add(okButton);
			}

			{
				JButton cancelButton = new JButton("Close");
				cancelButton.addActionListener(UTFDialog.this);
				cancelButton.setActionCommand("cancel");
				buttonPane.add(cancelButton);
			}
		}
		
		txfDecimal.setFocusCycleRoot(true);
	}

	
	private void highlightError() {

		String hexstringtmp = hexstring, decimalstringtmp = decimalstring;

		hexstring 		= questionIfNonempty(hexstring);	decimalstring	= questionIfNonempty(decimalstring);
		utfname 		= questionIfNonempty(utfname);		character 		= questionIfNonempty(character);

		txfDecimal.setText(decimalstring);
		txfHex.setText(hexstring);		
		txaName.setText(utfname);
		txfChar.setText(character);
		
		txfDecimal.setBackground(Color.red);		txfHex.setBackground(Color.red);

		this.paint(this.getGraphics());
		
		try { Thread.sleep(900); } catch (InterruptedException e) {}

		hexstring 		=  hexstringtmp;		decimalstring	= decimalstringtmp;
		utfname 		= "";					character 		= "";
		
		fillFields();

		txfDecimal.setBackground(ViewStatics.floralwhite);		txfHex.setBackground(ViewStatics.floralwhite);
	}

	private static String questionIfNonempty(String s) {
		return (s != "") ? ("?"+s+"?") : "?";
	}

	private void fillFields() {

		txfDecimal.setText(decimalstring);
		txfHex.setText(hexstring);		
		txaName.setText(utfname);
		txfChar.setText(character);
	}

	private boolean parseFields(JTextField txf) {
		
		try {
			
			if (txf.getName().equals("decimal")) { 
				
				decimalstring 	= txf.getText();				
				hexstring 		= HexFormat.of().toHexDigits(codepoint);
				
			} else {	
				
				hexstring 		= txf.getText();
				decimalstring	= Integer.toString(codepoint);
			}
			
			String nonzerolead = hexstring;
			int i = 0;
			char c = hexstring.charAt(i);
			
			while (c == '0' && i < hexstring.length()-1) {
				i++;
				nonzerolead = hexstring.substring(i);
				c = hexstring.charAt(i);			
			}
			
			hexstring = nonzerolead;
	
			codepoint 		= Integer.parseInt(decimalstring);
			utfname 		= Character.getName(codepoint);		
			character 		= Character.toString(codepoint);
	
			return true;
		
		} catch (Exception e) { return false; }
	}


	public void actionPerformed(ActionEvent ae) {
	
		String command = ae.getActionCommand();
		
		switch (command) {
		
			case ("cancel"):
				
				ViewStatics.switchContainer(parent, this);
			
				break;
			
			case ("add"):

				if (glyphset) {
					parent.addCustomGLyph(codepoint);
					ViewStatics.switchContainer(parent, this);
				}
			
				break;

			default:
				break;
		}
	}
}
