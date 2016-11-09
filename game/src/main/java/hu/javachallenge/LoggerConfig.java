package hu.javachallenge;

import java.util.logging.*;

public class LoggerConfig {

    private static final Level LOG_LEVEL = Level.FINEST;

    static {
        Logger logger = Logger.getLogger(App.class.getPackage().getName());

        logger.setUseParentHandlers(false);

        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new Formatter() {
            public String format(LogRecord record) {
                return "[" + record.getLevel() + "] "
                        + record.getSourceClassName().substring(record.getSourceClassName().lastIndexOf('.') + 1) + "."
                        + record.getSourceMethodName() + " - "
                        + record.getMessage() + "\n";
            }
        });
        consoleHandler.setLevel(LOG_LEVEL);
        logger.addHandler(consoleHandler);
        logger.setLevel(LOG_LEVEL);
    }
}
