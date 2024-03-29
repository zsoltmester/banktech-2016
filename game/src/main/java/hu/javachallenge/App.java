package hu.javachallenge;

import hu.javachallenge.strategy.NewAwesomeStrategy;
import hu.javachallenge.strategy.Player;

import java.util.logging.Logger;

public class App {

    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    public static String serverAddress;

    public static void main(String[] args) {

        // load the logger configuration
        try {
            Class.forName(LoggerConfig.class.getName());
        } catch (ClassNotFoundException e) {
            LOGGER.warning("Cannot load logger configuration. Default configuration will be used.");
            e.printStackTrace();
        }

        // query the server address
        if (args.length == 0) {
            serverAddress = "192.168.88.5:8080";
            LOGGER.warning("No server address given, using the default: " + serverAddress);
        } else {
            serverAddress = args[0];
            LOGGER.fine("Server started on:" + serverAddress);
        }

        Player.play(new NewAwesomeStrategy());
    }
}
