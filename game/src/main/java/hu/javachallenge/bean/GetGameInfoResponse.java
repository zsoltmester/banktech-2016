package hu.javachallenge.bean;

public class GetGameInfoResponse {

    private Game game;
    private String message;
    private Integer code;

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
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
        return "GetGameInfoResponse{" +
                "game=" + game +
                ", message='" + message + '\'' +
                ", code=" + code +
                '}';
    }
}
