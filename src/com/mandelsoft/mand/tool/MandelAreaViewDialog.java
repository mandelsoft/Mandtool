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

package com.mandelsoft.mand.tool;

import com.mandelsoft.io.AbstractFile;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import com.mandelsoft.mand.MandelData;
import com.mandelsoft.mand.MandelInfo;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.util.MandUtils;
import com.mandelsoft.mand.util.MandelList;
import com.mandelsoft.mand.util.MandelListFolder;
import com.mandelsoft.swing.BufferedComponent.RectModifiedEvent;
import com.mandelsoft.swing.BufferedComponent.RectModifiedEventListener;
import com.mandelsoft.swing.BufferedComponent.VisibleRect;
import com.mandelsoft.swing.DataField;
import com.mandelsoft.swing.GBC;
import com.mandelsoft.swing.GBCPanel;
import com.mandelsoft.swing.NumberField;
import com.mandelsoft.swing.TextField;
import com.mandelsoft.swing.Utils;
import com.mandelsoft.swing.WindowControlAction;
import java.awt.Component;

/**
 *
 * @author Uwe Krueger
 */
public abstract class MandelAreaViewDialog extends MandelDialog {

  static public boolean isReadonly(MandelWindowAccess owner, AbstractFile f,
                                   QualifiedMandelName name)
  {
    return (f!=null&&!f.isFile()) ||
            (name!=null && owner.getEnvironment().isReadonly(name.getLabel()) ||
            owner.getEnvironment().isReadonly());
  }

  ///////////////////////////////////////////////////////////////////////
  // View Panel
  ///////////////////////////////////////////////////////////////////////
  public  class MandelAreaView extends GBCPanel {
    protected QualifiedMandelName qname;
    protected VisibleRect rect;
    private boolean inupdate;
    private MandelInfo info;    // core data
    protected MandelData data;  // optional information
    private boolean change;
    protected boolean readonlyMode;
    private JLabel label;
    private int[] row;
    private int col;
    private int maxcol=0;
    private PropertyChangeListener updateListener;
    protected NumberField limitfield;
    protected JTextField infofield;

    protected JButton    showbutton;

    public MandelAreaView(QualifiedMandelName name, MandelInfo info, boolean change,
                                                        boolean readonly)
    {
      this.change=change&&!readonly;
      this.readonlyMode=readonly;
      this.info=info;
      this.updateListener=new UpdateListener();
      setup();
      setName(name);
    }

    public MandelAreaView(QualifiedMandelName name, MandelData data, boolean change,
                                                          boolean readonly)
    {
      this.change=change&&!readonly;
      this.readonlyMode=readonly;

      this.qname=name;
      this.data=data;
      this.info=data.getInfo();
     
      this.updateListener=new UpdateListener();
      
      setup();
      setName(name);
    }

    public MandelAreaView(boolean change)
    {
      this(null, new MandelInfo(), change, !change);
    }

    @Override
    protected void panelUnbound()
    {
      super.panelUnbound();
      if (rect!=null) {
        rect.discard();
      }
    }

    public void setName(QualifiedMandelName name)
    {
      if (name==null) return;
      String text;
      qname=name;
      if (name.isRoot()) {
        text="Root Area";
      }
      else {
        text="Area "+name.getName();
      }

      setName(text);
      if (altspec!=null) altspec.setName(text);
      if (modtimes!=null) modtimes.setName(text);
      if (tags!=null) tags.setName(text);
      if (attrs!=null) attrs.setName(text);
      label.setText(text);
    }

    protected void _setRect(VisibleRect rect)
    {
      this.rect=rect;
    }
    
    public void setInfo(String name, MandelInfo info)
    {
      data=null;
      setName(name);
      _setInfo(info);
    }

    protected void _setInfo(MandelInfo info)
    {
      this.info.setInfo(info);
      updateFields();
    }

    public void setInfo(MandelInfo info)
    {
      data=null;
      _setInfo(info);
    }

    public void setData(String name, MandelData data)
    {
      this.data=data;
      setName(name);
      _setInfo(data.getInfo());
    }

    public void setData(MandelData data)
    {
      this.data=data;
      _setInfo(data.getInfo());
    }

    public MandelInfo getInfo()
    {
      return info;
    }

    public MandelData getData()
    {
      return data;
    }

    public QualifiedMandelName getQualifiedName()
    {
      return qname;
    }

