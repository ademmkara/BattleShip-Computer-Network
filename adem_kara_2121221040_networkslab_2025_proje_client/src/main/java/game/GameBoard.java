/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package game;

import battleshipclient.Client;
import game.Game;
import static game.Game.btnFire;
import static game.Game.btnRestart;
import static game.Game.enemyBoard;
import static game.Game.gameFrame;
import static game.Game.playerBoard;
import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 *
 * @author cvsbilisim
 */
public class GameBoard extends javax.swing.JFrame {

    public static GameBoard ThisGame;
    public static Board enemyBoard;
    public static PlayerBoard playerBoard;
    public static boolean iAmReady = false;
    public static boolean rivalIsReady = false;
    public static boolean myTurn = false;

    public GameBoard() {
        initComponents();
        ThisGame = this;
        //setSize(1216, 766); // Açılış boyutunu tam olarak belirle
        setResizable(false); // pencere yeniden boyutlandırılamaz
        setLocationRelativeTo(null); // ekranın ortasında açılır
        btnFire.setEnabled(false);

        btnRestart.setVisible(false);

    }

    public void showGameBoards() {
        // Panel ayarları
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(1200, 700);
        this.setLayout(null);

        // Enemy Board
        enemyBoard = new Board();
        enemyBoard.drawBoard();
        enemyBoard.setBounds(50, 100, 300, 300);
        pnl_Board.add(enemyBoard);

        // Player Board
        playerBoard = new PlayerBoard();
        playerBoard.drawBoard();
        playerBoard.setBounds(500, 100, 300, 300);
        pnl_Board.add(playerBoard);

        // Etiketler güvenli olarak ayarla
        if (Game.ThisGame != null) {
            txt_rival_name.setText(Game.ThisGame.txt_rival_name.getText() + "'s board");
            txt_name.setText(Game.ThisGame.txt_name.getText() + "'s board (YOU)");
        } else {
            txt_rival_name.setText("Enemy Board");
            txt_name.setText("Your Board");
        }

        // Oyunu kapatırken bağlantıyı sonlandır
        // Oyunu kapatırken onay al, sonra bağlantıyı sonlandır
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                int result = JOptionPane.showConfirmDialog(
                        GameBoard.this,
                        "Oyunu kapatmak istediğinize emin misiniz?",
                        "Çıkış Onayı",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

                if (result == JOptionPane.YES_OPTION) {
                    Client.Stop();
                    dispose(); // pencereyi kapat
                    Message disconnect = new Message(Message.Message_Type.Disconnect);
                    Client.Send(disconnect);
                } else {
                    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // kapatma iptal
                }
            }
        });

    }

    public void initializeNewGame() {
        if (enemyBoard != null) {
            enemyBoard.resetBoard();
        }

        if (playerBoard != null) {
            playerBoard.resetBoard();

        }
        btnFire.setVisible(true);
        btnFire.setEnabled(false);
        btnRestart.setVisible(false);
        btnReady.setEnabled(true);

        GameBoard.iAmReady = false;
        GameBoard.rivalIsReady = false;

        txt_receive.setText("Yeni oyun başladı. Gemilerinizi yerleştirin.");
        btnHorizantal.setEnabled(true);
        btnVertical.setEnabled(true);
    }


    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnl_Board = new javax.swing.JPanel();
        btnReady = new javax.swing.JButton();
        btnFire = new javax.swing.JButton();
        btnRestart = new javax.swing.JButton();
        txt_rival_name = new javax.swing.JTextField();
        txt_name = new javax.swing.JTextField();
        btnHorizantal = new javax.swing.JButton();
        btnVertical = new javax.swing.JButton();
        pnl_message = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txt_send = new javax.swing.JTextArea();
        btn_send_message = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        txt_receive = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(1216, 766));

        pnl_Board.setBackground(new java.awt.Color(0, 0, 0));

        btnReady.setBackground(new java.awt.Color(0, 204, 204));
        btnReady.setFont(new java.awt.Font("Segoe UI Black", 1, 14)); // NOI18N
        btnReady.setText("Ready");
        btnReady.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReadyActionPerformed(evt);
            }
        });

        btnFire.setBackground(new java.awt.Color(255, 0, 51));
        btnFire.setFont(new java.awt.Font("Segoe UI Black", 1, 14)); // NOI18N
        btnFire.setText("Fire");
        btnFire.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnFireMouseClicked(evt);
            }
        });
        btnFire.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFireActionPerformed(evt);
            }
        });

        btnRestart.setBackground(new java.awt.Color(153, 153, 153));
        btnRestart.setFont(new java.awt.Font("Segoe UI Black", 1, 14)); // NOI18N
        btnRestart.setText("Restart");
        btnRestart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRestartActionPerformed(evt);
            }
        });

        txt_rival_name.setEditable(false);
        txt_rival_name.setBackground(new java.awt.Color(0, 0, 0));
        txt_rival_name.setFont(new java.awt.Font("Segoe UI Black", 1, 14)); // NOI18N
        txt_rival_name.setForeground(new java.awt.Color(255, 255, 255));
        txt_rival_name.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_rival_nameActionPerformed(evt);
            }
        });

        txt_name.setEditable(false);
        txt_name.setBackground(new java.awt.Color(0, 0, 0));
        txt_name.setFont(new java.awt.Font("Segoe UI Black", 1, 14)); // NOI18N
        txt_name.setForeground(new java.awt.Color(255, 255, 255));
        txt_name.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_nameActionPerformed(evt);
            }
        });

        btnHorizantal.setBackground(new java.awt.Color(255, 204, 0));
        btnHorizantal.setFont(new java.awt.Font("Segoe UI Black", 1, 14)); // NOI18N
        btnHorizantal.setText("Horizantal");
        btnHorizantal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHorizantalActionPerformed(evt);
            }
        });

        btnVertical.setBackground(new java.awt.Color(51, 153, 0));
        btnVertical.setFont(new java.awt.Font("Segoe UI Black", 1, 14)); // NOI18N
        btnVertical.setText("Vertical");
        btnVertical.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVerticalActionPerformed(evt);
            }
        });

        pnl_message.setBackground(new java.awt.Color(102, 102, 102));

        txt_send.setColumns(20);
        txt_send.setRows(5);
        jScrollPane1.setViewportView(txt_send);

        btn_send_message.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btn_send_message.setText("Send");
        btn_send_message.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_send_messageActionPerformed(evt);
            }
        });

        txt_receive.setColumns(20);
        txt_receive.setRows(5);
        jScrollPane2.setViewportView(txt_receive);

        jLabel1.setFont(new java.awt.Font("Segoe UI Black", 1, 36)); // NOI18N
        jLabel1.setText("BATTLESHIP GAME");

        javax.swing.GroupLayout pnl_messageLayout = new javax.swing.GroupLayout(pnl_message);
        pnl_message.setLayout(pnl_messageLayout);
        pnl_messageLayout.setHorizontalGroup(
            pnl_messageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnl_messageLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnl_messageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnl_messageLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_messageLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(pnl_messageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(65, 65, 65))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_messageLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btn_send_message, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(135, 135, 135))
        );
        pnl_messageLayout.setVerticalGroup(
            pnl_messageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnl_messageLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(jLabel1)
                .addGap(36, 36, 36)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(63, 63, 63)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36)
                .addComponent(btn_send_message, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(247, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnl_BoardLayout = new javax.swing.GroupLayout(pnl_Board);
        pnl_Board.setLayout(pnl_BoardLayout);
        pnl_BoardLayout.setHorizontalGroup(
            pnl_BoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnl_BoardLayout.createSequentialGroup()
                .addGroup(pnl_BoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnl_BoardLayout.createSequentialGroup()
                        .addGap(333, 333, 333)
                        .addComponent(btnRestart, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(385, 385, 385))
                    .addGroup(pnl_BoardLayout.createSequentialGroup()
                        .addGroup(pnl_BoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnl_BoardLayout.createSequentialGroup()
                                .addGap(111, 111, 111)
                                .addComponent(btnFire, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(pnl_BoardLayout.createSequentialGroup()
                                .addGap(102, 102, 102)
                                .addComponent(txt_rival_name, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 266, Short.MAX_VALUE)
                        .addGroup(pnl_BoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_BoardLayout.createSequentialGroup()
                                .addComponent(btnReady, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(49, 49, 49)
                                .addGroup(pnl_BoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(btnHorizantal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btnVertical, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(45, 45, 45))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_BoardLayout.createSequentialGroup()
                                .addComponent(txt_name, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(112, 112, 112)))))
                .addComponent(pnl_message, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnl_BoardLayout.setVerticalGroup(
            pnl_BoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnl_BoardLayout.createSequentialGroup()
                .addGroup(pnl_BoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnl_BoardLayout.createSequentialGroup()
                        .addGap(53, 53, 53)
                        .addGroup(pnl_BoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txt_rival_name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txt_name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 444, Short.MAX_VALUE)
                        .addGroup(pnl_BoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnFire, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnReady, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(53, 53, 53))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_BoardLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnHorizantal)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnVertical)
                        .addGap(39, 39, 39)))
                .addComponent(btnRestart, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(137, 137, 137))
            .addComponent(pnl_message, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnl_Board, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnl_Board, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnReadyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReadyActionPerformed
        if (playerBoard.getAllShipsPlaced()) {
            GameBoard.iAmReady = true;

            Message readyMsg = new Message(Message.Message_Type.Ready);
            Client.Send(readyMsg);
            JOptionPane.showMessageDialog(null, "Gemi Pozisyonlari alindi, Rakip Hazir oldugunda saldirabiliceksiniz.");
            btnReady.setEnabled(false);
            btnHorizantal.setEnabled(false);
            btnVertical.setEnabled(false);
            if (GameBoard.rivalIsReady) {
                btnFire.setEnabled(true);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Lütfen gemilerinizi yerleştiriniz.\nGemi Boyutları: 5-4-3-3-2");
        }
    }//GEN-LAST:event_btnReadyActionPerformed

    private void btnFireActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFireActionPerformed
        int row = GameBoard.enemyBoard.getRclick();
        int col = GameBoard.enemyBoard.getCclick();

        if (row < 0 || col < 0) {
            //Game.ThisGame.txt_receive.setText("Lütfen bir hücre seçin!");
            return;
        }

        Message attackMsg = new Message(Message.Message_Type.Attack);
        attackMsg.content = row + "," + col;
        Client.Send(attackMsg);

        btnFire.setEnabled(false);  // Sırayı rakibe ver
        //Game.ThisGame.txt_receive.setText("Saldırı gönderildi: " + (char) (row + 'A') + (col + 1));
        
    }//GEN-LAST:event_btnFireActionPerformed

    private void btnRestartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRestartActionPerformed
        Message restartMsg = new Message(Message.Message_Type.RestartRequest);
        Client.Send(restartMsg);
        btnRestart.setEnabled(false);
        System.out.println("Tekrar oyun isteği gönderildi. Rakibi bekliyorsunuz...");
        GameBoard.ThisGame.txt_receive.setText("Tekrar oyun isteği gönderildi. Rakibi bekliyorsunuz...");
    }//GEN-LAST:event_btnRestartActionPerformed

    private void btnFireMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnFireMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_btnFireMouseClicked

    private void btn_send_messageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_send_messageActionPerformed
        //metin mesajı gönder
        Message msg = new Message(Message.Message_Type.Text2);
        String x = txt_send.getText();
        msg.content = txt_send.getText();
        Client.Send(msg);
        txt_send.setText("");
    }//GEN-LAST:event_btn_send_messageActionPerformed

    private void txt_nameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_nameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_nameActionPerformed

    private void txt_rival_nameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_rival_nameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_rival_nameActionPerformed

    private void btnHorizantalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHorizantalActionPerformed
        playerBoard.setIsVertical(false);
    }//GEN-LAST:event_btnHorizantalActionPerformed

    private void btnVerticalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVerticalActionPerformed
        playerBoard.setIsVertical(true);
    }//GEN-LAST:event_btnVerticalActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(GameBoard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GameBoard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GameBoard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GameBoard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GameBoard().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JButton btnFire;
    public javax.swing.JButton btnHorizantal;
    public javax.swing.JButton btnReady;
    public javax.swing.JButton btnRestart;
    public javax.swing.JButton btnVertical;
    public javax.swing.JButton btn_send_message;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    public javax.swing.JPanel pnl_Board;
    private javax.swing.JPanel pnl_message;
    public javax.swing.JTextField txt_name;
    public javax.swing.JTextArea txt_receive;
    public javax.swing.JTextField txt_rival_name;
    public javax.swing.JTextArea txt_send;
    // End of variables declaration//GEN-END:variables
}
