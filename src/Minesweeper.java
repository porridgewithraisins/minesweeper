import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;

public class Minesweeper extends JFrame implements ActionListener, MouseListener {

    JFrame frame = new JFrame();
    JButton reset = new JButton("Reset");
    JButton giveUp = new JButton("Give Up");

    JPanel ButtonPanel = new JPanel();
    Container grid = new Container();
    int[][] counts;
    JButton[][] buttons;
    int size, windowWidth, windowHeight;
    final int MINE = 10;
    final double MINE_DENSITY = 1.5;
    final double SCALE_FACTOR = 4.5;
    final int[][] neighborOffsets = { { -1, -1 }, { -1, 0 }, { -1, 1 }, { 0, -1 }, { 0, 1 }, { 1, -1 }, { 1, 0 },
            { 1, 1 } };

    ImageIcon bombIcon;

    /**
     * @param size determines the size of the board
     */
    public Minesweeper(int size) {
        super("Minesweeper");

        initResources();

        this.size = size;
        counts = new int[size][size];
        buttons = new JButton[size][size];

        windowWidth = windowHeight = (int)(size*SCALE_FACTOR);
        frame.setSize(900, 900);
        frame.setLayout(new BorderLayout());
        frame.add(ButtonPanel, BorderLayout.SOUTH);
        reset.addActionListener(this);
        giveUp.addActionListener(this);

        grid.setLayout(new GridLayout(size, size));
        for (int a = 0; a < buttons.length; a++) {
            for (int b = 0; b < buttons[0].length; b++) {
                buttons[a][b] = new JButton();
                buttons[a][b].addActionListener(this);
                buttons[a][b].setBackground(Color.white);
                buttons[a][b].setForeground(Color.black);
                grid.add(buttons[a][b]);
            }
        }

        ButtonPanel.add(reset);
        ButtonPanel.add(giveUp);

        frame.add(grid, BorderLayout.CENTER);
        createMines(size);

        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setVisible(true);

    }

    private void initResources() {
        bombIcon = new ImageIcon("images/bomb.jpg");
    }
    
