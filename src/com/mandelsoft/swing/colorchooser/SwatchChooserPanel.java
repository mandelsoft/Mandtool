package com.mandelsoft.swing.colorchooser;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.accessibility.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;

/**
 * The standard color swatch chooser.
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with
 * future Swing releases. The current serialization support is
 * appropriate for short term storage or RMI between applications running
 * the same version of Swing.  As of 1.4, support for long term storage
 * of all JavaBeans<sup><font size="-2">TM</font></sup>
 * has been added to the <code>java.beans</code> package.
 * Please see {@link java.beans.XMLEncoder}.
 *
 * @version 1.34 11/17/05
 * @author Steve Wilson
 */
public class SwatchChooserPanel extends AbstractColorChooserPanel {

  SwatchPanel swatchPanel;
  UserSwatchPanel userSwatchPanel;
  MouseListener mainSwatchListener;
  MouseListener userSwatchListener;
  UserColorSource userColorSource;
  private static String userStr="User";

  public SwatchChooserPanel()
  {
    super();
    setInheritsPopupMenu(true);
  }

  public SwatchChooserPanel(UserColorSource src)
  {
    super();
    setInheritsPopupMenu(true);
    setUserColorSource(src);
  }

  public String getDisplayName()
  {
    return "Samples";
  }

  /**
   * Provides a hint to the look and feel as to the
   * <code>KeyEvent.VK</code> constant that can be used as a mnemonic to
   * access the panel. A return value <= 0 indicates there is no mnemonic.
   * <p>
   * The return value here is a hint, it is ultimately up to the look
   * and feel to honor the return value in some meaningful way.
   * <p>
   * This implementation looks up the value from the default
   * <code>ColorChooser.swatchesMnemonic</code>, or if it
   * isn't available (or not an <code>Integer</code>) returns -1.
   * The lookup for the default is done through the <code>UIManager</code>:
   * <code>UIManager.get("ColorChooser.swatchesMnemonic");</code>.
   *
   * @return KeyEvent.VK constant identifying the mnemonic; <= 0 for no
   *         mnemonic
   * @see #getDisplayedMnemonicIndex
   * @since 1.4
   */
  public int getMnemonic()
  {
    return -1;
  }

  /**
   * Provides a hint to the look and feel as to the index of the character in
   * <code>getDisplayName</code> that should be visually identified as the
   * mnemonic. The look and feel should only use this if
   * <code>getMnemonic</code> returns a value > 0.
   * <p>
   * The return value here is a hint, it is ultimately up to the look
   * and feel to honor the return value in some meaningful way. For example,
   * a look and feel may wish to render each
   * <code>AbstractColorChooserPanel</code> in a <code>JTabbedPane</code>,
   * and further use this return value to underline a character in
   * the <code>getDisplayName</code>.
   * <p>
   * This implementation looks up the value from the default
   * <code>ColorChooser.rgbDisplayedMnemonicIndex</code>, or if it
   * isn't available (or not an <code>Integer</code>) returns -1.
   * The lookup for the default is done through the <code>UIManager</code>:
   * <code>UIManager.get("ColorChooser.swatchesDisplayedMnemonicIndex");</code>.
   *
   * @return Character index to render mnemonic for; -1 to provide no
   *                   visual identifier for this panel.
   * @see #getMnemonic
   * @since 1.4
   */
  public int getDisplayedMnemonicIndex()
  {
    return -1;
  }

  public Icon getSmallDisplayIcon()
  {
    return null;
  }

  public Icon getLargeDisplayIcon()
  {
    return null;
  }

  public void setUserColorSource(UserColorSource src)
  {
    userColorSource=src;
  }

  /**
   * The background color, foreground color, and font are already set to the
   * defaults from the defaults table before this method is called.
   */
  public void installChooserPanel(JColorChooser enclosingChooser)
  {
    super.installChooserPanel(enclosingChooser);
  }

