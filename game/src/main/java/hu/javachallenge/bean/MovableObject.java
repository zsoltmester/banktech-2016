package hu.javachallenge.bean;

/**
 * Created by qqcs on 07/11/16.
 */
public abstract class MovableObject implements Cloneable {
    public abstract Position getPosition();
    public abstract Double getVelocity();
    public abstract Double getAngle();

    public abstract void setPosition(Position position);
    public abstract void setVelocity(Double velocity);
    public abstract void setAngle(Double angle);

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
