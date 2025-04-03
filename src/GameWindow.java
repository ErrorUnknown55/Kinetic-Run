import javax.swing.*;

public class GameWindow extends JFrame {
    
    public GameWindow() {
        //Set Game Title
        setTitle("Kinetic Run"); 
        
        //Set Panel Size
        setSize(700,650);
        
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }  
}
