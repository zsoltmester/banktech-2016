package hu.javachallenge.bean;

import java.util.List;

public class SonarResponse extends StatusResponse {

    private List<Entity> entities;

    public List<Entity> getEntities() {
        return entities;
    }

    public void setEntities(List<Entity> entities) {
        this.entities = entities;
    }

    @Override
    public String toString() {
        return "SonarResponse{" +
                "entities=" + entities +
                "} " + super.toString();
    }
}