    public boolean isChangeable()
    {
      return change;
    }
    
    @Override
    protected void adjustBorderArea(Rectangle rect)
    {
      rect.setRect(rect.getX()*2, rect.getY()+1,
                   rect.getWidth()*2, rect.getHeight());
    }

    protected void setup()
    {
      JSeparator sep;

      col=0;
      row=new int[col+1];
      row[col]=1;

      setupFields();

      add(label=new JLabel(getName()),
          new GBC(0, 0, (maxcol+1)*2, 1).setLayout(GBC.HORIZONTAL,
                                                   GBC.CENTER).
              setBottomInset(10));
      label.setHorizontalAlignment(JLabel.CENTER);
      label.setFont(label.getFont().deriveFont(Font.BOLD,
                                               label.getFont().getSize()+2));

      setupButtonPanel();

      //Put the panels in this panel, labels on left,
      //text fields on right.
      setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    protected void setCol(int col)
    {
      this.col=col;
      if (col>maxcol) {
        int[] old=row;
        int i;
        row=new int[col+1];
        for (i=0; i<old.length; i++) {
          row[i]=old[i];
        }
        for (; i<=col; i++) {
          row[i]=1;
        }
        maxcol=col;
      }
    }

    protected int getCol()
    {
      return col;
    }

    protected int getRow(int col)
    {
      if (col>maxcol) return 1;
      return row[col]-1;
    }

    protected int getRow()
    {
      return getRow(col);
    }

    protected void skipRows(int n)
    {
      while (n>0) {
        n--;
        JLabel d1=new JLabel("");
        JLabel d2=new JLabel("");
        //d1.setVisible(false);
        //d2.setVisible(false);
        addField(d1,d2);
      }
    }

    protected int getGridRowFieldCol(int col)
    {
      if (col>maxcol) return 1;
      return row[col];
    }

    protected int getGridRowForGridCol(int col)
    {
      if (col/2>maxcol) return 1;
      return row[col/2];
    }

    protected int getMaxGridRow()
    {
      int max=0;
      for (int i=0; i<row.length; i++) {
        if (row[i]>max) max=row[i];
      }
      return max;
    }

    protected int getMaxGridCol()
    {
      return row.length*2+1;
    }

    protected void setMaxGridRow(int max)
    {
      for (int i=0; i<row.length; i++) {
        if (row[i]<max) row[i]=max;
      }
    }

    protected void addSeparator()
    {
      JSeparator sep=new JSeparator();
      sep.setName("sep");

      add(sep, new GBC(col*2, row[col]++, 2, 1).setWeight(100, 0).
              setLayout(GBC.BOTH, GBC.CENTER).
              setInsets(10, 0, 10, 0));
    }

    protected JTextField createField(String name, String prop)
    {
      return createField(name, prop, change);
    }

    protected JTextField createField(String name, String prop, boolean change)
    {
      return createField(name, prop, change, null);
    }

    protected JTextField createField(String name, String prop, boolean change,
                                     DataField field)
    {
      BeanAccess access=new BeanAccess(info, prop);
      Class type=access.getType();
      if (!access.hasGetter()) {
        throw new IllegalArgumentException("no getter found for "+prop+" in "+
                info.getClass());
      }

      if (String.class==type) {
        field=createTextField((TextField)field,
                              (ValueAccess<String>)access);
      }
      else {
        if (!Number.class.isAssignableFrom(type)) {
          throw new IllegalArgumentException("illegal type for "+prop+" in "+
                  info.getClass());
        }
        field=createNumberField((NumberField)field,
                                (ValueAccess<Number>)access);
      }

      return setupField(field, name, change, access);
    }

    private DataField<String> createTextField(TextField field,
                                      ValueAccess<String> access)
    {
      if (field==null) field=new TextField(access.getValue());
      else field.setDataValue(access.getValue());
      return field;
    }

    private DataField<Number> createNumberField(NumberField field,
                                          ValueAccess<Number> access)
    {
      if (field==null) field=new NumberField(access.getValue());
      else field.setDataValue(access.getValue());
      field.setEnforceCorrectValue(true);
      return field;
    }

//    protected JTextField createField(String name, boolean change,
//                                     ValueAccess<Number> access)
//    {
//      return setupField(new NumberField(access.getValue()),
//                        name, change, access);
//
//    }

    private Map<DataField<?>, ValueAccess<?>> fields=
                     new HashMap<DataField<?>, ValueAccess<?>>();

    private <T> JTextField setupField(DataField<T> dfield, String name,
                                       boolean change,
                                       ValueAccess<T> up)
    { JTextField field=(JTextField)dfield;

      field.setEditable(change);
      //field.setEnabled(change);
      field.setHorizontalAlignment(JTextField.TRAILING);
      field.setColumns(field_length);

      if (up!=null) {
        field.addActionListener(up);
      }
      field.addPropertyChangeListener("value", updateListener);

      if (!change) {
        field.setBorder(null);
      }
      setupField(field, name);
      if (up!=null) fields.put(dfield, up);
      return field;
    }

    protected JTextField createInfoField(String name, String value)
    {
      return createInfoField(name, value, field_length);
    }

    protected JTextField createInfoField(String name, String value, int len)
    {
      JTextField field=new JTextField(value);
      field.setEditable(change);
      field.setColumns(len);
      field.setBorder(null);
      field.setHorizontalAlignment(JTextField.RIGHT);
      setupField(field, name);
      return field;
    }

    private static final int LEN_1=60;
    private static final int LEN_2=15;

    private int field_length=LEN_1;

    protected void updateFieldLength(int len)
    {
      if (field_length==len) return;
      field_length=len;
      for (Component c:getComponents()) {
        if (c instanceof JTextField) {
          ((JTextField)c).setColumns(len);
        }
      }
    }

    protected void addField(JLabel label, JComponent field)
    {
      label.setLabelFor(field);
      label.setHorizontalAlignment(JLabel.LEFT);
      add(label, new GBC(col*2, row[col]).setWeight(0, 0).setAnchor(GBC.WEST));
      add(field,
            new GBC(col*2+1, row[col]++).setWeight(100, 10).setLeftInset(10));
      if (col>0 && field_length==LEN_1) updateFieldLength(LEN_2);
    }

    protected void setupField(JTextField field, String name)
    {
      addField(new JLabel(name),field);
    }

    protected void updateFields()
    {
      setAltSpec();
      setModTimes();
      setTags();
      setAttrs();
      for (DataField f:fields.keySet()) {
        ValueAccess acc=fields.get(f);
        f.setDataValue(acc.getValue());
      }
    }
    
    protected JPanel buttons;

    protected void newButtonPanel()
    {
      int row=getMaxGridRow()+1;
      setMaxGridRow(row);
      buttons=new JPanel();
      add(buttons, GBC(0, row).setSpanW(getMaxGridCol()+1));
    }

    protected void setupButtonPanel()
    {
      newButtonPanel();
      setupButtons();
    }

    protected JButton createButton(String name, String tip, ActionListener l)
    {
      JButton b=new JButton(name);
      if (tip!=null) b.setToolTipText(tip);
      b.addActionListener(l);
      buttons.add(b);
      return b;
    }

    protected void setupButtons()
    {
      modtimes=new ModTimesAction();
      JButton b=modtimes_button=new JButton(modtimes);
      b.setToolTipText("Show modification times");
      buttons.add(b);
      altspec=new AltSpecAction();
      b=new JButton(altspec);
      b.setToolTipText("Show alternate coordinate format");
      buttons.add(b);
      tags=new TagsAction();
      b=tags_button=new JButton(tags);
      b.setToolTipText("Show area tags");
      buttons.add(b);
      attrs=new AttrsAction();
      b=attrs_button=new JButton(attrs);
      b.setToolTipText("Show area attributes");
      buttons.add(b);
      
      modtimes_button.setVisible(data!=null);
    }

    ////////////////////////////////////////////////////////////////////////
    // Sub Spec Windows
    ////////////////////////////////////////////////////////////////////////

     private abstract class SubSpecAction extends WindowControlAction {
      private MandelSpecDialog spec;

      public SubSpecAction(String label)
      {
        super(null, label);
      }

      public void setInfo(MandelInfo info)
      {
        if (spec!=null) spec.setInfo(info);
      }

      public void setData(MandelData data)
      {
        if (spec!=null) spec.setData(data);
      }

      public void setName(String name)
      {
        if (spec!=null) spec.setName(name);
      }

      @Override
      protected Window createWindow(Window owner)
      {
        if (owner==null) {
          Container c=MandelAreaView.this;
          while (c.getParent()!=null && !(c instanceof Window)) {
            c=c.getParent();
            //System.out.println("C: "+c);
          }
          owner=(Window)c;
        }
        spec=createDialog(owner,MandelAreaView.this.getName(),change);
        setInfo(getInfo());
        spec.addChangeListener(new ChangeListener() {

          public void stateChanged(ChangeEvent e)
          {
            MandelSpecDialog s=(MandelSpecDialog)e.getSource();
            inupdate=true;
            s.updateInfo(getInfo());
            updateFields();
            inupdate=false;
          }

        });
        return spec;
      }

      protected abstract MandelSpecDialog createDialog(Window owner, String name,
                                                       boolean change);
    }

   ////////////////////////////////////////////////////////////////////////
    // modufication time sub window

    private ModTimesAction modtimes;
    private JButton        modtimes_button;

    private void setModTimes()
    {
      if (modtimes!=null) {
        if (getData()!=null) {
          modtimes.setData(getData());
          modtimes_button.setVisible(true);
        }
        else {
          modtimes.setEnabled(true);         // close window and
          modtimes_button.setVisible(false); // hide active button
        }
      }
    }

    private class ModTimesAction extends SubSpecAction {
      public ModTimesAction()
      {
        super("Modification Times");
      }

      @Override
      protected MandelSpecDialog createDialog(Window owner, String name,
                                              boolean change)
      {
        MandelSpecDialog spec=new ModTimes(owner,name);
        spec.setData(MandelAreaView.this.getData());
        return spec;
      }
    }

    ////////////////////////////////////////////////////////////////////////
    // tags sub window

    private TagsAction tags;
    private JButton    tags_button;

    private void setTags()
    {
      if (tags!=null) {
        if (getInfo()!=null) {
          tags.setInfo(getInfo());
          tags_button.setVisible(true);
        }
        else {
          tags.setEnabled(true);         // close window and
          tags_button.setVisible(false); // hide active button
        }
      }
    }

    private class TagsAction extends SubSpecAction {
      public TagsAction()
      {
        super("Tags");
      }

      @Override
      protected MandelSpecDialog createDialog(Window owner, String name,
                                              boolean change)
      {
        return new TagSpec(owner,name,!readonlyMode);
      }
    }

    ////////////////////////////////////////////////////////////////////////
    // attribute sub window

    private AttrsAction attrs;
    private JButton    attrs_button;

    private void setAttrs()
    {
      if (attrs!=null) {
        if (getInfo()!=null) {
          attrs.setInfo(getInfo());
          attrs_button.setVisible(true);
        }
        else {
          attrs.setEnabled(true);         // close window and
          attrs_button.setVisible(false); // hide active button
        }
      }
    }

    private class AttrsAction extends SubSpecAction {
      public AttrsAction()
      {
        super("Attributes");
      }

      @Override
      protected MandelSpecDialog createDialog(Window owner, String name,
                                              boolean change)
      {
        return new AttrSpec(owner,name,!readonlyMode);
      }
    }
    
    ////////////////////////////////////////////////////////////////////////
    // alternate specification sub window

    private AltSpecAction altspec;

    private void setAltSpec()
    {
      if (altspec!=null)  altspec.setInfo(getInfo());
    }

    private class AltSpecAction extends SubSpecAction {
      public AltSpecAction()
      {
        super("Alt. Spec");
      }

      @Override
      protected MandelSpecDialog createDialog(Window owner, String name,
                                              boolean change)
      {
        return new AltSpec(owner,name,change);
      }
    }

    ////////////////////////////////////////////////////////////////////
    // prepare show
    ////////////////////////////////////////////////////////////////////

    private class ModifiedListener implements RectModifiedEventListener {
      public void rectModified(RectModifiedEvent e)
      {
        MandelInfo info=getInfo();
        System.out.println("info is "+info);
        updateInfo(info,rect._getRect());
        MandUtils.round(info);
        setInfo(info);
      }
    }

    protected String getRectLabel()
    {
      QualifiedMandelName n=getQualifiedName();
      if (n!=null) return n.toString();
      return getTitle();
    }

    protected void addShowButton(String help, boolean subst)
    {
       showbutton=createButton("Show", help, new ShowAction(subst));
    }

    private class ShowAction implements ActionListener {
      private boolean subst;

      public ShowAction(boolean subst)
      {
        this.subst=subst;
      }

      public void actionPerformed(ActionEvent e)
      {
        if (rect==null) {
          String label=getRectLabel();
          _setRect(getMandelWindowAccess().getMandelImagePane().getImagePane().
                createRect(label,label));
          rect.addRectModifiedEventListener(new ModifiedListener());
          rect.setFixed(!isChangeable());
        }
        // getMandelWindowAccess().getMandelImagePane().hideSubRects();
        rect.activate(subst);
        updateSlave();
        rect.setVisible(true);
      }
    }

    ////////////////////////////////////////////////////////////////////

    protected void setupFields()
    {
      createField("area center X", "XM");
      createField("area center Y", "YM");
      createField("dimension X", "DX");
      createField("dimension Y", "DY");
      limitfield=(NumberField)createField("iteration limit", "LimitIt");
      //addSeparator();
      createField("image width", "RX");
      createField("image height", "RY");
      createField("location hint", "Location").setEditable(!readonlyMode);

      if (qname!=null) {
        infofield=createInfoField("info", getInfoString());
      }
      else {
        skipRows(1);
      }

      addBorder(0, 7, 1, 2);
      addBorder(0, 5, 1, 2);
      addBorder(0, 0, 1, 5);
    }

    protected String getInfoString()
    {
      return MandelAreaViewDialog.this.getInfoString(getEnvironment(),qname);
    }

    protected void updateSlave()
    {
      System.out.println("update slave");
      if (qname!=null && infofield!=null) infofield.setText(getInfoString());
      if (rect!=null) updateRect(rect,getInfo());
    }

    protected void updateRect(VisibleRect rect, MandelInfo info)
    {
      getMandelWindowAccess().getMandelImagePane().updateRect(rect,info);
    }

    synchronized public void updateInfo(MandelInfo info, Rectangle rect)
    {
      getMandelWindowAccess().getMandelImagePane().updateInfo(info, rect);
    }

    class UpdateListener implements PropertyChangeListener {

      public void propertyChange(PropertyChangeEvent evt)
      { // only way to assure listener order
        // cannot add value acces listener as property change listener
        ValueAccess acc=fields.get((DataField)evt.getSource());
        if (acc!=null) acc.propertyChange(evt);
        if (!inupdate) {
          setAltSpec();
        }
        updateSlave();
      }
    }
  }

