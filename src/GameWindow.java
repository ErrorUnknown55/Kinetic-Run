import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.*;

public class GameWindow extends JFrame {
    
    public GameWindow() {
        //Set Game Title
        setTitle("Kinetic Run"); 
        
        //Set Panel Size
        setSize(700,650);

        //Creates a custom background for the mainPanel
        JPanel mainPanel =  new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                Color color1 = new Color(20,30,48);
                Color color2 = new Color(36, 59, 85);
                g2d.setPaint(new GradientPaint(0,0, color1, getWidth(), getHeight(), color2));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        mainPanel.setLayout(new BorderLayout());
        add(mainPanel);

        JLabel titleJLabel =  new JLabel("KINETIC RUN", SwingConstants.CENTER);
        titleJLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleJLabel.setForeground(Color.WHITE);
        titleJLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 100, 0));
        mainPanel.add(titleJLabel, BorderLayout.NORTH);

        setLocationRelativeTo(null);//Center the window
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }  
}
