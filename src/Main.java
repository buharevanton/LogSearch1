
import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater (() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                new LogInt();
            } catch (Exception e) {
                System.out.println("qwerty");
            }
        });
    }
}