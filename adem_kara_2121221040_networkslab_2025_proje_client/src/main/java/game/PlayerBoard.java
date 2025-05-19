package game;

import battleshipclient.Client;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import game.Message;
import java.awt.Point;

import javax.swing.JFrame;

public class PlayerBoard extends Board implements ActionListener {

    /**
     * private instance data
     */
    private Ship ship[];
    private int hp, index;
    private boolean isVertical, shipsPlaced;

    public PlayerBoard() {
        super();

        this.ship = new Ship[5];

        ship[0] = new Ship("Carrier", 5);
        ship[1] = new Ship("Battleship", 4);
        ship[2] = new Ship("Cruiser", 3);
        ship[3] = new Ship("Submarine", 3);
        ship[4] = new Ship("Destroyer", 2);

        this.hp = 17;
        this.index = 0;
        this.isVertical = false;
        this.shipsPlaced = false;
    }

    //Places a ship at a specified row and column. The cell represents either the
    //topmost or leftmost cell of the ship (depending on if it is vertical or horizontal).
    //Also before placing the ship, the method ensures the ship does not go off the board
    //or overlap with another ship.
    public void placeShip(int row, int col) {

        boolean fits = true;
        // cycles through the JButtons that the placed ship will occupy. If one of the JButtons
        // text is " " (one space) instead of "" (nothing), then the JButton/cell is occupied by another ship.
        try {
            for (int i = 0; i < ship[index].getLength(); i++) {
                if (isVertical) {	// if the ship is being placed vertically
                    if (btnGrid[row + i][col].getText().equals(" ")) {
                        fits = false;		// fits is false if one of the cells is occupied
                        break;				// breaks from loop
                    }
                } else {				// if the ship is being placed horizontally
                    if (btnGrid[row][col + i].getText().equals(" ")) {
                        fits = false;		// fits it false if one of the cells if occupied
                        break;				// breaks from loop
                    }
                }
            }
        } // if the ship goes off the board, an ArrayIndexOutOfBoundsException will be triggered,
        // this means the ship does not fit and fits will be false
        catch (ArrayIndexOutOfBoundsException e) {	// work smarter
            fits = false;							// not harder
        }// end of checking if the ship fits

        // placing the ship if it fits (fits == true)
        if (fits) {
            // the ship object is told the position
            ship[index].setPos(row, col, isVertical);

            if (isVertical) {		// if being placed vertically
                for (int i = 0; i < ship[index].getLength(); i++) {
                    btnGrid[row + i][col].setBackground(Color.GREEN);
                    btnGrid[row + i][col].setOpaque(true);
                    btnGrid[row + i][col].setBorderPainted(false);
                    btnGrid[row + i][col].setText(" ");
                    btnGrid[row + i][col].setForeground(Color.GREEN);
                    // ^ the text in the button is set to " " to represent being occupied
                    // and the buttons are colored blue
                }
            } else {					// if being placed horizontally
                for (int i = 0; i < ship[index].getLength(); i++) {
                    btnGrid[row][col + i].setBackground(Color.GREEN);
                    btnGrid[row][col + i].setOpaque(true);
                    btnGrid[row][col + i].setBorderPainted(false);
                    btnGrid[row][col + i].setText(" ");
                    btnGrid[row][col + i].setForeground(Color.GREEN);
                }
            }
            // to cycle through all the ships
            index++;

            if (index == ship.length) {		// when all the ships are placed disable the grid
                // System.out.println("click: " + (char)(row+65) + "" + (col + 1));
                this.shipsPlaced = true;
                // Sunucuya gemilerin konumlarını gönder
                Message shipInfoMsg = new Message(Message.Message_Type.SHIP_INFO);
                shipInfoMsg.content = getShipPositions(); // Gemilerin konumlarını döndüren metot
                Client.Send(shipInfoMsg);
                for (int i = 0; i < btnGrid.length; i++) {
                    for (int j = 0; j < btnGrid.length; j++) {
//						btnGrid[i][j].setEnabled(false);
                        btnGrid[i][j].setText("");

                    }
                }
            }
        }

    }
    
