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

package com.mandelsoft.swing.worker;

import java.awt.Component;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

/**
 *
 * @author Uwe Krueger
 */
public class WorkerProgressMonitor extends ProgressMonitor
                                   implements PropertyChangeListener {

  ///////////////////////////////////////////////////////////////////////
  // Note
  ///////////////////////////////////////////////////////////////////////

  public interface NoteFormatter {
    String format(int progress);
  }

  public static class DefaultNoteFormatter implements NoteFormatter {

    public String format(int progress)
    {
      return String.format("Completed %d%%...", progress);
    }
  }

  ///////////////////////////////////////////////////////////////////////
  // Monitor
  ///////////////////////////////////////////////////////////////////////

  private NoteFormatter formatter;
  private SwingWorker worker;

  public WorkerProgressMonitor(Component parentComponent, Object message,
                               SwingWorker worker)
  {
    this(parentComponent, message, new DefaultNoteFormatter(), worker);
  }

  public WorkerProgressMonitor(Component parentComponent, Object message,
                               NoteFormatter formatter, SwingWorker worker)
  {
    super(parentComponent, message, "Preparing...", 0, 100);
    this.formatter=formatter;
    this.worker=worker;
    worker.addPropertyChangeListener(this);
    setProgress(0);
    worker.execute();
  }
   
  public void propertyChange(PropertyChangeEvent evt)
  {
    if ("progress".equals(evt.getPropertyName())) {
      setProgress((Integer)evt.getNewValue());
    }
  }

  @Override
  public void setProgress(int progress)
  {
    String message=formatter.format(progress);
    if (!message.endsWith("\n")) message+="\n";
    setNote(message);
    super.setProgress(progress);
    if (isCanceled()||worker.isDone()) {
      Toolkit.getDefaultToolkit().beep();
      if (isCanceled()) {
        worker.cancel(true);
      }
      close();
    }
  }
}
