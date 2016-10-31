package hu.javachallenge.bean;

public class Submarine {

    private String type;
    private Long id;
    private Position position;
    private Owner owner;
    private Integer velocity;
    private Integer angle;
    private Integer hp;
    private Integer sonarCooldown;
    private Integer torpedoCooldown;
    private Integer sonarExtended;

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

    public Integer getHp() {
        return hp;
    }

    public void setHp(Integer hp) {
        this.hp = hp;
    }

    public Integer getSonarCooldown() {
        return sonarCooldown;
    }

    public void setSonarCooldown(Integer sonarCooldown) {
        this.sonarCooldown = sonarCooldown;
    }

    public Integer getTorpedoCooldown() {
        return torpedoCooldown;
    }

    public void setTorpedoCooldown(Integer torpedoCooldown) {
        this.torpedoCooldown = torpedoCooldown;
    }

    public Integer getSonarExtended() {
        return sonarExtended;
    }

    public void setSonarExtended(Integer sonarExtended) {
        this.sonarExtended = sonarExtended;
    }

    @Override
    public String toString() {
        return "Submarine{" +
                "type='" + type + '\'' +
                ", id=" + id +
                ", position=" + position +
                ", owner=" + owner +
                ", velocity=" + velocity +
                ", angle=" + angle +
                ", hp=" + hp +
                ", sonarCooldown=" + sonarCooldown +
                ", torpedoCooldown=" + torpedoCooldown +
                ", sonarExtended=" + sonarExtended +
                '}';
    }
}
