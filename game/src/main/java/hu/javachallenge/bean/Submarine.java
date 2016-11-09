package hu.javachallenge.bean;

public class Submarine extends Entity {
    private Integer hp;
    private Integer sonarCooldown;
    private Integer torpedoCooldown;
    private Integer sonarExtended;

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
                "type='" + getType() + '\'' +
                ", id=" + getId() +
                ", position=" + getPosition() +
                ", owner=" + getOwner() +
                ", velocity=" + getVelocity() +
                ", angle=" + getAngle() +
                ", hp=" + hp +
                ", sonarCooldown=" + sonarCooldown +
                ", torpedoCooldown=" + torpedoCooldown +
                ", sonarExtended=" + sonarExtended +
                '}';
    }
}
