package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.messages.*;

import java.util.*;

public class LiDarService extends MicroService {

    private LiDarWorkerTracker liDarWorkerTracker;
    
    private Set<String> uniqueObjectObservations = new HashSet<>();

    public LiDarService(LiDarWorkerTracker liDarWorkerTracker) {
        super("LiDarWorkerTracker" + liDarWorkerTracker.getID());
        this.liDarWorkerTracker = liDarWorkerTracker;
    }

    @Override
    protected void initialize() {
        messageBus.register(this);

        subscribeEvent(DetectObjectsEvent.class, ev -> {
            if (!validateServiceStatus(ev.getObjects())) {
                return;
            }

            processDetectedObjects(ev.getObjects());
        });

        subscribeBroadcast(TickBroadcast.class, this::handleTickBroadcast);

        setupErrorHandlingSubscriptions();

        messageBus.raiseInitCounter();
        waitForStart();
    }

    private boolean validateServiceStatus(StampedDetectedObjects objects) {
        int time = objects.getTime();
        
        // Don't check status until all objects processed
        if (time <= 2) { // Allow processing for full runtime
            return true;
        }
        
        liDarWorkerTracker.determineStatus(time);
        
        if (liDarWorkerTracker.getStatus() == STATUS.DOWN || 
            liDarWorkerTracker.checkIfError(time)) {
            processRemainingObjects();
            return false;
        }
    
        return true;
    }

    private void processDetectedObjects(StampedDetectedObjects objects) {
        int time = objects.getTime();
        List<DetectedObject> detectedObjects = objects.getDetectedObjects();

        LinkedList<TrackedObject> immediateTracking = new LinkedList<>();

        for (DetectedObject detectedObject : detectedObjects) {
            String id = detectedObject.getID();
            
            StampedCloudPoints cloudPoint = liDarWorkerTracker.getTrackedAtTime(time, id);
            
            if (cloudPoint != null && !cloudPoint.getCloudPoints().isEmpty()) {
                TrackedObject trackedObject = new TrackedObject(
                    id, 
                    time, 
                    detectedObject.getDescription(),
                    cloudPoint.getConvertedCloudPoints()
                );

                String observationKey = id + "_" + time;
                
                if (!uniqueObjectObservations.contains(observationKey)) {
                    immediateTracking.add(trackedObject);
                    uniqueObjectObservations.add(observationKey);
                    messageBus.getStatisticalFolder().raiseNumTrackedObjects();
                }
            }
        }

        if (!immediateTracking.isEmpty()) {
            TrackedObjectsEvent event = new TrackedObjectsEvent(immediateTracking);
            sendEvent(event);
        }
    }

    private void handleTickBroadcast(TickBroadcast ev) {
        int currentTime = ev.getTime();
        int frequency = liDarWorkerTracker.getFrequency();

        LinkedList<TrackedObject> objectsToSend = new LinkedList<>();
        for (TrackedObject obj : liDarWorkerTracker.getTrackedObjects()) {
            if ((currentTime - frequency) == obj.getTime()) {
                String observationKey = obj.getID() + "_" + obj.getTime();
                
                // Only track if this specific observation hasn't been seen before
                if (!uniqueObjectObservations.contains(observationKey)) {
                    objectsToSend.add(obj);
                    uniqueObjectObservations.add(observationKey);
                    messageBus.getStatisticalFolder().raiseNumTrackedObjects();
                }
            }
        }

        if (!objectsToSend.isEmpty()) {
            TrackedObjectsEvent event = new TrackedObjectsEvent(objectsToSend);
            sendEvent(event);
        }

        if (currentTime == messageBus.getDuration()) {
            liDarWorkerTracker.setStatus(STATUS.DOWN);
            sendBroadcast(new TerminatedBroadcast(getName()));
            terminate();
        }
    }

    private void setupErrorHandlingSubscriptions() {
        subscribeBroadcast(CrushedBroadcast.class, ev -> {
            liDarWorkerTracker.setStatus(STATUS.DOWN);
            sendBroadcast(new CrushedBroadcast(
                getName(), 
                "Connection to LiDar lost", 
                0
            ));
            terminate();
        });

        subscribeBroadcast(TerminatedBroadcast.class, ev -> {
            if (ev.getSender().equals("TimeService")) {
                liDarWorkerTracker.setStatus(STATUS.DOWN);
                sendBroadcast(new TerminatedBroadcast(getName()));
                terminate();
            }
        });
    }

    public Object getObject() {
        return liDarWorkerTracker;
    }

    private void processRemainingObjects() {
        // Process ALL tracked objects regardless of time
        for (TrackedObject obj : liDarWorkerTracker.getTrackedObjects()) {
            if (!uniqueObjectObservations.contains(obj.getID() + "_" + obj.getTime())) {
                TrackedObjectsEvent event = new TrackedObjectsEvent(
                    new LinkedList<>(Arrays.asList(obj))
                );
                sendEvent(event);
                uniqueObjectObservations.add(obj.getID() + "_" + obj.getTime());
                messageBus.getStatisticalFolder().raiseNumTrackedObjects();
            }
        }
    }
}