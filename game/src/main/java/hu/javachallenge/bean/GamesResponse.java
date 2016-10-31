package hu.javachallenge.bean;

import java.util.List;

public class GamesResponse extends StatusResponse {

    private List<Long> games;

    public List<Long> getGames() {
        return games;
    }

    public void setGames(List<Long> games) {
        this.games = games;
    }

    @Override
    public String toString() {
        return "GamesResponse{" +
                "games=" + games +
                '}';
    }
}
