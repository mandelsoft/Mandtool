/*
 *  Copyright 2011 Uwe Krueger.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.mandelsoft.mand.tool.cm;


import com.mandelsoft.mand.cm.ColormapModel;
import com.mandelsoft.mand.cm.InterpolationPointEvent;
import com.mandelsoft.mand.cm.InterpolationPointEventListener;
import com.mandelsoft.mand.cm.InterpolationPoint;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import com.mandelsoft.mand.cm.Colormap;
import com.mandelsoft.mand.cm.Colormaps;
import com.mandelsoft.mand.tool.DefaultPositionSelectionModel;
import com.mandelsoft.mand.tool.PositionSelectionModel;
import com.mandelsoft.swing.BufferedComponent;
import com.mandelsoft.swing.ScaleEvent;
import com.mandelsoft.swing.ScaleEventListener;
import com.mandelsoft.swing.colorchooser.ColorImageModel;

/**
 *
 * @author Uwe Krueger
 */
public class ColormapComponent extends BufferedComponent {
  static public boolean debug=false;

  static public final int PIX_X=1;
  static public final int PIX_Y=1;

  static public final int COL_X=3;
  static public final int COL_Y=44;

  static public final int IP_H=6;

  private Colormap colormap;
  private Graphics2D g;
  private Map<InterpolationPoint,IPUI> ipuis;
  private Window owner;
  private ColormapModel model;
  private int highlight=-1;

  // shared state for intrepolation points
  private ColorImageModel cim;

  private ChangeListener cl=new ChangeListener() {
    public void stateChanged(ChangeEvent e)
    { ColormapModel m=(ColormapModel)e.getSource();
      if (debug) System.out.println("colormap model change event");
      if (colormap!=m.getColormap()) {
        if (debug) System.out.println("--> setting new colormap");
        setColormap(m.getColormap());
      }
      else {
        redraw();
      }
    }
  };

  private InterpolationPointEventListener ipl=new InterpolationPointEventListener() {
    public void stateChanged(InterpolationPointEvent e)
    { InterpolationPoint ip=e.getSource();
      // System.out.println("C "+ip.getIndex()+" "+e.getId());
      if (e.getId()==InterpolationPointEvent.IPE_ADDED) {
        if (ipuis !=null && ipuis.get(ip)==null) {
          IPUI ui=new IPUI(ip);
          ipuis.put(ip,ui);
          repaint();
        }
      }
    }
  };

  //////////////////////////////////////////////////////////////////////////
  // Colormap Component
  //////////////////////////////////////////////////////////////////////////

  static private int cnt=0;

  public ColormapComponent(Window owner)
  {
    this(owner,null);
  }

  public ColormapComponent(Window owner, ColormapModel model)
  {
    this.owner=owner;
    setBorder(new BevelBorder(BevelBorder.LOWERED));
    setColormapModel(model==null?new ColormapModel():model);
    cim=new ColorImageModel();
    ML ml=new ML();
    addPaintHandler(new IndexHandler());
    getContentPane().addMouseListener(ml);
    getContentPane().addMouseMotionListener(ml);
    addScaleEventListener(new ScaleEventListener() {
      public void componentScaled(ScaleEvent e)
      {
        if (debug) System.out.println("REPACK "+(++cnt));
        repack();
      }

      public boolean succeedScale(ScaleEvent e)
      {
        return getImage().getWidth()*e.getScaleX()>200
                || (e.getScaleX()>=e.getOldX());
//        return true;
      }

    });
    setScaleMode(SCALEX);
    if (debug) System.out.println("scale colormap component");
    setScaleX(((double)COL_X)/PIX_X);
    setScaleY(((double)COL_Y)/PIX_Y);
  }

  public boolean isModifiable()
  {
    return model.isModifiable();
  }

  public ColormapModel getColormapModel()
  { return model;
  }

  public void setColormapModel(ColormapModel m)
  {
    if (model!=null) {
      model.removeChangeListener(cl);
      model.removeInterpolationPointEventListener(ipl);
    }
    this.model=m;
    model.addChangeListener(cl);
    model.addInterpolationPointEventListener(ipl);
    setColormap(m.getColormap());
  }


  private void setColormap(Colormap map)
  {
    if (this.colormap==map) return;
    if (map==null) {
      if (debug) System.out.println("clear colormap in dialog");
    }
    else {
      if (debug) System.out.println("set colormap in dialog "+map.getSize());
    }
    if (ipuis!=null) {
      for (IPUI ipui:ipuis.values()) ipui.cleanup();
    }
    this.colormap=map;
    this.highlight=-1;
    
    if (colormap!=null) {
      setImage(new BufferedImage(map.getSize()*PIX_X,PIX_Y,
               BufferedImage.TYPE_INT_RGB));
      ipuis=new HashMap<InterpolationPoint,IPUI>();
      for (InterpolationPoint ip:model.getInterpolationPoints()) {
        if (debug) System.out.println("found ip "+ip.getIndex()+": "+ip.getColor());
        ipuis.put(ip,new IPUI(ip));
      }
    }
    g=createGraphics();
   
    repack();
  }

