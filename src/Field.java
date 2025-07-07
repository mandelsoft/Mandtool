
import com.mandelsoft.swing.GBC;
import com.mandelsoft.swing.GBCPanel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

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


public class Field {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
          
            JFrame frame = new JFrame("Text Fields");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JPanel content = new GBCPanel();

            GBC g = new GBC().setInsets(20).setFill(GBC.HORIZONTAL).setWeight(1.0,0);
            for (int i = 0; i < 10; i++) {
                JTextField f = new JTextField();
                f.setColumns(10);
                content.add(new JLabel(String.format("Field %d", i)),  g.setX(0).setWeight(0, 0));
                content.add(f, g.setX(1).setWeight(1,0));
                g.nextY();
            }

            frame.setContentPane(content);
            frame.pack();
            frame.setVisible(true);
        });
    }
}
