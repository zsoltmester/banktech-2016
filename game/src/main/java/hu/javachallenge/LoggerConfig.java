package hu.javachallenge;

import java.util.logging.*;

public class LoggerConfig {

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
        logger.addHandler(consoleHandler);
    }
}
