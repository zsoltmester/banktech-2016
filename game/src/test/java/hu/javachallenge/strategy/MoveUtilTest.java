package hu.javachallenge.strategy;

import hu.javachallenge.bean.Entity;
import hu.javachallenge.bean.MapConfiguration;
import hu.javachallenge.bean.Position;
import hu.javachallenge.bean.Submarine;
import hu.javachallenge.map.IMap;
import hu.javachallenge.strategy.moving.MovingIsland;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;

/**
 * Created by qqcs on 05/11/16.
 */
public class MoveUtilTest {

    private Submarine submarine;
    private MapConfiguration mockedMapConfiguration;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {

        IMap mockedMap = Mockito.mock(IMap.class);
        mockedMapConfiguration = Mockito.mock(MapConfiguration.class);

        Mockito.when(mockedMap.getConfiguration()).thenReturn(mockedMapConfiguration);

        Field map = MoveUtil.class.getDeclaredField("map");
        map.setAccessible(true);
        map.set(null, mockedMap);

        submarine = new Submarine();
    }

    @Test
    public void getAngleForTargetPosition() throws Exception {
        submarine.setPosition(new Position(-123, 0));
        TestCase.assertEquals(0, MoveUtil.getAngleForTargetPosition(submarine, new Position(0, 0)), 0.00000001);

        submarine.setPosition(new Position(-5, -5));
        TestCase.assertEquals(45, MoveUtil.getAngleForTargetPosition(submarine, new Position(0, 0)), 0.00000001);

        submarine.setPosition(new Position(0, -5));
        TestCase.assertEquals(90, MoveUtil.getAngleForTargetPosition(submarine, new Position(0, 0)), 0.00000001);

        submarine.setPosition(new Position(5, -5));
        TestCase.assertEquals(90, MoveUtil.getAngleForTargetPosition(submarine, new Position(5, 0)), 0.00000001);

        submarine.setPosition(new Position(-5, -5));
        TestCase.assertEquals(45, MoveUtil.getAngleForTargetPosition(submarine, new Position(0, 0)), 0.00000001);
    }

    @Test
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

    @Test
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

    @Test
    public void getAccelerationToCloseThere() throws Exception {
        Mockito.when(mockedMapConfiguration.getMaxAccelerationPerRound()).thenReturn(5);
        Mockito.when(mockedMapConfiguration.getMaxSpeed()).thenReturn(20);

        submarine.setPosition(new Position(0, 50));
        submarine.setVelocity(20.0);

        TestCase.assertEquals(-mockedMapConfiguration.getMaxAccelerationPerRound(), MoveUtil.getAccelerationToCloseThere(submarine, new Position(0, 23)), 0.00000001);
        TestCase.assertEquals(-mockedMapConfiguration.getMaxAccelerationPerRound(), MoveUtil.getAccelerationToCloseThere(submarine, new Position(0, 20)), 0.00000001);

        TestCase.assertEquals(0.0, MoveUtil.getAccelerationToCloseThere(submarine, new Position(0, 0)), 0.00000001);

        submarine.setPosition(new Position(0, 1));
        submarine.setVelocity(0.0);
        TestCase.assertEquals(0.0, MoveUtil.getAccelerationToCloseThere(submarine, new Position(0, 0)), 0.00000001);

        // test of speed up, and slow down to the closest of the destination
        submarine.setVelocity(0.0);
        submarine.setPosition(new Position(0, 0.1395154));
        Position to = new Position(0, -212.1005428);

        boolean reachedPoint = false;
        for (int i = 0; i < 15; ++i) {
            double acceleration = MoveUtil.getAccelerationToCloseThere(submarine, to);
            submarine.setVelocity(submarine.getVelocity() + acceleration);
            submarine.getPosition().setY(submarine.getPosition().getY() - submarine.getVelocity());

            if (submarine.getPosition().distance(to) < (mockedMapConfiguration.getMaxAccelerationPerRound() / 2f) && submarine.getVelocity() == 0.0) {
                reachedPoint = true;
                break;
            }
        }
        TestCase.assertTrue(reachedPoint);
    }

    @Test
    public void stepForward() throws Exception {
        Position squareSteps = MoveUtil.stepForward(new Position(0, 0), 0.0, 0.0, 90.0, 1.0, 4).getLast();
        TestCase.assertEquals(2.0, squareSteps.getX(), 0.00000001);
        TestCase.assertEquals(-2.0, squareSteps.getY(), 0.00000001);

        Position linear = MoveUtil.stepForward(new Position(0, 0), 45.0, Math.sqrt(2), 0.0, 0.0, 3).getLast();
        TestCase.assertEquals(3.0, linear.getX(), 0.00000001);
        TestCase.assertEquals(3.0, linear.getY(), 0.00000001);
    }

    @Test
    public void getAccelerationToCloseThereWhenOnRightDirection() throws Exception {
        Mockito.when(mockedMapConfiguration.getMaxAccelerationPerRound()).thenReturn(5);
        Mockito.when(mockedMapConfiguration.getMaxSpeed()).thenReturn(20);

        // TODO
    }

    @Test
    public void evadeThis() throws Exception {
        Mockito.when(mockedMapConfiguration.getSubmarineSize()).thenReturn(1);

        submarine.setPosition(new Position(0, 0));

        Position position = MoveUtil.evadeThis(submarine, new Position(10, 0), new MovingIsland(new Position(5, 0)), 1);

        System.out.println(position);
        TestCase.assertEquals(5.0, position.getX(), 0.00000001);
        TestCase.assertEquals(3.0, Math.abs(position.getY()), 0.00000001);

        position = MoveUtil.evadeThis(submarine, new Position(10, 0), new MovingIsland(new Position(2, 1)), 3);

        System.out.println(position);
        TestCase.assertEquals(2.0, position.getX(), 0.00000001);
        TestCase.assertEquals(-4.0, position.getY(), 0.00000001);
    }
}