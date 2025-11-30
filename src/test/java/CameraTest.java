//package bgu.spl.mics.application
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.STATUS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.DetectedObject;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;


public class CameraTest {

    private Camera camera;

    @BeforeEach
    public void setUp() {
        camera = new Camera("camera tester",-1,5,new ArrayList<>());
    }

    @Test
    //test post condition= if detectedObjectsList.isEmpty() then status == STATUS.DOWN

public void testGetDetectedAtTime_EmptyListStatusDown() {
    StampedDetectedObjects obj = new StampedDetectedObjects(5, new LinkedList<DetectedObject>());
    camera.getDetectedObjects().add(obj);
    
    // Verify initial state is not DOWN
    assertNotEquals(STATUS.DOWN, camera.getStatus(), 
        "Camera status should not be DOWN initially with non-empty list");
    
    // Call method that will empty the list
    camera.getDetectedAtTime(10); // This will remove the only object
    
    // Add another call to ensure list stays empty
    camera.getDetectedAtTime(15);
    
    // Verify both post-conditions:
    // 1. List should be empty
    assertTrue(camera.getDetectedObjects().isEmpty(), 
        "List should be empty after removing the only object");
    
    // 2. Status should be DOWN when list is empty
    assertEquals(STATUS.DOWN, camera.getStatus(), 
        "Camera status should be DOWN when list becomes empty");
}


@Test
//check post condition  : if result != null then result.getTime() == time-frequency

public void testGetDetectedAtTime_ResultTimeMatchesTimeMinusFrequency() {
int requestTime = 10;
int frequency = camera.getFrequency(); 
int expectedTime = requestTime - frequency; 

// Create and add a stamped object with the expected time
StampedDetectedObjects expectedObject = new StampedDetectedObjects(
    expectedTime, 
    new LinkedList<DetectedObject>()
);
camera.getDetectedObjects().add(expectedObject);

// Execute
StampedDetectedObjects result = camera.getDetectedAtTime(requestTime);

// Verify
assertNotNull(result, "Result should not be null for matching time");
assertEquals(expectedTime, result.getTime(), 
    String.format("Expected time %d (requestTime %d - frequency %d), but got %d", 
        expectedTime, requestTime, frequency, result.getTime()));
}


}