  protected void buildChooser()
  {

    GridBagLayout gb=new GridBagLayout();
    GridBagConstraints gbc=new GridBagConstraints();
    JPanel superHolder=new JPanel(gb);

    swatchPanel=new MainSwatchPanel();
    swatchPanel.putClientProperty(AccessibleContext.ACCESSIBLE_NAME_PROPERTY,
            getDisplayName());
    swatchPanel.setInheritsPopupMenu(true);

    userSwatchPanel=new UserSwatchPanel();
    userSwatchPanel.putClientProperty(
            AccessibleContext.ACCESSIBLE_NAME_PROPERTY,
            userStr);

    mainSwatchListener=new MainSwatchListener();
    swatchPanel.addMouseListener(mainSwatchListener);
    userSwatchListener=new UserSwatchListener();
    userSwatchPanel.addMouseListener(userSwatchListener);

    JPanel mainHolder=new JPanel(new BorderLayout());
    Border border=new CompoundBorder(new LineBorder(Color.black),
            new LineBorder(Color.white));
    //border=BorderFactory.createBevelBorder(BevelBorder.LOWERED);
    mainHolder.setBorder(border);
    mainHolder.add(swatchPanel, BorderLayout.CENTER);

//    gbc.anchor=GridBagConstraints.LAST_LINE_START;
//    gbc.gridwidth=1;
//    gbc.gridheight=3;
    gbc.gridheight=2;
    gbc.gridwidth=1;
    gbc.gridx=0;
    gbc.gridy=0;
    Insets oldInsets=gbc.insets;
    gbc.insets=new Insets(0, 0, 0, 10);
    superHolder.add(mainHolder, gbc);
    gbc.insets=oldInsets;

    userSwatchPanel.addMouseListener(userSwatchListener);
    userSwatchPanel.setInheritsPopupMenu(true);
    JPanel userHolder=new JPanel(new BorderLayout());
    userHolder.setBorder(border);
    userHolder.setInheritsPopupMenu(true);
    userHolder.add(userSwatchPanel, BorderLayout.CENTER);

    JLabel l=new JLabel(userStr);
    l.setLabelFor(userSwatchPanel);

//    gbc.gridwidth=GridBagConstraints.REMAINDER;
//    gbc.gridheight=1;
    gbc.gridheight=1;
    gbc.gridwidth=1;
    gbc.gridx=1;
    gbc.gridy=0;
    gbc.weighty=1.0;
    superHolder.add(l, gbc);

    gbc.weighty=0;
//    gbc.gridheight=GridBagConstraints.REMAINDER;
    gbc.gridheight=1;
    gbc.gridwidth=1;
    gbc.gridx=1;
    gbc.gridy=1;
    gbc.insets=new Insets(0, 0, 0, 2);
    superHolder.add(userHolder, gbc);

    JPanel buttonHolder = new JPanel();
    JButton b=new JButton(new SetColorAction());
    buttonHolder.add(b);
    b=new JButton(new RemoveColorAction());
    buttonHolder.add(b);

    gbc.gridwidth=2;
    gbc.gridheight=1;
    gbc.gridx=0;
    gbc.gridy=2;
    gbc.insets=new Insets( 10, 0, 0, 0);
    superHolder.add(buttonHolder,gbc);
   
    superHolder.setInheritsPopupMenu(true);

    add(superHolder);
  }

  public void uninstallChooserPanel(JColorChooser enclosingChooser)
  {
    super.uninstallChooserPanel(enclosingChooser);
    swatchPanel.removeMouseListener(mainSwatchListener);
    userSwatchPanel.removeMouseListener(userSwatchListener);
    swatchPanel=null;
    userSwatchPanel=null;
    mainSwatchListener=null;
    userSwatchListener=null;
    removeAll();  // strip out all the sub-components
  }

  public void updateChooser()
  {
  }

  public void addUserColor(Color color)
  {
    userSwatchPanel.addUserColor(color);
  }

  public void removeUserColor(Color color)
  {
    userSwatchPanel.removeUserColor(color);
  }

  class UserSwatchListener extends MouseAdapter implements Serializable {

    public void mousePressed(MouseEvent e)
    {
      Color color=userSwatchPanel.getColorForLocation(e.getX(), e.getY());
      getColorSelectionModel().setSelectedColor(color);

    }
  }

  class MainSwatchListener extends MouseAdapter implements Serializable {

    public void mousePressed(MouseEvent e)
    {
      Color color=swatchPanel.getColorForLocation(e.getX(), e.getY());
      getColorSelectionModel().setSelectedColor(color);
      //userSwatchPanel.setUserColor(color);

    }
  }

  class SetColorAction extends AbstractAction {
    public SetColorAction()
    { super("Memorize Color");
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
      addUserColor(getColorSelectionModel().getSelectedColor());
    }
  }

