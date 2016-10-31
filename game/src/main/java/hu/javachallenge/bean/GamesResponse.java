package hu.javachallenge.bean;

import java.util.List;

public class GamesResponse {

    private List<Long> games;
    private String message;
    private Integer code;

    public List<Long> getGames() {
        return games;
    }

    public void setGames(List<Long> games) {
        this.games = games;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "GamesResponse{" +
                "games=" + games +
                ", message='" + message + '\'' +
                ", code=" + code +
                '}';
    }
}