  public static String getInfoString(ToolEnvironment env,
                                     QualifiedMandelName qname)
  {
    if (qname==null) return "";
    StringBuilder sb=new StringBuilder();
    if (env.getFavorites()!=null) {
      if (contains(env.getFavorites().getRoot(), qname.getMandelName())) {
        sb.append("Favorite");
      }
    }

    if (env.getTodos()!=null) {
      if (contains(env.getTodos().getRoot(), qname.getMandelName())) {
        if (sb.length()!=0) sb.append(", ");
        sb.append("Todo");
      }
    }

    if (env.getAreas()!=null) {
      if (contains(env.getAreas(), qname.getMandelName())) {
        if (sb.length()!=0) sb.append(", ");
        sb.append("Key area");
      }
    }
    return sb.toString();
  }

  static private boolean contains(MandelList list, MandelName name)
  {
    for (QualifiedMandelName n:list) {
      if (name.equals(n.getMandelName())) return true;
    }
    return false;
  }

  static private boolean contains(MandelListFolder folder, MandelName name)
  {
    for (QualifiedMandelName n:folder.allentries()) {
      if (name.equals(n.getMandelName())) return true;
    }
    return false;
  }

  ///////////////////////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////
  //// Dialog
  ///////////////////////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////

  private MandelAreaView view;

