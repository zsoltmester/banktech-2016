package hu.javachallenge.bean;

import java.util.Map;

public class Scores {

    private Map<String, Integer> scores;

    public Map<String, Integer> getScores() {
        return scores;
    }

    public void setScores(Map<String, Integer> scores) {
        this.scores = scores;
    }

    @Override
    public String toString() {
        return "Scores{" +
                "scores=" + scores +
                '}';
    }
}
