package game;

import java.io.Serializable;

public class Ship implements Serializable {
    private int health, length;
    private String name;
    private boolean pos[][];
    private int r, c;
    private boolean vertical; 

    public Ship(String n, int l) {
        this.name = n;
        this.length = l;
        this.health = l;
        this.pos = new boolean[10][10];
        
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                this.pos[i][j] = false;
            }
        }
    }

    public void setPos(int row, int col, boolean isVertical) {
        this.r = row;
        this.c = col;
        this.vertical = isVertical; // DURUMU KAYDET
        
        if (isVertical) {
            for (int i = 0; i < length; i++) {
                if(row+i < 10) { // SINIR KONTROLÜ EKLENDİ
                    pos[row + i][col] = true;
                }
            }
        } else {
            for (int i = 0; i < length; i++) {
                if(col+i < 10) { // SINIR KONTROLÜ EKLENDİ
                    pos[row][col + i] = true;
                }
            }
        }
    }

    public boolean checkHit(int row, int col) {
        // ÖNCE SINIR KONTROLÜ
        if(row < 0 || row >= 10 || col < 0 || col >= 10) {
            return false;
        }
        return pos[row][col];
    }

    // YENİ METOD: Gemi dikey mi
    public boolean isVertical() {
        return vertical;
    }


    public int getHealth() { return health; }
    public int getLength() { return length; }
    public String getName() { return name; }
    public int getRow() { return r; }
    public int getCol() { return c; }
}