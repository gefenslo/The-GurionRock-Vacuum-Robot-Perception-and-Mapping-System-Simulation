package bgu.spl.mics.application.objects;
import java.io.FileReader;
import java.io.Reader;
import java.util.LinkedList;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.services.CameraService;

/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */
public class GPSIMU {
    private int currentTick;
    private STATUS status;
    private LinkedList<Pose> PoseList;
    private int finalTime;

    public GPSIMU(String filePath){
        currentTick=0;
        status=STATUS.UP;
        PoseList=new LinkedList<Pose>();
        try (Reader reader = new FileReader(filePath)){
            Gson gson = new Gson();
            JsonArray poseArray = JsonParser.parseReader(reader).getAsJsonArray();           
            for (int i = 0; i < poseArray.size(); i++) {
                JsonObject PoseConfig = poseArray.get(i).getAsJsonObject();
                int time = PoseConfig.get("time").getAsInt();
                float x = PoseConfig.get("x").getAsFloat();
                float y = PoseConfig.get("y").getAsFloat();
                float yaw = PoseConfig.get("yaw").getAsFloat();
                Pose pose=new Pose(x,y,yaw,time);
                PoseList.add(pose);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finalTime=PoseList.get(PoseList.size()-1).getTime();
    }
    public LinkedList<Pose> getPoseList(){
        return PoseList;
    }
    public void setStatus(STATUS status){
        this.status=status;
    }
    public int getFinalTime(){
        return finalTime;
    }
    public Pose getPoseAtTime(int time) {
        for(int i=0;(i<PoseList.size());i++){
            if(time==PoseList.get(i).getTime()){
                Pose pose=PoseList.get(i);
                PoseList.remove(pose);
                if(PoseList.isEmpty()){
                    status = STATUS.DOWN;
                }
                return pose;
            }
        }
        return null;
    }
    public STATUS getStatus() {
        return status;
    }
}
