package bgu.spl.mics.application.services;
import bgu.spl.mics.application.messages.CrushedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.LandMark;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.MicroService;

import java.util.LinkedList;

import bgu.spl.mics.Future;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedDetectedObjects;


/**
 * PoseService is responsible for maintaining the robot's current pose (position and orientation)
 * and broadcasting PoseEvents at every tick.
 */
public class PoseService extends MicroService {

    private GPSIMU gpsimu;
    private int finalTime;
    /**
     * Constructor for PoseService.
     *
     * @param gpsimu The GPSIMU object that provides the robot's pose data.
     */
    public PoseService(GPSIMU gpsimu) {
        super("PoseService");
        this.gpsimu=gpsimu;
        finalTime=gpsimu.getFinalTime();
    }

    /**
     * Initializes the PoseService.
     * Subscribes to TickBroadcast and sends PoseEvents at every tick based on the current pose.
     */
    @Override
    protected void initialize() {
        messageBus.register(this);
        subscribeBroadcast(TickBroadcast.class, ev -> {
        Pose pose=gpsimu.getPoseAtTime(ev.getTime());
        PoseEvent event = new PoseEvent(pose);
        sendEvent(event);
       
        
        
        if(gpsimu.getStatus() == STATUS.DOWN){
            sendBroadcast(new TerminatedBroadcast(getName()));
            gpsimu.setStatus(STATUS.DOWN);
            terminate();  
            return;          
        }
        if (ev.getTime()==messageBus.getDuration()){
            gpsimu.setStatus(STATUS.DOWN);
            sendBroadcast(new TerminatedBroadcast(this.getName()));
            terminate();     

    
        }
        });
        subscribeBroadcast(CrushedBroadcast.class,ev -> {
            gpsimu.setStatus(STATUS.DOWN);
            CrushedBroadcast b = new CrushedBroadcast(getName(),"",0);
            sendBroadcast(b);
            terminate();     
        });
        subscribeBroadcast(TerminatedBroadcast.class,ev -> {
            if(ev.getSender().equals("TimeService")){
                gpsimu.setStatus(STATUS.DOWN);
                sendBroadcast(new TerminatedBroadcast(this.getName()));
                terminate();  
            }
        });
        messageBus.raiseInitCounter();
        waitForStart();
    }
    
    public Object getObject(){
        return null;
    }
}
