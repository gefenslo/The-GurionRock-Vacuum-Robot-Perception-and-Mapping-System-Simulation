import bgu.spl.mics.application.services.FusionSlamService;
import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.Pose;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;


public class FusionSlamTest {

    @Test
    public void testTransformToGlobal_1() {
/*      there are 2 pre conditions:
        a list of the liDar cloud points for this object is defined, not null or empty.
        a pose object - the location of the robot at the time of object detection- is defined, not epty or null.*/

        List<CloudPoint> points = new ArrayList<>();
        points.add(new CloudPoint(1, 1));
        points.add(new CloudPoint(2, 2));
        Pose pose = new Pose(10, 20, 0, 0); 

        FusionSlamService.transformToGlobal(points, pose);

        /*there is 1 post condition:
         each cloud point in the "points" list is translated to global coordinate system of the charging station.
         */

        assertEquals(11, points.get(0).getX(), 0.01);
        assertEquals(21, points.get(0).getY(), 0.01);
        assertEquals(12, points.get(1).getX(), 0.01);
        assertEquals(22, points.get(1).getY(), 0.01);
    }

    @Test
    public void testTransformToGlobal_2() {
        /*
        there are 2 pre conditions:
        a list of the liDar cloud points for this object is defined, not null or empty.
        a pose object - the location of the robot at the time of object detection- is defined, not epty or null.
        yaw is equal to 90 degrees, which means the robot has rotated against clock wise by 90 degrees.*/

        List<CloudPoint> points = new ArrayList<>();
        points.add(new CloudPoint(1, 0));
        points.add(new CloudPoint(0, 1));
        Pose pose = new Pose(0, 0, 90, 0); 

        FusionSlamService.transformToGlobal(points, pose);

        /* there is one post conditon:
        each cloud point in the "points" list is translated to global coordinate system of the charging station,
        and is rotated by 90 degrees.
*/

        assertEquals(0, points.get(0).getX(), 0.01);
        assertEquals(1, points.get(0).getY(), 0.01);
        assertEquals(-1, points.get(1).getX(), 0.01);
        assertEquals(0, points.get(1).getY(), 0.01);
    }
}

