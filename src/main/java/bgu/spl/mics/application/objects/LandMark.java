package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * Represents a landmark in the environment map.
 * Landmarks are identified and updated by the FusionSlam service.
 */
public class LandMark {
private String id;
private String description;
private List<CloudPoint> coordinates;



public LandMark(String id, String description,List<CloudPoint> list){
this.id=id;
this.description=description;
coordinates=list;
}

public String getID(){
    return id;
}
public List<CloudPoint> getCoordinates(){
    return coordinates;
}

public void setCoordinates(List<CloudPoint> coordinates) {
    this.coordinates = coordinates;
}

}
