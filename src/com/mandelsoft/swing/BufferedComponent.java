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

package com.mandelsoft.swing;

import com.mandelsoft.mand.tool.Decoration.ColorHandler;
import com.mandelsoft.mand.tool.DynamicColor;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author Uwe Krueger
 */
public class BufferedComponent extends JComponent
                               implements ProportionProvider, DynamicColor.ImageSource {
  static public final int TEXT_INSETS=2;
  static public final int SELECT_INSETS=1;
  static public final int CORNER_RECT=20;

  // rect correction for double / int width and height
  static public final double RECTW=1;
  static public final double RECTH=1;

  static public boolean debug=false;

  static public int toInt(double d)
  {
    return (int)Math.round(d);
  }
  
  static public int toInt(long l)
  {
    return (int)l;
  }

  public interface PaintHandler {
    void paintComponent(Graphics g);
  }

  public interface ToolTipHandler {
    String getToolTipText(MouseEvent e);
  }

  private class ContentPane extends JComponent {
    private int max=5;
    private int limitCnt=max;

    public ContentPane()
    { setInheritsPopupMenu(true);
    }

    @Override
    public String getToolTipText(MouseEvent event)
    {
      if (tooltiphandler!=null) {
        return tooltiphandler.getToolTipText(translate(event));
      }
      return null;
    }

    @Override
    public void paintComponent(Graphics g)
    {
      Insets o=getInsets();
      //System.out.println("paint with "+o+": "+this.getBounds());
      if (image!=null) g.drawImage(image, o.left, o.top,
                        toInt(image.getWidth()*scale.getX()),
                        toInt(image.getHeight()*scale.getY()),null);
      for (PaintHandler h:painthandlers) {
        h.paintComponent(g.create());
      }
      if (limitPending) {
        //System.out.println("limit cnt = "+(limitCnt-1));
        if (--limitCnt<=0) {
          limitPending=false;
          limitCnt=max;
        }
      }
      limitWindowSize();
    }

    @Override
    public Graphics   getGraphics()
    { return createGraphics();
    }

    public Graphics2D createGraphics()
    { return image.createGraphics();
    }
  }
  
  static public final int SCALEX=1;
  static public final int SCALEY=2;
  
  private BufferedImage image;
  private int           scalemode;
  private Rectangle     fullviewrect;
  private Graphics2D    drawer;
  private boolean       limitWindowSize=false;
  private ContentPane   content;
  private List<PaintHandler> painthandlers=new ArrayList<PaintHandler>();
  private ToolTipHandler   tooltiphandler;

  private Set<VisibleRect> rects=new HashSet<VisibleRect>();
  private MouseHandler     mousehandler;
  private Point            adjust;
  private BooleanAttribute selectinvisible;
  private BooleanAttribute showdecoration;
  private BooleanAttribute pixeltooltip;

  private boolean          limitPending;

  private ColorHandler     colorHandler;
  
  public BufferedComponent()
  { this(1,1);
  }

  public BufferedComponent(int width, int height)
  { this(new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB));
  }

  public BufferedComponent(BufferedImage image)
  {
    colorHandler=new DynamicColor(this);
    setupLocalModels();
    
    setLayout(new BorderLayout());
    content=new ContentPane();
    content.setDoubleBuffered(false);
    content.setAutoscrolls(true);
    add(content);
    setupImage(image);

    mousehandler=new MouseHandler();
    setInheritsPopupMenu(true);
    setDoubleBuffered(false);
    setAutoscrolls(true);
    content.addMouseListener(mousehandler);
    content.addMouseMotionListener(mousehandler);
    addComponentListener(new ComponentAdapter() {

      @Override
      public void componentResized(ComponentEvent e)
      {
        //System.out.println("Buffer RS: "+e);

        JViewport vp=getViewPort();
        if (vp!=null) {
          //System.out.println("VP: "+vp.getViewPosition());
          //System.out.println("VP: "+vp.getExtentSize());
          if (adjust!=null) vp.setViewPosition(adjust);
          adjust=null;
        }

        ((BufferedComponent)e.getSource()).limitWindowSize();
      }
    });

    addPropertyChangeListener(new PropertyChangeListener() {

      public void propertyChange(PropertyChangeEvent evt)
      {
        //System.out.println("->"+evt.getPropertyName()+"="+evt.getNewValue());
        if (evt.getPropertyName().equals("border")) {
          updatePreferredSize();
        }
      }

    });
  }

  private void setupLocalModels()
  {
    selectinvisible=new BooleanAttribute(this,"selectinvisible");
    showdecoration=new BooleanAttribute(this,"showdecoration") {
      @Override
      protected void stateChanged()
      {
        Set<VisibleRect> set=new HashSet<VisibleRect>();

        for (VisibleRect r:rects) {
          if (r.isVisible()) set.add(r);
          r.setVisible(false);
        }
        setState();
        for (VisibleRect r:set) {
          r.setVisible(true);
        }
      }
    };
    showdecoration.setState(true);

    pixeltooltip=new BooleanAttribute(this,"pixeltooltip") {
      @Override
      protected void stateChanged()
      {
        if (tooltiphandler!=null) {
          if (isSelected()) {
            ToolTipManager.sharedInstance().registerComponent(content);
          }
          else {
            ToolTipManager.sharedInstance().unregisterComponent(content);
          }
        }
      }
    };
  }

  public ColorHandler getColorHandler()
  {
    return colorHandler;
  }
  
  public BooleanAttribute getDecorationModel()
  {
    return showdecoration;
  }

  public BooleanAttribute getSelectInvisibleModel()
  {
    return selectinvisible;
  }

  public BooleanAttribute getPixelToolTipModel()
  {
    return pixeltooltip;
  }

  public void setToolTipHandler(ToolTipHandler h)
  {
    ToolTipHandler old=tooltiphandler;
    tooltiphandler=h;
    if ((h==null)!=(old==null)) {
      if (h==null) {
        ToolTipManager.sharedInstance().unregisterComponent(content);
      }
      else {
        if (pixeltooltip.isSet())
          ToolTipManager.sharedInstance().registerComponent(content);
      }
    }
  }

  public void addPaintHandler(PaintHandler h)
  {
    painthandlers.add(h);
  }

  public void removePaintHandler(PaintHandler h)
  {
    painthandlers.remove(h);
  }

  public void updatePreferredSize()
  {
    //new Throwable().printStackTrace(System.out);
    Insets o=getInsets();
    Dimension d=new Dimension(toInt(image.getWidth()*scale.getX())+o.left+o.right,
                         toInt(image.getHeight()*scale.getY())+o.top+o.bottom);
    if (debug) System.out.println("update size: image "+image.getWidth()*scale.getX()+","+
                                image.getHeight()*scale.getY()+" "+o);
    setPreferredSize(d);
    setMaximumSize(d);
  }

  private boolean isAdjusting()
  { return adjust!=null;
  }

  private void setupImage(BufferedImage image)
  {
    this.image=image;
    //System.out.println("setup image "+image.getWidth()+"x"+image.getHeight());
    this.fullviewrect=new Rectangle(0,0,toInt(image.getWidth()/scale.getX()),
                                        toInt(image.getHeight()/scale.getY()));
    this.drawer=null;
    updatePreferredSize();
  }
  
  public void setImage(BufferedImage image)
  { BufferedImage old=this.image;
    discardAllRects();
    setupImage(image);
    if (old==null || image.getWidth()!=old.getWidth() ||
                     image.getHeight()!=old.getHeight()) {
      revalidate();
      limitWindowSize();
    }
    repaint();
  }
  
  public void setScaleMode(int m)
  {
    if (scalemode!=m) {
      int old=scalemode;
      if ((m==0)!=(scalemode==0)) {
        if (m!=0) content.addMouseWheelListener(mousehandler);
        else content.removeMouseWheelListener(mousehandler);
      }
      scalemode=m;
      firePropertyChange("scalemode", old, scalemode);
    }
  }

  public void setScaleMode(boolean b)
  {
    setScaleMode(b?(SCALEX|SCALEY):0);
  }
  
  public JComponent getContentPane()
  { return content;
  }

  public void setLimitWindowSize(boolean b)
  { 
    if (limitWindowSize!=b) {
      limitWindowSize=b;
      if (b) limitWindowSize();
      firePropertyChange("limitwindowsize", !limitWindowSize, limitWindowSize);
    }
  }

  public boolean isLimitWindowSize()
  { return limitWindowSize;
  }

  public boolean isShowDecoration()
  {
    return showdecoration.isSet();
  }

  public void setShowDecoration(boolean showdecoration)
  {
    this.showdecoration.setState(showdecoration);
  }

  public Graphics2D createGraphics()
  { return image.createGraphics();
  }

  private static class FrameInfo {
    Insets insets=new Insets(0,0,0,0);
    Container root;
    
    public void setup(Container c)
    {
      insets.bottom=0;
      insets.left=0;
      insets.right=0;
      insets.top=0;

      while (c!=null) {
        Insets cur=c.getInsets();
        // System.out.println("insets "+c.getClass()+": "+cur);
        if (c.getClass().equals(JScrollPane.class)) {
          JScrollPane p=(JScrollPane)c;
          JScrollBar v=p.getVerticalScrollBar();
          //System.out.println("  v=vis="+v.isVisible()+", en="+v.isEnabled()+
          //                       ",w ="+v.getWidth()+", h="+v.getHeight());
          if (v.isVisible()) insets.right+=v.getWidth();
          JScrollBar h=p.getHorizontalScrollBar();
          if (h.isVisible()) insets.bottom+=v.getWidth();
        }
        insets.bottom+=cur.bottom;
        insets.left+=cur.left;
        insets.right+=cur.right;
        insets.top+=cur.top;
        root=c;
        if (c instanceof Window) {
          c=null;
        }
        else {
          c=c.getParent();
        }
      }
      //System.out.println("ROOT Window is "+root);
    }
  }
  
  private boolean limitWindowSize()
  { int w=0;
    int h=0;

    if (!limitWindowSize) return false;
    boolean limit=false;
    FrameInfo info=new FrameInfo();
    info.setup(this.getParent());
    int dw=info.insets.left+info.insets.right;
    int dh=info.insets.top+info.insets.bottom;
    Dimension max=getMaximumSize();

    if (info.root==null) {
      return false;
    }
    //System.out.println("dw="+dw+", dh="+dh);
    if (info.root.getWidth()-dw>max.getWidth()) {
      w=toInt(max.getWidth()+dw);
      h=info.root.getHeight();
      limit=true;
    }
    if (info.root.getHeight()-dh>max.getHeight()) {
      h=toInt(max.getHeight()+dh);
      if (w==0) w=info.root.getWidth();
      limit=true;
    }
    //System.out.println("window "+info.root.getWidth()+","+info.root.getHeight());
    //System.out.println("max    "+max.getWidth()+","+max.getHeight());
    if (limit) {
      limitPending=true;
      //System.out.println("limit to "+w+","+h);
      info.root.setSize(w, h);
    }
    return limit;
  }

  public double getProportion()
  { double p=((double)image.getWidth())/image.getHeight();
    //System.out.println("deliver proportion "+p);
    return p;
  }

  public BufferedImage getImage()
  { return image;
  }
  
  public Scale getScale()
  { return scale;
  }
  
  //////////////////////////////////////////////////////////////////////
  // Assigned Rects
  //////////////////////////////////////////////////////////////////////

  private Graphics2D getDrawer()
  { if (drawer==null) {
      drawer=content.createGraphics();
      drawer.setColor(Color.BLACK);
      drawer.setXORMode(Color.WHITE);
      drawer.setFont(drawer.getFont().deriveFont(Font.BOLD));
    }
    return drawer;
  }

  public VisibleRect createRect(String name)
  { return new VisibleRect(name);
  }

  public VisibleRect createRect(String name, String label)
  { return new VisibleRect(name,label);
  }

  public VisibleRect createRect(String name, int x, int y, int w, int h)
  {
    return new VisibleRect(name,x,y,w,h);
  }

  public VisibleRect createRect(String name, String label, int x, int y, int w, int h)
  {
    return new VisibleRect(name,label,x,y,w,h);
  }

  public VisibleRect createRect(String name, Rectangle r)
  { return new VisibleRect(name,r);
  }

  public VisibleRect createRect(String name, String label, Rectangle r)
  { return new VisibleRect(name,label,r);
  }
  
  public VisibleRect createRect(String name, Object o)
  { return createRect(name,name).setOwner(o);
  }

  public VisibleRect createRect(String name, String label, Object o)
  { return createRect(name,label).setOwner(o);
  }

  public VisibleRect createRect(String name, Object o, int x, int y, int w, int h)
  { return createRect(name,x,y,w,h).setOwner(o);
  }

  public VisibleRect createRect(String name, String label, Object o, int x, int y, int w, int h)
  { return createRect(name,label,x,y,w,h).setOwner(o);
  }

  public VisibleRect createRect(String name, Object o, Rectangle r)
  { return createRect(name,r).setOwner(o);
  }

  public VisibleRect createRect(String name, String label, Object o, Rectangle r)
  { return createRect(name,label,r).setOwner(o);
  }

  public void setSelectInvisible(boolean b)
  { this.selectinvisible.setState(b);
  }

  public void showAllRects()
  {
    //System.out.println("show rects:");
    for (VisibleRect r:rects) r.setVisible(true);
  }

  public void hideAllRects()
  {
    //System.out.println("hide rects:");
    //new Throwable().printStackTrace(System.out);
    for (VisibleRect r:rects) r.setVisible(false);
  }

  public void discardAllRects()
  {
    //System.out.println("discard rects:");
    //new Throwable().printStackTrace(System.out);
    Set<VisibleRect> set=new HashSet<VisibleRect>(rects);
    for (VisibleRect r:set) r.discard();
  }

  public void discardAllRects(VisibleRectFilter f)
  {
    //System.out.println("discard filtered rects:");
    Set<VisibleRect> set=new HashSet<VisibleRect>(rects);
    for (VisibleRect r:set) if (f.match(r)) r.discard();
  }

  public VisibleRect getRect(String name)
  {
    for (VisibleRect r:rects) {
      if (r.getName()!=null && r.getName().equals(name)) return r;
    }
    return null;
  }

  public Iterator<VisibleRect> getRects()
  {
    return rects.iterator();
  }

  public VisibleRect findRect(int x, int y)
  { 
    return findRect((double)x,(double)y);
  }           
    
  public VisibleRect findRect(double x, double y, boolean fixed)
  { return findRect(x,y,fixed,selectinvisible.isSet());
  }

  public VisibleRect findRect(double x, double y, boolean fixed,
                                                  boolean invisible)
  { VisibleRect found=null;
    
    for (VisibleRect r:rects) {
      if (r.match(x,y)) {
        if ((invisible || r.isVisible()) && (fixed || !r.isFixed())) {
          if (found!=null){
            if (found.getWidth()*found.getHeight()<=
                    r.getWidth()*r.getHeight()) continue;
          }
          found=r;
        }
      }
    }
    return found;
  }

  public VisibleRect findRect(double x, double y)
  { return findRect(x,y,true);
  }

  public VisibleRect findRect(Point p, boolean fixed, boolean invisible)
  { return findRect(p.getX(), p.getY(), fixed, invisible);
  }

  public VisibleRect findRect(Point p, boolean fixed)
  { return findRect(p.getX(), p.getY(),fixed);
  }

  public VisibleRect findRect(Point p)
  { return findRect(p.getX(), p.getY());
  }

  
  
  public VisibleRectBorder findRectBorder(double x, double y, boolean fixed)
  { VisibleRectBorder found=null;
    VisibleRectBorder border=new VisibleRectBorder();

    for (VisibleRect r:rects) {
      if (r.isPointOnRect(toInt(x),toInt(y),SELECT_INSETS,border)) {
        if (!border.valid())
          throw new IllegalStateException("invalid border");
        if (r.isVisible() && (fixed || !r.isFixed())) {
          if (found!=null){
            if (found.getRect().getWidth()*found.getRect().getHeight()<=
                    r.getWidth()*r.getHeight()) continue;
          }
          found=border;
          border=new VisibleRectBorder();
        }
      }
    }
    return found;
  }

  public VisibleRectBorder findRectBorder(double x, double y)
  { return findRectBorder(x, y, false);
  }

  public VisibleRectBorder findRectBorder(Point p, boolean fixed)
  { return findRectBorder(p.getX(), p.getY(),fixed);
  }

  public VisibleRectBorder findRectBorder(Point p)
  { return findRectBorder(p.getX(), p.getY(),true);
  }

  //////////////
  // Rect filtering

  public interface VisibleRectFilter {
    public boolean match(VisibleRect r);
  }

  //////////////
  // RectBorder

  public static class VisibleRectBorder {
    static public final Side LEFT=Side.LEFT;
    static public final Side RIGHT=Side.RIGHT;
    static public final Side TOP=Side.TOP;
    static public final Side BOTTOM=Side.BOTTOM;
          
    static public final Corner TOP_LEFT=RectPointEvent.TOP_LEFT;
    static public final Corner TOP_RIGHT=RectPointEvent.TOP_RIGHT;
    static public final Corner BOTTOM_LEFT=RectPointEvent.BOTTOM_LEFT;
    static public final Corner BOTTOM_RIGHT=RectPointEvent.BOTTOM_RIGHT;
    
    private VisibleRect rect;
    private Side side;
    private Corner corner;

    public VisibleRectBorder()
    {
    }

    public VisibleRectBorder(VisibleRect rect, Side side, Corner corner)
    {
      this.rect=rect;
      this.side=side;
      this.corner=corner;
    }

    public boolean valid()
    { return rect!=null && side!=null && corner!=null;
    }

    public Corner getCorner()
    { return corner;
    }

    public VisibleRect getRect()
    { return rect;
    }

    public Side getSide()
    { return side;
    }

    public void setCorner(Corner corner)
    { this.corner=corner;
    }

    public void setRect(VisibleRect rect)
    { this.rect=rect;
    }

    public void setSide(Side side)
    { this.side=side;
    }

    public Corner getOppositeCorner()
    { return corner.getOppositeCorner();
    }

    public Side getOppositeSide()
    { return side.getOppositeSide();
    }
    
    public Point getCornerPoint()
    { return getCorner().getPoint(getRect()._getRect());
    }
    
    public Point getOppositeCornerPoint()
    { return getOppositeCorner().getPoint(getRect()._getRect());
    }
  }

  //////////////
  // VisibleRect

  private volatile int id_cnt=0;

  public class VisibleRect implements PaintHandler  {
    private int       id;
    private String    label;
    private String    name;
    private Rectangle _rect;
    private boolean   visible;
    private boolean   active;
    private boolean   fixed;
    private Object    owner;

    private ProportionProvider proportionProvider;
    
    private Stroke    line;

    private VisibleRect()
    { this.id=id_cnt++;
      activate();
    }

    protected VisibleRect(String name)
    {
      this(name,name);
    }

    protected VisibleRect(String name, String label)
    {
      this(name,label,new Rectangle(0,0,0,0));
    }

    protected VisibleRect(String name, int x, int y, int w, int h)
    { 
      this(name,new Rectangle(x,y,w,h));
    }

    protected VisibleRect(String name, String label, int x, int y, int w, int h)
    {
      this(name,label,new Rectangle(x,y,w,h));
    }

    protected VisibleRect(String name, Rectangle r)
    {
      this(name,name,r);
    }

    protected VisibleRect(String name, String label, Rectangle r)
    { this();
      this.name=name;
      if (label==null) label=name;
      this.label=label;
      this._rect=new Rectangle(r);
    }


    public void setStroke(Stroke line)
    {
      draw();
      this.line=line;
      draw();
    }

    public ProportionProvider getProportionProvider()
    {
      return proportionProvider;
    }

    public void setProportionProvider(ProportionProvider proportionProvider)
    {
      this.proportionProvider=proportionProvider;
    }

    public void discard()
    {
      setVisible(false);
      active=false;
      rects.remove(this);
    }

    public void activate()
    {
      activate(false);
    }

    public void activate(boolean subst)
    {
      if (!active) {
        if (subst && getName()!=null) {
          VisibleRect old=getRect(getName());
          if (old!=null && old!=this) {
            old.discard();
          }
        }
        active=true;
        rects.add(this);
      }
    }
    
    public boolean isVisible()
    { return visible;
    }

    public boolean isFixed()
    { return fixed;
    }
    
    public boolean isActive()
    { return active;
    }

    synchronized
    public int getId()
    { return id;
    }

    synchronized
    public String getName()
    { return name;
    }

    synchronized
    public String getLabel()
    { return label;
    }

    synchronized
    public Object getOwner()
    { return owner;
    }

    synchronized
    public VisibleRect setOwner(Object o)
    { this.owner=o;
      return this;
    }

    synchronized
    public Rectangle _getRect()
    { return new Rectangle(_rect);
    }

    synchronized
    public double getY()
    {
      return _rect.getY();
    }

    synchronized
    public double getX()
    {
      return _rect.getX();
    }

    synchronized
    public Point getCenter()
    {
      return new Point(getCenterX(),getCenterY());
    }

    public int getCenterY()
    {
      return toInt(_rect.getCenterY()-RECTW/2);
    }

    public int  getCenterX()
    {
      return toInt(_rect.getCenterX()-RECTH/2);
    }

    public int getWidth()
    {
      return toInt(_rect.getWidth());
    }

    public Dimension getSize()
    {
      return _rect.getSize();
    }

    public Point getLocation()
    {
      return _rect.getLocation();
    }

    public int getHeight()
    {
      return toInt(_rect.getHeight());
    }

    public boolean contains(Rectangle2D r)
    {
      return _rect.contains(r);
    }

    public boolean contains(double x, double y, double w, double h)
    {
      return _rect.contains(x, y, w, h);
    }

    public boolean contains(Point2D p)
    {
      return _rect.contains(p);
    }

    public boolean contains(double x, double y)
    {
      return _rect.contains(x, y);
    }

    public boolean match(double x, double y)
    {
      if (contains(x,y)) return true;
      if (toInt(_rect.getWidth())<5 || toInt(_rect.getHeight())<5) {
        if (Math.abs(x-getCenterX())<2 || Math.abs(y-getCenterY())<2)
          return true;
      }
      return false;
    }

    public VisibleRect setFixed(boolean fixed)
    {
      this.fixed=fixed;
      return this;
    }

    public VisibleRect setName(String name)
    {
      this.name=name;
      if (label==null) setLabel(name);
      return this;
    }

    public VisibleRect setLabel(String label)
    {
      draw();
      this.label=label;
      draw();
      return this;
    }

    public void setRect(Rectangle2D r)
    {
      draw();
      _rect.setRect(r);
      draw();
    }

    public void setSize(Dimension2D d)
    { setSize(toInt(d.getWidth()),toInt(d.getHeight()));
    }

    public void setSize(int w, int h)
    {
      draw();
      _rect.setSize(w,h);
      draw();
    }

    public void setLocation(int x, int y)
    {
      draw();
      _rect.setLocation(x, y);
      draw();
    }

    public void setLocation(Point2D p)
    {
      setLocation(toInt(p.getX()),toInt(p.getY()));
    }

    public void grow(int h, int v)
    {
      draw();
      _rect.grow(h, v);
      draw();
    }

    public void translate(int x, int y)
    {
      draw();
      _rect.translate(x, y);
      draw();
    }

    public void setFrameFromCenter(Point2D center, Point2D corner)
    {
      draw();
      _rect.setFrameFromCenter(center, corner);
      _rect.setSize(toInt(_rect.getWidth()+RECTW),toInt(_rect.getHeight()+RECTH));
      draw();
    }


    public void setFrameFromDiagonal(Point2D p1, Point2D p2)
    {
      draw();
      _rect.setFrameFromDiagonal(p1, p2);
      _rect.setSize(toInt(_rect.getWidth()+RECTW),toInt(_rect.getHeight()+RECTH));
      draw();
    }

   public VisibleRect setVisible(boolean v)
    { if (v!=visible) {
        //System.out.println("  vis "+v+" for "+getName());
        draw();
        visible=v;
        draw();
      }
      else {
        //System.out.println("  vis unchanged "+v+" for "+getName());
      }
      return this;
    }

    public void paintComponent(Graphics g)
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }
    
    protected void draw()
    {
      if (!visible) {
        //System.out.println(" not visible");
        return;
      }
      //System.out.println(" draw rect "+_rect);

      Graphics2D g=getDrawer();
      draw(g);
      repaint();
    }
    
    protected void draw(Graphics2D g)
    {
      Stroke orig=g.getStroke();
      int x=toInt(_rect.getX());
      int y=toInt(_rect.getY());
      int w=toInt(_rect.getWidth());
      int h=toInt(_rect.getHeight());
      if (line!=null) g.setStroke(line);
      if (w<5 || h<5) {
        g.drawLine(toInt(getCenterX()), toInt(getCenterY()-10),
                   toInt(getCenterX()), toInt(getCenterY()+10));
        g.drawLine(toInt(getCenterX()-10), toInt(getCenterY()),
                   toInt(getCenterX()+10), toInt(getCenterY()));
      }
      else {
        g.drawLine(x, y, x, y+h-1);
        g.drawLine(x, y, x+w-1, y);
        g.drawLine(x+w-1, y+h-1, x, y+h-1);
        g.drawLine(x+w-1, y+h-1, x+w-1, y);
        // g.draw(rect); // draw addtional width and height
      }
      g.setStroke(orig);

      if (label!=null && isShowDecoration()) {
        // get metrics from the graphics
        FontMetrics metrics=g.getFontMetrics();
        // get the height of a line of text in this font and render context
        int hgt=metrics.getHeight();
        // get the advance of my text in this font and render context
        int adv=metrics.stringWidth(label);
        // calculate the size of a box to hold the text with some padding.
        Dimension d=new Dimension(adv, hgt);

        // try to find a useful position
        // first below bottom right rectPoint
        if (!_draw(g, label, d, 1, 1, 1, 1)) {
          // second below bottom left rectPoint
          if (!_draw(g, label, d, 0, 1, 0, 1)) {
            // third above top right rectPoint
            if (!_draw(g, label, d, 1, 0, 1, -1)) {
              // fourth above top left rectPoint
              if (!_draw(g, label, d, 0, 0, 0, -1)) {
                // dont't draw at all
              }
            }
          }
        }
      }
    }

    private boolean _draw(Graphics2D g, String name, Dimension d,
                          int fw, int fh, int fdw, int fdh)
    {
      double py=_rect.getY()+(_rect.getHeight()-1)*fh+(d.getHeight()+TEXT_INSETS)*fdh;
      if (0<=py && py <image.getHeight()) {
        double px=_rect.getX()+(_rect.getWidth()-1)*fw+TEXT_INSETS*fdw;
        double px2=px-(d.getWidth()+TEXT_INSETS)*fdw;
        if (0<=px && px<image.getWidth() && px2>0) {
          g.drawString(name, toInt(px2),
                             toInt(py-d.getHeight()*Integer.signum(fdh-1)
                                     -TEXT_INSETS*(fdh+1)));
          return true;
        }
      }
      return false;
    }

    private boolean checkLine(double sx, double ex,
                              double cy,
                              double px, double py,
                              int t, VisibleRectBorder b, Side side)
    {
      if (sx>ex) {
        double tmp=sx;
        sx=ex;
        ex=tmp;
      }
      boolean r=(cy-t<=py && py<=cy+t &&
                 sx-t<=px && px<=ex+t);
      if (r && b!=null) {
        Corner c;
        b.setSide(side);
        if (px<(sx+ex)/2) {
          c=side.getLowerCorner();
          if (c==null) throw new IllegalStateException("LC");
        }
        else {
          c=side.getHigherCorner();
          if (c==null) throw new IllegalStateException("HC");
        }
        b.setCorner(c);
        b.setRect(this);
      }
      return r;
    }

    public boolean isPointOnRect(int x, int y, int t)
    { return isPointOnRect(x,y,t,null);
    }
    
    public boolean isPointOnRect(int x, int y, int t, VisibleRectBorder b)
    {
      //upper
      if (checkLine(getX(), getX()+getWidth()-1,
                    getY(),
                    x,y,t,
                    b, VisibleRectBorder.TOP)) return true;
      //lower
      if (checkLine(getX(), getX()+getWidth()-1,
                    getY()+getHeight()-1,
                    x,y,t,
                    b, VisibleRectBorder.BOTTOM)) return true;
      //left
      if (checkLine(getY(), getY()+getHeight()-1,
                    getX(),
                    y,x,t,
                    b, VisibleRectBorder.LEFT)) return true;
      //right
      if (checkLine(getY(), getY()+getHeight()-1,
                    getX()+getWidth()-1,
                    y,x,t,
                    b, VisibleRectBorder.RIGHT)) return true;
      return false;
    }

    @Override
    public String toString()
    { String n=getName();
      if (n==null) n="Id "+getId();
      return  "rectangle "+n+" at ("+getX()+","+getY()+") with size ("+
                                     getWidth()+","+getHeight()+")";
    }
    //////////////////////////////////////////////////////////////////////////
    private List<RectEventListener> listeners=new ArrayList<RectEventListener>();

    public void addRectEventListener(RectEventListener l)
    {
      listeners.add(l);
    }

    public void removeRectEventListener(RectEventListener l)
    {
      listeners.remove(l);
    }

    public boolean hasRectEventListeners()
    {
      return !listeners.isEmpty();
    }
   
    protected void fireRectEvent(MouseEvent e)
    {
      fireRectEvent(new RectEvent(this, e));
    }

    protected void fireRectEvent(RectEvent e)
    {
      for (RectEventListener l:listeners) {
        l.buttonClicked(e);
      }
    }

    //////////////////////////////////////////////////////////////////////////
    private Set<RectModifiedEventListener> mlisteners=new HashSet<RectModifiedEventListener>();

    public void addRectModifiedEventListener(RectModifiedEventListener l)
    {
      mlisteners.add(l);
    }

    public void removeRectModifiedEventListener(RectModifiedEventListener l)
    {
      mlisteners.remove(l);
    }

    public boolean hasRectModifiedEventListeners()
    {
      return !mlisteners.isEmpty();
    }

    protected void fireRectModifiedEvent(int action)
    {
      fireRectModifiedEvent(new RectModifiedEvent(this, action));
    }

    protected void fireRectModifiedEvent(RectModifiedEvent e)
    {
      for (RectModifiedEventListener l:mlisteners) {
        l.rectModified(e);
      }
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  // Event handling
  ///////////////////////////////////////////////////////////////////////////

  ///////////////////////////////////////////////////////////////////////////
  // own events

  public static class ClickEvent extends EventObject {
    private MouseEvent evt;

    public ClickEvent(Object source, MouseEvent e)
    { super(source);
      this.evt=e;
    }

    public MouseEvent getMouseEvent()
    { return evt;
    }

    public BufferedComponent getComponent()
    { return (BufferedComponent)
              ((JComponent)getMouseEvent().getSource()).getParent();
    }

    public boolean isShiftDown()
    {
      return evt.isShiftDown();
    }

    public boolean isMetaDown()
    {
      return evt.isMetaDown();
    }

    public boolean isControlDown()
    {
      return evt.isControlDown();
    }

    public boolean isAltGraphDown()
    {
      return evt.isAltGraphDown();
    }

    public boolean isAltDown()
    {
      return evt.isAltDown();
    }

    public int getY()
    {
      return evt.getY();
    }

    public int getX()
    {
      return evt.getX();
    }

    public Point getPoint()
    {
      return evt.getPoint();
    }

    public int getClickCount()
    {
      return evt.getClickCount();
    }

    public int getButton()
    {
      return evt.getButton();
    }

    public String getDescription()
    { return "button "+getButton()+" "+getClickCount()+"x at ("+
              getX()+","+getY()+")";
    }
  }

  //////////////////////////////////////////////////////////////////////////
  public static class RectEvent extends ClickEvent {
    private VisibleRect rect;

    public RectEvent(VisibleRect r,MouseEvent e)
    { super(r,e);
      this.rect=r;
    }

    public VisibleRect getRect()
    { return rect;
    }

    public Object getOwner()
    { return rect.getOwner();
    }

    @Override
    public String getDescription()
    { String n=rect.getName();
      if (n==null) n="id "+rect.getId();
      return "rectangle "+n+" with "+super.getDescription();
    }

  }

  public interface RectEventListener {
    void buttonClicked(RectEvent e);
  }
  
  //////////////////////////////////////////////////////////////////////////
  private List<RectEventListener> listeners=new ArrayList<RectEventListener>();
  
  public void addRectEventListener(RectEventListener l)
  { listeners.add(l);
  }
  
  public void removeRectEventListener(RectEventListener l)
  { listeners.remove(l);
  }
  
  protected void fireRectEvent(VisibleRect r, MouseEvent e)
  {
    fireRectEvent(new RectEvent(r,e));
  }
  
  protected void fireRectEvent(RectEvent e)
  { 
    e.getRect().fireRectEvent(e);
    for (RectEventListener l:listeners) {
      l.buttonClicked(e);
    }
  }
  
  //////////////////////////////////////////////////////////////////////////

  public static class RectPointEvent extends ClickEvent {
    static final public Corner TOP_LEFT=    Corner.TOP_LEFT;
    static final public Corner TOP_RIGHT=   Corner.TOP_RIGHT;
    static final public Corner BOTTOM_LEFT= Corner.BOTTOM_LEFT;
    static final public Corner BOTTOM_RIGHT=Corner.BOTTOM_RIGHT;

    static final public RectanglePoint MIDDLE_LEFT=  RectanglePoint.MIDDLE_LEFT;
    static final public RectanglePoint MIDDLE_RIGHT= RectanglePoint.MIDDLE_RIGHT;
    static final public RectanglePoint MIDDLE_TOP=   RectanglePoint.MIDDLE_TOP;
    static final public RectanglePoint MIDDLE_BOTTOM=RectanglePoint.MIDDLE_BOTTOM;
    
    private RectanglePoint rectPoint;
    
    public RectPointEvent(MouseEvent evt, RectanglePoint loc)
    { super(evt.getSource(),evt);
      this.rectPoint=loc;
    }

    public Corner getCorner()
    { return isCornerEvent()?(Corner)rectPoint:null;
    }

    public boolean isCornerEvent()
    {
      return (rectPoint instanceof Corner);
    }

    public RectanglePoint getRectanglePoint()
    {
      return rectPoint;
    }

    public String getPointName()
    { if (rectPoint==null) return "None";
      return rectPoint.getName();
    }

    @Override
    public String getDescription()
    { return getPointName()+" with "+super.getDescription();
    }
  }

  public interface RectPointEventListener {
    void rectanglePointClicked(RectPointEvent e);
  }

  //////////////////////////////////////////////////////////////////////////
  
  private List<RectPointEventListener> clisteners=new ArrayList<RectPointEventListener>();
  
  public void addRectPointEventListener(RectPointEventListener l)
  { clisteners.add(l);
  }
  
  public void removeRectPointEventListener(RectPointEventListener l)
  { clisteners.remove(l);
  }
  
  protected void fireRectangleEvent(MouseEvent evt, RectanglePoint loc)
  {
    fireRectangleEvent(new RectPointEvent(evt,loc));
  }
  
  protected void fireRectangleEvent(RectPointEvent e)
  { for (RectPointEventListener l:clisteners) {
      l.rectanglePointClicked(e);
    }
    fireRectangleEvent(e.getRectanglePoint(),e);
  }

  //////////////////////////////////////////////////////////////////////////

  private Map<RectanglePoint,List<ActionListener>> points=
          new HashMap<RectanglePoint,List<ActionListener>>();

  public void addActionListener(ActionListener l, RectanglePoint c)
  {
    List<ActionListener> list=points.get(c);
    if (list==null) {
      list=new ArrayList<ActionListener>();
      points.put(c,list);
    }
    list.add(l);
  }

  public void removeActionListener(ActionListener l, RectanglePoint c)
  {
    List<ActionListener> list=points.get(c);
    if (list!=null) {
      list.remove(l);
    }
  }

  public static class RectangleActionEvent extends ActionEvent {
    private RectPointEvent p;

    public RectangleActionEvent(Object source, int id, String command, long when,
                                int modifiers, RectPointEvent p)
    { super(source,id,command,when,modifiers);
      this.p=p;
    }

    public RectangleActionEvent(Object source, int id, String command,
                                int modifiers, RectPointEvent p)
    { super(source,id,command,modifiers);
      this.p=p;
    }

    public RectangleActionEvent(Object source, int id, String command,
                                RectPointEvent p)
    { super(source,id,command);
      this.p=p;
    }

    public RectPointEvent getRectanglePointEvent()
    {
      return p;
    }
  }

  protected void fireRectangleEvent(RectanglePoint c, RectPointEvent p)
  { ActionEvent e=new RectangleActionEvent(this,ActionEvent.ACTION_PERFORMED,c.getName(),
                                  System.currentTimeMillis(),0,p);
    List<ActionListener> list=points.get(c);
    if (list!=null) {
      List<ActionListener> selected=new ArrayList<ActionListener>();
      for (ActionListener l:list) {
        if (l instanceof Action) {
          if (!((Action)l).isEnabled()) {
            //System.out.println("Action "+l+" disabled");
            continue;
          }
        }
        selected.add(l);
      }
      for (ActionListener l:selected) {
        l.actionPerformed(e);
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////

  public static class RectModifiedEvent extends EventObject {
    static public final int RECT_CREATED = 1;
    static public final int RECT_MOVED   = 2;
    static public final int RECT_RESIZED = 3;

    private VisibleRect rect;
    private int action;

    public RectModifiedEvent(VisibleRect source, int action)
    { super(source);
      this.rect=source;
      this.action=action;
    }

    public int getAction()
    { return action;
    }

    public VisibleRect getRect()
    { return rect;
    }
  }

  public interface RectModifiedEventListener {
    void rectModified(RectModifiedEvent e);
  }

  //////////////////////////////////////////////////////////////////////////

  private Set<RectModifiedEventListener> mlisteners=new HashSet<RectModifiedEventListener>();

  public void addRectModifiedEventListener(RectModifiedEventListener l)
  { mlisteners.add(l);
  }

  public void removeRectModifiedEventListener(RectModifiedEventListener l)
  { mlisteners.remove(l);
  }

  protected void fireRectModifiedEvent(VisibleRect rect, int action)
  {
    fireRectModifiedEvent(new RectModifiedEvent(rect,action));
  }

  protected void fireRectModifiedEvent(RectModifiedEvent e)
  { 
    e.getRect().fireRectModifiedEvent(e);
    for (RectModifiedEventListener l:mlisteners) {
      l.rectModified(e);
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  // scaling

  private Scale scale=Scale.One;
  private RectangleSelector selector=null;
  private Insets insets=new Insets(0,0,0,0); // not used anymore, always 0

  public boolean setScale(Scale s)
  {
     JViewport vp=getViewPort();
    if (vp!=null) {
      Rectangle r=vp.getViewRect();
      return setScale(s.getX(), s.getY(),new Point2D.Double(r.getCenterX(), r.getCenterY()));
    }
    else {
      return setScale(s.getX(), s.getY(),null);
    }
  }
  
  public boolean setScale(double s)
  {
    JViewport vp=getViewPort();
    if (vp!=null) {
      Rectangle r=vp.getViewRect();
      return setScale(s,new Point2D.Double(r.getCenterX(), r.getCenterY()));
    }
    else {
      return setScale(s,null);
    }
  }

  public boolean setScaleX(double s)
  {
    JViewport vp=getViewPort();
    if (vp!=null) {
      Rectangle r=vp.getViewRect();
      return setScaleX(s,new Point2D.Double(r.getCenterX(), r.getCenterY()));
    }
    else {
      return setScaleX(s,null);
    }
  }

  public boolean setScaleY(double s)
  {
    JViewport vp=getViewPort();
    if (vp!=null) {
      Rectangle r=vp.getViewRect();
      return setScaleY(s,new Point2D.Double(r.getCenterX(), r.getCenterY()));
    }
    else {
      return setScaleY(s,null);
    }
  }

  public boolean setScaleX(double s, Point2D p)
  { return setScale(s,scale.getY(),p);
  }

  public boolean setScaleY(double s, Point2D p)
  { return setScale(scale.getX(),s,p);
  }

  public boolean setScale(double s, Point2D p)
  { return setScale(s,s,p);
  }

  public boolean setScale(double sx, double sy, Point2D p)
  {
    return setScale(new Scale(sx,sy), p);
  }
  
  public boolean setScale(Scale s, Point2D p)
  { Point n=null;

    //System.out.println("call set scale to "+sx+"/"+sy+"/"+p);
    if (!s.equals(scale)) {
      double scaleX=scale.getX();
      double scaleY=scale.getY();
      Scale old=scale;
      ScaleEvent e=new ScaleEvent(this, s, old);
      for (ScaleEventListener h:slisteners) {
        if (!h.succeedScale(e)) return false;
      }
      scale=s;
      JViewport vp=getViewPort();
      if (vp!=null && p!=null) {
        Rectangle r=vp.getViewRect();
        double fX=scaleX/old.getX();
        double fY=scaleX/old.getY();
        double x=p.getX()*fX-(p.getX()-r.getX());
        double y=p.getY()*fY-(p.getY()-r.getY());
        if (x+r.getWidth()>=image.getWidth()*scaleX) {
          x=image.getWidth()*scaleX-r.getWidth();
          //System.out.println("adjust x");
        }
        if (y+r.getHeight()>=image.getHeight()*scaleY) {
          y=image.getHeight()*scaleY-r.getHeight();
          //System.out.println("adjust y");
        }
        n=new Point(toInt(x<0?0:x),
                    toInt(y<0?0:y));
        adjust=n;
//        System.out.println("factor: "+f+
//                           " state origin: "+r.getX()+","+r.getY()+
//                           " point: "+p.getX()+","+p.getY()+
//                           " new origin: "+n.getX()+","+n.getY());
//        System.out.println(""+vp.getViewPosition());
      }
      if (debug) System.out.println("*** set scale "+s);
      updatePreferredSize();
      limitWindowSize();
      repaint();
      revalidate();
      if (n!=null)
        vp.setViewPosition(n); //doesn't work always -> defer adhustment
      fireScaleEvent(e);
      return true;
    }
    return false;
  }

  public double getScaleX()
  { return scale.getX();
  }

  public double getScaleY()
  { return scale.getY();
  }

  private Set<ScaleEventListener> slisteners=new HashSet<ScaleEventListener>();

  public void addScaleEventListener(ScaleEventListener h)
  {
    slisteners.add(h);
  }

  public void removeScaleEventListener(ScaleEventListener h)
  {
    slisteners.remove(h);
  }

  private void fireScaleEvent(ScaleEvent e)
  {
    for (ScaleEventListener h:slisteners) {
      h.componentScaled(e);
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  // intern Event handling

  private JViewport getViewPort()
  {
    try {
      return (JViewport)getParent();
    }
    catch (ClassCastException cce) {
      return null;
    }
  }

  public int translateX(int x)
  {
    x=(int)((x-insets.left)/scale.getX());
    if (x<0) x=0;
    if (x>=image.getWidth()) x=image.getWidth()-1;
    return x;
  }

  public int translateY(int y)
  {
    y=(int)((y-insets.top)/scale.getY());
    if (y<0) y=0;
    if (y>=image.getHeight()) y=image.getHeight()-1;
    return y;
  }

  public int translateX(MouseEvent e)
  {
    return translateX(e.getX());
  }

  public int translateY(MouseEvent e)
  {
    return translateY(e.getY());
  }

  public int translateToComponentX(int x)
  {
    return toInt(x*scale.getX()+insets.left);
  }

  public int translateToComponentY(int y)
  {
    return toInt(y*scale.getY()+insets.top);
  }

  public int componentMiddleX(int x)
  {
    return translateToComponentX(x)+((int)scale.getX())/2;
  }
  
  public int componentMiddleY(int y)
  {
    return translateToComponentY(y)+((int)scale.getY())/2;
  }
  
  public MouseEvent translate(MouseEvent e)
  {
    //getInsets(insets);
    if (scale.isOne() && insets.left==0 && insets.top==0) return e;

    int x=translateX(e);
    int y=translateY(e);
    e=new MouseEvent((Component)e.getSource(), e.getID(), e.getWhen(), e.getModifiers(),
                      x, y, e.getXOnScreen(), e.getYOnScreen(),
                      e.getClickCount(), e.isPopupTrigger(), e.getButton());
    return e;
  }

  //
  // translate events for external mouse handlers
  //

  private class MouseHandler extends MouseAdapter
                             implements MouseListener, MouseMotionListener {
    final Cursor point_cursor=Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    final Cursor def_cursor=Cursor.getDefaultCursor();

    private RectanglePoint checkRectPoints(Rectangle r, Point p)
    {
      if (p.getX()<r.getX()+CORNER_RECT&&
          p.getY()<r.getY()+CORNER_RECT) {
        return RectPointEvent.TOP_LEFT;
      }
      else if (p.getX()<r.getX()+CORNER_RECT&&
               p.getY()>r.getY()+r.getHeight()-CORNER_RECT) {
        return RectPointEvent.BOTTOM_LEFT;
      }
      else if (p.getX()>=r.getX()+r.getWidth()-CORNER_RECT&&
               p.getY()<r.getY()+CORNER_RECT) {
        return RectPointEvent.TOP_RIGHT;
      }
      else if (p.getX()>=r.getX()+r.getWidth()-CORNER_RECT&&
               p.getY()>=r.getY()+r.getHeight()-CORNER_RECT) {
        return RectPointEvent.BOTTOM_RIGHT;
      }

      else if (p.getX()<r.getX()+CORNER_RECT&&
               p.getY()>=r.getY()+(r.getHeight()-CORNER_RECT)/2 &&
               p.getY()<=r.getY()+(r.getHeight()+CORNER_RECT)/2) {
        return RectPointEvent.MIDDLE_LEFT;
      }
      else if (p.getX()>=r.getX()+r.getWidth()-CORNER_RECT&&
               p.getY()>=r.getY()+(r.getHeight()-CORNER_RECT)/2 &&
               p.getY()<=r.getY()+(r.getHeight()+CORNER_RECT)/2) {
        return RectPointEvent.MIDDLE_RIGHT;
      }
      else if (p.getY()<r.getY()+CORNER_RECT&&
               p.getX()>=r.getX()+(r.getWidth()-CORNER_RECT)/2 &&
               p.getX()<=r.getX()+(r.getWidth()+CORNER_RECT)/2) {
        return RectPointEvent.MIDDLE_TOP;
      }
      else if (p.getY()>=r.getY()+r.getHeight()-CORNER_RECT&&
               p.getX()>=r.getX()+(r.getWidth()-CORNER_RECT)/2 &&
               p.getX()<=r.getX()+(r.getWidth()+CORNER_RECT)/2) {
        return RectPointEvent.MIDDLE_BOTTOM;
      }

      return null;
    }

    public RectanglePoint getRectanglePoint(MouseEvent e)
    {
      Point p=e.getPoint();
      RectanglePoint rect_point;
      // check points
      //System.out.println("rectPoint check for "+p.getX()+","+p.getY());
      //System.out.println("  full "+fullviewrect);
      rect_point=checkRectPoints(fullviewrect, p);


      // check visible rect
      if (rect_point==null) {
        //System.out.println(" visible "+content.getVisibleRect());
        rect_point=checkRectPoints(content.getVisibleRect(), p);
      }

      // check possible view port points
      if (rect_point==null) {
        JViewport vp=getViewPort();
        if (vp!=null) {
          //System.out.println(" vieport "+vp.getViewRect());
          rect_point=checkRectPoints(vp.getViewRect(), p);
        }
      }
      return rect_point;
    }

    @Override
    public void mouseClicked(MouseEvent e)
    { MouseEvent t=translate(e);

      if (debug) {
        System.out.println("click at     "+e.getX()+ ","+e.getY());
        System.out.println("  translated "+t.getX()+ ","+t.getY());
      }
      VisibleRect r=findRect(t.getPoint());
      if (r!=null) {
        fireRectEvent(r,t);
      }
      
      if (e.getButton()==MouseEvent.BUTTON1) {
        RectanglePoint rect_point=getRectanglePoint(e);
        if (rect_point!=null) fireRectangleEvent(t,rect_point);
      }   
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e)
    {
//      System.out.println("wheel "+e.getWheelRotation()+" "+e.getScrollAmount()+
//              " "+scalemode);
      if (scalemode!=0) {
        int diff=e.getUnitsToScroll()/e.getScrollAmount();
        switch (scalemode) {
          case SCALEX:
            if (diff>0) {
              setScaleX(getScaleX()*1.1, e.getPoint());
            }
            else {
              setScaleX(getScaleX()/1.1, e.getPoint());
            }
            break;
          case SCALEY:
            if (diff>0) {
              setScaleY(getScaleX()*1.1, e.getPoint());
            }
            else {
              setScaleY(getScaleX()/1.1, e.getPoint());
            }
            break;
          default:
            if (diff>0) {
              setScale(scale.scale(1.1), e.getPoint());
            }
            else {
              setScale(scale.scale(1.0/1.1), e.getPoint());
            }
        }
        //System.out.println("diff="+diff);
        
      }
    }

    @Override
    public void mouseMoved(MouseEvent e)
    {
      MouseEvent t;
      setCursor(def_cursor);
      if (selector!=null) {
        t=translate(e);
        selector.mouseMoved(t);
      }
      RectanglePoint rect_point=getRectanglePoint(e);
      if (rect_point!=null &&
          points.get(rect_point)!=null && !points.get(rect_point).isEmpty()) {
        setCursor(point_cursor);
      }
    }

    @Override
    public void mouseDragged(MouseEvent e)
    {
      //System.out.println("drag "+e.getPoint()+"/"+e.getButton());
      super.mouseDragged(e);
      // assure visibility of drag position in case of scroll panels
      JViewport vp=getViewPort();
      if (vp!=null) {
        Insets parent=getParent().getInsets();
        Point p=new Point(e.getX(), //+parent.left,
                          e.getY()  //+parent.top
                          );
        Rectangle r=vp.getViewRect();
        //System.out.println("Viewport rect "+r);
        if (!r.contains(p)) {
          r=new Rectangle(toInt(p.getX()-10), toInt(p.getY()-10), 20, 20);
          scrollRectToVisible(r);
          //System.out.println("adjust done");
        }
      }

      MouseEvent t;
      if (selector!=null) {
        t=translate(e);
        selector.mouseDragged(t);
      }
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
      MouseEvent t;
      if (selector!=null) {
        t=translate(e);
        selector.mousePressed(t);
      }
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
      MouseEvent t;
      if (selector!=null) {
        t=translate(e);
        selector.mouseReleased(t);
      }
    }
  }

  //////////////////////////////////////////////////////////////////////
  // rectangle creation

  public void setRectangleSelector(RectangleSelector s)
  {
    if (selector!=null) selector.uninstall();
    selector=s;
    if (selector!=null) selector.install(this);
  }

  public RectangleSelector getRectangleSelector()
  {
    return selector;
  }

  public static class RectangleSelector {
    private boolean           active;
    private Point             origin;
    private VisibleRect       rect;
    private boolean           move;
    private int               action;
    private BufferedComponent comp;
    private Stroke            line;

    static final Cursor move_cursor=Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
    static final Cursor cross_cursor=Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
    static final Cursor def_cursor=Cursor.getDefaultCursor();

    public void install(BufferedComponent comp)
    {
      if (this.comp!=null) {
        throw new IllegalStateException("selector already assigned");
      }
      this.comp=comp;
    }

    public void uninstall()
    {
      active=false;
      origin=null;
      comp=null;
      if (rect!=null) {
        rect.discard();
      }
      rect=null;
    }

    public void setStroke(Stroke line)
    { this.line=line;
    }

    protected VisibleRect getVisibleRect()
    {
      return rect;
    }
    
    protected void select(VisibleRect rect, int action)
    {
      //System.out.println("**** rect selected "+action+": "+rect);
      comp.fireRectModifiedEvent(rect,action);
    }

    private Cursor adjustRect(VisibleRect rect,
                              Point origin, Point current)
    { Dimension d=adjustDimension(
             new Dimension(toInt(current.getX()-origin.getX()),
                           toInt(current.getY()-origin.getY())));
      Point p1=getRectP1(origin,d);
      Point p2=getRectP2(origin,d);
      rect.setFrameFromDiagonal(p1, p2);

      return getCursor(d);
    }

    protected Cursor getCursor(Dimension d)
    {
      if (d.getWidth()<0) {
        if (d.getHeight()<0) {
          return Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
        }
        else {
          return Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
        }
      }
      else {
        if (d.getHeight()<0) {
          return Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
        }
        else {
          return Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
        }
      }
    }

    protected Dimension adjustDimension(Dimension d)
    { return d;
    }

    protected Dimension adjustMove(VisibleRect rect, Dimension d)
    {
      return d;
    }

    protected Point getOrigin(VisibleRectBorder b)
    {
      return b.getOppositeCornerPoint();
    }

    protected Point getRectP1(Point origin, Dimension d)
    { return origin;
    }

    protected Point getRectP2(Point origin, Dimension d)
    { Point p=new Point(origin);
      p.translate(toInt(d.getWidth()-1),toInt(d.getHeight()-1));
      return p;
    }

    // event processing
    public void mousePressed(MouseEvent e)
    {
      if (e.getButton()==MouseEvent.BUTTON1) active=true;
    }

    public void mouseReleased(MouseEvent e)
    {
      //System.out.println("released "+e.getPoint()+"/"+e.getButton());
      if (e.getButton()==MouseEvent.BUTTON1) {
        active=false;
        origin=null;
        if (rect!=null) {
          VisibleRect selected=rect;
          rect=null;
          if (debug) System.out.println("reset cursor");
          comp.setCursor(Cursor.getDefaultCursor());
          select(selected,action);
        }
      }
    }

    public void mouseMoved(MouseEvent e)
    {
      Point current=e.getPoint();

      if (e.isShiftDown()) {
        VisibleRect r=comp.findRect(current,false,false);
        if (r!=null) {
          //System.out.println("in rect");
          comp.setCursor(cross_cursor);
          return;
        }
      }
      else {
        VisibleRectBorder b=comp.findRectBorder(current,false);
        if (b!=null) {
          //System.out.println("on border");
          comp.setCursor(cross_cursor);
          return;
        }
      }
    }

    public void mouseDragged(MouseEvent e)
    {
      //System.out.println("drag "+e.getPoint()+"/"+e.getButton());

      if (active) {
        Point current=e.getPoint();
        if (!comp.getVisibleRect().contains(current)) return;
        if (origin==null) {
          // start rectangle
          
          if (move=e.isShiftDown()) {
            rect=comp.findRect(current,false,false);
            if (rect==null) {
              //System.out.println("found no move rect");
              active=false;
              return;
            }
            if (debug) System.out.println("found move rect "+rect.getId());
            comp.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            origin=current;
            action=RectModifiedEvent.RECT_MOVED;
          }
          else {
            VisibleRectBorder b=comp.findRectBorder(current,false);
            if (b!=null) {
              rect=b.getRect();
              origin=getOrigin(b);
              action=RectModifiedEvent.RECT_RESIZED;
            }
            else {
              origin=current;
              action=RectModifiedEvent.RECT_CREATED;
            }
          }
        }
        else {
          if (rect==null) {
            rect=comp.createRect(null,toInt(e.getX()),toInt(e.getY()),0,0);
            rect.setVisible(true);
            if (line!=null) rect.setStroke(line);
          }
          if (move) {
            rect.translate(toInt(current.getX()-origin.getX()),
                           toInt(current.getY()-origin.getY()));
            origin=current;
          }
          else {
            //System.out.println("adjust rect "+rect);
            Cursor c=adjustRect(rect, origin, current);
            if (c!=null) {
              comp.setCursor(c);
            }
          }
        }
      }
    }
  }

  public static class CenteredRectangleSelector extends RectangleSelector {
    @Override
    protected Point getRectP1(Point origin, Dimension d)
    {
      Point p=new Point(origin);
      p.translate(-toInt(d.getWidth()),-toInt(d.getHeight()));
      return p;
    }

    @Override
    protected Point getOrigin(VisibleRectBorder b)
    { return b.getRect().getCenter();
    }
  }

  public static class ProportionalRectangleSelector extends RectangleSelector {
    private ProportionProvider proportion;

    protected class ExtendedDimension extends Dimension {
      private Dimension adjust;

      public ExtendedDimension(int width, int height, Dimension adjust)
      { super(width, height);
        this.adjust=adjust;
      }

      public Dimension getAdjustment()
      { return adjust;
      }
    }

    public ProportionalRectangleSelector(Dimension d)
    { this.proportion=new ProportionProvider.Proportion(d);
    }

    public ProportionalRectangleSelector(ProportionProvider d)
    { this.proportion=d;
    }

    public ProportionalRectangleSelector(int w, int h)
    { this(new Dimension(w,h));
    }

    public ProportionProvider getProportionProvider()
    {
      ProportionProvider p=null;
      if (getVisibleRect()!=null) {
        p=getVisibleRect().getProportionProvider();
      }
      return p==null?proportion:p;
    }

    public void setProportionProvider(ProportionProvider proportion)
    {
      this.proportion=proportion;
    }

    @Override
    protected Dimension adjustDimension(Dimension d)
    { double dx=d.getWidth();
      double dy=d.getHeight();
      double sx=Math.abs(dx);
      double sy=Math.abs(dy);
      double prop=this.getProportionProvider().getProportion();
      double ax=sy*prop;
      double ay=sx/prop;
      Dimension adjust=new Dimension(0,0);

      //System.out.println("PROPORTION "+prop+" for "+d);
      if (ax>sx) {
        adjust=new Dimension(toInt((ax-sx)*Math.signum(dx)),0);
        sx=ax;
      }
      if (ay>sy) {
        adjust=new Dimension(0,toInt((ay-sy)*Math.signum(dy)));
        sy=ay;
      }

      int nx=toInt(sx*Math.signum(dx));
      int ny=toInt(sy*Math.signum(dy));
      //System.out.println("adjusted "+nx+","+ny);
      return new ExtendedDimension(nx,ny,adjust);
    }

    @Override
    protected Cursor getCursor(Dimension d)
    {
      if (d instanceof ExtendedDimension) {
        ExtendedDimension ed=(ExtendedDimension)d;
        //System.out.println("adjust "+ed.getAdjustment());
        double ax=ed.getAdjustment().getWidth();
        if (-20>ax || ax>20) {
          if (d.getHeight()>0) {
             return Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
          }
          else {
            return Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
          }
        }
        double ay=ed.getAdjustment().getHeight();
        if (-20>ay || ay>20) {
          if (d.getWidth()>0) {
             return Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
          }
          else {
            return Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
          }
        }
      }
      return super.getCursor(d);
    }
  }

  public static class CenteredProportionalRectangleSelector
         extends ProportionalRectangleSelector {
    public CenteredProportionalRectangleSelector(int w, int h)
    { super(w, h);
    }

    public CenteredProportionalRectangleSelector(Dimension d)
    { super(d);
    }

    public CenteredProportionalRectangleSelector(ProportionProvider d)
    { super(d);
    }

    @Override
    protected Point getOrigin(VisibleRectBorder b)
    { return b.getRect().getCenter();
    }

    @Override
    protected Point getRectP1(Point origin, Dimension d)
    { Point p=new Point(origin);
      p.translate(-(toInt(d.getWidth())),-(toInt(d.getHeight())));
      return p;
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

    // Test code
    System.out.println("***2D   1,1,10,10");
    Rectangle2D rect=new Rectangle2D.Double(1,1,10,10);
    System.out.println("rect: 1,1,10,10:"+rect);
    System.out.println("center "+rect.getCenterX()+","+rect.getCenterY());
    rect.setFrameFromDiagonal(new Point(1,1), new Point(3,3));
    System.out.println("rect D(I): 1,1 to 3,3:"+rect);
    rect.setFrameFromDiagonal(new Point2D.Double(1,1), new Point2D.Double(3,3));
    System.out.println("rect D(D): 1,1 to 3,3:"+rect);

    System.out.println("***I    1,1,10,10");
    rect=new Rectangle(1,1,10,10);
    System.out.println("rect: 1,1,10,10:"+rect);
    System.out.println("center "+rect.getCenterX()+","+rect.getCenterY());
    rect.setFrameFromDiagonal(new Point(1,1), new Point(3,3));
    System.out.println("rect D(D): 1,1 to 3,3:"+rect);
    rect.setFrameFromDiagonal(new Point2D.Double(1,1), new Point2D.Double(3,3));
    System.out.println("rect D(I): 1,1 to 3,3:"+rect);
    rect.setFrameFromCenter(new Point(2,2), new Point(3,3));
    System.out.println("rect C(I): 2,2 to 3,3:"+rect);
    rect.setFrameFromCenter(new Point(2,2), new Point(4,4));
    System.out.println("rect C(I): 2,2 to 4,4:"+rect);

    rect=new Rectangle2D.Double(1,1,11,11);
    System.out.println("*** rect: 1,1,11,11:"+rect);
    System.out.println("center "+rect.getCenterX()+","+rect.getCenterY());
    System.out.println("bounds "+rect.getBounds());
    System.out.println(" contains 0,0: "+rect.contains(0,0));
    System.out.println(" contains 1,1: "+rect.contains(1,1));
    System.out.println(" contains 10,10: "+rect.contains(10,10));
    System.out.println(" contains 11,11: "+rect.contains(11,11));
    System.out.println(" contains 12,12: "+rect.contains(12,12));
    System.out.println(" contains 13,13: "+rect.contains(13,13));
  }

  static class TestComponent extends BufferedComponent {
    TestComponent()
    { super(200,200);
      setup();
      setLimitWindowSize(true);
      setScaleMode(true);
    }

    void setup()
    { Graphics2D g;
      //
      g=createGraphics();

      int test=2;
      if ((test&4)==4) {
        setBorder(new EmptyBorder(10,10,10,10));
      }
      if ((test&1)==1) {
        
        g.setColor(Color.GREEN);
        g.drawRect(0, 0, 199, 199);
        g.setColor(Color.BLACK);
        g.drawRect(1, 1, 197, 197);
        g.setColor(Color.RED);
        g.drawRect(2, 2, 195, 195);
        g.setColor(Color.BLACK);
        g.drawRect(3, 3, 193, 193);
        g.setColor(Color.GREEN);
        g.drawRect(4, 4, 191, 191);

        BufferedImage img=new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        System.out.println(img.getWidth()+"x"+img.getHeight());
        Graphics2D gi=img.createGraphics();
        int end=9;
        gi.setColor(Color.WHITE);
        gi.drawLine(0, 0, 0, end);
        gi.drawLine(0, 0, end, 0);
        gi.drawLine(end, end, 0, end);
        gi.drawLine(end, end, end, 0);

        g.drawImage(img, 111, 100, null);
        g.drawImage(img, 100, 110, null);

        Rectangle rect=new Rectangle(100, 100, 10, 10);
        g.draw(rect);

        g.drawRect(6, 6, 2, 2);
        g.setColor(Color.WHITE);
        g.drawLine(7, 9, 7, 15);

        g.setColor(Color.BLUE);
        g.draw(getBounds());
      }
      if ((test&2)==2) {
        g.setColor(Color.BLACK);
        g.setXORMode(Color.WHITE);
        g.drawRect(10, 10, 20, 20);
        g.drawLine(20, 20, 40, 40);
        g.drawRect(1, 1, 197, 197);

        createRect("TestRect1",15,40,30,40).setVisible(true).setFixed(true);
        createRect("TestRect2", 5, 150, 40, 35).setVisible(true);
        createRect("TestRect3", 155, 5, 30, 40).setVisible(true);
        createRect("TestRect4", 155, 150, 40, 35).setVisible(true);
      }
    }
  }

  

  static class TestFrame extends JFrame {

    TestComponent tc;

    class MyListener implements RectPointEventListener,
                                RectEventListener {

      RectangleSelector[] sel=new RectangleSelector[]{
        new RectangleSelector(),
        new CenteredRectangleSelector(),
        new ProportionalRectangleSelector(tc),
        new CenteredProportionalRectangleSelector(tc)
      };
      int current=-1;

      public void rectanglePointClicked(RectPointEvent e)
      {
        System.out.println(e.getDescription());

        if (e.getClickCount()==2&&e.getButton()==MouseEvent.BUTTON1) {
          if (e.getCorner()==RectPointEvent.BOTTOM_LEFT) {
            System.out.println("  hide all");
            e.getComponent().hideAllRects();
          }
          if (e.getCorner()==RectPointEvent.BOTTOM_RIGHT) {
            System.out.println("  show all");
            e.getComponent().showAllRects();
          }
        }
        if (e.getClickCount()==1&&e.getButton()==MouseEvent.BUTTON1) {
          if (e.getCorner()==RectPointEvent.TOP_LEFT) {
            current++;
            if (current>=sel.length) current=0;
            System.out.println("  select mode "+current);
            e.getComponent().setRectangleSelector(sel[current]);
          }
        }
      }

      public void buttonClicked(RectEvent e)
      {
        System.out.println(e.getDescription());
        if (e.getClickCount()==2&&e.getButton()==MouseEvent.BUTTON1) {
          System.out.println("  hide "+e.getRect().getName());
          e.getRect().setVisible(false);
        }
      }
    }

    TestFrame()
    {
      tc=new TestComponent();
      JScrollPane sp=new JScrollPane(tc);
      //sp.setBorder(new BevelBorder(BevelBorder.RAISED));
      add(sp);
      pack();
      //setSize(100, 100);
      //setMaximumSize(getSize());
      System.out.println("SP: "+sp.getInsets());
      MyListener l=new MyListener();
      tc.addRectEventListener(l);
      tc.addRectPointEventListener(l);
    }
  }
}