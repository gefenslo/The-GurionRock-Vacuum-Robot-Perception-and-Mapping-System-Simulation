package bgu.spl.mics.application.services;
import bgu.spl.mics.application.messages.CrushedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.LandMark;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.objects.TrackedObject;
import bgu.spl.mics.application.services.FusionSlamService.OutputData;
import bgu.spl.mics.Message;
import bgu.spl.mics.MicroService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * FusionSlamService integrates data from multiple sensors to build and update
 * the robot's global map.
 * 
 * This service receives TrackedObjectsEvents from LiDAR workers and PoseEvents from the PoseService,
 * transforming and updating the map with new landmarks.
 */
public class FusionSlamService extends MicroService {
    private AtomicInteger cameraCounter;
    private AtomicInteger LiDarCounter;
    private int poseCounter=1;
    private FusionSlam fusionSlam;
    private String firstError;
    private String sender= null;
    private String folderName;
    private int ErrorRunTime=0;


    /**
     * Constructor for FusionSlamService.
     *
     * @param fusionSlam The FusionSLAM object responsible for managing the global map.
     */
    public FusionSlamService(FusionSlam fusionSlam,int cameraCounter,int LiDarCounter,  String folderName)
     {
        super("FusionSlam");
        this.fusionSlam=fusionSlam;
        this.cameraCounter=new AtomicInteger(cameraCounter);
        this.LiDarCounter=new AtomicInteger(LiDarCounter);
        this.folderName= folderName;
    }

    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and TickBroadcasts,
     * and sets up callbacks for updating the global map.
     */
    @Override
    protected void initialize() {
        messageBus.register(this);      
        subscribeBroadcast(CrushedBroadcast.class,ev -> {
            if (firstError == null&& sender==null) {
                sender = ev.getSender();
                firstError = ev.getError();
                ErrorRunTime= ev.getErrorTime();
            }           

            if (ev.getSender().startsWith("camera")) {
                 cameraCounter.decrementAndGet();
 
            }
            if (ev.getSender().startsWith("LiDar")) {
                LiDarCounter.decrementAndGet();
 
            }
            if (ev.getSender().startsWith("Pose")) {
                poseCounter--;
            }
            if (cameraCounter.get() == 0 && LiDarCounter.get() == 0 && poseCounter == 0) {
                createErrorOutput(sender,firstError);
                System.out.println(" we finished! go check your output");
                terminate();  
            }   
        });

        subscribeBroadcast(TerminatedBroadcast.class,ev -> {
            if(ev.getSender().startsWith("camera")){
                cameraCounter.decrementAndGet();

            }
            if(ev.getSender().startsWith("LiDar")){
                LiDarCounter.decrementAndGet();

            }
            if(ev.getSender().startsWith("Pose")){
                poseCounter--;

            }
            if (cameraCounter.get() == 0 && LiDarCounter.get() == 0 && poseCounter == 0) {
                createOutput();
                System.out.println(" we finished! go check your output");
                terminate();
            }
        });

        subscribeEvent(TrackedObjectsEvent.class, ev -> {
            int time = ev.getObjects().get(0).getTime();
            // Log initial state of landmarks
            List<TrackedObject> trackedObjects = ev.getObjects();
            CopyOnWriteArrayList<LandMark>  landmarks = fusionSlam.getLandmarks();
        
            // Check objects in the event
            for (int i = 0; i < trackedObjects.size(); i++) {
                TrackedObject trackedObject = trackedObjects.get(i);
        
                List<CloudPoint> cloudPoints = trackedObject.getCoordinates();   
                Pose pose = fusionSlam.getPoses().get(time - 1);
                if (pose != null) {
                    transformToGlobal(cloudPoints, pose);
                    boolean isExits = false;
                    for (int j = 0; j < landmarks.size(); j++) {
                        if (landmarks.get(j).getID().equals(trackedObject.getID())) {
                            isExits = true;
                            List<CloudPoint> updatedCoords = new ArrayList<>();
                            List<CloudPoint> landmarkCoords = landmarks.get(j).getCoordinates();
                            int minSize = Math.min(cloudPoints.size(), landmarkCoords.size());

                            for (int k = 0; k < minSize; k++) {
                                CloudPoint existing = cloudPoints.get(k);
                                CloudPoint newCoord = landmarkCoords.get(k);
                                updatedCoords.add(new CloudPoint(
                                    (existing.getX() + newCoord.getX()) / 2,
                                    (existing.getY() + newCoord.getY()) / 2
                                ));
                            }

                            List<CloudPoint> sourceList = cloudPoints.size() > minSize ? cloudPoints : landmarkCoords;
                            if (sourceList.size() > minSize) {
                                updatedCoords.addAll(sourceList.subList(minSize, sourceList.size()));
                            }
                            landmarks.get(j).setCoordinates(updatedCoords);
                            break;
                        }
                    }
                    if (!isExits) {
                        LandMark newLandMark = new LandMark(trackedObject.getID(), trackedObject.getDescription(), cloudPoints);
                        fusionSlam.addLandMark(newLandMark);
                        messageBus.getStatisticalFolder().raiseNumLandmarks();
                    }
                }

            }
        });
        
        subscribeEvent(PoseEvent.class,ev->{
            fusionSlam.addPose(ev.getPose());
        });

        messageBus.raiseInitCounter();
        waitForStart();
    }

