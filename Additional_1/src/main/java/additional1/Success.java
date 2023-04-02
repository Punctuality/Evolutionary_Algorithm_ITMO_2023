package additional1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.*;
import java.util.Random;

class Success extends JFrame{

    public Success(){
        JPanel panel=new JPanel();
        getContentPane().add(panel);
        setSize(450,450);

        JButton button =new JButton("press");
        button.addActionListener(e -> this.repaint());
        panel.add(button);
    }

    public void paint(Graphics g) {
        super.paint(g);  // fixes the immediate problem.
        Graphics2D g2 = (Graphics2D) g;
        Random rng = new Random();
        Line2D lin = new Line2D.Float(
                rng.nextInt(100) + 100,
                rng.nextInt(100) + 100,
                rng.nextInt(250) + 100,
                rng.nextInt(250) + 100);
        g2.setColor(Color.getHSBColor(rng.nextFloat(), 1, 1));
        g2.draw(lin);
        g2.setBackground(Color.WHITE);
    }

    public static void main(String []args){
        Success s=new Success();
        s.setVisible(true);
    }
}