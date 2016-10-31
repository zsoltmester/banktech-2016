package hu.javachallenge.bean;

public class MoveRequest {

    private Double speed;
    private Double turn;

    public MoveRequest(Double speed, Double turn) {
        this.speed = speed;
        this.turn = turn;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Double getTurn() {
        return turn;
    }

    public void setTurn(Double turn) {
        this.turn = turn;
    }

    @Override
    public String toString() {
        return "MoveRequest{" +
                "speed=" + speed +
                ", turn=" + turn +
                '}';
    }
}
