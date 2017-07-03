package entity.Logger;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by Irina on 18.02.2017.
 * Support Logger for Log4J
 */
public interface LoggableI {
    Logger getLog();

    static aspect Impl pertypewithin(LoggableI) {
        private static Logger logger;

        after(): staticinitialization(*) {
            logger = LogManager.getLogger(getWithinTypeName());
        }
        public Logger LoggableI.getLog() {
            return logger;
        }
    }
}
