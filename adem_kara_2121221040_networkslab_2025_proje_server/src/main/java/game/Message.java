/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;


/**
 *
 * @author INSECT
 */

public class Message implements java.io.Serializable {
    public static enum Message_Type {None,Name, Disconnect,RivalConnected, Text, Selected, Bitis,Start,PairStatus,SHIP_INFO,Attack,AttackResult,Ready,RestartRequest,Text2,RivalDisconnected,Turn,Ping,Pong
}
    
    public Message_Type type;
    public Object content;
    public String sender; // saldırıyı yapan kişinin adı
    public Message(Message_Type t)
    {
        this.type=t;
    }
 

    
    
}
