package com.mandelsoft.swing;

import java.awt.event.MouseEvent;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

public final class ListSelection extends Selection {
  private JList list;

  ListSelection(JList list)
  {
    this.list=list;
  }

  ListSelection(JList list, int lead)
  {
    this.list=list;
    setLeadSelection(lead);
  }

  ListSelection(JList list, MouseEvent e)
  {
    this.list=list;
    setLeadSelection(e);
  }

  void setLeadSelection(MouseEvent e)
  {
    setLeadSelection(list.locationToIndex(e.getPoint()));
  }

  @Override
  protected ListSelectionModel getSelectionModel()
  {
    return list.getSelectionModel();
  }
}
