package hu.javachallenge.bean;

public class Position {

    private Double x;
    private Double y;

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public Position() {
    }

    public Position(Number x, Number y) {
        this.x = x.doubleValue();
        this.y = y.doubleValue();
    }

    public double distance(Position other) {
        return Math.sqrt((other.getX() - getX()) * (other.getX() - getX()) +
                (other.getY() - getY()) * (other.getY() - getY()));
    }

    @Override
    public String toString() {
        return "Position{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    public void translate(Position deltaPosition) {
        x += deltaPosition.getX();
        y += deltaPosition.getY();
    }

    public void normalize() {
        double distanceToOrigo = distance(new Position(0, 0));
        x /= distanceToOrigo;
        y /= distanceToOrigo;
    }
}
