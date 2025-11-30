package bgu.spl.mics.application.objects;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Holds statistical information about the system's operation.
 * This class aggregates metrics such as the runtime of the system,
 * the number of objects detected and tracked, and the number of landmarks identified.
 */
public class StatisticalFolder {
private AtomicInteger systemRuntime;
private AtomicInteger numDetectedObjects;
private AtomicInteger numTrackedObjects;
private AtomicInteger numLandmarks;




public StatisticalFolder(){
    this.systemRuntime=new AtomicInteger(0);
    this.numDetectedObjects=new AtomicInteger(0);
    this.numTrackedObjects=new AtomicInteger(0);
    this.numLandmarks=new AtomicInteger(0);

}

public void raiseTime(){
        systemRuntime.incrementAndGet();
    
}

public void raiseNumDetectedObjects(){
    numDetectedObjects.incrementAndGet();
}
public void raiseNumTrackedObjects(){
    numTrackedObjects.incrementAndGet();
}
public void raiseNumLandmarks(){
    numLandmarks.incrementAndGet();
}

public AtomicInteger getSystemRuntime() {
    return systemRuntime;
    //return result;
}
public AtomicInteger getNumDetectedObjects() {
    return numDetectedObjects;
}
public AtomicInteger getNumTrackedObjects() {
    return numTrackedObjects;
}
public AtomicInteger getNumLandmarks() {
    return numLandmarks;
}

public void setSystemRuntime(){
    
}

}
