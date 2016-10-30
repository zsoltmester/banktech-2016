package hu.javachallenge.bean;

import java.util.List;

public class GameListResponse {

    private List<Integer> games;
    private String message;
    private Integer code;

    public List<Integer> getGames() {
        return games;
    }

    public void setGames(List<Integer> games) {
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
}
