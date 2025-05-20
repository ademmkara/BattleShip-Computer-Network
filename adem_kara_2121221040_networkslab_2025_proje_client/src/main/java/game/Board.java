package game;

import battleshipclient.Client;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

public class Board extends JPanel implements ActionListener {

    public JButton btnGrid[][];
    private int rClick, cClick;
    private Border dborder, cborder;
    private JLabel markers[];


    public Board() {

        super();


        this.btnGrid = new JButton[10][10];
        rClick = 0;
        cClick = 0;
        dborder = new LineBorder(Color.GREEN, 1);
        cborder = new LineBorder(Color.GREEN, 4);
        markers = new JLabel[21];

        for (int i = 0; i < btnGrid.length; i++) {
            for (int j = 0; j < btnGrid.length; j++) {

                btnGrid[i][j] = new JButton();
                btnGrid[i][j].addActionListener(this);
                btnGrid[i][j].setText("");

                btnGrid[i][j].setBackground(Color.BLACK);
                btnGrid[i][j].setBorder(dborder);
            }
        }

        for (int i = 0; i < markers.length; i++) {
            markers[i] = new JLabel("");
            markers[i].setForeground(Color.GREEN);
            markers[i].setFont(new Font("Monospaced", Font.BOLD, 11));
        }
    }

    public void drawBoard() {

        setSize(400, 400);
        setLayout(new GridLayout(11, 11));
        setBounds(0, 0, 200, 200);
        setBackground(Color.BLACK); // panelin arka planı
        setOpaque(true);            // arka plan rengi düzgün görünsün

        int index = 1;

        add(new JLabel(""));
        for (int i = 1; i < 11; i++) {
            markers[index].setText(Integer.toString(i));
            add(markers[index]);
            index++;
        }

        for (int i = 0; i < btnGrid.length; i++) {
            for (int j = 0; j < btnGrid.length; j++) {
                if (j == 0) {
                    markers[index].setText(Character.toString((char) (65 + i)));
                    add(markers[index]);
                    index++;
                }
                add(btnGrid[i][j]);
            }
        }
        setVisible(true);

    }
    
    

    public void placeHitMarker(int row, int col) {
        btnGrid[row][col].setBackground(Color.YELLOW);
        btnGrid[row][col].setForeground(Color.BLACK);
        btnGrid[row][col].setOpaque(true);
        btnGrid[row][col].setBorderPainted(false);
        btnGrid[row][col].setText("X");
        btnGrid[row][col].setEnabled(true);

    }

    public void placeMissMarker(int row, int col) {
        btnGrid[row][col].setBackground(Color.GRAY);
        btnGrid[row][col].setForeground(Color.red);
        btnGrid[row][col].setOpaque(true);
        btnGrid[row][col].setBorderPainted(false);
        btnGrid[row][col].setText("O");
        btnGrid[row][col].setEnabled(false);
    }
    public void resetBoard() {

    if (dborder == null) {
        dborder = new LineBorder(Color.GREEN, 1);
    }

    for (int i = 0; i < btnGrid.length; i++) {
        for (int j = 0; j < btnGrid[i].length; j++) {
            btnGrid[i][j].setBackground(Color.BLACK);
            btnGrid[i][j].setText("");
            btnGrid[i][j].setEnabled(true);
            btnGrid[i][j].setOpaque(true);

          
            btnGrid[i][j].setBorder(dborder);
        }
    }

    rClick = -1;
    cClick = -1;
    
}


    

    public int getRclick() {
        return rClick;
    }


    public int getCclick() {
        return cClick;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        for (int row = 0; row < btnGrid.length; row++) {
            for (int col = 0; col < btnGrid.length; col++) {
                btnGrid[row][col].setBorder(dborder);
            }
        }

        for (int row = 0; row < btnGrid.length; row++) {
            for (int col = 0; col < btnGrid.length; col++) {
                if (btnGrid[row][col] == e.getSource()) {
                    System.out.println("click: " + (char) (row + 65) + "" + (col + 1));
                    rClick = row;
                    cClick = col;
                    btnGrid[row][col].setBorder(cborder);

                }
            }
        }
    }

}


