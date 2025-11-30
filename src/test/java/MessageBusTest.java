import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.services.CameraService;
import bgu.spl.mics.application.services.FusionSlamService;
import bgu.spl.mics.Event;
import bgu.spl.mics.Message;
import bgu.spl.mics.Broadcast;
import bgu.spl.mics.application.objects.FusionSlam;


public class MessageBusTest {
    private MessageBusImpl messageBus;
    private MicroService mockMicroService;

    @BeforeEach
    void setUp() {
            Gson gson = new Gson();
            String configFilePath = System.getProperty("user.dir") + "/example_input_2/configuration_file.json";
            try{
                Reader reader = new FileReader(configFilePath);
                File file = new File (configFilePath);
                String folderName = file.getParent();  
                messageBus = MessageBusImpl.getInstance();
                mockMicroService =  new FusionSlamService(FusionSlam.getInstance(), 0, 0,folderName);    
                assertNotNull(messageBus, "MessageBus should not be null"); // Ensure messageBus is initialized
                assertNotNull(mockMicroService, "MicroService should not be null"); // Ensure mockMicroService is initialized        
            }
        catch (FileNotFoundException e) {
            // Handle the case where the file does not exist
            System.err.println("Error: The file was not found: " + configFilePath);}
        }
    

    @Test
    void testRegister() {
        // Test pre-condition: m != null
        assertThrows(NullPointerException.class, () -> {
            messageBus.register(null);
        }, "Should throw exception when registering null microservice");

        // Test post-conditions
        messageBus.register(mockMicroService);
        
        // Verify queue was allocated
        assertNotNull(messageBus.getQueues().get(mockMicroService), 
            "Microservice should have an allocated queue");
        
        // Verify queue is empty
        assertTrue(messageBus.isQueueEmpty(mockMicroService), 
            "Newly allocated queue should be empty");

        // Cleanup
        messageBus.unregister(mockMicroService);
    }

    @Test
    void testSubscribeEvent() {
        // Register microservice first
        messageBus.register(mockMicroService);

        // Create test event type
        Class<? extends Event<String>> testEventType = new Event<String>(){}.getClass();

        // Test pre-conditions
        assertThrows(NullPointerException.class, () -> {
            messageBus.subscribeEvent(null, mockMicroService);
        }, "Should throw exception when event type is null");

        assertThrows(NullPointerException.class, () -> {
            messageBus.subscribeEvent(testEventType, null);
        }, "Should throw exception when microservice is null");

        // Test post-condition
        messageBus.subscribeEvent(testEventType, mockMicroService);
        
        assertTrue(messageBus.getMessages().get(testEventType).contains(mockMicroService),
            "Microservice should be subscribed to event type");

        // Cleanup
        messageBus.unregister(mockMicroService);
    }

    @Test
    void testAwaitMessage() {
        // Test pre-condition: microservice must be registered
        assertThrows(IllegalStateException.class, () -> {
            messageBus.awaitMessage(mockMicroService);
        }, "Should throw exception when microservice is not registered");

        // Register microservice
        messageBus.register(mockMicroService);

        // Test null microservice
        assertThrows(NullPointerException.class, () -> {
            messageBus.awaitMessage(null);
        }, "Should throw exception when microservice is null");

        // Test post-condition: message removal
        Broadcast testBroadcast = new Broadcast() {};
        messageBus.subscribeBroadcast(testBroadcast.getClass(), mockMicroService);
        messageBus.sendBroadcast(testBroadcast);

        try {
            Message message = messageBus.awaitMessage(mockMicroService);
            assertNotNull(message, "Should receive message");
            assertTrue(messageBus.isQueueEmpty(mockMicroService), 
                "Message should be removed from queue after awaiting");
        } catch (InterruptedException e) {
            fail("Should not throw InterruptedException");
        }

        // Cleanup
        messageBus.unregister(mockMicroService);
    }
}