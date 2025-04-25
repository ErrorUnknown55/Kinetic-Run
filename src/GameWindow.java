import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import javax.swing.*;

public class GameWindow extends JFrame {

    private GamePanel gamePanel;

    int scrWidth = 700, scrHeight= 650;
    
    public GameWindow() {
        //Set Game Title
        setTitle("Kinetic Run"); 
        
        //Set Panel Size
        setSize(700,650);

        gamePanel = new GamePanel(700,650);

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

        //Game Title
        JLabel titleJLabel =  new JLabel("KINETIC RUN", SwingConstants.CENTER);
        titleJLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleJLabel.setForeground(Color.WHITE);
        titleJLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 100, 0));
        mainPanel.add(titleJLabel, BorderLayout.NORTH);

        //Button Panel
        JPanel buttonPanel =  new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new GridLayout(3, 1, 0, 30));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 250, 250, 250));

        //Play button
        JButton playBtn = createButtonStyle("PLAY");
        playBtn.addActionListener((actionEvent) -> {
            //Removes all components from main panel
            mainPanel.removeAll();
            //Add the gamePanel
            mainPanel.add(gamePanel, BorderLayout.CENTER);
            gamePanel.setVisible(true);
            gamePanel.startGame();
            //Refresh the windown
            revalidate();
            repaint();
        });
        
        //Setting button 
        //JButton settingBtn = createButtonStyle("SETTING");
        
        //Exit
        JButton exitBtn = createButtonStyle("Exit");
        exitBtn.addActionListener((actionEvent) -> {
            System.exit(0);
        });


        buttonPanel.add(playBtn);
        buttonPanel.add(settingBtn);
        buttonPanel.add(exitBtn);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        setLocationRelativeTo(null);//Center the window
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    
    private JButton createButtonStyle(String text) {

        JButton button = new  JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 25));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(70, 130, 180));//Steel Blue
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        //Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(100, 150, 200));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(70, 130, 180));
            }
        });
        return button;
    }

}
