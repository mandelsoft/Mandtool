/*
 * Rework from original HSBChooserPanel from Sun
 * (Copyright 2006 Sun Microsystems, Inc.)
 * see @(#)DefaultHSBChooserPanel.java	1.27 05/11/17
 *
 */
package com.mandelsoft.swing.colorchooser;


import com.mandelsoft.swing.GBC;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.ImageConsumer;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Implements the default HSB Color chooser
 *
 *  @version based on 1.27 11/17/05
 *  @author Tom Santos
 *  @author Steve Wilson
 *  @author Mark Davidson
 *  @author Shannon Hickey
 */
public class HSBChooserPanel extends AbstractColorChooserPanel implements
        ChangeListener, HierarchyListener {

  private transient HSBImage palette;
  private transient HSBImage sliderPalette;
  private transient Image paletteImage;
  private transient Image sliderPaletteImage;
  private JSlider slider;
  private JSpinner hField;
  private JSpinner sField;
  private JSpinner bField;
  private JTextField redField;
  private JTextField greenField;
  private JTextField blueField;
  private boolean isAdjusting=false; // Flag which indicates that values are set internally
  private Point paletteSelection=new Point();
  private JLabel paletteLabel;
  private JLabel sliderPaletteLabel;
  private JRadioButton hRadio;
  private JRadioButton sRadio;
  private JRadioButton bRadio;
  private static final int PALETTE_DIMENSION=200;
  private static final int MAX_HUE_VALUE=359;
  private static final int MAX_SATURATION_VALUE=100;
  private static final int MAX_BRIGHTNESS_VALUE=100;
  private int currentMode=HUE_MODE;
  private static final int HUE_MODE=0;
  private static final int SATURATION_MODE=1;
  private static final int BRIGHTNESS_MODE=2;

  public HSBChooserPanel()
  {
  }

  private void addPaletteListeners()
  {
    paletteLabel.addMouseListener(new MouseAdapter() {

      public void mousePressed(MouseEvent e)
      {
        float[] hsb=new float[3];
        palette.getHSBForLocation(e.getX(), e.getY(), hsb);
        updateHSB(hsb[0], hsb[1], hsb[2]);
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

        float[] hsb=new float[3];
        palette.getHSBForLocation(x, y, hsb);
        updateHSB(hsb[0], hsb[1], hsb[2]);
      }
    });
  }

  private void updatePalette(float h, float s, float b)
  {
    int x=0;
    int y=0;

    switch (currentMode) {
      case HUE_MODE:
        if (h!=palette.getHue()) {
          palette.setHue(h);
          palette.nextFrame();
        }
        x=PALETTE_DIMENSION-(int)(s*PALETTE_DIMENSION);
        y=PALETTE_DIMENSION-(int)(b*PALETTE_DIMENSION);
        break;
      case SATURATION_MODE:
        if (s!=palette.getSaturation()) {
          palette.setSaturation(s);
          palette.nextFrame();
        }
        x=(int)(h*PALETTE_DIMENSION);
        y=PALETTE_DIMENSION-(int)(b*PALETTE_DIMENSION);
        break;
      case BRIGHTNESS_MODE:
        if (b!=palette.getBrightness()) {
          palette.setBrightness(b);
          palette.nextFrame();
        }
        x=(int)(h*PALETTE_DIMENSION);
        y=PALETTE_DIMENSION-(int)(s*PALETTE_DIMENSION);
        break;
    }

    paletteSelection.setLocation(x, y);
    paletteLabel.repaint();
  }

  private void updateSlider(float h, float s, float b)
  {
    // Update the slider palette if necessary.
    // When the slider is the hue slider or the hue hasn't changed,
    // the hue of the palette will not need to be updated.
    if (currentMode!=HUE_MODE&&h!=sliderPalette.getHue()) {
      sliderPalette.setHue(h);
      sliderPalette.nextFrame();
    }

    float value=0f;

    switch (currentMode) {
      case HUE_MODE:
        value=h;
        break;
      case SATURATION_MODE:
        value=s;
        break;
      case BRIGHTNESS_MODE:
        value=b;
        break;
    }

    slider.setValue(Math.round(value*(slider.getMaximum())));
  }

  private void updateHSBTextFields(float hue, float saturation, float brightness)
  {
    int h=Math.round(hue*359);
    int s=Math.round(saturation*100);
    int b=Math.round(brightness*100);

    if (((Integer)hField.getValue()).intValue()!=h) {
      hField.setValue(new Integer(h));
    }
    if (((Integer)sField.getValue()).intValue()!=s) {
      sField.setValue(new Integer(s));
    }
    if (((Integer)bField.getValue()).intValue()!=b) {
      bField.setValue(new Integer(b));
    }
  }

  /**
   * Updates the values of the RGB fields to reflect the new color change
   */
  private void updateRGBTextFields(Color color)
  {
    redField.setText(String.valueOf(color.getRed()));
    greenField.setText(String.valueOf(color.getGreen()));
    blueField.setText(String.valueOf(color.getBlue()));
  }

  /**
   * Main internal method of updating the ui controls and the color model.
   */
  private void updateHSB(float h, float s, float b)
  {
    if (!isAdjusting) {
      isAdjusting=true;

      updatePalette(h, s, b);
      updateSlider(h, s, b);
      updateHSBTextFields(h, s, b);

      Color color=Color.getHSBColor(h, s, b);
      updateRGBTextFields(color);

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
      float[] hsb=getHSBColorFromModel();
      updateHSB(hsb[0], hsb[1], hsb[2]);
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
  private float[] getHSBColorFromModel()
  {
    Color color=getColorFromModel();
    float[] hsb=new float[3];
    Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);

    return hsb;
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
  private JComponent buildRGBControls()
  {
    JPanel panel=new JPanel(new GridBagLayout());
    panel.setInheritsPopupMenu(true);

    Color color=getColorFromModel();
    redField=new JTextField(String.valueOf(color.getRed()), 3);
    redField.setEditable(false);
    redField.setHorizontalAlignment(JTextField.RIGHT);
    redField.setInheritsPopupMenu(true);

    greenField=new JTextField(String.valueOf(color.getGreen()), 3);
    greenField.setEditable(false);
    greenField.setHorizontalAlignment(JTextField.RIGHT);
    greenField.setInheritsPopupMenu(true);

    blueField=new JTextField(String.valueOf(color.getBlue()), 3);
    blueField.setEditable(false);
    blueField.setHorizontalAlignment(JTextField.RIGHT);
    blueField.setInheritsPopupMenu(true);

    String redString=UIManager.getString("ColorChooser.hsbRedText");
    String greenString=UIManager.getString("ColorChooser.hsbGreenText");
    String blueString=UIManager.getString("ColorChooser.hsbBlueText");

    panel.add(new JLabel(redString),new GBC(0,0));
    panel.add(redField, new GBC(1,0));
    panel.add(new JLabel(greenString),new GBC(0,1));
    panel.add(greenField,new GBC(1,1));
    panel.add(new JLabel(blueString), new GBC(0,2));
    panel.add(blueField, new GBC(1,2));

    return panel;
  }

  /**
   * Creates the panel with the editable HSB fields and the radio buttons.
   */
  private JComponent buildHSBControls()
  {

    String hueString=UIManager.getString("ColorChooser.hsbHueText");
    String saturationString=UIManager.getString("ColorChooser.hsbSaturationText");
    String brightnessString=UIManager.getString("ColorChooser.hsbBrightnessText");

    RadioButtonHandler handler=new RadioButtonHandler();

    hRadio=new JRadioButton(hueString);
    hRadio.addActionListener(handler);
    hRadio.setSelected(true);
    hRadio.setInheritsPopupMenu(true);

    sRadio=new JRadioButton(saturationString);
    sRadio.addActionListener(handler);
    sRadio.setInheritsPopupMenu(true);

    bRadio=new JRadioButton(brightnessString);
    bRadio.addActionListener(handler);
    bRadio.setInheritsPopupMenu(true);

    ButtonGroup group=new ButtonGroup();
    group.add(hRadio);
    group.add(sRadio);
    group.add(bRadio);

    float[] hsb=getHSBColorFromModel();

    hField=new JSpinner(new SpinnerNumberModel((int)(hsb[0]*359), 0, 359, 1));
    sField=new JSpinner(new SpinnerNumberModel((int)(hsb[1]*100), 0, 100, 1));
    bField=new JSpinner(new SpinnerNumberModel((int)(hsb[2]*100), 0, 100, 1));

    hField.addChangeListener(this);
    sField.addChangeListener(this);
    bField.addChangeListener(this);

    hField.setInheritsPopupMenu(true);
    sField.setInheritsPopupMenu(true);
    bField.setInheritsPopupMenu(true);

    JPanel panel=new JPanel(new GridBagLayout());

    panel.add(hRadio,new GBC(0,0));
    panel.add(hField,new GBC(1,0));
    panel.add(sRadio,new GBC(0,1));
    panel.add(sField,new GBC(1,1));
    panel.add(bRadio,new GBC(0,2));
    panel.add(bField,new GBC(1,2));
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
        if (button==hRadio) {
          setMode(HUE_MODE);
        }
        else if (button==sRadio) {
          setMode(SATURATION_MODE);
        }
        else if (button==bRadio) {
          setMode(BRIGHTNESS_MODE);
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

    float[] hsb=getHSBColorFromModel();

    switch (currentMode) {
      case HUE_MODE:
        slider.setInverted(true);
        slider.setMaximum(MAX_HUE_VALUE);
        palette.setValues(HSBImage.HSQUARE, hsb[0], 1.0f, 1.0f);
        sliderPalette.setValues(HSBImage.HSLIDER, 0f, 1.0f, 1.0f);
        break;
      case SATURATION_MODE:
        slider.setInverted(false);
        slider.setMaximum(MAX_SATURATION_VALUE);
        palette.setValues(HSBImage.SSQUARE, hsb[0], hsb[1], 1.0f);
        sliderPalette.setValues(HSBImage.SSLIDER, hsb[0], 1.0f, 1.0f);
        break;
      case BRIGHTNESS_MODE:
        slider.setInverted(false);
        slider.setMaximum(MAX_BRIGHTNESS_VALUE);
        palette.setValues(HSBImage.BSQUARE, hsb[0], 1.0f, hsb[2]);
        sliderPalette.setValues(HSBImage.BSLIDER, hsb[0], 1.0f, 1.0f);
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
    slider=new JSlider(JSlider.VERTICAL, 0, MAX_HUE_VALUE, 0);
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

    float[] hsb=getHSBColorFromModel();

    switch (currentMode) {
      case HUE_MODE:
        palette=new HSBImage(HSBImage.HSQUARE, PALETTE_DIMENSION,
                             PALETTE_DIMENSION, hsb[0], 1.0f, 1.0f);
        sliderPalette=new HSBImage(HSBImage.HSLIDER, 16, PALETTE_DIMENSION, 0f,
                                   1.0f, 1.0f);
        break;
      case SATURATION_MODE:
        palette=new HSBImage(HSBImage.SSQUARE, PALETTE_DIMENSION,
                             PALETTE_DIMENSION, 1.0f, hsb[1], 1.0f);
        sliderPalette=new HSBImage(HSBImage.SSLIDER, 16, PALETTE_DIMENSION, 1.0f,
                                   0f, 1.0f);
        break;
      case BRIGHTNESS_MODE:
        palette=new HSBImage(HSBImage.BSQUARE, PALETTE_DIMENSION,
                             PALETTE_DIMENSION, 1.0f, 1.0f, hsb[2]);
        sliderPalette=new HSBImage(HSBImage.BSLIDER, 16, PALETTE_DIMENSION, 1.0f,
                                   1.0f, 0f);
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
    String s=UIManager.getString("ColorChooser.hsbNameText");
    if (s==null || s.isEmpty()) {
      s="HSB";
    }
    return s;
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
  class HSBImage extends SyntheticImage {

    protected float h=.0f;
    protected float s=.0f;
    protected float b=.0f;
    protected float[] hsb=new float[3];
    protected boolean isDirty=true;
    protected int cachedY;
    protected int cachedColor;
    protected int type;
    private static final int HSQUARE=0;
    private static final int SSQUARE=1;
    private static final int BSQUARE=2;
    private static final int HSLIDER=3;
    private static final int SSLIDER=4;
    private static final int BSLIDER=5;

    protected HSBImage(int type, int width, int height, float h, float s,
                       float b)
    {
      super(width, height);
      setValues(type, h, s, b);
    }

    public void setValues(int type, float h, float s, float b)
    {
      this.type=type;
      cachedY=-1;
      cachedColor=0;
      setHue(h);
      setSaturation(s);
      setBrightness(b);
    }

    public final void setHue(float hue)
    {
      h=hue;
    }

    public final void setSaturation(float saturation)
    {
      s=saturation;
    }

    public final void setBrightness(float brightness)
    {
      b=brightness;
    }

    public final float getHue()
    {
      return h;
    }

    public final float getSaturation()
    {
      return s;
    }

    public final float getBrightness()
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
      if (type>=HSLIDER&&y==cachedY) {
        return cachedColor;
      }

      getHSBForLocation(x, y, hsb);
      cachedY=y;
      cachedColor=Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);

      return cachedColor;
    }

    public void getHSBForLocation(int x, int y, float[] hsbArray)
    {
      switch (type) {
        case HSQUARE: {
          float saturationStep=((float)x)/width;
          float brightnessStep=((float)y)/height;
          hsbArray[0]=h;
          hsbArray[1]=s-saturationStep;
          hsbArray[2]=b-brightnessStep;
          break;
        }
        case SSQUARE: {
          float brightnessStep=((float)y)/height;
          float step=1.0f/((float)width);
          hsbArray[0]=x*step;
          hsbArray[1]=s;
          hsbArray[2]=1.0f-brightnessStep;
          break;
        }
        case BSQUARE: {
          float saturationStep=((float)y)/height;
          float step=1.0f/((float)width);
          hsbArray[0]=x*step;
          hsbArray[1]=1.0f-saturationStep;
          hsbArray[2]=b;
          break;
        }
        case HSLIDER: {
          float step=1.0f/((float)height);
          hsbArray[0]=y*step;
          hsbArray[1]=s;
          hsbArray[2]=b;
          break;
        }
        case SSLIDER: {
          float saturationStep=((float)y)/height;
          hsbArray[0]=h;
          hsbArray[1]=s-saturationStep;
          hsbArray[2]=b;
          break;
        }
        case BSLIDER: {
          float brightnessStep=((float)y)/height;
          hsbArray[0]=h;
          hsbArray[1]=s;
          hsbArray[2]=b-brightnessStep;
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
        float value=(float)sliderValue/(float)sliderRange;

        float[] hsb=getHSBColorFromModel();

        switch (currentMode) {
          case HUE_MODE:
            updateHSB(value, hsb[1], hsb[2]);
            break;
          case SATURATION_MODE:
            updateHSB(hsb[0], value, hsb[2]);
            break;
          case BRIGHTNESS_MODE:
            updateHSB(hsb[0], hsb[1], value);
            break;
        }
      }
    }
    else if (e.getSource() instanceof JSpinner) {
      float hue=((Integer)hField.getValue()).floatValue()/359f;
      float saturation=((Integer)sField.getValue()).floatValue()/100f;
      float brightness=((Integer)bField.getValue()).floatValue()/100f;

      updateHSB(hue, saturation, brightness);
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
