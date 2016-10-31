package hu.javachallenge.bean;

public class Entity {

    private String type;
    private Long id;
    private Position position;
    private Owner owner;
    private Integer velocity;
    private Integer angle;
    private Integer roundsMoved;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public Integer getVelocity() {
        return velocity;
    }

    public void setVelocity(Integer velocity) {
        this.velocity = velocity;
    }

    public Integer getAngle() {
        return angle;
    }

    public void setAngle(Integer angle) {
        this.angle = angle;
    }

    public Integer getRoundsMoved() {
        return roundsMoved;
    }

    public void setRoundsMoved(Integer roundsMoved) {
        this.roundsMoved = roundsMoved;
    }

    @Override
    public String toString() {
        return "Entity{" +
                "type='" + type + '\'' +
                ", id=" + id +
                ", position=" + position +
                ", owner=" + owner +
                ", velocity=" + velocity +
                ", angle=" + angle +
                ", roundsMoved=" + roundsMoved +
                '}';
    }
}
