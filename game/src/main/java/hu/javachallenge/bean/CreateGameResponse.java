package hu.javachallenge.bean;

public class CreateGameResponse extends StatusResponse {

    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "CreateGameResponse{" +
                "id=" + id +
                "} " + super.toString();
    }
}
