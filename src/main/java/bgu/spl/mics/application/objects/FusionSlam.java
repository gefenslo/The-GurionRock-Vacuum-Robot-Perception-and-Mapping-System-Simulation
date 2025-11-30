package bgu.spl.mics.application.objects;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {

    private static class FusionSlamHolder {
        private static final FusionSlam INSTANCE = new FusionSlam();
    }

    private volatile CopyOnWriteArrayList<LandMark> landmarks; 
    private LinkedList<Pose> poses;

    private ConcurrentHashMap<String,StampedDetectedObjects> cameraMap;
    private ConcurrentHashMap<String,TrackedObject> LiDarMap;

    private FusionSlam() {
        this.landmarks = new CopyOnWriteArrayList<LandMark> ();  
        this.poses = new LinkedList<>();  
        cameraMap=new ConcurrentHashMap<>();
        LiDarMap=new ConcurrentHashMap<>();
    }

    public static FusionSlam getInstance() {
        return FusionSlamHolder.INSTANCE;
    }

    public synchronized CopyOnWriteArrayList<LandMark>  getLandmarks() {
        return landmarks;
    }

    public void setLandmarks(CopyOnWriteArrayList<LandMark>  landmarks) {
        this.landmarks = landmarks;
    }

    public LinkedList<Pose> getPoses() {
        return poses;
    }

    public void addPose(Pose pose) {
        this.poses.add(pose);
    }

    public synchronized void addLandMark(LandMark landmark){
        landmarks.add(landmark);
    }

    public void updateCameraMap(String id, StampedDetectedObjects object){
        cameraMap.clear();
        cameraMap.put(id,object);
    }
    public void updateLiDarMap(String id, TrackedObject object){
        LiDarMap.clear();
        LiDarMap.put(id,object);
    }

    public ConcurrentHashMap<String,StampedDetectedObjects> getCameraMap(){
        return cameraMap;
    }

    public ConcurrentHashMap<String,TrackedObject> getLiDarMap(){
        return LiDarMap;
    }

    public void processRemainingFrames() {
        synchronized(this) {

    
            // Process LiDAR frames
            for (TrackedObject obj : LiDarMap.values()) {
                // For LiDAR objects, use converted cloud points
                LandMark landmark = new LandMark(
                    obj.getID(), 
                    obj.getDescription(),
                    obj.getCoordinates()
                );
                if (!landmarks.contains(landmark)) {
                    landmarks.add(landmark);
                }
            }
        }
    }

}
