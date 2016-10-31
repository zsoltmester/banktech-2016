package hu.javachallenge.bean;

import java.util.List;

public class SubmarinesResponse extends StatusResponse {

    private List<Submarine> submarines;

    public List<Submarine> getSubmarines() {
        return submarines;
    }

    public void setSubmarines(List<Submarine> submarines) {
        this.submarines = submarines;
    }

    @Override
    public String toString() {
        return "SubmarinesResponse{" +
                "submarines=" + submarines +
                "} " + super.toString();
    }
}
