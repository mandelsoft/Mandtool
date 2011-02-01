/*
 * RGB Chooser Panel Alternative
 */
package com.mandelsoft.swing.colorchooser;

import com.mandelsoft.swing.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.awt.image.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;

/**
 
 */
public class RGBChooserPanel extends AbstractColorChooserPanel
                              implements ChangeListener, HierarchyListener {

  private transient RGBImage palette;
  private transient RGBImage sliderPalette;
  private transient Image paletteImage;
  private transient Image sliderPaletteImage;
  private JSlider slider;
  private JSpinner redField;
  private JSpinner greenField;
  private JSpinner blueField;
  private JTextField hField;
  private JTextField sField;
  private JTextField bField;
  private boolean isAdjusting=false; // Flag which indicates that values are set internally
  private Point paletteSelection=new Point();
  private JLabel paletteLabel;
  private JLabel sliderPaletteLabel;
  private JRadioButton redRadio;
  private JRadioButton greenRadio;
  private JRadioButton blueRadio;

  private static final int PALETTE_DIMENSION=200;

  private static final int MAX_RED_VALUE=255;
  private static final int MAX_GREEN_VALUE=255;
  private static final int MAX_BLUE_VALUE=255;

  private static final int MAX_HUE_VALUE=359;
  private static final int MAX_SATURATION_VALUE=100;
  private static final int MAX_BRIGHTNESS_VALUE=100;
  private int currentMode=RED_MODE;
  private static final int RED_MODE=0;
  private static final int GREEN_MODE=1;
  private static final int BLUE_MODE=2;

  public RGBChooserPanel()
  {
  }

  private void addPaletteListeners()
  {
    paletteLabel.addMouseListener(new MouseAdapter() {

      public void mousePressed(MouseEvent e)
      {
        float[] rgb=new float[3];
        palette.getRGBForLocation(e.getX(), e.getY(), rgb);
        updateRGB(rgb[0], rgb[1], rgb[2]);
      }
    });

    paletteLabel.addMouseMotionListener(new MouseMotionAdapter() {

      public void mouseDragged(MouseEvent e)
      {
        int labelWidth=paletteLabel.getWidth();

        int labelHeight=paletteLabel.getHeight();
        int x=e.getX();
        int y=e.getY();

        if (x>=labelWidth) {
          x=labelWidth-1;
        }

        if (y>=labelHeight) {
          y=labelHeight-1;
        }

        if (x<0) {
          x=0;
        }

        if (y<0) {
          y=0;
        }

        float[] rgb=new float[3];
        System.out.println(""+x+","+y);
        palette.getRGBForLocation(x, y, rgb);
        updateRGB(rgb[0], rgb[1], rgb[2]);
      }
    });
  }

  private void updatePalette(float r, float g, float b)
  {
    int x=0;
    int y=0;

    switch (currentMode) {
      case RED_MODE:
        if (r!=palette.getRed()) {
          palette.setRed(r);
          palette.nextFrame();
        }
        x=(int)(g*PALETTE_DIMENSION/MAX_GREEN_VALUE);
        y=(int)(b*PALETTE_DIMENSION/MAX_GREEN_VALUE);
        break;
      case GREEN_MODE:
        if (g!=palette.getGreen()) {
          palette.setGreen(g);
          palette.nextFrame();
        }
        x=(int)(r*PALETTE_DIMENSION/MAX_RED_VALUE);
        y=(int)(b*PALETTE_DIMENSION/MAX_BLUE_VALUE);
        break;
      case BLUE_MODE:
        if (b!=palette.getBlue()) {
          palette.setBlue(b);
          palette.nextFrame();
        }
        x=(int)(r*PALETTE_DIMENSION/MAX_RED_VALUE);
        y=(int)(g*PALETTE_DIMENSION/MAX_GREEN_VALUE);
        break;
    }

    paletteSelection.setLocation(x, y);
    paletteLabel.repaint();
  }

  private void updateSlider(float r, float g, float b)
  {
    // Update the slider palette if necessary.
  
    float value=0f;

    switch (currentMode) {
      case RED_MODE:
        value=r;
        break;
      case GREEN_MODE:
        value=g;
        break;
      case BLUE_MODE:
        value=b;
        break;
    }

    slider.setValue(Math.round(value*(slider.getMaximum())/MAX_RED_VALUE));
  }

  private void updateRGBTextFields(float red, float green, float blue)
  {
    int r=(int)red;
    int g=(int)green;
    int b=(int)blue;

    if (((Integer)redField.getValue()).intValue()!=r) {
      redField.setValue(new Integer(r));
    }
    if (((Integer)greenField.getValue()).intValue()!=g) {
      greenField.setValue(new Integer(g));
    }
    if (((Integer)blueField.getValue()).intValue()!=b) {
      blueField.setValue(new Integer(b));
    }
  }

  /**
   * Updates the values of the RGB fields to reflect the new color change
   */
  private void updateHSBTextFields(Color color)
  { float[] hsb=new float[3];
    Color.RGBtoHSB(color.getRed(),color.getGreen(),color.getBlue(),hsb);
    int h=Math.round(hsb[0]*MAX_HUE_VALUE);
    int s=Math.round(hsb[1]*MAX_SATURATION_VALUE);
    int b=Math.round(hsb[2]*MAX_BRIGHTNESS_VALUE);

//    if (((Integer)hField.getValue()).intValue()!=h) {
//      hField.setValue(new Integer(h));
//    }
//    if (((Integer)sField.getValue()).intValue()!=s) {
//      sField.setValue(new Integer(s));
//    }
//    if (((Integer)bField.getValue()).intValue()!=b) {
//      bField.setValue(new Integer(b));
//    }
    hField.setText(String.valueOf(h));
    sField.setText(String.valueOf(s));
    bField.setText(String.valueOf(b));
  }

  /**
   * Main internal method of updating the ui controls and the color model.
   */
  private void updateRGB(float r, float g, float b)
  {
    if (!isAdjusting) {
      isAdjusting=true;

      updatePalette(r, g, b);
      updateSlider(r, g, b);
      updateRGBTextFields(r, g, b);

      Color color=new Color((int)r, (int)g, (int)b);

      updateHSBTextFields(color);

      getColorSelectionModel().setSelectedColor(color);

      isAdjusting=false;
    }
  }

  /**
   * Invoked automatically when the model's state changes.
   * It is also called by <code>installChooserPanel</code> to allow
   * you to set up the initial state of your chooser.
   * Override this method to update your <code>ChooserPanel</code>.
   */
  public void updateChooser()
  {
    if (!isAdjusting) {
      float[] rgb=getRGBColorFromModel();
      updateRGB(rgb[0], rgb[1], rgb[2]);
    }
  }

  public void installChooserPanel(JColorChooser enclosingChooser)
  {
    super.installChooserPanel(enclosingChooser);
    setInheritsPopupMenu(true);
    addHierarchyListener(this);
  }

  /**
   * Invoked when the panel is removed from the chooser.
   */
  public void uninstallChooserPanel(JColorChooser enclosingChooser)
  {
    super.uninstallChooserPanel(enclosingChooser);
    cleanupPalettesIfNecessary();
    removeAll();
    removeHierarchyListener(this);
  }

  /**
   * Returns an float array containing the HSB values of the selected color from
   * the ColorSelectionModel
   */
  private float[] getRGBColorFromModel()
  {
    Color color=getColorFromModel();
    float[] rgb=new float[3];
    rgb[0]=color.getRed();
    rgb[1]=color.getGreen();
    rgb[2]=color.getBlue();
    return rgb;
  }

  /**
   * Builds a new chooser panel.
   */
  protected void buildChooser()
  {
    setLayout(new BorderLayout());
    JComponent spp=buildSliderPalettePanel();
    spp.setInheritsPopupMenu(true);
    add(spp, BorderLayout.BEFORE_LINE_BEGINS);

    JPanel controlHolder=new JPanel(new GridBagLayout());
    JComponent hsbControls=buildHSBControls();
    hsbControls.setInheritsPopupMenu(true);
    controlHolder.add(hsbControls, new GBC(0,0));

    controlHolder.add(new JLabel(" "),new GBC(1,0)); // spacer

    JComponent rgbControls=buildRGBControls();
    rgbControls.setInheritsPopupMenu(true);
    controlHolder.add(rgbControls, new GBC(0,1));
    controlHolder.setInheritsPopupMenu(true);

    controlHolder.setBorder(new EmptyBorder(10, 5, 10, 5));
    add(controlHolder, BorderLayout.CENTER);
  }

  /**
   * Creates the panel with the uneditable RGB field
   */
  private JComponent buildHSBControls()
  {
    JPanel panel=new JPanel(new GridBagLayout());
    panel.setInheritsPopupMenu(true);

    Color color=getColorFromModel();
    float[] hsb=Color.RGBtoHSB(color.getRed(),
                               color.getGreen(), color.getBlue(),null);

    hField=new JTextField(String.valueOf(hsb[0]), 3);
    hField.setEditable(false);
    hField.setHorizontalAlignment(JTextField.RIGHT);
    hField.setInheritsPopupMenu(true);

    sField=new JTextField(String.valueOf(hsb[1]), 3);
    sField.setEditable(false);
    sField.setHorizontalAlignment(JTextField.RIGHT);
    sField.setInheritsPopupMenu(true);

    bField=new JTextField(String.valueOf(hsb[2]), 3);
    bField.setEditable(false);
    bField.setHorizontalAlignment(JTextField.RIGHT);
    bField.setInheritsPopupMenu(true);

    String hString="H";
    String sString="S";
    String bString="B";

    panel.add(new JLabel(hString),new GBC(0,0));
    panel.add(hField, new GBC(1,0));
    panel.add(new JLabel(sString),new GBC(0,1));
    panel.add(sField,new GBC(1,1));
    panel.add(new JLabel(bString), new GBC(0,2));
    panel.add(bField, new GBC(1,2));

    return panel;
  }

  /**
   * Creates the panel with the editable HSB fields and the radio buttons.
   */
  private JComponent buildRGBControls()
  {

    String redString="R";
    String greenString="G";
    String blueString="B";

    RadioButtonHandler handler=new RadioButtonHandler();

    redRadio=new JRadioButton(redString);
    redRadio.addActionListener(handler);
    redRadio.setSelected(true);
    redRadio.setInheritsPopupMenu(true);

    greenRadio=new JRadioButton(greenString);
    greenRadio.addActionListener(handler);
    greenRadio.setInheritsPopupMenu(true);

    blueRadio=new JRadioButton(blueString);
    blueRadio.addActionListener(handler);
    blueRadio.setInheritsPopupMenu(true);

    ButtonGroup group=new ButtonGroup();
    group.add(redRadio);
    group.add(greenRadio);
    group.add(blueRadio);

    Color c=getColorFromModel();

    redField  =new JSpinner(new SpinnerNumberModel(c.getRed(),
                                                   0, MAX_RED_VALUE, 1));
    greenField=new JSpinner(new SpinnerNumberModel(c.getGreen(),
                                                   0, MAX_GREEN_VALUE, 1));
    blueField =new JSpinner(new SpinnerNumberModel(c.getBlue(),
                                                   0, MAX_BLUE_VALUE, 1));

    redField.addChangeListener(this);
    greenField.addChangeListener(this);
    blueField.addChangeListener(this);

    redField.setInheritsPopupMenu(true);
    greenField.setInheritsPopupMenu(true);
    blueField.setInheritsPopupMenu(true);

    JPanel panel=new JPanel(new GridBagLayout());

    panel.add(redRadio,new GBC(0,0));
    panel.add(redField,new GBC(1,0));
    panel.add(greenRadio,new GBC(0,1));
    panel.add(greenField,new GBC(1,1));
    panel.add(blueRadio,new GBC(0,2));
    panel.add(blueField,new GBC(1,2));
    panel.setInheritsPopupMenu(true);

    return panel;
  }

  /**
   * Handler for the radio button classes.
   */
  private class RadioButtonHandler implements ActionListener {

    public void actionPerformed(ActionEvent evt)
    {
      Object obj=evt.getSource();

      if (obj instanceof JRadioButton) {
        JRadioButton button=(JRadioButton)obj;
        if (button==redRadio) {
          setMode(RED_MODE);
        }
        else if (button==greenRadio) {
          setMode(GREEN_MODE);
        }
        else if (button==blueRadio) {
          setMode(BLUE_MODE);
        }
      }
    }
  }

  private void setMode(int mode)
  {
    if (currentMode==mode) {
      return;
    }

    isAdjusting=true;  // Ensure no events propagate from changing slider value.
    currentMode=mode;

    Color c=getColorFromModel();
    int r=c.getRed();
    int g=c.getGreen();
    int b=c.getBlue();
    switch (currentMode) {
      case RED_MODE:
        slider.setInverted(true);
        slider.setMaximum(MAX_RED_VALUE);
        slider.setValue(r);
        palette.setValues(RGBImage.RSQUARE, r,g,b);
        sliderPalette.setValues(RGBImage.RSLIDER, r,g,b);
        break;
      case GREEN_MODE:
        slider.setInverted(true);
        slider.setMaximum(MAX_GREEN_VALUE);
        slider.setValue(g);
        palette.setValues(RGBImage.GSQUARE, r,g,b);
        sliderPalette.setValues(RGBImage.GSLIDER, r,g,b);
        break;
      case BLUE_MODE:
        slider.setInverted(true);
        slider.setMaximum(MAX_BLUE_VALUE);
        slider.setValue(b);
        palette.setValues(RGBImage.BSQUARE, r,g,b);
        sliderPalette.setValues(RGBImage.BSLIDER, r,g,b);
        break;
    }

    isAdjusting=false;

    palette.nextFrame();
    sliderPalette.nextFrame();

    updateChooser();
  }

  protected JComponent buildSliderPalettePanel()
  {

    // This slider has to have a minimum of 0.  A lot of math in this file is simplified due to this.
    slider=new JSlider(JSlider.VERTICAL, 0, MAX_RED_VALUE, 0);
    slider.setInverted(true);
    slider.setPaintTrack(false);
    slider.setPreferredSize(new Dimension(slider.getPreferredSize().width,
                                          PALETTE_DIMENSION+15));
    slider.addChangeListener(this);
    slider.setInheritsPopupMenu(true);
    // We're not painting ticks, but need to ask UI classes to
    // paint arrow shape anyway, if possible.
    slider.putClientProperty("Slider.paintThumbArrowShape", Boolean.TRUE);
    paletteLabel=createPaletteLabel();
    addPaletteListeners();
    sliderPaletteLabel=new JLabel();

    JPanel panel=new JPanel();
    panel.add(paletteLabel);
    panel.add(slider);
    panel.add(sliderPaletteLabel);

    initializePalettesIfNecessary();

    return panel;
  }

  private void initializePalettesIfNecessary()
  {
    if (palette!=null) {
      return;
    }

    Color c=getColorFromModel();
    int r=c.getRed();
    int g=c.getGreen();
    int b=c.getBlue();
    switch (currentMode) {
      case RED_MODE:
        palette=new RGBImage(RGBImage.RSQUARE, PALETTE_DIMENSION,
                             PALETTE_DIMENSION, r,g,b);
        sliderPalette=new RGBImage(RGBImage.RSLIDER, 16, PALETTE_DIMENSION,
                             r,g,b);
        break;
      case GREEN_MODE:
        palette=new RGBImage(RGBImage.GSQUARE, PALETTE_DIMENSION,
                             PALETTE_DIMENSION, r,g,b);
        sliderPalette=new RGBImage(RGBImage.GSLIDER, 16, PALETTE_DIMENSION,
                             r,g,b);
        break;
      case BLUE_MODE:
        palette=new RGBImage(RGBImage.BSQUARE, PALETTE_DIMENSION,
                             PALETTE_DIMENSION, r,g,b);
        sliderPalette=new RGBImage(RGBImage.BSLIDER, 16, PALETTE_DIMENSION,
                             r,g,b);
        break;
    }
    paletteImage=Toolkit.getDefaultToolkit().createImage(palette);
    sliderPaletteImage=Toolkit.getDefaultToolkit().createImage(sliderPalette);

    paletteLabel.setIcon(new ImageIcon(paletteImage));
    sliderPaletteLabel.setIcon(new ImageIcon(sliderPaletteImage));
  }

  private void cleanupPalettesIfNecessary()
  {
    if (palette==null) {
      return;
    }

    palette.aborted=true;
    sliderPalette.aborted=true;

    palette.nextFrame();
    sliderPalette.nextFrame();

    palette=null;
    sliderPalette=null;

    paletteImage=null;
    sliderPaletteImage=null;

    paletteLabel.setIcon(null);
    sliderPaletteLabel.setIcon(null);
  }

  protected JLabel createPaletteLabel()
  {
    return new JLabel() {

      protected void paintComponent(Graphics g)
      {
        super.paintComponent(g);
        g.setColor(Color.white);
        g.drawOval(paletteSelection.x-4, paletteSelection.y-4, 8, 8);
      }
    };
  }

  public String getDisplayName()
  {
    return "RGB";
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
   * <code>ColorChooser.hsbMnemonic</code>, or if it
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
    return  -1;
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
   * <code>UIManager.get("ColorChooser.hsbDisplayedMnemonicIndex");</code>.
   *
   * @return Character index to render mnemonic for; -1 to provide no
   *                   visual identifier for this panel.
   * @see #getMnemonic
   * @since 1.4
   */
  public int getDisplayedMnemonicIndex()
  {
    return  -1;
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
   * Class for the slider and palette images.
   */
  class RGBImage extends SyntheticImage {

    protected float r=.0f;
    protected float g=.0f;
    protected float b=.0f;
    protected float[] rgb=new float[3];
    protected boolean isDirty=true;
    protected int cachedY;
    protected int cachedColor;
    protected int type;
    private static final int RSQUARE=0;
    private static final int GSQUARE=1;
    private static final int BSQUARE=2;
    private static final int RSLIDER=3;
    private static final int GSLIDER=4;
    private static final int BSLIDER=5;

    protected RGBImage(int type, int width, int height,
                       float r, float g, float b)
    {
      super(width, height);
      setValues(type, r, g, b);
    }

    public void setValues(int type, float r, float g, float b)
    {
      this.type=type;
      cachedY=-1;
      cachedColor=0;
      setRed(r);
      setGreen(g);
      setBlue(b);
    }

    public final void setRed(float red)
    {
      r=red;
    }

    public final void setGreen(float green)
    {
      g=green;
    }

    public final void setBlue(float blue)
    {
      b=blue;
    }

    public final float getRed()
    {
      return r;
    }

    public final float getGreen()
    {
      return g;
    }

    public final float getBlue()
    {
      return b;
    }

    protected boolean isStatic()
    {
      return false;
    }

    public synchronized void nextFrame()
    {
      isDirty=true;
      notifyAll();
    }

    public synchronized void addConsumer(ImageConsumer ic)
    {
      isDirty=true;
      super.addConsumer(ic);
    }

    private int getRGBForLocation(int x, int y)
    {
      if (type>=RSLIDER&&y==cachedY) {
        return cachedColor;
      }

      getRGBForLocation(x, y, rgb);
      cachedY=y;
      cachedColor=new Color((int)rgb[0], (int)rgb[1], (int)rgb[2]).getRGB();

      return cachedColor;
    }

    public void getRGBForLocation(int x, int y, float[] rgbArray)
    {
      float xStep=((float)x)/(width-1);
      float yStep=((float)y)/(height-1);

      switch (type) {
        case RSQUARE: {
          
          rgbArray[0]=r;
          rgbArray[1]=xStep*(MAX_GREEN_VALUE);
          rgbArray[2]=yStep*(MAX_BLUE_VALUE);
          break;
        }
        case GSQUARE: {
          rgbArray[0]=xStep*(MAX_RED_VALUE);
          rgbArray[1]=g;
          rgbArray[2]=yStep*(MAX_BLUE_VALUE);
          break;
        }
        case BSQUARE: {
          rgbArray[0]=xStep*(MAX_RED_VALUE);
          rgbArray[1]=yStep*(MAX_GREEN_VALUE);
          rgbArray[2]=b;
          break;
        }

        case RSLIDER: {
          rgbArray[0]=y*yStep;
          rgbArray[1]=0;
          rgbArray[2]=0;
          break;
        }
        case GSLIDER: {
          rgbArray[0]=0;
          rgbArray[1]=y*yStep;
          rgbArray[2]=0;
          break;
        }
        case BSLIDER: {
          rgbArray[0]=0;
          rgbArray[1]=0;
          rgbArray[2]=y*yStep;
          break;
        }
      }
    }

    /**
     * Overriden method from SyntheticImage
     */
    protected void computeRow(int y, int[] row)
    {
      if (y==0) {
        synchronized (this) {
          try {
            while (!isDirty) {
              wait();
            }
          }
          catch (InterruptedException ie) {
          }
          isDirty=false;
        }
      }

      if (aborted) {
        return;
      }

      for (int i=0; i<row.length; ++i) {
        row[i]=getRGBForLocation(i, y);
      }
    }
  }

  public void stateChanged(ChangeEvent e)
  {
    if (e.getSource()==slider) {
      boolean modelIsAdjusting=slider.getModel().getValueIsAdjusting();

      if (true || !modelIsAdjusting&&!isAdjusting) {
        int sliderValue=slider.getValue();
        int sliderRange=slider.getMaximum();
        int value=sliderValue;

        float[] rgb=getRGBColorFromModel();

        switch (currentMode) {
          case RED_MODE:
            updateRGB(value, rgb[1], rgb[2]);
            break;
          case GREEN_MODE:
            updateRGB(rgb[0], value, rgb[2]);
            break;
          case BLUE_MODE:
            updateRGB(rgb[0], rgb[1], value);
            break;
        }
      }
    }
    else if (e.getSource() instanceof JSpinner) {
      int red=((Integer)redField.getValue());
      int green=((Integer)greenField.getValue());
      int blue=((Integer)blueField.getValue());

      updateRGB(red, green, blue);
    }
  }

  public void hierarchyChanged(HierarchyEvent he)
  {
    if ((he.getChangeFlags()&HierarchyEvent.DISPLAYABILITY_CHANGED)!=0) {
      if (isDisplayable()) {
        initializePalettesIfNecessary();
      }
      else {
        cleanupPalettesIfNecessary();
      }
    }
  }
}
