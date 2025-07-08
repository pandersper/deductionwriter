package control.statics;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.event.InputEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RGBImageFilter;
import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;

import javax.swing.ImageIcon;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;

import control.session.Shortcut;
import model.description.abstraction.Described;
import view.abstraction.InitiableContainer;

public final class ViewStatics {

	
	public static Rectangle2D buttonbounds;		// set by NavigatePanel computing the smallest 
	
	private static final InitConstants init = new InitConstants();

	private static final GraphicsEnvironment 	guienv = GraphicsEnvironment.getLocalGraphicsEnvironment();

	public static final GraphicsDevice 			device 			= guienv.getDefaultScreenDevice();
	public static final GraphicsConfiguration 	configuration 	= device.getDefaultConfiguration();
	public static final BufferedImage 			testimage 		= configuration.createCompatibleImage(100,100);
	
	public static final Color 		btnBkgr 		= new Color(80, 150, 240);
	public static final Color 		btnBkgrAlarm 	= new Color(255, 70, 0);
	public static final Insets 		btnInset		= new Insets(1, 1, 1, 1);
	public static final Dimension 	btnSize 		= new Dimension(80, 18);
	public static final Font 		btnFontBold		= new Font("Dialog", Font.BOLD, 8);
	public static final Font 		btnFontPlus		= new Font("Dialog", Font.PLAIN, 9);
	public static final Font 		btnFont			= new Font("Dialog", Font.PLAIN, 8);
	public static final int 		btnvgap 		= 4; 
	public static final int 		btnhgap 		= 2;

	public static final SoftBevelBorder btnBorder 	= new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null);
	public static final Rectangle2D 	charbounds 	= btnFont.getMaxCharBounds(((Graphics2D)testimage.getGraphics()).getFontRenderContext());

	public static final int 	a4height 	= 297;	
	public static final int 	a4width 	= 210;
	public static final double 	afactor		= 594.0 / 420;

	public static final Color 		floralwhite 	= new Color(255, 250, 240);

	public static final Dimension 	cncpnlsize 		= new Dimension(220,300);
	public static final Dimension 	navpnlsize 		= new Dimension(300,150);
	public static final Dimension 	pckpnlminsize 	= new Dimension(40, 260);
	public static final Dimension	pckpnlmaxsize 	= new Dimension(1000,1000);

	public static final Dimension 	canvasdimension 		= new Dimension((int)(2*a4width), (int)(2*a4height));
	public static final Dimension 	makercanvasdimension 	= new Dimension((int)(1.2*a4width), (int)(1.2*a4height));

	public static final List<String> CATEGORIES 	= Arrays.asList(new String[] {"default", "lowercase", "uppercase", "algebra", "logic", "fundamental_sets", "custom"});

	private static class InitConstants {
		
		private InitConstants() {
			checkEnvironment();
		}
		
		private static void checkEnvironment() {			
			
			GraphicsEnvironment.getLocalGraphicsEnvironment();
			
			if (GraphicsEnvironment.isHeadless()) {
				System.err.println("No graphics environment! Exiting.");
				System.exit(-1);
			}
		}
	}

	public static final class Mode {

		// Changes and confirm
		public static final boolean CURSOR 		= true;
		public static final boolean PRELIMINARY = true;
		public static final boolean CONFIRM 	= true; 
		public static final boolean PAINT		= true; 
	}

	
	/**
	 * Filter for drawing selected items.
	 */
	public final static class 		SelectedFilter extends RGBImageFilter {	
		/** {@inheritDoc} */
		public int filterRGB(int x, int y, int rgb) {
			
			return (rgb & 0x8888ff88);	// greenish
		}
	}
	/**
	 * Filter for drawing pressed items.
	 */
	public final static class 		PressedFilter extends RGBImageFilter {	
		/** {@inheritDoc} */
		public int filterRGB(int x, int y, int rgb) {
			
			return (rgb & 0x888888ff);	// blueish
		}
	}
	/**
	 * Filter for drawing composites.
	 */
	public final static class 		CompositeFilter extends RGBImageFilter {	
		/** {@inheritDoc} */
		public int filterRGB(int x, int y, int rgb) {
			
			return (rgb & 0xffaaffaa);
		}
	}

	public static Dimension grow(Dimension d, int dx, int dy) {
		return new Dimension(d.width + dx, d.height + dy);
	}
	
	public static String bindingString(Shortcut bound) {
		return (bound != null) ? ((char) (int) bound.keycode) + InputEvent.getModifiersExText(bound.modifiers) : "?";
	}
	
	public static ImageIcon describedIcon(Described formal) {
		return new ImageIcon(formal.getImage());
	}

	/**
	 * Switch from one container to another in the gui of the application.
	 * @param <F>
	 * @param <T>
	 *
	 * @param to The container switched to, going to.
	 * @param from The container switched from, comming from.
	 */
	public static <F extends Container & InitiableContainer,T>  void switchContainer(Container to, Container from) {
		from.setVisible(false);
		from.repaint();
		to.setVisible(true);
		to.repaint();
	}
	/**
	 * Switch from one container to another in the gui of the application.
	 * @param <F>
	 * @param <T>
	 *
	 * @param to The container switched to, going to.
	 * @param from The container switched from, comming from.
	 */
	public static <F extends Container & InitiableContainer,T>  void switchContainer(F to, Container from, UnaryOperator<T> initiator, T initial) {
		from.setVisible(false);
		from.repaint();
		if (initiator!=null)
			to.initiate(initiator, initial);
		to.setVisible(true);
		to.repaint();
	}
}
