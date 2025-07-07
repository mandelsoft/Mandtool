
import com.mandelsoft.swing.BufferedComponent;
import com.mandelsoft.swing.BufferedComponent.VisibleRect;
import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayer;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.plaf.LayerUI;

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


public class Layer {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
          BufferedComponent.debug=true;
          
            JFrame frame = new JFrame("JLayer Scroll Drag Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 400);

            BufferedComponent content = new BufferedComponent(1000,1000);
            content.setBackground(Color.LIGHT_GRAY);

            JPopupMenu m = new JPopupMenu();
            for (int i = 0; i < 10; i++) {
                JTextField f = new JTextField();
                f.setColumns(10);
                content.add(f);
                JButton button = new JButton("Button " + i);
                button.setBounds(50 + i * 100, 50 + i * 40, 100, 30);
               // content.add(button);
                m.add(new JMenuItem(String.format("item %d",i)));
            }

            VisibleRect r = content.createRect("test", "label", 100, 100, 100, 100);
            r.setVisible(true);
           
            
            content.setComponentPopupMenu(m);
            
            JScrollPane scrollPane = new JScrollPane();
            JLayer<JComponent> layeredContent = new JLayer<>(content, new DragScrollLayerUI(scrollPane));
            layeredContent.setOpaque(true);
            scrollPane.setViewportView(layeredContent);
            //scrollPane.setViewportView(content);

            frame.setContentPane(scrollPane);
            frame.setVisible(true);
        });
    }

    static class DragScrollLayerUI extends LayerUI<JComponent> {
        private final JScrollPane scrollPane;
        private Point lastPoint;

        DragScrollLayerUI(JScrollPane scrollPane) {
            this.scrollPane = scrollPane;
        }

        @Override
        public void installUI(JComponent c) {
            super.installUI(c);
            ((JLayer<?>) c).setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
        }

        @Override
        public void uninstallUI(JComponent c) {
            ((JLayer<?>) c).setLayerEventMask(0);
            super.uninstallUI(c);
        }

        @Override
        protected void processMouseEvent(MouseEvent e, JLayer<? extends JComponent> l) {
            if (e.getID() == MouseEvent.MOUSE_PRESSED && isModifierPressed(e)) {
                lastPoint = e.getPoint();
                System.out.println("Mouse press detected");
                e.consume();
            }
        }

        @Override
        protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JComponent> l) {
            if (e.getID() == MouseEvent.MOUSE_DRAGGED && lastPoint != null && isModifierPressed(e)) {
                Point current = e.getPoint();
                Point viewPos = scrollPane.getViewport().getViewPosition();
                int dx = lastPoint.x - current.x;
                int dy = lastPoint.y - current.y;
                viewPos.translate(dx, dy);
                scrollPane.getViewport().setViewPosition(viewPos);
                lastPoint = current;
                e.consume();
            }
        }

        private boolean isModifierPressed(MouseEvent e) {
            return e.isControlDown() || e.isAltDown(); // Customize as needed
        }
    }
}