  public MandelAreaViewDialog(MandelWindowAccess owner, String title)
  {
    super(owner, title);
  }

  public MandelAreaViewDialog(MandelWindowAccess owner, String title,
                              QualifiedMandelName name, Object info,
                              boolean change, boolean readonly)
  {
    super(owner, title);
    setup(name, info, change, readonly);
    setVisible(true);
  }

  public MandelAreaViewDialog(MandelWindowAccess owner, String title,
                              boolean change, boolean readonly)
  {
    super(owner, title);
    setup(null, new MandelInfo(), change, readonly);
  }

  protected void setup(QualifiedMandelName name, Object info,
                       boolean change, boolean readonly)
  {
    view=createView(name, info, change, readonly);
    add(view);
    pack();
    this.setResizable(false);
  }

  protected MandelAreaView createView(QualifiedMandelName name, Object info,
                                      boolean change, boolean readonly)
  {
    return new MandelAreaView(name, (MandelInfo)info, change, readonly);
  }

  protected MandelAreaView getView()
  {
    return view;
  }

  protected JDialog getDialog()
  {
    return this;
  }

  public void setInfo(String name, MandelInfo info)
  {
    getView().setInfo(name, info);
  }

  public void setData(String name, MandelData data)
  {
    getView().setData(name, data);
  }

