package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class CrushedBroadcast implements Broadcast{
    private String sender;
    private String error;
    private int crushTime;
    public CrushedBroadcast(String sender,String error,int crushTime){
        this.sender=sender;
        this.error=error;
        this.crushTime=crushTime;
    }

    public String getSender(){
        return sender;
    }

    public String getError(){
        return error;
    }

    public int getErrorTime(){
        return crushTime;
    }
}
