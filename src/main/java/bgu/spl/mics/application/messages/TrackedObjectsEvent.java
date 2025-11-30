package bgu.spl.mics.application.messages;

import java.util.LinkedList;

import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.LandMark;
import bgu.spl.mics.application.objects.TrackedObject;
import bgu.spl.mics.Event;
import java.util.List;


public class TrackedObjectsEvent implements Event<List<TrackedObject>>{
    private LinkedList<TrackedObject> objects;

    public TrackedObjectsEvent(LinkedList<TrackedObject> objects){
        this.objects=objects;
    }

    public LinkedList<TrackedObject> getObjects(){
        return objects;
    }
}
