package bgu.spl.mics.application.messages;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.Future;

import bgu.spl.mics.Event;


public class PoseEvent implements Event<Pose>{
    private Future result;
    private final Pose pose;

    public PoseEvent(Pose pose){
        this.pose=pose;
    }
    public Pose getPose(){
        return pose;
    }


}
