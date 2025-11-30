package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class TickBroadcast implements Broadcast{
    private int timeTick;

    public TickBroadcast(int timeTick){
        this.timeTick=timeTick;
        }

    public int getTime(){
        return timeTick;
    }
}