  private void repack()
  {
    redraw();
    revalidate();
    Component c=this;
    while (!(c instanceof Window) && c.getParent()!=null) {
      c=c.getParent();
//      System.out.println(c);
    }
//    System.out.println(""+getPreferredSize());
    if (c!=null && (c instanceof Window)) {
      Window w=(Window)c;
      w.pack();
      w.repaint();
    }
  }

  public void redraw()
  {
    if (colormap!=null) {
      for (int i=0; i<colormap.getSize(); i++) {
        g.setColor(colormap.getColor(i));
        g.fillRect(i*PIX_X, 0, PIX_X, PIX_Y);
      }
      repaint();
    }
  }

  public void highLight(int ix)
  {
    //System.out.println("index = "+ix);
    if (highlight!=ix) repaint();
    highlight=ix;
  }

  private class IndexHandler implements PaintHandler {
    private BasicStroke stroke=new BasicStroke(1, BasicStroke.CAP_ROUND,
                             BasicStroke.JOIN_ROUND, 10, new float[]{4, 4}, 0);

    public void paintComponent(Graphics g)
    {
      for (IPUI ui:ipuis.values()) {
        ui.paintComponent(g);
      }
      // System.out.println("paint "+highlight);
      if (highlight>=0) {
        Graphics2D g2=(Graphics2D)g;
        g2.setColor(Color.RED);
        g2.setStroke(stroke);
        g2.drawLine(middleX(highlight), 0,
                    middleX(highlight), COL_Y);
      }
    }
  }
  

  //////////////////////////////////////////////////////////////////////
  // Interpolation point ipui
  //////////////////////////////////////////////////////////////////////

  public class IPUI implements InterpolationPointEventListener {
    private InterpolationPoint ip;
    private ColorChooser chooser;
    private SliderSample slider;
    private javax.swing.event.ChangeListener colorListener;
    private javax.swing.event.ChangeListener positionListener;
    private int last_index;

    private IPUI(InterpolationPoint ip)
    { this.ip=ip;
      ip.addInterpolationPointEventListener(this);
      colorListener=new javax.swing.event.ChangeListener() {
        public void stateChanged(javax.swing.event.ChangeEvent e)
        { ColorSelectionModel m=(ColorSelectionModel)e.getSource();
          if (debug) System.out.println("color from dialog: "+m.getSelectedColor());
          IPUI.this.ip.setColor(m.getSelectedColor());
        }
      };
      positionListener=new javax.swing.event.ChangeListener() {
        public void stateChanged(javax.swing.event.ChangeEvent e)
        { PositionSelectionModel m=(PositionSelectionModel)e.getSource();
          if (debug) System.out.println("position from dialog: "+m.getSelectedPosition());
          //undraw();
          IPUI.this.ip.setRelativePosition(m.getSelectedPosition());
          //draw();
        }
      };
    }
    
    private void cleanup()
    {
      ip.removeInterpolationPointEventListener(this);
      disposeChooser();
    }
    
    private void setupChooser()
    {
      if (chooser==null) {
        //System.out.println("owner is "+owner);
        
        chooser=new ColorChooser(owner);
        chooser.setColorImageModel(cim);
        chooser.setModal(false);
        chooser.setDefaultCloseOperation(
                        JDialog.HIDE_ON_CLOSE);
        chooser.addWindowListener(new WindowAdapter() {
              @Override
              public void windowClosing(WindowEvent we)
              { if (debug) System.out.println("closing color edit for "+ip.getIndex());
              }
        });
        chooser.getSelectionModel().addChangeListener(colorListener);
        if (!ip.isFixed()) {
          slider=new SliderSample(200,20);
          chooser.setSampleComponent(slider);
          slider.getPositionSelectionModel().setSelectedPosition(ip.getRelativePosition());
          slider.getPositionSelectionModel().addChangeListener(positionListener);

        }
      }
    } 
    
    public void showChooser()
    {
      setupChooser();
      chooser.setColor(ip.getColor());
      chooser.getSelectionModel().addChangeListener(colorListener);
      chooser.setVisible(true);
    }

    private void disposeChooser()
    {
      if (chooser!=null) {
        chooser.getSelectionModel().removeChangeListener(colorListener);
        chooser.dispose();
        chooser=null;
      }
    }

    private void paintComponent(Graphics g)
    {
      last_index=ip.getIndex();
      //System.out.println("draw "+last_index);
      int x=middleX(last_index)-COL_X/2;
      g.setColor(Color.WHITE);
      g.fillRect(x, 0, COL_X, IP_H);
      g.setColor(Color.BLACK);
      g.fillRect(x, COL_Y-IP_H, COL_X, IP_H);
    }