  class RemoveColorAction extends AbstractAction {
    public RemoveColorAction()
    { super("Remove Color");
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
      removeUserColor(getColorSelectionModel().getSelectedColor());
    }
  }

  public interface UserColorSource {
    Color getUserColor(int i);
  }

class SwatchPanel extends JPanel {

  protected Color[] colors;
  protected Dimension swatchSize;
  protected Dimension numSwatches;
  protected Dimension gap;

  public SwatchPanel()
  {
    initValues();
    initColors();
    setToolTipText(""); // register for events
    setOpaque(true);
    setBackground(Color.white);
    setRequestFocusEnabled(false);
    setInheritsPopupMenu(true);
  }

  public boolean isFocusTraversable()
  {
    return false;
  }

  protected void initValues()
  {
  }

  public void paintComponent(Graphics g)
  {
    g.setColor(getBackground());
    g.fillRect(0, 0, getWidth(), getHeight());
    for (int row=0; row<numSwatches.height; row++) {
      int y=row*(swatchSize.height+gap.height);
      for (int column=0; column<numSwatches.width; column++) {

        g.setColor(getColorForCell(column, row));
        int x;
        if ((!this.getComponentOrientation().isLeftToRight())&&
                (this instanceof UserSwatchPanel)) {
          x=(numSwatches.width-column-1)*(swatchSize.width+gap.width);
        }
        else {
          x=column*(swatchSize.width+gap.width);
        }
        g.fillRect(x, y, swatchSize.width, swatchSize.height);
        g.setColor(Color.black);
        g.drawLine(x+swatchSize.width-1, y, x+swatchSize.width-1,
                   y+swatchSize.height-1);
        g.drawLine(x, y+swatchSize.height-1, x+swatchSize.width-1,
                   y+swatchSize.height-1);
      }
    }
  }

  public Dimension getPreferredSize()
  {
    int x=numSwatches.width*(swatchSize.width+gap.width)-1;
    int y=numSwatches.height*(swatchSize.height+gap.height)-1;
    return new Dimension(x, y);
  }

  protected void initColors()
  {
  }

  public String getToolTipText(MouseEvent e)
  {
    Color color=getColorForLocation(e.getX(), e.getY());
    return color.getRed()+", "+color.getGreen()+", "+color.getBlue();
  }

  public Color getColorForLocation(int x, int y)
  {
    int column;
    if ((!this.getComponentOrientation().isLeftToRight())&&
            (this instanceof UserSwatchPanel)) {
      column=numSwatches.width-x/(swatchSize.width+gap.width)-1;
    }
    else {
      column=x/(swatchSize.width+gap.width);
    }
    int row=y/(swatchSize.height+gap.height);
    return getColorForCell(column, row);
  }

  private Color getColorForCell(int column, int row)
  {
    return colors[(row*numSwatches.width)+column]; // (STEVE) - change data orientation here
  }
}

class UserSwatchPanel extends SwatchPanel {

  protected void initValues()
  {
    swatchSize=UIManager.getDimension("ColorChooser.swatchesRecentSwatchSize");
    numSwatches=new Dimension(7, 7);
    gap=new Dimension(1, 1);
  }

  protected void initColors()
  {
    Color defaultUserColor=UIManager.getColor(
            "ColorChooser.swatchesDefaultRecentColor");
    int numColors=numSwatches.width*numSwatches.height;

    colors=new Color[numColors];
    for (int i=0; i<numColors; i++) {
      Color c=null;
      if (userColorSource!=null) {
        c=userColorSource.getUserColor(i);
      }
      if (c==null) c=defaultUserColor;
      colors[i]=c;
    }
  }

  public void addUserColor(Color c)
  {

    for (Color o:colors) {
      if (o.equals(c)) return;
    }
    System.arraycopy(colors, 0, colors, 1, colors.length-1);
    colors[0]=c;
    userColorAdded(c);
    repaint();
  }

  public void removeUserColor(Color c)
  {
    for (int i=0; i<colors.length; i++) {
      if (colors[i].equals(c)) {
        System.arraycopy(colors, i+1, colors, i, colors.length-i-1);
        Color n=null;
        if (userColorSource!=null) {
          n=userColorSource.getUserColor(i);
        }
        if (n==null) n=UIManager.getColor(
                                 "ColorChooser.swatchesDefaultRecentColor");
        colors[colors.length-1]=n;
        userColorRemoved(c);
        repaint();
      }
    }
  }
}

class MainSwatchPanel extends SwatchPanel {

