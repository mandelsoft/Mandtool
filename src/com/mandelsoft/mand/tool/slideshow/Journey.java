
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
package com.mandelsoft.mand.tool.slideshow;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.util.MandelList;

/**
 *
 * @author Uwe Krüger
 */
public class Journey extends AbstractSlideShow {
  private List<QualifiedMandelName> list;

  private QualifiedMandelName start;
  private QualifiedMandelName end;
  private MandelName top;
  private QualifiedMandelName current;
  private MandelName next;
  private boolean up;
  private boolean highlight;

  Journey()
  {
    super("Journey",5000);
    addAction(new JourneyAction()); // journey from one area up to the common
                                    // base area down to another area
    addAction(new ZoomInAction());  // journey from the root to an area
    addAction(new ZoomOutAction()); // journey from an area to the root
    addAction(new TripAction());    // journey from the the first area
                                    // of a list to all the others
  }

  @Override
  public void startShow(MandelList list)
  {
    if (list.size()>=2) {
      this.list=new ArrayList<QualifiedMandelName>(list);
      QualifiedMandelName start=this.list.get(0);
      this.list.remove(0);
      startShow(start,start);
    }
  }

  @Override
  public void startShow(QualifiedMandelName start, QualifiedMandelName end)
  {
    model.setActive(this);
    _startShow(start,end);
    if (initiateNext()) start();
    else cancel();
  }

  private void _startShow(QualifiedMandelName start, QualifiedMandelName end)
  {
    System.out.println("start journey from "+start+" to "+end);
    this.up=true;
    this.highlight=false;
    this.start=start;
    this.end=end;
    this.top=start.getMandelName();
    while (!top.isAbove(end.getMandelName())) {
      top=top.getParentName();
    }
    System.out.println("top is "+top);
    if (!model.getCurrentQualifiedMandelName().equals(start)) {
      model.show(start);
    }
    current=start;
  }

  private boolean initiateNext()
  {
    if (current.equals(end)) {
      System.out.println("journey destination reached");
      if (list==null || list.size()==0)
        return false;
      QualifiedMandelName n=list.get(0);
      list.remove(0);
      _startShow(end,n);
      return initiateNext();
    }
    else {
      if (current.getMandelName().equals(top)) {
        System.out.println("journey top reached");
        up=false;
      }
      if (up) {
        next=current.getMandelName().getParentName();
        System.out.println("next up "+next);
      }
      else {
//        int l=current.isRoot()?0:current.getMandelName().getEffective().length();
//        next=new MandelName(end.getMandelName().getEffective().substring(0, l+1));
        next=current.getMandelName().sub(end.getMandelName());
        this.setInitialDelay(5000);
        System.out.println("next down "+next);
      }
    }
    return true;
  }

  public void actionPerformed(ActionEvent e)
  {
    boolean cont=true;

    if (up) {
      System.out.println("handle up");
      if (!highlight) {
        QualifiedMandelName old=current;
        highlight=true;
        if (end.getMandelName().equals(next)) {
           model.show(end);
           current=end;
        }
        else {
          model.show(next);
          current=model.getCurrentQualifiedMandelName();
        }
        model.setHighLight(old);
      }
      else {
        highlight=false;
        cont=initiateNext();
      }
    }
    else {
      System.out.println("handle down "+highlight);
      QualifiedMandelName n;
      if (next.getName().equals(end.getMandelName())) {
        n=end;
      }
      else {
        n=new QualifiedMandelName(next);
      }

      if (!highlight) {
        highlight=true;
        model.setHighLight(n);
      }
      else {
        highlight=false;
        model.show(n);
        current=model.getCurrentQualifiedMandelName();
        cont=initiateNext();
      }
    }
    if (cont) {
      System.out.println("start timer");
      start();
    }
    else cancel();
  }

  @Override
  public void cancel()
  {
    super.cancel();
    if (list!=null) list.clear();
  }

  /////////////////////////////////////////////////////////////////////////
  // supported actions
  /////////////////////////////////////////////////////////////////////////

  private class TripAction extends SlideShowActionBase {
    public TripAction()
    {
      super("Trip", LIST);
    }

    public void actionPerformed(ActionEvent e)
    {
      if (isEnabled()) {
        MandelList start=getMandelList(e);
        if (start!=null) {
          startShow(start);
        }
      }
    }

    protected MandelList getMandelList(ActionEvent e)
    {
      MandelList l=null;
      SlideShowSource.ListMode m=model.getSource(e).getListMode(model);
      return m==null?null:m.getMandelList(model);
    }
  }

  private class JourneyAction extends SlideShowActionBase {
    protected JourneyAction(String name, int mode)
    {
      super(name, mode);
    }

    public JourneyAction()
    {
      this("Journey",TWO);
    }

    protected QualifiedMandelName getStartName(ActionEvent e)
    {
      SlideShowSource.TwoMode m=model.getSource(e).getTwoMode(model);
      return m==null?null:m.getFirstName(model);
    }

    protected QualifiedMandelName getEndName(ActionEvent e)
    {
      SlideShowSource.TwoMode m=model.getSource(e).getTwoMode(model);
      return m==null?null:m.getSecondName(model);
    }

    public void actionPerformed(ActionEvent e)
    {
      if (isEnabled()) {
        QualifiedMandelName start=getStartName(e);
        QualifiedMandelName end=getEndName(e);
        if (start!=null&&end!=null) {
          startShow(start, end);
        }
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////

  private abstract class ZoomAction extends JourneyAction {
    public ZoomAction(String name)
    {
      super(name,ONE);
    }

    protected QualifiedMandelName getSingleName(ActionEvent e)
    {
      SlideShowSource.OneMode m=model.getSource(e).getOneMode(model);
      return m==null?null:m.getSingleName(model);
    }
  }

  /////////////////////////////////////////////////////////////////////////

  private class ZoomInAction extends ZoomAction {
    public ZoomInAction()
    {
      super("Zoom In");
    }

    @Override
    protected QualifiedMandelName getStartName(ActionEvent e)
    {
      return QualifiedMandelName.ROOT;
    }

    @Override
    protected QualifiedMandelName getEndName(ActionEvent e)
    {
      return getSingleName(e);
    }
  }

  /////////////////////////////////////////////////////////////////////////

  private class ZoomOutAction extends ZoomAction {
    public ZoomOutAction()
    {
      super("Zoom Out");
    }

    @Override
    protected QualifiedMandelName getStartName(ActionEvent e)
    {
       return getSingleName(e);
    }

    @Override
    protected QualifiedMandelName getEndName(ActionEvent e)
    {
      return QualifiedMandelName.ROOT;
    }
  }
}
