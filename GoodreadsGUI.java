import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class GoodreadsGUI extends JFrame {
    private JTextField inputField1;
    private JTextField inputField2;
    private JTextField inputField3;
    private JTextField inputField4;
    private JTextArea outputArea;
    private JButton submitButton;

    public GoodreadsGUI() {
        // Set up the JFrame
        setTitle("Goodreads GUI");
        setSize(1600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        // Create components
        inputField1 = new JTextField(10);
        inputField2 = new JTextField(10);
        inputField3 = new JTextField(10);
        inputField4 = new JTextField(10);
        outputArea = new JTextArea(20, 40);
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        submitButton = new JButton("Submit");

        JPanel mainPanel = new JPanel(new GridLayout(1, 2));

        JPanel inputPanel = new JPanel(new BorderLayout());
        JPanel inputFieldsPanel = new JPanel(new GridLayout(0, 1));

        // questions to ask the user
        String q1 = "What genre do you enjoy reading? " +
                "\n (input a genre like fantasy or historical fiction or say 'surprise me')";

        String q2 = "Are you looking for most read books or recently released books?" +
                "\n- Most Read" +
                "\n- New Release";

        String q3 = "What length of book are you looking for? " +
                "(Enter the choice only not page numbers)" +
                "\n- Novella (less than 150 pages)" +
                "\n- Short (150 - 300 pages)" +
                "\n- Mid-length (300-500 pages)" +
                "\n- Long (500+ pages)" +
                "\n- Surprise Me (any length)";

        String q4 = "If the genre belongs to the Goodreads Choice Awards, are you interested in seeing " +
                "\n3 more recommendations of the top books from this year?" +
                "\n - yes" +
                "\n - no";

        inputFieldsPanel.add(createInputFieldPanel("<html>" + q1.replace("\n", "<br>") + "</html>", inputField1));
        inputFieldsPanel.add(createInputFieldPanel("<html>" + q2.replace("\n", "<br>") + "</html>", inputField2));
        inputFieldsPanel.add(createInputFieldPanel("<html>" + q3.replace("\n", "<br>") + "</html>", inputField3));
        inputFieldsPanel.add(createInputFieldPanel("<html>" + q4.replace("\n", "<br>") + "</html>", inputField4));

        JScrollPane inputScrollPane = new JScrollPane(inputFieldsPanel);
        inputPanel.add(inputScrollPane, BorderLayout.CENTER);
        inputPanel.add(submitButton, BorderLayout.SOUTH);
        mainPanel.add(inputPanel);

        JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.add(new JScrollPane(outputArea), BorderLayout.CENTER);
        mainPanel.add(outputPanel);

        add(mainPanel);

        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                if (inputField1.getText().isEmpty() || inputField2.getText().isEmpty()
                        || inputField3.getText().isEmpty() || inputField4.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(GoodreadsGUI.this,
                            "Please fill in all input fields", "Error",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    outputArea.setText("Loading... (This may take up to a few minutes.)");

                    // get inputs
                    String input1 = inputField1.getText();
                    String input2 = inputField2.getText();
                    String input3 = inputField3.getText();
                    String input4 = inputField4.getText();


                    new Thread(() -> {
                        // process inputs
                        String output = null;
                        try {
                            output = processInputs(input1, input2, input3, input4);

                            String finalOutput = output;
                            SwingUtilities.invokeLater(() -> outputArea.setText(finalOutput));
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }


                    }).start();
                }

                // reset input fields
                inputField1.setText("");
                inputField2.setText("");
                inputField3.setText("");
                inputField4.setText("");
            }
        });
    }

    private String processInputs(String input1, String input2, String input3, String input4) throws IOException {

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        GoodreadsParser parser = new GoodreadsParser();



        String res1 = parser.findRecommendations(input1, input2, input3);
        String inputs = "Here are recommendations for: " + parser.currGenre + ", " + input2 + ", " + input3;
        String res = res1;
        if (input4.equalsIgnoreCase("yes")) {
            String res2 = parser.findAwardBooks(parser.currGenre);
            res = res + "\n\nHere are recommendations from the Goodreads Choice Awards " +
                    "in the " + parser.currGenre + " category:\n" + res2;
        }


        return inputs + "\n" + res;
    }


    private JPanel createInputFieldPanel(String label, JTextField textField) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel(label));
        panel.add(textField);
        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                GoodreadsGUI gui = new GoodreadsGUI();
                gui.setVisible(true);
            }
        });
    }
}