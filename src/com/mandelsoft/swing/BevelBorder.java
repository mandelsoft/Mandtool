
package com.mandelsoft.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.border.AbstractBorder;

public class BevelBorder extends AbstractBorder {

  /** Raised bevel type. */
  public static final int RAISED=0;
  /** Lowered bevel type. */
  public static final int LOWERED=1;
  protected int bevelType;
  protected int bevelWidth;
  protected Color highlightOuter;
  protected Color highlightInner;
  protected Color shadowInner;
  protected Color shadowOuter;

  /**
   * Creates a bevel border with the specified type and whose
   * colors will be derived from the background color of the
   * component passed into the paintBorder method.
   * @param bevelType the type of bevel for the border
   */
  public BevelBorder(int bevelType)
  {
    this(bevelType, 2);
  }

  public BevelBorder(int bevelType, int width)
  {
    this.bevelType=bevelType;
    this.bevelWidth=width;
  }

  /**
   * Creates a bevel border with the specified type, highlight and
   * shadow colors.
   * @param bevelType the type of bevel for the border
   * @param highlight the color to use for the bevel highlight
   * @param shadow the color to use for the bevel shadow
   */
  public BevelBorder(int bevelType, Color highlight, Color shadow)
  {
    this(bevelType, highlight.brighter(), highlight, shadow, shadow.brighter());
  }

  public BevelBorder(int bevelType, int width, Color highlight, Color shadow)
  {
    this(bevelType, width, highlight.brighter(), highlight, shadow, shadow.brighter());
  }

  /**
   * Creates a bevel border with the specified type, highlight and
   * shadow colors.
   * <p>
   * Note: The shadow inner and outer colors are
   * switched for a lowered bevel border.
   *
   * @param bevelType the type of bevel for the border
   * @param highlightOuterColor the color to use for the bevel outer highlight
   * @param highlightInnerColor the color to use for the bevel inner highlight
   * @param shadowOuterColor the color to use for the bevel outer shadow
   * @param shadowInnerColor the color to use for the bevel inner shadow
   */
  public BevelBorder(int bevelType, Color highlightOuterColor,
                     Color highlightInnerColor, Color shadowOuterColor,
                     Color shadowInnerColor)
  {
    this(bevelType,2,highlightOuterColor,highlightInnerColor,
                     shadowOuterColor,shadowInnerColor);
  }

  public BevelBorder(int bevelType, int width,  Color highlightOuterColor,
                     Color highlightInnerColor, Color shadowOuterColor,
                     Color shadowInnerColor)
  {
    this(bevelType, width);
    this.highlightOuter=highlightOuterColor;
    this.highlightInner=highlightInnerColor;
    this.shadowOuter=shadowOuterColor;
    this.shadowInner=shadowInnerColor;
  }

  /**
   * Paints the border for the specified component with the specified
   * position and size.
   * @param c the component for which this border is being painted
   * @param g the paint graphics
   * @param x the x position of the painted border
   * @param y the y position of the painted border
   * @param bevelWidth the bevelWidth of the painted border
   * @param height the height of the painted border
   */
  @Override
  public void paintBorder(Component c, Graphics g, int x, int y, int width,
                          int height)
  {
    if (bevelType==RAISED) {
      paintRaisedBevel(c, g, x, y, width, height);

    }
    else if (bevelType==LOWERED) {
      paintLoweredBevel(c, g, x, y, width, height);
    }
  }

  /**
   * Returns the insets of the border.
   * @param c the component for which this border insets value applies
   */
  @Override
  public Insets getBorderInsets(Component c)
  {
    return new Insets(bevelWidth, bevelWidth, bevelWidth, bevelWidth);
  }

  /**
   * Reinitialize the insets parameter with this Border's current Insets.
   * @param c the component for which this border insets value applies
   * @param insets the object to be reinitialized
   */
  @Override
  public Insets getBorderInsets(Component c, Insets insets)
  {
    insets.left=insets.top=insets.right=insets.bottom=bevelWidth;
    return insets;
  }

  /**
   * Returns the outer highlight color of the bevel border
   * when rendered on the specified component.  If no highlight
   * color was specified at instantiation, the highlight color
   * is derived from the specified component's background color.
   * @param c the component for which the highlight may be derived
   * @since 1.3
   */
  public Color getHighlightOuterColor(Component c)
  {
    Color highlight=getHighlightOuterColor();
    return highlight!=null?highlight
            :c.getBackground().brighter().brighter();
  }

  /**
   * Returns the inner highlight color of the bevel border
   * when rendered on the specified component.  If no highlight
   * color was specified at instantiation, the highlight color
   * is derived from the specified component's background color.
   * @param c the component for which the highlight may be derived
   * @since 1.3
   */
  public Color getHighlightInnerColor(Component c)
  {
    Color highlight=getHighlightInnerColor();
    return highlight!=null?highlight
            :c.getBackground().brighter();
  }

