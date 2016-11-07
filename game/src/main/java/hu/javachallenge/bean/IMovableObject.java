package hu.javachallenge.bean;

/**
 * Created by qqcs on 07/11/16.
 */
public interface IMovableObject {
    Position getPosition();
    Double getVelocity();
    Double getAngle();

    void setPosition(Position position);
    void setVelocity(Double velocity);
    void setAngle(Double angle);
}
