package hu.javachallenge.bean;

import java.util.Map;

public class ConnectionStatus {

    private Map<String, Boolean> connected;

    public Map<String, Boolean> getConnected() {
        return connected;
    }

    public void setConnected(Map<String, Boolean> connected) {
        this.connected = connected;
    }

    @Override
    public String toString() {
        return "ConnectionStatus{" +
                "connected=" + connected +
                '}';
    }
}