  /**
   * Returns the inner shadow color of the bevel border
   * when rendered on the specified component.  If no shadow
   * color was specified at instantiation, the shadow color
   * is derived from the specified component's background color.
   * @param c the component for which the shadow may be derived
   * @since 1.3
   */
  public Color getShadowInnerColor(Component c)
  {
    Color shadow=getShadowInnerColor();
    return shadow!=null?shadow
            :c.getBackground().darker();
  }

  /**
   * Returns the outer shadow color of the bevel border
   * when rendered on the specified component.  If no shadow
   * color was specified at instantiation, the shadow color
   * is derived from the specified component's background color.
   * @param c the component for which the shadow may be derived
   * @since 1.3
   */
  public Color getShadowOuterColor(Component c)
  {
    Color shadow=getShadowOuterColor();
    return shadow!=null?shadow
            :c.getBackground().darker().darker();
  }

  /**
   * Returns the outer highlight color of the bevel border.
   * Will return null if no highlight color was specified
   * at instantiation.
   * @since 1.3
   */
  public Color getHighlightOuterColor()
  {
    return highlightOuter;
  }

  /**
   * Returns the inner highlight color of the bevel border.
   * Will return null if no highlight color was specified
   * at instantiation.
   * @since 1.3
   */
  public Color getHighlightInnerColor()
  {
    return highlightInner;
  }

  /**
   * Returns the inner shadow color of the bevel border.
   * Will return null if no shadow color was specified
   * at instantiation.
   * @since 1.3
   */
  public Color getShadowInnerColor()
  {
    return shadowInner;
  }

  /**
   * Returns the outer shadow color of the bevel border.
   * Will return null if no shadow color was specified
   * at instantiation.
   * @since 1.3
   */
  public Color getShadowOuterColor()
  {
    return shadowOuter;
  }

  /**
   * Returns the type of the bevel border.
   */
  public int getBevelType()
  {
    return bevelType;
  }

   /**
   * Returns the bevelWidth of the bevel border.
   */
  public int getBevelWidth()
  {
    return bevelWidth;
  }

  /**
   * Returns whether or not the border is opaque.
   */
  @Override
  public boolean isBorderOpaque()
  {
    return true;
  }

  protected Color getGradient(Color c1, Color c2, int a)
  {
    return new Color( c1.getRed()+(c2.getRed()-c1.getRed())*a/(bevelWidth-1),
                      c1.getGreen()+(c2.getGreen()-c1.getGreen())*a/(bevelWidth-1),
                      c1.getBlue()+(c2.getBlue()-c1.getBlue())*a/(bevelWidth-1));
  }

  protected void paintRaisedBevel(Component c, Graphics g, int x, int y,
                                  int width, int height)
  {
    Color oldColor=g.getColor();
    int h=height;
    int w=width;
    int d=bevelWidth/2;

    g.translate(x, y);

    for (int i=0; i<bevelWidth; i++) {
      g.setColor(getGradient(getHighlightOuterColor(c),getHighlightInnerColor(c),i));
      g.drawLine(i, i, i, h-2-i);
      g.drawLine(1+i, i, w-2-i, i);
    }

    for (int i=0; i<bevelWidth; i++) {
      g.setColor(getGradient(getShadowOuterColor(c),getShadowInnerColor(c),i));
      g.drawLine(0+i, h-1-i, w-1-i, h-1-i);
      g.drawLine(w-1-i, 0+i, w-1-i, h-2-i);
    }

    g.translate(-x, -y);
    g.setColor(oldColor);

  }

  protected void paintLoweredBevel(Component c, Graphics g, int x, int y,
                                   int width, int height)
  {
    Color oldColor=g.getColor();
    int h=height;
    int w=width;
    int d=bevelWidth/2;

    g.translate(x, y);

    for (int i=0; i<bevelWidth; i++) {
      g.setColor(getGradient(getShadowInnerColor(c),getShadowOuterColor(c),i));
      g.drawLine(0+i, 0+i, 0+i, h-1-i);
      g.drawLine(1+i, 0+i, w-1-i, 0+i);
    }

    for (int i=0; i<bevelWidth; i++) {
      g.setColor(getGradient(getHighlightOuterColor(c),getHighlightInnerColor(c),i));
      g.drawLine(1+i, h-1-i, w-1-i, h-1-i);
      g.drawLine(w-1-i, 1+i, w-1-i, h-2-i);
    }

    g.translate(-x, -y);
    g.setColor(oldColor);

    javax.swing.border.BevelBorder q;
  }
}
