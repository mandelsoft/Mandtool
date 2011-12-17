package com.mandelsoft.swing.worker;

import java.awt.Component;

public interface UIExecution<O extends Component> {
  public void execute(O owner);
}
