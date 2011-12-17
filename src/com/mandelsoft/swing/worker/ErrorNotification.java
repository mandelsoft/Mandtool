package com.mandelsoft.swing.worker;

import java.awt.Component;
import javax.swing.JOptionPane;

public class ErrorNotification<O extends Component> implements UIExecution<O> {

  private String msg;
  private String title;

  public ErrorNotification(String title, String msg)
  {
    this.msg=msg;
    this.title=title;
  }

  public void execute(O owner)
  {
    JOptionPane.showMessageDialog(owner, msg, title,
                                  JOptionPane.WARNING_MESSAGE);
  }
}
