package bgu.spl.mics.application.objects;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import bgu.spl.mics.application.services.CameraService;

/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera {
private String key;
private final int id;
private final int frequency;
private  STATUS status;
private List<StampedDetectedObjects> detectedObjectsList;
private String error=new String();

public Camera(String key, int id, int frequency, List<StampedDetectedObjects> detectedObjectsList){
    this.key=key;
    this.id=id;
    this.frequency=frequency;
    status=STATUS.UP;
    this.detectedObjectsList=detectedObjectsList;
}



    
public int getID(){
    return id;
}

public int getFrequency(){
    return frequency;
}

public STATUS getStatus(){
    return status;
}

public void setStatus(STATUS status){
    this.status=status;
}
public void addDetectedObject(StampedDetectedObjects object){
    detectedObjectsList.add(object);

}

public List<StampedDetectedObjects> getDetectedObjects(){
    return detectedObjectsList;
}


public String getKey(){
    return key;
}


public String getErrorDescription(){
    return error;
}
/**
 * Class invariants:
 * @inv detectedObjectsList != null
 * @inv status != null
 * @inv frequency > 0
 * @inv key != null
 * 
 * Gets the detected objects at a specific time point
 * 
 * Pre-conditions:
 * 
 * Post-conditions:
 * @post if detectedObjectsList.isEmpty() then status == STATUS.DOWN
 * @post if result != null then result.getTime() == time-frequency
 * @post if result returned, original detectedObjectsList.size() == current detectedObjectsList.size() + 1
 * @post if no matching time found, returns null
 * 
 * @param time The time point to check for detected objects
 * @return StampedDetectedObjects at the specified time, or null if not found or if list is empty
 */
public StampedDetectedObjects getDetectedAtTime (int time){
if(this.detectedObjectsList.isEmpty()){
    this.status= STATUS.DOWN;
    return null;
}

for(int i=0;i<detectedObjectsList.size();i++){
    if(detectedObjectsList.get(i).getTime()== time-frequency){
        StampedDetectedObjects result =detectedObjectsList.get(i);
        if(result.getDetectedObjects()==null){
            return null;
        }
        this.detectedObjectsList.remove(i);
       return result;
    }
}
return null;
}

public boolean checkIfError(int time){
    for(int i=0;i<detectedObjectsList.size();i++){
        if(detectedObjectsList.get(i).getTime()== time){
            List <DetectedObject> list = detectedObjectsList.get(i).getDetectedObjects();
            for(int j=0;j<list.size();j++){
                if(list.get(j).getID().equals("ERROR")){
                    this.status=STATUS.ERROR;
                    error = list.get(i).getDescription();
                    return true;
                }   
            }
            return false;
        }

    }
return false;  

}
}



