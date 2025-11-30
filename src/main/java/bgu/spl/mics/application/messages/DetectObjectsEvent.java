package bgu.spl.mics.application.messages;

import java.util.LinkedList;

import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.Event;


public class DetectObjectsEvent implements Event<StampedDetectedObjects>{
    private StampedDetectedObjects objects;

    public DetectObjectsEvent(StampedDetectedObjects objects){
        this.objects=objects;
    }

    public StampedDetectedObjects getObjects(){
        return objects;
    }

    
}