  protected void initValues()
  {
    swatchSize=UIManager.getDimension("ColorChooser.swatchesSwatchSize");
    numSwatches=new Dimension(31, 9);
    gap=new Dimension(1, 1);
  }

  protected void initColors()
  {
    int[] rawValues=initRawValues();
    int numColors=rawValues.length/3;

    colors=new Color[numColors];
    for (int i=0; i<numColors; i++) {
      colors[i]=new Color(rawValues[(i*3)], rawValues[(i*3)+1],
                          rawValues[(i*3)+2]);
    }
  }

  private int[] initRawValues()
  {

    int[] rawValues={
      255, 255, 255, // first row.
      204, 255, 255,
      204, 204, 255,
      204, 204, 255,
      204, 204, 255,
      204, 204, 255,
      204, 204, 255,
      204, 204, 255,
      204, 204, 255,
      204, 204, 255,
      204, 204, 255,
      255, 204, 255,
      255, 204, 204,
      255, 204, 204,
      255, 204, 204,
      255, 204, 204,
      255, 204, 204,
      255, 204, 204,
      255, 204, 204,
      255, 204, 204,
      255, 204, 204,
      255, 255, 204,
      204, 255, 204,
      204, 255, 204,
      204, 255, 204,
      204, 255, 204,
      204, 255, 204,
      204, 255, 204,
      204, 255, 204,
      204, 255, 204,
      204, 255, 204,
      204, 204, 204, // second row.
      153, 255, 255,
      153, 204, 255,
      153, 153, 255,
      153, 153, 255,
      153, 153, 255,
      153, 153, 255,
      153, 153, 255,
      153, 153, 255,
      153, 153, 255,
      204, 153, 255,
      255, 153, 255,
      255, 153, 204,
      255, 153, 153,
      255, 153, 153,
      255, 153, 153,
      255, 153, 153,
      255, 153, 153,
      255, 153, 153,
      255, 153, 153,
      255, 204, 153,
      255, 255, 153,
      204, 255, 153,
      153, 255, 153,
      153, 255, 153,
      153, 255, 153,
      153, 255, 153,
      153, 255, 153,
      153, 255, 153,
      153, 255, 153,
      153, 255, 204,
      204, 204, 204, // third row
      102, 255, 255,
      102, 204, 255,
      102, 153, 255,
      102, 102, 255,
      102, 102, 255,
      102, 102, 255,
      102, 102, 255,
      102, 102, 255,
      153, 102, 255,
      204, 102, 255,
      255, 102, 255,
      255, 102, 204,
      255, 102, 153,
      255, 102, 102,
      255, 102, 102,
      255, 102, 102,
      255, 102, 102,
      255, 102, 102,
      255, 153, 102,
      255, 204, 102,
      255, 255, 102,
      204, 255, 102,
      153, 255, 102,
      102, 255, 102,
      102, 255, 102,
      102, 255, 102,
      102, 255, 102,
      102, 255, 102,
      102, 255, 153,
      102, 255, 204,
      153, 153, 153, // fourth row
      51, 255, 255,
      51, 204, 255,
      51, 153, 255,
      51, 102, 255,
      51, 51, 255,
      51, 51, 255,
      51, 51, 255,
      102, 51, 255,
      153, 51, 255,
      204, 51, 255,
      255, 51, 255,
      255, 51, 204,
      255, 51, 153,
      255, 51, 102,
      255, 51, 51,
      255, 51, 51,
      255, 51, 51,
      255, 102, 51,
      255, 153, 51,
      255, 204, 51,
      255, 255, 51,
      204, 255, 51,
      153, 255, 51,
      102, 255, 51,
      51, 255, 51,
      51, 255, 51,
      51, 255, 51,
      51, 255, 102,
      51, 255, 153,
      51, 255, 204,
      153, 153, 153, // Fifth row
      0, 255, 255,
      0, 204, 255,
      0, 153, 255,
      0, 102, 255,
      0, 51, 255,
      0, 0, 255,
      51, 0, 255,
      102, 0, 255,
      153, 0, 255,
      204, 0, 255,
      255, 0, 255,
      255, 0, 204,
      255, 0, 153,
      255, 0, 102,
      255, 0, 51,
      255, 0, 0,
      255, 51, 0,
      255, 102, 0,
      255, 153, 0,
      255, 204, 0,
      255, 255, 0,
      204, 255, 0,
      153, 255, 0,
      102, 255, 0,
      51, 255, 0,
      0, 255, 0,
      0, 255, 51,
      0, 255, 102,
      0, 255, 153,
      0, 255, 204,
      102, 102, 102, // sixth row
      0, 204, 204,
      0, 204, 204,
      0, 153, 204,
      0, 102, 204,
      0, 51, 204,
      0, 0, 204,
      51, 0, 204,
      102, 0, 204,
      153, 0, 204,
      204, 0, 204,
      204, 0, 204,
      204, 0, 204,
      204, 0, 153,
      204, 0, 102,
      204, 0, 51,
      204, 0, 0,
      204, 51, 0,
      204, 102, 0,
      204, 153, 0,
      204, 204, 0,
      204, 204, 0,
      204, 204, 0,
      153, 204, 0,
      102, 204, 0,
      51, 204, 0,
      0, 204, 0,
      0, 204, 51,
      0, 204, 102,
      0, 204, 153,
      0, 204, 204,
      102, 102, 102, // seventh row
      0, 153, 153,
      0, 153, 153,
      0, 153, 153,
      0, 102, 153,
      0, 51, 153,
      0, 0, 153,
      51, 0, 153,
      102, 0, 153,
      153, 0, 153,
      153, 0, 153,
      153, 0, 153,
      153, 0, 153,
      153, 0, 153,
      153, 0, 102,
      153, 0, 51,
      153, 0, 0,
      153, 51, 0,
      153, 102, 0,
      153, 153, 0,
      153, 153, 0,
      153, 153, 0,
      153, 153, 0,
      153, 153, 0,
      102, 153, 0,
      51, 153, 0,
      0, 153, 0,
      0, 153, 51,
      0, 153, 102,
      0, 153, 153,
      0, 153, 153,
      51, 51, 51, // eigth row
      0, 102, 102,
      0, 102, 102,
      0, 102, 102,
      0, 102, 102,
      0, 51, 102,
      0, 0, 102,
      51, 0, 102,
      102, 0, 102,
      102, 0, 102,
      102, 0, 102,
      102, 0, 102,
      102, 0, 102,
      102, 0, 102,
      102, 0, 102,
      102, 0, 51,
      102, 0, 0,
      102, 51, 0,
      102, 102, 0,
      102, 102, 0,
      102, 102, 0,
      102, 102, 0,
      102, 102, 0,
      102, 102, 0,
      102, 102, 0,
      51, 102, 0,
      0, 102, 0,
      0, 102, 51,
      0, 102, 102,
      0, 102, 102,
      0, 102, 102,
      0, 0, 0, // ninth row
      0, 51, 51,
      0, 51, 51,
      0, 51, 51,
      0, 51, 51,
      0, 51, 51,
      0, 0, 51,
      51, 0, 51,
      51, 0, 51,
      51, 0, 51,
      51, 0, 51,
      51, 0, 51,
      51, 0, 51,
      51, 0, 51,
      51, 0, 51,
      51, 0, 51,
      51, 0, 0,
      51, 51, 0,
      51, 51, 0,
      51, 51, 0,
      51, 51, 0,
      51, 51, 0,
      51, 51, 0,
      51, 51, 0,
      51, 51, 0,
      0, 51, 0,
      0, 51, 51,
      0, 51, 51,
      0, 51, 51,
      0, 51, 51,
      51, 51, 51};
    return rawValues;
  }
}
  //////////////////////////////////////////////////////////////////////////

  public interface UserColorListener {
    void colorAdded(Color c);
    void colorRemoved(Color c);
  }

  private Set<UserColorListener> listeners=new HashSet<UserColorListener>();

  public void addColorListener(UserColorListener h)
  {
    listeners.add(h);
  }

  public void removeColorListener(UserColorListener h)
  {
    listeners.remove(h);
  }

  protected void userColorAdded(Color c)
  {
    for (UserColorListener h:listeners) {
      h.colorAdded(c);
    }
  }

  protected void userColorRemoved(Color c)
  {
    for (UserColorListener h:listeners) {
      h.colorRemoved(c);
    }
  }
}