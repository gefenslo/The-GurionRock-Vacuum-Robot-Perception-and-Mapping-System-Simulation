package bgu.spl.mics.application.services;

import java.util.LinkedList;
import java.util.List;

import bgu.spl.mics.Callback;
import bgu.spl.mics.Future;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.Message;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrushedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.example.messages.ExampleEvent;
import java.util.concurrent.ConcurrentHashMap;

import java.util.Queue;
/**
 * CameraService is responsible for processing data from the camera and
 * sending DetectObjectsEvents to LiDAR workers.
 *
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService {
    private final Camera camera;
    private int finalTime;
    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(Camera camera) {
        super("camera"+String.valueOf(camera.getID()));
        this.camera=camera;
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for sending
     * DetectObjectsEvents.
     */
    @Override
    protected void initialize() {
        messageBus.register(this);
        subscribeBroadcast(TickBroadcast.class, ev -> {
        camera.checkIfError(ev.getTime());
        StampedDetectedObjects stamppedObjects= camera.getDetectedAtTime(ev.getTime());
        if(camera.getStatus()== STATUS.DOWN){
        sendBroadcast(new TerminatedBroadcast(getName()));
        terminate();
        return;
        }
        else if (camera.getStatus()==STATUS.ERROR){
            CrushedBroadcast b = new CrushedBroadcast(getName(),camera.getErrorDescription(),ev.getTime());
            sendBroadcast(b);
            terminate();
            return;
        }
        else if (stamppedObjects== null){
            return;
        }

        else{
            DetectObjectsEvent event = new DetectObjectsEvent(stamppedObjects);
            int size= stamppedObjects.getDetectedObjects().size();
            sendEvent(event);
            for(int i=0;i<size;i++){
                messageBus.getStatisticalFolder().raiseNumDetectedObjects();
            }
            FusionSlam.getInstance().updateCameraMap(getName(), stamppedObjects);
            }
        if (ev.getTime()==messageBus.getDuration()){
            camera.setStatus(STATUS.DOWN);
            sendBroadcast(new TerminatedBroadcast(this.getName()));
            terminate();
        }

        });

        subscribeBroadcast(CrushedBroadcast.class,ev -> {
            camera.setStatus(STATUS.DOWN);
            CrushedBroadcast b = new CrushedBroadcast(getName(),camera.getErrorDescription(),0);
            sendBroadcast(b);
            terminate();
        });

        subscribeBroadcast(TerminatedBroadcast.class,ev -> {
            if(ev.getSender().equals("TimeService")){
                camera.setStatus(STATUS.DOWN);
                sendBroadcast(new TerminatedBroadcast(this.getName()));
                terminate();
            }
        });
        messageBus.raiseInitCounter();
        waitForStart();
    }


    public Object getObject(){
        return camera;
    }

}
