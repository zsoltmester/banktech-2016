package hu.javachallenge.strategy;

import hu.javachallenge.bean.MapConfiguration;
import hu.javachallenge.bean.Position;
import hu.javachallenge.bean.Submarine;
import hu.javachallenge.map.Map;
import junit.framework.TestCase;
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

}