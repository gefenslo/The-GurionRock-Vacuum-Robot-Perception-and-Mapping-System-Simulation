package bgu.spl.mics.application.objects;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class LiDarWorkerTracker {
    private static LinkedList<TrackedObject> trackedObjects = new LinkedList<>();
    private static Set<String> trackedObjectIds = new HashSet<>();
    private static int frequency;
    private final int id;
    private STATUS status;
    private LiDarDataBase dataBase = LiDarDataBase.getInstance();

    public LiDarWorkerTracker(int id, int frequency) {
        this.id = id;
        this.frequency = frequency;
        status = STATUS.UP;
    }

    public int getID() {
        return id;
    }

    public static int getFrequency() {
        return frequency;
    }

    public static void setFrequency(int freq) {
        frequency = freq;
    }

    public STATUS getStatus() {
        return status;
    }

    public  void setStatus(STATUS status) {
        this.status = status;
        }

    public void setStatusInstance(STATUS status) {
        this.status = status;
    }

    public static void addTrackedObject(TrackedObject obj) {
        String objectId = obj.getID() + "_" + obj.getTime();
        if (!trackedObjectIds.contains(objectId)) {
            trackedObjects.add(obj);
            trackedObjectIds.add(objectId);
        }
    }

    public static LinkedList<TrackedObject> getTrackedObjects() {
        return new LinkedList<>(trackedObjects);
    }

    public LiDarDataBase getDataBase() {
        return dataBase;
    }

    public StampedCloudPoints getTrackedAtTime(double time, String id) {
        if (this.dataBase == null || this.dataBase.getCloudPoints() == null || this.dataBase.getCloudPoints().isEmpty()) {
            return null;
        }

        ArrayList<StampedCloudPoints> cloudPointsCopy = new ArrayList<>(this.dataBase.getCloudPoints());

        for (StampedCloudPoints current : cloudPointsCopy) {
            if (current.getTime() == time && current.getID().equals(id)) {
                if (current.getCloudPoints() == null) {
                    return null;
                }

                return current;
            }
        }
        return null;
    }

    public void determineStatus(int time) {
        if (dataBase.getCloudPoints().isEmpty()) {
            setStatusInstance(STATUS.DOWN);
            return;
        }
    }

    public boolean checkIfError(int time) {
        for (StampedCloudPoints point : dataBase.getCloudPoints()) {
            if (point.getTime() == time && point.getID().equals("ERROR")) {
                this.status = STATUS.ERROR;
                return true;
            }
        }
        return false;
    }
}