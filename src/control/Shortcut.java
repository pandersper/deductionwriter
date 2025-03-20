package control;

import javax.swing.KeyStroke;

/**
 * A binding between an UTF codepoint and a keyboard key and modifiers integer mask (a shortcut).
 * 
 * @see model.logic.abstraction.Formal#getCodepoint()
 * @see java.awt.AWTKeyStroke 
 */
public class Shortcut implements Comparable<Shortcut> {

	/** The keyboard key stroke integer code. 
	 * 
	 * @see java.awt.AWTKeyStroke 
	 */
	public Integer keycode;

	/** The modifiers mask.
	 * 
	 * @see java.awt.AWTKeyStroke 
	 */
	public Integer modifiers;

	/**
	 * Instantiates a new binding.
	 * 
	 * @param keycode The keycode.
	 * @param modifiers The modifiers mask.
	 */
	public Shortcut(int keycode, int modifiers) {
		this.keycode = keycode;
		this.modifiers = modifiers;		
	}

	/**
	 * Instantiates a new binding.
	 * 
	 * @param stroke A keyboard key stroke.
	 *
	 * @see javax.swing.KeyStroke
	 * @see java.awt.AWTKeyStroke
	 */
	public Shortcut(KeyStroke stroke) {
		this.keycode = stroke.getKeyCode();
		this.modifiers = stroke.getModifiers();		
	}

	/**
	 * Sets the key and modifier.
	 *
	 * @param second The new key and modifier given as another binding.
	 */
	public void setKeyAndModifier(Shortcut second) {

		this.keycode = second.keycode;
		this.modifiers = second.modifiers;
	}	

	/**
	 * Used for sortability. Otherwise nonsense semantics. Compares modifiers mask first and then codepoint. 
	 *
	 * @param other The binding compared to.
	 * 
	 * @return Minus one if smaller, zero if equal and positive one if bigger, as customary.
	 */
	public int compareTo(Shortcut other) {

		if (modifiers.equals(other.modifiers))
			return keycode.compareTo(other.keycode) ;
		else
			return modifiers.compareTo(other.modifiers);
	}

}
