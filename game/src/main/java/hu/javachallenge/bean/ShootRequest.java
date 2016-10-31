package hu.javachallenge.bean;

public class ShootRequest {

    private Double angle;

    public ShootRequest(Double angle) {
        this.angle = angle;
    }

    public Double getAngle() {
        return angle;
    }

    public void setAngle(Double angle) {
        this.angle = angle;
    }

    @Override
    public String toString() {
        return "ShootRequest{" +
                "angle=" + angle +
                '}';
    }
}
