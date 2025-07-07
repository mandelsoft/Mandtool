/*
 * Copyright 2025 uwekr.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mandelsoft.swing;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.ExecutionException;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.getRootFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;

/**
 *
 * @author uwekr
 */
public class WorkerProgressDialog<T,V> extends JDialog {
  private SwingWorker<T,V> worker;
  private JDialog dialog;
  private JProgressBar myBar;
  private JLabel noteLabel;
  private Component parentComponent;
  private String note;
  private Object message;
  private PropertyChangeListener listener;

  
  public static <T, V> T show(Component parent, SwingWorker<T, V> worker, String title, String note, boolean cancel) throws ExecutionException, InterruptedException {
    WorkerProgressDialog<T, V> d;

    Window window = getWindowForComponent(parent);
    if (window instanceof Frame) {
      d = new WorkerProgressDialog<T, V>((Frame) window, title);
    } else {
      d = new WorkerProgressDialog<T, V>((Dialog) window, title);
    }
    d.initDialog(parent, worker, title, note, cancel);
    if (worker.isCancelled()) {
      throw new InterruptedException(String.format("%s cancelled", title));
    }
    return d.worker.get();
  }
  
   private static Window getWindowForComponent(Component parentComponent)
          throws HeadlessException {
    if (parentComponent == null) {
      return getRootFrame();
    }
    if (parentComponent instanceof Frame || parentComponent instanceof Dialog) {
      return (Window) parentComponent;
    }
    return getWindowForComponent(parentComponent.getParent());
  }
   
  private WorkerProgressDialog(Frame window, String title) {
    super(window, title, true);
  }

  private WorkerProgressDialog(Dialog window, String title) {
    super(window, title, true);
  }

private void initDialog(Component parent,SwingWorker worker, String title, String note, boolean cancel)
{
  this.parentComponent=parent;
    this.worker=worker;
    listener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        {
          if ("progress".equals(evt.getPropertyName())) {
            myBar.setValue((Integer) evt.getNewValue());
          }
          if ("note".equals(evt.getPropertyName())) {
            setNote((String) evt.getNewValue());
          }
          if ("state".equals(evt.getPropertyName())) {
            if (evt.getNewValue()==SwingWorker.StateValue.DONE) {
              close();
            }
          }
        }
      }
    };
    
    
    this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    this.setUndecorated(true);
    
    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());
    //panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    //panel.setBorder(BorderFactory.createSoftBevelBorder(BevelBorder.RAISED));
    panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createSoftBevelBorder(BevelBorder.RAISED), title, TitledBorder.CENTER, TitledBorder.BELOW_TOP));
    
    GBC gbc = new GBC().setInsets(5,5,5,5).setAnchor(GBC.CENTER);
    
//    JLabel l = new JLabel(title);
//    panel.add(l, gbc);

    gbc.setFill(GBC.HORIZONTAL);
    if (note != null) {
      this.note=note;
      noteLabel = new JLabel(note);
      panel.add(noteLabel, gbc.nextY());
    } else {
      this.note="";
    }
    myBar = new JProgressBar();
    myBar.setMinimum(0);
    myBar.setMaximum(100);
    myBar.setValue(0);
    panel.add(myBar, gbc.nextY());
   
    if (cancel) {
      panel.add(new JButton(new AbstractAction("Cancel"){
        @Override
        public void actionPerformed(ActionEvent e) {
          worker.cancel(true);
        }
      }), gbc.nextY().setFill(GBC.NONE));
    }
    add(panel);
    this.pack();
    setLocationRelativeTo(parentComponent);
    setResizable(false);
    
    worker.addPropertyChangeListener(listener);
    worker.execute();
    setVisible(true);
  }

  synchronized
  public void close()
  {
      worker.removePropertyChangeListener(listener);
      setVisible(false);
      dispose();
  }
  
  public void setNote(String s)
  {
      if (s == null) {
        s="";
      }
    if (noteLabel!=null) {
      noteLabel.setText(s);
      if (s.length()>note.length()) {
        pack();
        setLocationRelativeTo(parentComponent);
      }
    }
    this.note=note;
  }
  
    
    /////////////////////////////////////////////////////////////////////////////
    ///// TEST
  
    
    public static void main(String[] args)
    {
      SwingUtilities.invokeLater(()->{
        JFrame frame = new JFrame("Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());
        
        JButton start=new JButton("start");
        JButton other=new JButton("other");
        
                
        other.addActionListener(e-> JOptionPane.showMessageDialog(frame, "other triggered"));
        start.addActionListener(e -> {
          try {
            SwingWorker<Void, Void> w = new SwingWorker() {
              @Override
              protected Object doInBackground() throws Exception {
                for (int i = 0; i <= 100; i++) {
                  System.out.printf("doing step %d\n", i);
                  Thread.sleep(1000);
                  setProgress(i);

                  this.firePropertyChange("note", null, String.format("step %d", i));
                }
                return null;
              }
            };
            WorkerProgressDialog.show(frame, w, "worker", "preparing...", true);
          } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
          }
        });
        
        frame.add(start);
        frame.add(other);
        frame.pack();
        frame.setVisible(true);
      });
    }
}
