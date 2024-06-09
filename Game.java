import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import javax.swing.Timer;
class Game {
    public static class Controller {
        final JFrame window;
        Model model;
        View view;
         // Constructor
        public Controller(Model model) {
            this.window = new JFrame("Match Mystery"); // Create the window
            this.window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);  
            this.window.setResizable(false);
            this.reset(model);
        }
        // Reset the game
        public void reset(Model model) {
            this.model = model;  
            this.view = new View(model);
            this.window.setVisible(false);
            this.window.setContentPane(view);
            this.window.pack();
            this.window.setLocationRelativeTo(null);
            for (JButton button : this.model.getButtons()) {
                button.addActionListener(new ButtonActionListener(this));
            }
            Utilities.timer(200, (ignored) -> this.window.setVisible(true)); 
        }
        public JFrame getWindow() {
            return this.window;
        }
        public Model getModel() {
            return this.model;
        }
        public View getView() {
            return this.view;
        }
    }
    public static class Model {
        // Constants for the game
        static final String[] AVAILABLE_IMAGES = new String[]{"0.png", "1.png", "2.png", "3.png", "4.png", "5.png", "6.png", "7.png", "8.png"};
        static final Integer MAX_REGISTERED_SCORES = 10;
        final ArrayList<Float> scores;
        final ArrayList<JButton> buttons;
        final int columns; 
        int tries; 
        boolean gameStarted; 
        public Model(int columns) {
            this.columns = columns; 
            this.buttons = new ArrayList<>(); 
            this.scores = new ArrayList<>(); 
            this.tries =  10; 
            this.gameStarted = false; 
            int numberOfImage = columns * columns; // Number of images
            Vector<Integer> v = new Vector<>(); // Vector to store the images
            for (int i = 0; i < numberOfImage - numberOfImage % 2; i++) { // Add the images twice
                v.add(i % (numberOfImage / 2));
            }
            if (numberOfImage % 2 != 0) v.add(AVAILABLE_IMAGES.length - 1); 
            for (int i = 0; i < numberOfImage; i++) { 
                int rand = (int) (Math.random() * v.size()); // Randomly select an image
                String reference = AVAILABLE_IMAGES[v.elementAt(rand)];
                this.buttons.add(new MemoryButton(reference)); 
                v.removeElementAt(rand);
            }
        }
        public int getColumns() {
            return columns;
        }
        public ArrayList<JButton> getButtons() {
            return buttons;
        }
    
        public int getTries() {
            return tries;
        }
       
        public void decrementTries() {
            this.tries--;
        }
       
        public boolean isGameStarted() {
            return this.gameStarted;
        }
      
        public void startGame() {
            this.gameStarted = true;
        }
    }
    // class to handle the UI of the game
    public static class View extends JPanel {
        final JLabel tries;
        public View(Model model) {
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.tries = new JLabel("", SwingConstants.CENTER);
            this.tries.setFont(new Font("MV Boli", Font.BOLD, 30));
            this.tries.setForeground(Color.WHITE);
            
            JPanel imagePanel = new JPanel();
            int columns = model.getColumns();
            imagePanel.setLayout(new GridLayout(columns, columns));
            for (JButton button : model.getButtons()) {
                imagePanel.add(button);
            }
            this.setTries(model.getTries());
            JPanel triesPanel = new JPanel();
            triesPanel.add(this.tries);
            triesPanel.setAlignmentX(CENTER_ALIGNMENT);
            triesPanel.setBackground(new Color(0X8946A6));
            this.add(triesPanel);
            this.add(imagePanel);
        }
        public void setTries(int triesLeft) {
            this.tries.setText("Tries left : " + triesLeft);
        }
    }
    // class to handle the button clicks
    public static class ReferencedIcon extends ImageIcon {
        final String reference;
        public ReferencedIcon(Image image, String reference) { 
            super(image);
            this.reference = reference;
        }
        public String getReference() {
            return reference;
        }
    }
    // class to handle the button on the images
    public static class MemoryButton extends JButton {
        static final String IMAGE_PATH = "";
        static final Image NO_IMAGE = Utilities.loadImage("no_image.png");
        public MemoryButton(String reference) {
            Image image = Utilities.loadImage(IMAGE_PATH + reference);
            Dimension dimension = new Dimension(120, 120);
            this.setPreferredSize(dimension);
            this.setIcon(new ImageIcon(NO_IMAGE));
            this.setDisabledIcon(new ReferencedIcon(image, reference));
        }
    }
    public static class Dialogs {
        public static void showLoseDialog(JFrame window) {
            UIManager.put("OptionPane.background", new Color(0XEA99D5));
            UIManager.put("Panel.background", new Color(0XEA99D5));
            JOptionPane.showMessageDialog(window, "You lost, try again !", "You lost !", JOptionPane.INFORMATION_MESSAGE);
        }
        public static void showWinDialog(JFrame window, Model model) {
            String message = String.format("Congrats you won!!");
            UIManager.put("OptionPane.background", new Color(0XEA99D5));
            UIManager.put("Panel.background", new Color(0XEA99D5));
            JOptionPane.showMessageDialog(window.getContentPane(), message, "You Won !", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    // class to handle the button clicks
    public static class ButtonActionListener implements ActionListener {
        final Controller controller;
        final Model model;
        final View view;
        final JFrame window;
        static int disabledButtonCount = 0;
        static JButton lastDisabledButton = null;
        static final Image TRAP_IMAGE = Utilities.loadImage("no_image.png");
        final ReferencedIcon trap;
        public ButtonActionListener(Controller controller) {
            this.controller = controller;
            this.model = controller.getModel();
            this.view = controller.getView();
            this.window = controller.getWindow();
            this.trap = new ReferencedIcon(TRAP_IMAGE, "no_image.png");
        }
        // Method to handle the button clicks and check if two images are same
        @Override
        public void actionPerformed(ActionEvent e) {
            JButton button = (JButton) e.getSource();
            button.setEnabled(false);
            ReferencedIcon thisIcon = (ReferencedIcon) button.getDisabledIcon();
            disabledButtonCount++;
            if (!model.isGameStarted()) { 
                model.startGame(); 
            }
            if (disabledButtonCount == 2) { 
                ReferencedIcon thatIcon = (ReferencedIcon) lastDisabledButton.getDisabledIcon();
                boolean isPair = thisIcon.getReference().equals(thatIcon.getReference()); 
                if (!isPair) { // If the two images are not the same
                    model.decrementTries(); 
                    view.setTries(model.getTries());
                    JButton lastButton = lastDisabledButton; 
                    Utilities.timer(500, ((ignored) -> { // Wait 500ms before re-enabling the buttons
                        button.setEnabled(true); 
                        lastButton.setEnabled(true);
                    }));
                }
                disabledButtonCount = 0; 
            }
            ArrayList<JButton> enabledButtons = (ArrayList<JButton>) model.getButtons().stream().filter(Component::isEnabled).collect(Collectors.toList());
            if (enabledButtons.size() == 0) { 
                controller.reset(new Model(controller.getModel().getColumns())); 
                Dialogs.showWinDialog(window, model); 
            }
            lastDisabledButton = button;    
            if (model.getTries() == 0) { 
                controller.reset(new Model(controller.getModel().getColumns())); // Reset the game
                Dialogs.showLoseDialog(window); 
                Utilities.timer(1000, (ignored) -> model.getButtons().forEach(btn -> btn.setEnabled(false))); // Wait 1s before disabling all the buttons
            }
        }
    }
    public static class Utilities {
        static final ClassLoader cl = Utilities.class.getClassLoader();
        // Method to create a timer
        public static void timer(int delay, ActionListener listener) {
            Timer t = new Timer(delay, listener);
            t.setRepeats(false);
            t.start();
        }
        // Method to load an image
        public static Image loadImage(String s) {
            Image image = null;
            try {
                InputStream resourceStream = cl.getResourceAsStream(s);
                if (resourceStream != null) {
                    ImageInputStream imageStream = ImageIO.createImageInputStream(resourceStream);
                    image = ImageIO.read(imageStream);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return image;
        }
    }
}
//  Main class to run the game
class Main {
    static final int DEFAULT_SIZE = 4;
    public static void main(String[] args) {
        Locale.setDefault(Locale.ENGLISH);
        SwingUtilities.invokeLater(() -> new Game.Controller(new Game.Model(DEFAULT_SIZE)));
    }
}