  public MandelInfo getInfo()
  {
    return getView().getInfo();
  }
  
  ///////////////////////////////////////////////////////////////////////
  // utilitiy classes
  ///////////////////////////////////////////////////////////////////////
  static protected abstract class ValueAccess<T>
                            implements ActionListener, PropertyChangeListener {

    public void actionPerformed(ActionEvent e)
    {
      //System.out.println("action "+e);
      DataField<T> f=(DataField<T>)e.getSource();
      setValue(f.getDataValue());
    }

    public void propertyChange(PropertyChangeEvent e)
    {
      String propertyName=e.getPropertyName();
      System.out.println("CHANGE "+propertyName+"="+e.getOldValue()+
              "->"+e.getNewValue());
      setValue((T)e.getNewValue());
    }

    public abstract void setValue(T n);
    public abstract T getValue();
  }

  protected static class BeanAccess<T> extends ValueAccess<T> {

    private String prop;
    private Object bean;
    private Method setter;
    private Method getter;
    private Class type;

    public BeanAccess(Object bean, String prop)
    {
      this.bean=bean;
      this.prop=prop;

      String mname="get"+prop;
      for (Method m:bean.getClass().getMethods()) {
        if (m.getName().equals(mname)) {
          Class<?>[] params=m.getParameterTypes();
          if (params==null||params.length==0) {
            Class<?> r=mapType(m.getReturnType());
            if (r!=null&&(Number.class.isAssignableFrom(r)||
                    String.class.isAssignableFrom(r))) {
              getter=m;
              type=r;
            }
          }
        }
      }

      mname="set"+prop;
      if (type!=null) {
        //System.out.println("trying to get setter for "+prop+": "+type);
        try {
          Method m=bean.getClass().getMethod(mname, new Class[]{type});
          setter=checkSetter(m);
        }
        catch (Exception ex) {
          // ignore
        }
      }
      if (setter==null) {
        for (Method m:bean.getClass().getMethods()) {
          if (m.getName().equals(mname)) {
            //System.out.println("checking "+m);
            setter=checkSetter(m);
            if (setter!=null) break;
          }
        }
      }
    }

