package hu.javachallenge.bean;

public class GameResponse extends StatusResponse {

    private Game game;

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    @Override
    public String toString() {
        return "GameResponse{" +
                "game=" + game +
                '}';
    }
}
