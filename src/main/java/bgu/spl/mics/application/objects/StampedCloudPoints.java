package bgu.spl.mics.application.objects;
import java.util.LinkedList;

/**
 * Represents a group of cloud points corresponding to a specific timestamp.
 * Used by the LiDAR system to store and process point cloud data for tracked objects.
 */
public class StampedCloudPoints {
private final String id;
private final int time;
private final LinkedList<LinkedList<Double>> cloudPoints;

public StampedCloudPoints(String id,int time,LinkedList<LinkedList<Double>> cloudPoints){
    this.id=id;
    this.time=time;
    this.cloudPoints=cloudPoints;
}

public String getID(){
    return id;
}

public int getTime(){
    return time;
}

public LinkedList<LinkedList<Double>> getCloudPoints(){
    return cloudPoints;
}

public LinkedList<CloudPoint> getConvertedCloudPoints(){
    LinkedList<CloudPoint> result=new LinkedList<>();
    for(int i=0;i<cloudPoints.size();i++){
        CloudPoint point = new CloudPoint(cloudPoints.get(i).get(0),cloudPoints.get(i).get(1));
        result.add(point);
    }
    return result;
}

}
