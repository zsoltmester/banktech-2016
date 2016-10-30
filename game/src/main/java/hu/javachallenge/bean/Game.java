package hu.javachallenge.bean;

import java.util.Date;

public class Game {

    private Long id;
    private Integer round;
    private Scores scores;
    private ConnectionStatus connectionStatus;
    private MapConfiguration mapConfiguration;
    private String status;
    private Date createdTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getRound() {
        return round;
    }

    public void setRound(Integer round) {
        this.round = round;
    }

    public Scores getScores() {
        return scores;
    }

    public void setScores(Scores scores) {
        this.scores = scores;
    }

    public ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(ConnectionStatus connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public MapConfiguration getMapConfiguration() {
        return mapConfiguration;
    }

    public void setMapConfiguration(MapConfiguration mapConfiguration) {
        this.mapConfiguration = mapConfiguration;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    @Override
    public String toString() {
        return "Game{" +
                "id=" + id +
                ", round=" + round +
                ", scores=" + scores +
                ", connectionStatus=" + connectionStatus +
                ", mapConfiguration=" + mapConfiguration +
                ", status='" + status + '\'' +
                ", createdTime='" + createdTime + '\'' +
                '}';
    }
}
