package bgu.spl.mics.application.services;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrushedBroadcast;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.STATUS;

/**
 * TimeService acts as the global timer for the system, broadcasting TickBroadcast messages
 * at regular intervals and controlling the simulation's duration.
 */
public class TimeService extends MicroService {

    /**
     * Constructor for TimeService.
     *
     * @param TickTime  The duration of each tick in milliseconds.
     * @param Duration  The total number of ticks before the service terminates.
     */
    private final int TickTime;
    private final int Duration;
    private int tickCount = 0;
    private ScheduledExecutorService executor;

    public TimeService(int TickTime, int Duration) {
        super("TimeService");
        this.TickTime=TickTime;
        this.Duration=Duration;
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Initializes the TimeService.
     * Starts broadcasting TickBroadcast messages and terminates after the specified duration.
     */
    @Override
    protected void initialize() {
        messageBus.register(this);
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if(tickCount==0){
                    synchronized (lock) {
                        lock.notifyAll(); }
                }
                if (tickCount < Duration) {
                    sendBroadcast(new TickBroadcast(tickCount));
                    tickCount++;
                    messageBus.getStatisticalFolder().raiseTime();
                } else {
                    sendBroadcast(new TickBroadcast(tickCount));
                    //messageBus.getStatisticalFolder().raiseTime();
                    sendBroadcast(new TerminatedBroadcast("TimeService"));
                    executor.shutdown();
                    terminate();
                }
            }
        }, 0, TickTime, TimeUnit.SECONDS);  
    }

    public Object getObject(){
        return null;
    }


}
