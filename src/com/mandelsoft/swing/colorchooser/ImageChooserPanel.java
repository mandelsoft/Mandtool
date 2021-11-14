/*
 * * Rework from original ImageChooserPanel from Sun
 * (Copyright 2006 Sun Microsystems, Inc.)
 * @(#)DefaultRGBChooserPanel.java	1.31 03/01/23
 *
 */
package com.mandelsoft.swing.colorchooser;

import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import com.mandelsoft.swing.BufferedComponent;
import com.mandelsoft.swing.FilePanel;
import com.mandelsoft.swing.GBC;
import com.mandelsoft.swing.GBCPanel;
import com.mandelsoft.util.Utils;

/**
 * The standard RGB chooser.
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
 * @version based on 1.31 01/23/03
 * @author Steve Wilson
 * @author Mark Davidson
 * @see JColorChooser
 * @see AbstractColorChooserPanel
 */
public class ImageChooserPanel extends AbstractColorChooserPanel {

  private final int SCALE=4;
  private final int SIZE=150;

  private FilePanel file;
  private JScrollPane sp;
  private BufferedComponent buffer;
  private ImageListener listener;

  private PropertyChangeListener modellistener;
  private ColorImageModel model;
  
  public ImageChooserPanel()
  {
    super();
    setInheritsPopupMenu(true);
    modellistener=new ModelListener();
    listener=new ImageListener();
    setColorImageModel(new ColorImageModel());
  }

  public ColorImageModel getColorImageModel()
  {
    return model;
  }
  
  public void setColorImageModel(ColorImageModel model)
  {
    if (this.model!=null) {
      this.model.removePropertyChangeListener(modellistener);
    }
   
    this.model=model;
    this.model.addPropertyChangeListener(modellistener);
    if (buffer!=null) buffer.setImage(model.getImage());
    if (file!=null) file.setFilename(model.getFilename());
  }
  
  private Dimension limit(BufferedImage i, int w, int h)
  {
    if (w>i.getWidth()) {
      w=i.getWidth();
      //sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }
    else {
      //sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }
    if (h>i.getHeight()) {
      h=i.getHeight();
      //sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
    }
    else {
      //sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    }
    return new Dimension(w, h);
  }

  public String getDisplayName()
  {
    return "ImageColors";
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
   * <code>ColorChooser.rgbMnemonic</code>, or if it
   * isn't available (or not an <code>Integer</code>) returns -1.
   * The lookup for the default is done through the <code>UIManager</code>:
   * <code>UIManager.get("ColorChooser.rgbMnemonic");</code>.
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
   * <code>UIManager.get("ColorChooser.rgbDisplayedMnemonicIndex");</code>.
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
    Color color=getColorFromModel();
    //setBorder(new EmptyBorder(10, 10, 10, 10));

    final GBCPanel panel=new GBCPanel();
    buffer=new BufferedComponent(model.getImage());
    buffer.setScale(SCALE);
    buffer.setScaleMode(true);
    buffer.getContentPane().addMouseListener(listener);
    buffer.getContentPane().addMouseMotionListener(listener);

    sp=new JScrollPane(buffer);
    sp.setVisible(true);
    sp.setPreferredSize(new Dimension(SIZE,SIZE));
    panel.add(sp, new GBC(0, 0));
    String n=model.getFilename();
    if (Utils.isEmpty(n)) n="C:/work/Mandelbrot.Test/src/my/mand/resources/splash.png";
    file=new FilePanel("Image filename",
            //"",getFrame());
            n, null);
    //file.setMinimumSize(new Dimension(100,10));
    file.addPropertyChangeListener(FilePanel.PROP_FILENAME,modellistener);
    panel.add(file, new GBC(0, 1));
    JButton b=new JButton("Load");
    b.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e)
      {
        String n=file.getFilename();
        if (Utils.isEmpty(n)) return;
        File f=new File(n);
        try {
          BufferedImage image=ImageIO.read(f);
          //System.out.println(image.getWidth());
          model.setImage(image);
        //ImageDialog imgd=new ImageDialog(getFrame(),image);
        //imgd.setVisible(true);
        }
        catch (IOException ex) {
        }
      }
    });
    panel.add(b, new GBC(0, 2).setFill(GBC.NONE));
    add(panel);
  }

  public void uninstallChooserPanel(JColorChooser enclosingChooser)
  {
    super.uninstallChooserPanel(enclosingChooser);
    buffer.removeMouseMotionListener(listener);
    buffer.removeMouseListener(listener);
    buffer=null;
    removeAll();
  }

  public void updateChooser()
  {
  }

  private boolean adjusting=false;
  
  private class ModelListener implements PropertyChangeListener {

    public void propertyChange(PropertyChangeEvent evt)
    {
      if (evt.getSource()==model) {
        //System.out.println("EV: "+evt.getPropertyName());
        if (evt.getPropertyName().equals(ColorImageModel.PROP_IMAGE)) {
          BufferedImage image=model.getImage();
          sp.setVisible(true);
          sp.setPreferredSize(limit(image, SIZE, SIZE));
          buffer.setImage(image);
          sp.revalidate();
        }
        else if (evt.getPropertyName().equals(ColorImageModel.PROP_FILENAME)) {
          if (!adjusting) {
            adjusting=true;
             file.setFilename(model.getFilename());
            adjusting=false;
          }
        }
      }
      else {
        //System.out.println("fEV: "+evt.getPropertyName());
        if (!adjusting) {
          adjusting=true;
          model.setFilename((String)evt.getNewValue());
          adjusting=false;
        }
      }
    }   
  }
  
  private class ImageListener extends MouseAdapter {

    private boolean active;

    @Override
    public void mouseDragged(MouseEvent e)
    {
      if (active) handle(e);
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
      if (e.getButton()==MouseEvent.BUTTON1)
        active=false;
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
      if (e.getButton()==MouseEvent.BUTTON1) {
        active=true;
        handle(e);
      }
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
       if (e.getButton()==MouseEvent.BUTTON1) handle(e);
    }

    private Color getColor(MouseEvent e)
    {
      int x=(int)Math.round(e.getX()/buffer.getScaleX());
      int y=(int)Math.round(e.getY()/buffer.getScaleY());
      BufferedImage im=buffer.getImage();
      //System.out.println("color at "+x+","+y+": "+im.getRGB(x, y));
      return new Color(im.getRGB(x, y));
    }

    private void handle(MouseEvent e)
    {
      int x=(int)Math.round((e.getX()-buffer.getScaleX()/2)/buffer.getScaleX());
      int y=(int)Math.round((e.getY()-buffer.getScaleY()/2)/buffer.getScaleY());
      BufferedImage im=buffer.getImage();
      if (im==null || im.getWidth()<=x || im.getHeight()<=y) return;
      //System.out.println("color at "+x+","+y+": "+im.getRGB(x, y));
      getColorSelectionModel().setSelectedColor(new Color(im.getRGB(x, y)));
    }
  }

}


