package bgu.spl.mics;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Queue;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import bgu.spl.mics.application.objects.StatisticalFolder;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

	private static class SingletoneHolder{
		private static MessageBusImpl instance = new MessageBusImpl();
	}

	private ConcurrentHashMap<Class<? extends Message>,Queue> messages;
	private ConcurrentHashMap<MicroService, Queue<Message>> queues;
	private ConcurrentHashMap<MicroService, ConcurrentHashMap<Class<? extends Message>, Callback>> subscriptions;
	private ConcurrentHashMap< Event,Future> futures;
	private int unit=0;
	private StatisticalFolder statisticalFolder;
	private AtomicInteger initCounter=new AtomicInteger(0);
	private Future future =new Future();
	int programDuration;
	

		// Private constructor to prevent instantiation
		private MessageBusImpl() {
			messages=new ConcurrentHashMap<>();
			queues= new ConcurrentHashMap<>();
			subscriptions=new ConcurrentHashMap<>();
			futures=new ConcurrentHashMap<>();
			statisticalFolder=new StatisticalFolder();
		}
		public static MessageBusImpl getInstance(){
			return SingletoneHolder.instance;
		}
		

		// Other methods for sending and receiving messages
	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		if (m== null|| type==null){
		throw new NullPointerException();
		}
		messages.computeIfAbsent(type, k -> new LinkedList<>());
		synchronized (messages.get(type)) {
		messages.get(type).add(m);

			}
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		messages.computeIfAbsent(type, k -> new LinkedList<>());
		synchronized (messages.get(type)) {
		messages.get(type).add(m);

	}
	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		Future<T> future=futures.get(e);
		future.resolve(result);
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		synchronized (messages.get(b.getClass())){
			Queue<MicroService> msQueue=messages.get(b.getClass());
			if (!msQueue.isEmpty()){
				for(MicroService ms:msQueue){
					if (queues.get(ms)!=null){
						synchronized(queues.get(ms)){
							if ((queues.get(ms)!=null)){
							queues.get(ms).add(b);
							}
						}
					}
				}
			}
		}
	}



	@Override
	public <T> Future<T> sendEvent(Event<T> e) {

		synchronized (messages.get(e.getClass())) {
			Queue<MicroService> msQueue=messages.get(e.getClass());
			if (msQueue != null && !msQueue.isEmpty()) {
				MicroService ms = msQueue.remove();
				messages.get(e.getClass()).add(ms);
				if (queues.get(ms) != null) {
					synchronized (queues.get(ms)) {
						if (queues.get(ms) != null){
							queues.get(ms).add(e);
						}
					}
				}
			}
			
		}

		return future;
	}

	@Override
	public void register(MicroService m) {
		queues.put(m, new LinkedList<>());
	}

	@Override
	public void unregister(MicroService m) {

		synchronized(queues.get(m)){
			queues.remove(m); 
		}
		for (Map.Entry<Class<? extends Message>, Queue> entry : messages.entrySet()) {
   	 		Queue queue = entry.getValue(); 
    		synchronized (queue) {
        	queue.remove(m); 
    		}
	}
}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		if (queues.get(m)== null){
			throw new IllegalStateException();
		}
		synchronized (queues.get(m)){
			Queue<Message> queue=queues.get(m);
			if(!queue.isEmpty()){
				Message message =queue.remove();
				return message;
			}	
			else{
				return null;
			}
		}		
	}

	public void addMessage(MicroService ms, Class<? extends Message> message, Callback callback){
		subscriptions.computeIfAbsent(ms, k -> new ConcurrentHashMap<>());
		subscriptions.get(ms).put(message,callback);
	}
	public ConcurrentHashMap<MicroService, ConcurrentHashMap<Class<? extends Message>, Callback>> getSubscriptions(){
		return subscriptions;
	}

	public void setUnit(int unit){
		this.unit=unit;
	}

	public int getUnit(){
		return unit;
	}

	public Queue getQueue(MicroService ms){
		synchronized (queues.get(ms)) {
			return queues.get(ms);
		}			
	}

	public ConcurrentHashMap< Event,Future> getFutures(){
		return futures;
	}
	public void setFuture(Event event, Future future){
		futures.put(event,future);
	}

	public boolean isQueueEmpty(MicroService ms){
		return this.getQueue(ms).isEmpty();
	}
	public StatisticalFolder getStatisticalFolder(){
			return statisticalFolder;	
	}

	public  ConcurrentHashMap<MicroService, Queue<Message>> getQueues(){
		return queues;
	}

	public void raiseInitCounter(){
		initCounter.incrementAndGet();
	}
	public int getInitCounter(){
		return initCounter.get();
	}
	public Future getFuture(){
		return future;
	}

	public void setDuration (int programDuration){
		this.programDuration= programDuration;
	}

	public int getDuration() {
		return programDuration;
	}
	public ConcurrentHashMap<Class<? extends Message>,Queue> getMessages(){
	return messages;
	}
}