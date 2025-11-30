package bgu.spl.mics.application.objects;
import java.util.LinkedList;

/**
 * Represents objects detected by the camera at a specific timestamp.
 * Includes the time of detection and a list of detected objects.
 */
public class StampedDetectedObjects {
int time;
//Camera camera;
LinkedList<DetectedObject> detectedObjects;

    public StampedDetectedObjects(int time, LinkedList<DetectedObject> stampedList){
        this.time=time;
        //this.camera=camera;
        this.detectedObjects = stampedList;

    }
    public void addDetectedObject(DetectedObject object){
        detectedObjects.add(object);
    }

    public LinkedList<DetectedObject> getDetectedObjects(){
        return detectedObjects;
    }
    public int getTime(){
        return time;
    }
}