    public boolean match(int x)
    {
      int l=middleX(last_index)-COL_X/2;
      return l<=x && x<l+COL_X;
    }

    public void stateChanged(InterpolationPointEvent ipe)
    {
      //System.out.println("U "+last_index+" "+ipe.getId());

      if (ipe.getId()==InterpolationPointEvent.IPE_NEIGHBOR_CHANGED) {
        if (slider!=null) slider.getPositionSelectionModel().
                setSelectedPosition(ip.getRelativePosition());
      }
      else {
        if (ipe.getId()==InterpolationPointEvent.IPE_DELETED) {
          ipuis.remove(ip);
          cleanup();
        }
        repaint();
      }
    }
  }

  private InterpolationPoint getInterpolationPoint(int index, int x)
  {
    Iterator<InterpolationPoint> i=model.interpolationPoints();
    while (i.hasNext()) {
      InterpolationPoint ip=i.next();
      IPUI ipui=ipuis.get(ip);
      if (ipui.match(x)) return ip;
    }
    return model.getInterpolationPoint(index);
  }

  private InterpolationPoint getInterpolationPoint(MouseEvent e)
  {
    int index=translateC(e);
    if (!valid(index)) return null;
    return getInterpolationPoint(index,e.getX());
  }

  //////////////////////////////////////////////////////////////////////
  // Event handling
  //////////////////////////////////////////////////////////////////////
 
  static Cursor move_cursor=Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
  static Cursor cross_cursor=Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
  static Cursor def_cursor=Cursor.getDefaultCursor();   

  private int middleX(int c)
  {
    int x=translateColorToComponent(c)+((int)getScaleX())/2;
    return x;
  }

  private int translateC(Point2D p)
  { return translateX(toInt(p.getX())/PIX_X);
  }

  private int translateC(MouseEvent e)
  { return translateC(e.getPoint());
  }

  private int translateColorToComponent(double c)
  {
    return translateToComponentX(toInt(c*PIX_X));
  }

  private boolean valid(int index)
  { if (!isModifiable()) return false;
    if (index<1 || index>=colormap.getSize()) return false;
    return true;
  }
  
  public class ML extends MouseAdapter {
    private InterpolationPoint current=null;
         
    @Override
    public void mouseClicked(MouseEvent e)
    { int index=translateC(e);
      //System.out.println("clicked: map "+index+": "+e);
      if (!valid(index)) return;
      InterpolationPoint ip=getInterpolationPoint(index,e.getX());
      if (debug) System.out.println("clicked "+index+": "+e);
      if (e.getButton()==MouseEvent.BUTTON1) {
        if (e.getClickCount()==1) {
          if (!e.isControlDown()) {
            if (ip==null) {
              if (debug) System.out.println("  create "+index);
              model.createInterpolationPoint(index);
              setCursor(cross_cursor);
            }
            else {
              IPUI ui=ipuis.get(ip);
              if (debug) System.out.println("  found "+ip.toString());
              ui.showChooser();
            }
          }
          else {
            if (ip!=null) {
              ip.delete();
            }
          }
        }
      }
    }

    @Override
    public void mouseDragged(MouseEvent e)
    { int index=translateC(e);
      
      if (!valid(index)) return;
      //InterpolationPoint ip=getInterpolationPoint(index,e.getX());
      //System.out.println("drag "+current+" to "+index+": "+e);

      if (current!=null&&!current.isFixed()) {
        //System.out.println("drag "+current.getIndex()+" "+index);
        if (index<=current.getPrev().getIndex())
          index=current.getPrev().getIndex()+1;
        if (index>=current.getNext().getIndex())
          index=current.getNext().getIndex()-1;
        current.setIndex(index,true);
      }
    }

    @Override
    public void mousePressed(MouseEvent e)
    { 
      InterpolationPoint ip=getInterpolationPoint(e);
      if (e.getButton()==MouseEvent.BUTTON1) {
        if (ip!=null) {
          if (debug) System.out.println("select "+ip.getIndex());
          setCursor(move_cursor);
          current=ip;
        }
      }
    }

    @Override
    public void mouseReleased(MouseEvent e)
    { 
      InterpolationPoint ip=getInterpolationPoint(e);
      if (e.getButton()==MouseEvent.BUTTON1) {
        if (current!=null) {
          if (debug) System.out.println("reset cursor2");
          setCursor(def_cursor);
          current=null;
        }
      }
    }

    @Override
    public void mouseMoved(MouseEvent e)
    { 
      InterpolationPoint ip=getInterpolationPoint(e);
      if (ip==null) setCursor(def_cursor);
      else setCursor(cross_cursor);
    }
  }

