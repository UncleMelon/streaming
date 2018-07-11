package com.niuwa.streaming.logging.log4j;


import com.niuwa.streaming.logging.CSLogger;
import com.niuwa.streaming.logging.CSLoggerFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.net.SyslogAppender;

/**
 * BlogInfo: william
 * Date: 11-9-1
 * Time: 下午3:44
 */
public class Log4jFactory extends CSLoggerFactory {
    @Override
    protected CSLogger newInstance(String prefix, String name) {
        final Logger logger = Logger.getLogger(name);
        return new Log4jCSLogger(prefix, logger);
    }

    private static class HADOOLevel extends Level {

        protected HADOOLevel(int level, String levelStr, int syslogEquivalent) {
            super(level, levelStr, syslogEquivalent);
        }
    }

    public interface CSLogLevel {
        public static final Level HADOO_LEVEL = new HADOOLevel(Priority.DEBUG_INT - 1, "HADOO", SyslogAppender.LOG_LOCAL0);
    }

}