    static class OutputData {
        private StatisticalFolder statisticalFolder;
        private List<LandMark> landmarks;

        public OutputData(StatisticalFolder statisticalFolder, List<LandMark> landmarks) {
            this.statisticalFolder = statisticalFolder;
            this.landmarks = landmarks;
        }
    }
        public void createOutput(){    
        StatisticalFolder statisticalFolder = messageBus.getStatisticalFolder();
        List<LandMark> landmarks = fusionSlam.getLandmarks(); 
        OutputData outputData = new OutputData(statisticalFolder, landmarks);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
try {
        File folder = new File(folderName);
        if (!folder.exists()) {
            folder.mkdirs(); 
        }

        // Write the JSON file in the specific folder
        File file = new File(folder, "output_file.json");
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(outputData, writer);
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
    }

    static class OutputDataError{
        private String error;
        private String sender;
        private StatisticalFolder statisticalFolder;
        private List<LandMark> landmarks;
        private ConcurrentHashMap<String, StampedDetectedObjects> cameraMap;
        private ConcurrentHashMap<String, TrackedObject> LiDarMap;
        private List<Pose> poses;

        public OutputDataError(String error, String sender, StatisticalFolder statisticalFolder, List<LandMark> landmarks,
        ConcurrentHashMap<String, StampedDetectedObjects> cameraMap,
        ConcurrentHashMap<String, TrackedObject> LiDarMap, List<Pose> poses) {
            this.error = error;
            this.sender = sender;
            this.statisticalFolder = statisticalFolder;
            this.landmarks = landmarks;
            this.cameraMap = cameraMap;
            this.LiDarMap = LiDarMap;
            this.poses = poses;
        }
    }
public void createErrorOutput(String sender, String error) {
    ConcurrentHashMap<String,StampedDetectedObjects>  cameraMap = fusionSlam.getCameraMap();
     ConcurrentHashMap<String,TrackedObject> liDarMap = fusionSlam.getLiDarMap();
    Pose pose = fusionSlam.getPoses().get((fusionSlam.getPoses().size())-1);
    List<LandMark> landmarks = fusionSlam.getLandmarks();
    StatisticalFolder stats = messageBus.getStatisticalFolder();

    Map<String, Object> outputData = new LinkedHashMap<>();
    outputData.put("error", error);
    outputData.put("faultySensor", sender);
    fusionSlam.processRemainingFrames();


    ConcurrentHashMap<String,StampedDetectedObjects> lastCamerasFrame =fusionSlam.getCameraMap();
    ConcurrentHashMap<String,TrackedObject>  lastLiDarFrame = fusionSlam.getLiDarMap();
    outputData.put("lastCamerasFrame", lastCamerasFrame);
    outputData.put("lastLiDarWorkerTrackersFrame", lastLiDarFrame);
    outputData.put("poses", pose);
    Map<String, Object> statistics = new LinkedHashMap<>();
    statistics.put("systemRuntime", ErrorRunTime-1);
    statistics.put("numDetectedObjects", stats.getNumDetectedObjects());
    statistics.put("numTrackedObjects", stats.getNumTrackedObjects());
    statistics.put("numLandmarks", stats.getNumLandmarks());
    statistics.put("landMarks", fusionSlam.getLandmarks());
    outputData.put("statistics", statistics);

    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    try {
        File folder = new File(folderName);
        if (!folder.exists()) {
            folder.mkdirs(); 
        }

        // Write the JSON file in the specific folder
        File file = new File(folder, "errorOutput.json");
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(outputData, writer);
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}


    public Object getObject(){
        return null;
    }
    public static void transformToGlobal(List<CloudPoint> points, Pose robotPose) {
        double yawRad = Math.toRadians(robotPose.getYaw()); 
        double cosYaw = Math.cos(yawRad);
        double sinYaw = Math.sin(yawRad);

        for (CloudPoint point : points) {
            double globalX = cosYaw * point.getX() - sinYaw * point.getY() + robotPose.getX();
            double globalY = sinYaw * point.getX() + cosYaw * point.getY() + robotPose.getY();
            point.setX(globalX);
            point.setY(globalY);
        }
    }

    public CloudPoint getAverage(CloudPoint point1, CloudPoint point2){
        return new CloudPoint((point1.getX()+point2.getX())/2,((point1.getY()+point2.getY())/2));
    }

}