    private Icon resizeIcon(ImageIcon icon, int resizedWidth, int resizedHeight) {
        Image img = icon.getImage();
        Image resizedImage = img.getScaledInstance(resizedWidth, resizedHeight, java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }

    /**
     * Function to check whether user has lost the game ( i.e clicked a mine).
     * 
     * @param sourceOfLoss indicated whether the function has been called when user
     *                     clicks a mine( m=1) or when they clicks the give up
     *                     button.(m = any other integer). Shows a
     *                     dialog box which tells the user that they have lost the
     *                     game.
     */
    public void takeTheL(int sourceOfLoss) {

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (buttons[x][y].isEnabled()) {
                    if (counts[x][y] != MINE) {
                        buttons[x][y].setText("" + counts[x][y]);
                        buttons[x][y].setEnabled(false);
                    }

                    else {
                        buttons[x][y].setEnabled(false);
                        int w = buttons[x][y].getWidth(), h = buttons[x][y].getHeight();
                        buttons[x][y].setIcon(resizeIcon(bombIcon, w, h));
                    }
                }
            }
        }
        JOptionPane.showMessageDialog(null, sourceOfLoss == 1 ? "You clicked a mine!" : "You Gave Up", "Game Over",
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Function to check whether user has won or not It performs this by checking
     * whether a cell that is NOT a mine remains to be clicked by the user. (Works
     * because, when a user clicks a mine,a line is written to disable corresponding
     * button). Function prints a pop-up message congratulating user on victory.
     */

    public void takeTheW() {
        boolean won = true;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (counts[i][j] != MINE && buttons[i][j].isEnabled()) {
                    won = false;
                }
            }
        }
        if (won) {
            JOptionPane.showMessageDialog(null, "You have won!", "Congratulations!", JOptionPane.INFORMATION_MESSAGE);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    if (buttons[x][y].isEnabled()){
                        int w = buttons[x][y].getWidth(), h = buttons[x][y].getHeight();
                        buttons[x][y].setIcon(resizeIcon(bombIcon, w, h));
                    }
                }
            }
        }
    }

    /**
     * Method which listens, and acts upon the user's actions on the buttons.
     * Function retrieves the event source(i.e the button that was clicked). Then it
     * calls appropriate functions.
     * 
     * @param ae variable name of action event
     */

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == reset) // resets grid
        {
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    buttons[x][y].setEnabled(true);
                    buttons[x][y].setBackground(Color.white);
                    buttons[x][y].setText("");
                    buttons[x][y].setIcon(new ImageIcon());
                }
            }
            createMines(size); // triggers a new game.
        }

        else if (ae.getSource() == giveUp) // user has given up. trigger takeTheL( m!= 1).
        {
            takeTheL(0);
        }

        else // click was on a cell
        {
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    if (ae.getSource() == buttons[x][y]) {
                        switch (counts[x][y]) {
                            case MINE:
                                takeTheL(1);
                                break;
                            case 0:
                                buttons[x][y].setText(counts[x][y] + "");
                                buttons[x][y].setEnabled(false);
                                buttons[x][y].setBackground(Color.green);
                                ArrayList<Integer> clear = new ArrayList<>(); // If count = 0, domino effect is called
                                clear.add(x * 100 + y);
                                dominoEffect(clear);
                                takeTheW(); // checks win every move
                                break;
                            default:
                                buttons[x][y].setText("" + counts[x][y]);
                                buttons[x][y].setEnabled(false);
                                buttons[x][y].setBackground(Color.green);
                                takeTheW(); // its a number > 0 and not a mine, so just check for win
                                break;
                        }
                    }
                }
            }
        }

    }

    /**
     * Function creates mines at random positions.
     * 
     * @param s the size of the board(row or column count)
     */

    public void createMines(int s) {
        ArrayList<Integer> list = new ArrayList<>();
        for (int x = 0; x < s; x++) {
            for (int y = 0; y < s; y++) {
                list.add(x * 100 + y); // x & y shall be individually retrieved by dividing by 100 and then modulo 100
                                       // respectively.
            }
        }
        counts = new int[s][s]; // resetting back-end array

        for (int a = 0; a < (int) (s * MINE_DENSITY); a++) {
            int choice = (int) (Math.random() * list.size());
            counts[list.get(choice) / 100][list.get(choice) % 100] = MINE; // Using corollary of before-last comment to
                                                                           // set mines as well.
            list.remove(choice); // We don't want two mines in the same pos., so remove that pos. from list.
        }
        /*
         * Following segment initializes 'neighbor counts' for each cell. That is, the
         * number of mines that are present in the eight surrounding cells. IF the cell
         * isn't a mine. Note : It is done in the back-end array as that contains the
         * numbers (MINE or 1 or 2 or 3 or 4 or 5 or 6 or 7 or 8)
         */
        for (int x = 0; x < s; x++) {
            for (int y = 0; y < s; y++) {
                if (counts[x][y] != MINE) {
                    int neighbor = 0;
                    for (int[] offsets : neighborOffsets) {
                        int nearby_x = x + offsets[0], nearby_y = y + offsets[1];
                        if (withinBounds(nearby_x, nearby_y) && counts[nearby_x][nearby_y] == MINE) {
                            neighbor++;
                        }
                    }
                    counts[x][y] = neighbor;
                }
            }
        }
    }

    /**
     * This function, called the domino effect, is an implementation of the idea
     * that, when a cell with no surrounding mines is clicked, there's no point in
     * user clicking all eight surrounding cells, and those eight surrounding cells.
     * Therefore, all surrounding cells' counts will be displayed in corresponding
     * cells. The above is done recursively.
     * 
     * @param toClear the ArrayList which is passed to the function with positions
     *                in array that are zero, and are subsequently clicked.
     */

    public void dominoEffect(ArrayList<Integer> toClear) {
        if (toClear.isEmpty())
            return; // base case

        int x = toClear.get(0) / 100; // getting x pos.
        int y = toClear.get(0) % 100; // getting y pos.
        toClear.remove(0); // remove that element from queue to prevent infinite recursion.

        if (counts[x][y] == 0) {
            for (int[] offsets : neighborOffsets) {
                int nearby_x = x + offsets[0], nearby_y = y + offsets[1];
                if (withinBounds(nearby_x, nearby_y) && buttons[nearby_x][nearby_y].isEnabled()) {
                    buttons[nearby_x][nearby_y].setText(counts[nearby_x][nearby_y] + "");
                    buttons[nearby_x][nearby_y].setEnabled(false);
                    buttons[nearby_x][nearby_y].setBackground(Color.green);
                    if (counts[nearby_x][nearby_y] == 0) {
                        toClear.add((nearby_x) * 100 + (nearby_y));
                    }
                }
            }
        }
        dominoEffect(toClear); // recursive call with list containing surrounding cells, for further
                               // check-and-clear of THEIR surr. cells.
    }

    public boolean withinBounds(int x, int y) {
        return (x < size) && (y < size) && (x > 0) && (y > 0);
    }

    @Override
    public void mouseClicked(MouseEvent me) {
        if (SwingUtilities.isRightMouseButton(me)) {
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    if (me.getComponent().equals(buttons[x][y]) && buttons[x][y].isEnabled())
                        buttons[x][y].setBackground(Color.yellow);
                }
            }
        }
    }

    // if i don't put these some weird error happens
    @Override
    public void mousePressed(MouseEvent me) {
        // Do nothing
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        // Do nothing
    }

    @Override
    public void mouseEntered(MouseEvent me) {
        // Do nothing
    }

    @Override
    public void mouseExited(MouseEvent me) {
        // Do nothing
    }

}