package bgu.spl.mics.application.objects;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import bgu.spl.mics.MessageBusImpl;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {

    private List<StampedCloudPoints> cloudPoints;
    private static volatile LiDarDataBase instance;
    private LiDarDataBase(List<StampedCloudPoints> cloudPoints){
    this.cloudPoints = cloudPoints;
        }
    /**
     * Returns the singleton instance of LiDarDataBase.
     *
     * @param filePath The path to the LiDAR data file.
     * @return The singleton instance of LiDarDataBase.
     */
 
     public static  void setInstance(List<StampedCloudPoints> cloudPoints) {
        if (instance == null) {
            instance = new LiDarDataBase(cloudPoints);
        }
    }

    public synchronized static LiDarDataBase getInstance() {
        if (instance == null) {
            throw new IllegalStateException("LiDarDataBase has not been initialized");
        }
        return instance;
    }

    public List<StampedCloudPoints> getCloudPoints(){
        return cloudPoints;
    }

    public StampedCloudPoints getStampedCloudPoints(int time, String id){
        for(int i=0;i<cloudPoints.size();i++){
            if(cloudPoints.get(i).getID().equals("ERROR")){
                return null;
            }
            if(cloudPoints.get(i).getTime()==time&&id.equals(cloudPoints.get(i).getID())){
                return cloudPoints.get(i);
            }
        }
        return null;
    }

 
}