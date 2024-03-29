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
        double distanceToOrigo = length();
        x /= distanceToOrigo;
        y /= distanceToOrigo;
    }

    public double length() {
        return distance(new Position(0, 0));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Position position = (Position) o;

        if (!x.equals(position.x)) return false;
        return y.equals(position.y);

    }

    @Override
    public int hashCode() {
        int result = x.hashCode();
        result = 31 * result + y.hashCode();
        return result;
    }
}