  //////////////////////////////////////////////////////////////////////
  // RGB Chooser Slider Sample
  //////////////////////////////////////////////////////////////////////

  public class SliderSample extends ColorChooser.ColorSample {

    static public final int W=2;
    private Insets insets;
    private javax.swing.event.ChangeListener adjustHandler=new AdjustHandler();
    private PositionSelectionModel position;

    public SliderSample(int width, int height)
    {
      super(width, height);
      insets=new Insets(0, 0, 0, 0);
      SliderHandler h=new SliderHandler();
      adjustHandler=new AdjustHandler();
      addMouseListener(h);
      addMouseMotionListener(h);
      getInsets(insets);
      setPositionSelectionModel(new DefaultPositionSelectionModel());
    }

    public void setPositionSelectionModel(PositionSelectionModel p)
    {
      if (position!=null) position.removeChangeListener(adjustHandler);
      position=p;
      p.addChangeListener(adjustHandler);
    }

    public PositionSelectionModel getPositionSelectionModel()
    {
      return position;
    }

    protected int getPosition()
    {
      int max=getWidth()-insets.right-2*W-1-(insets.left+W);
      int x=(int)(position.getSelectedPosition()*max);
      return x+insets.left+W;
    }

    protected void setPosition(int p)
    {
      int x=p-insets.left-W;
      int max=getWidth()-insets.right-2*W-1-(insets.left+W);
      getPositionSelectionModel().setSelectedPosition(x/(double)(max));
      //System.out.println("= "+getPositionSelectionModel().
      //        getSelectedPosition()*100);
    }

    @Override
    protected void paintComponent(Graphics g)
    {
      getInsets(insets);
      super.paintComponent(g);
      drawSlider(g);
    }

    private void drawSlider(Graphics g)
    {
      int pos=getPosition();
      g.setColor(Color.BLACK);
      for (int i=1; i<=W; i++) {
        g.drawLine(pos-i, insets.top,
                pos-i, getHeight()-insets.bottom-1);
        g.drawLine(pos+W+i-1, insets.top,
                pos+W+i-1, getHeight()-insets.bottom-1);
      }
      g.setColor(Color.WHITE);
      for (int i=0; i<W; i++) {
        g.drawLine(pos+i, insets.top,
                pos+i, getHeight()-insets.bottom-1);
      }

    }

    private class AdjustHandler implements javax.swing.event.ChangeListener {

      public void stateChanged(javax.swing.event.ChangeEvent e)
      {
        repaint();
      }
    }

    private class SliderHandler extends MouseAdapter {

      private boolean active;
      private int offset;

      public SliderHandler()
      {
      }

      private boolean isValidPosition(int x)
      {
        //return true;
        return x>=insets.left+W && x<getWidth()-insets.right-2*W;
      }

      private int adjustPosition(int x)
      {
        //return true;
        if (x<insets.left+W) return insets.left+W;
        if (x>=getWidth()-insets.right-2*W) return getWidth()-insets.right-2*W-1;
        return x;
      }

      @Override
      public void mouseDragged(MouseEvent e)
      {
        //System.out.println("drag");
        if (active) {
          int x=adjustPosition(e.getX()-offset);
          //System.out.println("move to "+x);
          setPosition(x);
        }
      }

      @Override
      public void mousePressed(MouseEvent e)
      {
        getInsets(insets);

        if (e.getButton()==MouseEvent.BUTTON1) {
          int x=e.getX();
          int position=getPosition();
          if (debug) System.out.println("button pressed at "+x+": "+insets);
          if (x>=position-W&&x<position+2*W) {
            //System.out.println("activate");
            offset=e.getX()-position;
            active=true;
          }
        }
      }

      @Override
      public void mouseReleased(MouseEvent e)
      {
        if (e.getButton()==MouseEvent.BUTTON1) {
          //System.out.println("deactivate");
          active=false;
        }
      }
    }

  }

  //////////////////////////////////////////////////////////////////////
  // main
  //////////////////////////////////////////////////////////////////////

  public static void main(String[] args)
  {
    //Schedule a job for the event dispatch thread:
    //creating and showing this application's GUI.
    SwingUtilities.invokeLater(new Runnable() {

      public void run()
      {
        JFrame frame=new TestFrame();

        frame.setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
      }
    });
  }

  static class TestFrame extends JFrame {
    ColormapComponent cc;
    ColormapModel model;
    TestFrame()
    { 
      setup();
    }

    void setup()
    { cc=new ColormapComponent(this);
      model=new ColormapModel();
      model.setColormap(new Colormaps.Simple(256,Color.BLUE,Color.WHITE));
      model.setModifiable(true);
      add(cc);
      cc.setColormapModel(model);
      pack();
      setResizable(false);
    }
  }
}
