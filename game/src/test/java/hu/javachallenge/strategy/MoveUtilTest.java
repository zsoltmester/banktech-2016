package hu.javachallenge.strategy;

import hu.javachallenge.bean.Entity;
import hu.javachallenge.bean.MapConfiguration;
import hu.javachallenge.bean.Position;
import hu.javachallenge.bean.Submarine;
import hu.javachallenge.map.Map;
import junit.framework.TestCase;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;

/**
 * Created by qqcs on 05/11/16.
 */
public class MoveUtilTest {

    private Submarine submarine;
    private MapConfiguration mockedMapConfiguration;

    @org.junit.Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {

        Map mockedMap = Mockito.mock(Map.class);
        mockedMapConfiguration = Mockito.mock(MapConfiguration.class);

        Mockito.when(mockedMap.getConfiguration()).thenReturn(mockedMapConfiguration);

        Field map = MoveUtil.class.getDeclaredField("map");
        map.setAccessible(true);
        map.set(null, mockedMap);

        submarine = new Submarine();
    }

    @Test
    public void getAngleForTargetPosition() throws Exception {
        submarine.setPosition(new Position(0, -10.0));

        TestCase.assertEquals(90, MoveUtil.getAngleForTargetPosition(submarine, new Position(0, 0)), 0.00000001);
    }

    @org.junit.Test
    public void getTurnForTargetPositionTestAngle() throws Exception {
        submarine.setPosition(new Position(2, 2));
        submarine.setAngle(0.0);

        Mockito.when(mockedMapConfiguration.getMaxSteeringPerRound()).thenReturn(180);

        TestCase.assertEquals(45.0, MoveUtil.getTurnForTargetPosition(submarine, new Position(3, 3)), 0.00000001);
        TestCase.assertEquals(0.0, MoveUtil.getTurnForTargetPosition(submarine, new Position(3, 2)), 0.00000001);
        TestCase.assertEquals(90.0, MoveUtil.getTurnForTargetPosition(submarine, new Position(2, 3)), 0.00000001);
        TestCase.assertEquals(0.0, MoveUtil.getTurnForTargetPosition(submarine, new Position(2, 2)), 0.00000001);
        TestCase.assertEquals(-135.0, MoveUtil.getTurnForTargetPosition(submarine, new Position(1, 1)), 0.00000001);
        TestCase.assertEquals(-45.0, MoveUtil.getTurnForTargetPosition(submarine, new Position(3, 1)), 0.00000001);
    }

    @org.junit.Test
    public void getTurnForTargetPositionTestMaxSteering() throws Exception {
        submarine.setPosition(new Position(10, 10));
        submarine.setAngle(36.0);
        Mockito.when(mockedMapConfiguration.getMaxSteeringPerRound()).thenReturn(10);

        TestCase.assertEquals(-10.0, MoveUtil.getTurnForTargetPosition(submarine, new Position(3, 3)), 0.00000001);
        TestCase.assertEquals(9.0, MoveUtil.getTurnForTargetPosition(submarine, new Position(11, 11)), 0.00000001);
        TestCase.assertEquals(10.0, MoveUtil.getTurnForTargetPosition(submarine, new Position(10, 20)), 0.00000001);

        submarine.setAngle(355.0);

        TestCase.assertEquals(10.0, MoveUtil.getTurnForTargetPosition(submarine, new Position(11, 11)), 0.00000001);
    }

    @Test
    public void getPositionWhereShootTarget() throws Exception {
        Mockito.when(mockedMapConfiguration.getTorpedoSpeed()).thenReturn(2.0);

        submarine.setPosition(new Position(0, 10));

        Entity target = new Entity();
        target.setPosition(new Position(5, 0));
        target.setVelocity(1.0);
        target.setAngle(180.0);

        Position result = MoveUtil.getPositionWhereShootTarget(submarine, target);
        TestCase.assertNotNull(result);
        TestCase.assertEquals(0, result.getX(), 0.00000001);
        TestCase.assertEquals(0, result.getY(), 0.00000001);
    }
}