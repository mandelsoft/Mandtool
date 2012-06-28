package com.mandelsoft.swing;

import java.awt.event.MouseEvent;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

public final class TableSelection extends Selection {

  private int col=-1;
  private JTable table;

  TableSelection(JTable table, MouseEvent e)
  {
    this(table);
    setLeadSelection(e);
  }

  TableSelection(JTable table, int lead, int col)
  {
    this(table);
    setLeadSelection(lead,col);
  }

  TableSelection(JTable table)
  {
    this.table=table;
  }

  @Override
  protected ListSelectionModel getSelectionModel()
  {
    return table.getSelectionModel();
  }

  void setLeadSelection(MouseEvent e)
  {
    setLeadSelection(table.rowAtPoint(e.getPoint()),
                     table.columnAtPoint(e.getPoint()));
  }

  void setLeadSelection(int lead, int col)
  {
    this.col=col<0?-1:table.convertColumnIndexToModel(col);
    super.setLeadSelection(lead);
  }

  @Override
  void setLeadSelection(int lead)
  {
    this.col=-1;
    super.setLeadSelection(lead);
  }

  public int getCol()
  {
    return col;
  }

  @Override
  protected int convertIndexToModel(int index)
  {
    return table.convertRowIndexToModel(index);
  }

  @Override
  public String toString()
  {
    return "index "+getLeadSelection()+", col "+col+", "+getSelectedIndices();
  }
}
