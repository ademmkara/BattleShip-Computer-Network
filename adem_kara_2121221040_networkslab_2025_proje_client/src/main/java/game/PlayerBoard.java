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


    public void placeShip(int row, int col) {

        boolean fits = true;

        try {
            for (int i = 0; i < ship[index].getLength(); i++) {
                if (isVertical) {	
                    if (btnGrid[row + i][col].getText().equals(" ")) {
                        fits = false;		
                        break;				
                    }
                } else {				
                    if (btnGrid[row][col + i].getText().equals(" ")) {
                        fits = false;		
                        break;				
                    }
                }
            }
        } 

        catch (ArrayIndexOutOfBoundsException e) {	
            fits = false;							
        }

     
        if (fits) {

            ship[index].setPos(row, col, isVertical);

            if (isVertical) {	
                for (int i = 0; i < ship[index].getLength(); i++) {
                    btnGrid[row + i][col].setBackground(Color.GREEN);
                    btnGrid[row + i][col].setOpaque(true);
                    btnGrid[row + i][col].setBorderPainted(false);
                    btnGrid[row + i][col].setText(" ");
                    btnGrid[row + i][col].setForeground(Color.GREEN);

                }
            } else {					
                for (int i = 0; i < ship[index].getLength(); i++) {
                    btnGrid[row][col + i].setBackground(Color.GREEN);
                    btnGrid[row][col + i].setOpaque(true);
                    btnGrid[row][col + i].setBorderPainted(false);
                    btnGrid[row][col + i].setText(" ");
                    btnGrid[row][col + i].setForeground(Color.GREEN);
                }
            }

            index++;

            if (index == ship.length) {

                this.shipsPlaced = true;

                Message shipInfoMsg = new Message(Message.Message_Type.SHIP_INFO);
                shipInfoMsg.content = getShipPositions(); // Gemilerin konumlarını döndüren metot
                Client.Send(shipInfoMsg);
                for (int i = 0; i < btnGrid.length; i++) {
                    for (int j = 0; j < btnGrid.length; j++) {

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

    this.hp = 1; // ya da 17 gibi toplam hücre sayısı
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



    public boolean hasLost() {
        if (hp == 0) {
            return true;
        } else {
            return false;
        }
    }

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

    public void actionPerformed(ActionEvent e) {
        for (int row = 0; row < btnGrid.length; row++) {
            for (int col = 0; col < btnGrid.length; col++) {
                if (btnGrid[row][col] == e.getSource()) {
                    placeShip(row, col);
                }
            }
        }
    }


}
