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

import java.awt.Dialog;
import java.awt.Dialog.ModalityType;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Window;
import javax.swing.JDialog;

/**
 *
 * @author Uwe Krüger
 */

public class IJDialog extends JDialog {

  public IJDialog(Window owner, String title, ModalityType modalityType,
                  GraphicsConfiguration gc)
  {
    super(owner, title, modalityType, gc);
  }

  public IJDialog(Window owner, String title, ModalityType modalityType)
  {
    super(owner, title, modalityType);
  }

  public IJDialog(Window owner, String title)
  {
    super(owner, title);
  }

  public IJDialog(Window owner, ModalityType modalityType)
  {
    super(owner, modalityType);
  }

  public IJDialog(Window owner)
  {
    super(owner);
  }

  public IJDialog(Dialog owner, String title, boolean modal,
                  GraphicsConfiguration gc)
  {
    super(owner, title, modal, gc);
  }

  public IJDialog(Dialog owner, String title, boolean modal)
  {
    super(owner, title, modal);
  }

  public IJDialog(Dialog owner, String title)
  {
    super(owner, title);
  }

  public IJDialog(Dialog owner, boolean modal)
  {
    super(owner, modal);
  }

  public IJDialog(Dialog owner)
  {
    super(owner);
  }

  public IJDialog(Frame owner, String title, boolean modal,
                  GraphicsConfiguration gc)
  {
    super(owner, title, modal, gc);
  }

  public IJDialog(Frame owner, String title, boolean modal)
  {
    super(owner, title, modal);
  }

  public IJDialog(Frame owner, String title)
  {
    super(owner, title);
  }

  public IJDialog(Frame owner, boolean modal)
  {
    super(owner, modal);
  }

  public IJDialog(Frame owner)
  {
    super(owner);
  }

  public IJDialog()
  {
  }

  @Override
  public void dispose()
  {
    super.dispose();
    System.out.println("CLOSED");
    cleanup();
  }

  protected void cleanup()
  {
  }
}
