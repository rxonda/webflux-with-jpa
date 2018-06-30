import ch.qos.logback.classic.Level

appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{HH:mm:ss.SSS} [%-5level] [%t] %logger{36} - %msg%n"
    }
}

root(Level.DEBUG, ["CONSOLE"])

logger('com.rxonda', Level.DEBUG)
logger('reactor.ipc.netty', Level.ERROR)
logger('io.netty', Level.ERROR)
logger('org.springframework', Level.ERROR)
logger('org.hibernate', Level.ERROR)
logger('com.zaxxer.hikari', Level.ERROR)