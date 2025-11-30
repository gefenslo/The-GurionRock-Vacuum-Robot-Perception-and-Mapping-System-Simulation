package bgu.spl.mics.application;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.LiDarDataBase;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.StampedCloudPoints;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.services.CameraService;
import bgu.spl.mics.application.services.FusionSlamService;
import bgu.spl.mics.application.services.LiDarService;
import bgu.spl.mics.application.services.PoseService;
import bgu.spl.mics.application.services.TimeService;

public class GurionRockRunner {

    public static void main(String[] args) {
        try {
            MessageBusImpl messageBus=MessageBusImpl.getInstance();
            Gson gson = new Gson();

            String configFilePath = args [0];

            Reader reader = new FileReader(configFilePath);
            File file = new File (configFilePath);
            String folderName = file.getParent();
            JsonObject config = JsonParser.parseReader(reader).getAsJsonObject();

            
            JsonArray camerasArray = config.getAsJsonObject("Cameras").getAsJsonArray("CamerasConfigurations");
            JsonArray liDarArray = config.getAsJsonObject("LiDarWorkers").getAsJsonArray("LidarConfigurations");

            // Total number of services to wait for registration
            int numberOfServices = camerasArray.size() + liDarArray.size() + 2; // Cameras + LiDars + Pose + FusionSlam
            //CountDownLatch registrationLatch = new CountDownLatch(numberOfServices);

            // Initialize Camera Services
            String cameraDataPathDot = config.getAsJsonObject("Cameras").get("camera_datas_path").getAsString();
            String cameraDataPath = folderName+cameraDataPathDot.substring(1);
            for (int i = 0; i < camerasArray.size(); i++) {
                JsonObject cameraConfig = camerasArray.get(i).getAsJsonObject();
                int id = cameraConfig.get("id").getAsInt();
                int frequency = cameraConfig.get("frequency").getAsInt();
                String key = cameraConfig.get("camera_key").getAsString();
                Reader cameraReader = new FileReader(cameraDataPath);
                Type type = new TypeToken<Map<String, List<StampedDetectedObjects>>>(){}.getType();
                Map<String, List<StampedDetectedObjects>> data = gson.fromJson(cameraReader, type);
                List<StampedDetectedObjects>  detectedObjectsList = data.get(key);
                Camera camera = new Camera(key,id, frequency, detectedObjectsList);
                CameraService cameraService = new CameraService(camera);
                new Thread(() -> {
                        cameraService.run();
                    }, cameraService.getName()+"Thread").start();
                }

 // Initialize LiDar Services
 String liDarPathDot = config.getAsJsonObject("LiDarWorkers").get("lidars_data_path").getAsString();
 String lidarDataPath =folderName+ liDarPathDot.substring(1);
 try (FileReader lidarReader = new FileReader(lidarDataPath)) {
     Type listType = new TypeToken<List<StampedCloudPoints>>() {}.getType();
     List<StampedCloudPoints> cloudPoints = gson.fromJson(lidarReader, listType);
     LiDarDataBase.setInstance(cloudPoints);
 } 
 catch (IOException e) {
     e.printStackTrace();
 }
 for (int i = 0; i < liDarArray.size(); i++) {
     JsonObject liDarConfig = liDarArray.get(i).getAsJsonObject();
     int id = liDarConfig.get("id").getAsInt();
     int frequency = liDarConfig.get("frequency").getAsInt();
     LiDarWorkerTracker liDar = new LiDarWorkerTracker(id, frequency);
     LiDarService liDarService = new LiDarService(liDar);        
 
     new Thread(() -> {
             liDarService.run();
         }, liDarService.getName()+"Thread").start();
     }


            // Initialize Pose Service
            String posePathDot = config.get("poseJsonFile").getAsString();
            String poseDataPath = folderName+posePathDot.substring(1);
            GPSIMU gps = new GPSIMU(poseDataPath);
            PoseService poseService = new PoseService(gps);

            new Thread(() -> {
                    poseService.run();
                }, poseService.getName()+"Thread").start();

            // Initialize FusionSlam Service
            FusionSlamService fusionSlamService = new FusionSlamService(FusionSlam.getInstance(), camerasArray.size(), liDarArray.size(),folderName);

            new Thread(() -> {
                    fusionSlamService.run();
                }, fusionSlamService.getName()+"Thread").start();


            // Start TimeService after all services register
            TimeService timeService = new TimeService(config.get("TickTime").getAsInt(), config.get("Duration").getAsInt());
            int programDuration = config.get("Duration").getAsInt();
            messageBus.setUnit(config.get("TickTime").getAsInt());
            messageBus.setDuration(programDuration);
            boolean isOperate=false;

            Thread.sleep(100);
            Thread timeServiceThread = new Thread(timeService, "TimeService");
            timeServiceThread.start();
            reader.close();

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
