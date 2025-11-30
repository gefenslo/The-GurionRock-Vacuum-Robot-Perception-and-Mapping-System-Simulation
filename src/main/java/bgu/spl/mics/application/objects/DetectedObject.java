package bgu.spl.mics.application.objects;

/**
 * DetectedObject represents an object detected by the camera.
 * It contains information such as the object's ID and description.
 */
public class DetectedObject {
    private final String id;
    private final String description;
    private final int timeTick;


    public DetectedObject(String id, String description, int tick){
        this.id=id;
        this.description=description;
        this.timeTick=tick;
    }

public String getID(){
    return id;
}

public String getDescription(){
    return description;
}

public int getTime(){
    return timeTick;
}
}
