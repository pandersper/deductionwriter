package view.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;

public final class ViewConstants {

	public static Rectangle2D buttonbounds;		// set by NavigatePanel computing the smallest 
	
	private static final InitConstants init = new InitConstants();

	private static final GraphicsEnvironment guienv = GraphicsEnvironment.getLocalGraphicsEnvironment();

	public static final GraphicsDevice 			device 			= guienv.getDefaultScreenDevice();
	public static final GraphicsConfiguration 	configuration 	= device.getDefaultConfiguration();
	public static final BufferedImage 			testimage 		= configuration.createCompatibleImage(100,100);
	
	public static final Color 	btnBkgr 		= new Color(82, 151, 252);
	public static final Color 	btnBkgrAlarm 	= new Color(255, 69, 0);
	public static final Insets 	btnInset		= new Insets(2, 2, 2, 2);
	public static final Font 	btnFontBold		= new Font("Dialog", Font.BOLD, 8);
	public static final Font 	btnFont			= new Font("Dialog", Font.PLAIN, 8);
	public static final Font 	btnFontPlus		= new Font("Dialog", Font.PLAIN, 9);
	public static final int 	btnvgap 		= 4; 
	public static final int 	btnhgap 		= 2;

	public static final SoftBevelBorder btnBorder 	= new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null);
	public static final Rectangle2D 	charbounds 	= btnFont.getMaxCharBounds(((Graphics2D)testimage.getGraphics()).getFontRenderContext());

	public static final int 	a4height 	= 297;	
	public static final int 	a4width 	= 210;
	public static final double 	afactor		= 594.0 / 420;
	
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

}
