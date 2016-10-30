package hu.javachallenge.bean;

public class CreateGameResponse {

    private Long id;
    private String message;
    private Integer code;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        return "CreateGameResponse{" +
                "id=" + id +
                ", message='" + message + '\'' +
                ", code=" + code +
                '}';
    }
}