    @Override
public void resetBoard() {
    super.resetBoard(); // Board sınıfındaki temel reset işlemleri
    this.shipsPlaced = false;



    // PlayerBoard'a özel temizlik
    this.ship = new Ship[5];

    ship[0] = new Ship("Carrier", 5);
    ship[1] = new Ship("Battleship", 4);
    ship[2] = new Ship("Cruiser", 3);
    ship[3] = new Ship("Submarine", 3);
    ship[4] = new Ship("Destroyer", 2);

    this.hp = 17; // ya da 17 gibi toplam hücre sayısı
    this.index = 0;
    this.isVertical = false;
    this.shipsPlaced = false;

    // Gemi yerleşimini tekrar aktif hale getir
    for (int i = 0; i < btnGrid.length; i++) {
        for (int j = 0; j < btnGrid.length; j++) {
            btnGrid[i][j].addActionListener(this);
        }
    }
}


    private String getShipPositions() {
        StringBuilder positions = new StringBuilder();
        for (Ship s : ship) {
            positions.append(s.getRow()).append(",")
                    .append(s.getCol()).append(",")
                    .append(s.isVertical()).append(",")
                    .append(s.getLength()).append(";");  
        }
        return positions.toString();
    }

   public boolean checkEnemyShot(int row, int col) {
    System.out.println("Vuruş kontrolü: " + row + "," + col);
    for (Ship ship : ship) {
        if (ship.checkHit(row, col)) {
            System.out.println("VURULDU!");
            hp--;
            placeHitMarker(row, col);

            // HP 0 ise oyun biter
            if (hp == 0) {
                System.out.println("TÜM GEMİLER VURULDU! OYUN BİTTİ!");
                Message gameOver = new Message(Message.Message_Type.Bitis);
                gameOver.sender = Game.ThisGame.txt_name.getText();
                Client.Send(gameOver);
            }

            return true;
        }
    }
    System.out.println("ISKA!");
    placeMissMarker(row, col);
    return false;
}


    //Checks if the hp of the PlayerBoard is 0. The hp is an int that represents the sum of
    //the health (num of cells) of all the ships. When it reaches 0, every cell of every
    //ship has been hit. Returns true if hp is 0, false if not.
    public boolean hasLost() {
        if (hp == 0) {
            return true;
        } else {
            return false;
        }
    }

    //Sets the isVertical variable to true or false, so when the next ship is placed,
    //it is placed in the correct orientation.
    public void setIsVertical(boolean in) {
        this.isVertical = in;
    }

    public boolean getIsVertical() {
        return isVertical;
    }

    //Returns true if all the ships have been placed
    public boolean getAllShipsPlaced() {
        return shipsPlaced;
    }

    public void setHP(int in) {
        hp = in;
    }

    //Overrides the actionPerformed method from the superclass Board. (EXAMPLE OF DYNAMIC 
    //POLYMORPHISM). Instead of setting rClick and cClick corresponding to button pressed,
    //instead this actionPerformed places a Ship at the clicked button.
    public void actionPerformed(ActionEvent e) {
        for (int row = 0; row < btnGrid.length; row++) {
            for (int col = 0; col < btnGrid.length; col++) {
                if (btnGrid[row][col] == e.getSource()) {
                    placeShip(row, col);
                }
            }
        }
    }
//
//    public static void main(String[] args) {
//        // TODO Auto-generated method stub
//
//        PlayerBoard b = new PlayerBoard();
//        b.drawBoard();
//
//        //b.setIsVertical(true);
//        JFrame f1 = new JFrame();
//        f1.setSize(500, 500);
//        f1.setLayout(null);
//        f1.add(b);
//        f1.setVisible(true);
//
//    }

}