    public String getProperty()
    { return prop;
    }

    protected Method checkSetter(Method m)
    {
      //System.out.println("checking "+m);
      Class<?>[] params=m.getParameterTypes();
      if (params!=null) {
        if (params.length==1) {
          if (params[0]==type) return m;
          Class t=mapType(params[0]);
          if (Number.class.isAssignableFrom(t)) {
            if (type==null) {
              type=t;
            }
            return m;
          }
          else {
            //System.out.println("no assignment "+params[0]+"/"+t);
          }
        }
      }
      return null;
    }

    private Class mapType(Class t)
    {
      if (t==double.class) {
        t=Double.class;
      }
      else if (t==float.class) {
        t=Float.class;
      }
      else if (t==long.class) {
        t=Long.class;
      }
      else if (t==int.class) {
        t=Integer.class;
      }
      else if (t==short.class) {
        t=Short.class;
      }
      else if (t==byte.class) {
        t=Byte.class;
      }
      return t;
    }

    public Class getType()
    {
      return type;
    }

    public boolean hasGetter()
    {
      return getter!=null;
    }

    public boolean hasSetter()
    {
      return setter!=null;
    }

    public void setValue(T n)
    {
      System.out.println("setting "+prop+" to "+n);
      if (setter==null) {
        throw new IllegalArgumentException("no setter for property "+prop);
      }
      try {
        setter.invoke(bean,
                      new Object[]{Utils.convertValueToValueClass(n, type)});
      }
      catch (Exception ex) {
        throw new IllegalArgumentException("cannot access property "+setter);
      }
    }

    public T getValue()
    {
      if (getter==null) {
        throw new IllegalArgumentException("no getter for property "+prop);
      }
      try {
        return (T)getter.invoke(bean, new Object[]{});
      }
      catch (Exception ex) {
        throw new IllegalArgumentException("cannot access property "+setter);
      }
    }
  }
}
