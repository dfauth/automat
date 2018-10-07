package automat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static java.time.Instant.now;


public class HeartbeatContext {

    private static final Logger logger = LogManager.getLogger(HeartbeatContext.class);

    private Optional<Heartbeat> last = Optional.empty();
    private CompletableFuture<Heartbeat> pending = new CompletableFuture<>();
    private Duration heartbeatInterval;

    public HeartbeatContext(Duration heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public void heartbeat() {
        Heartbeat heartbeat = new Heartbeat();
        last = Optional.of(heartbeat);
        logger.info("heartbeat received: "+heartbeat);
        CompletableFuture<Heartbeat> tmp = pending;
        pending = new CompletableFuture<>();
        tmp.complete(heartbeat);
    }

    public Optional<Heartbeat> last() {
        return last;
    }

    public CompletableFuture<Heartbeat> next() {
        return pending;
    }

    public boolean isAlive() {
        return last().map(h -> {
            return h.elapsedSince().compareTo(heartbeatInterval.multipliedBy(2)) < 0;
        }).orElseThrow(()->new IllegalStateException("No heartbeats received"));
    }

    public Timer onOverdue(Consumer<OverdueHeartbeatEvent> consumer) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(!isAlive()) consumer.accept(new OverdueHeartbeatEvent(last));
            }
        }, heartbeatInterval.toMillis()*2, heartbeatInterval.toMillis());
        return timer;
    }

    public static class Heartbeat {

        private Instant timeStamp = now();

        public Duration elapsedSince() {
            return Duration.between(timeStamp, now());
        }
    }

    public static class OverdueHeartbeatEvent {

        private final Optional<Heartbeat> last;

        public OverdueHeartbeatEvent(Optional<Heartbeat> last) {
            this.last = last;
        }

        public Optional<Heartbeat> last() {
            return last;
        }
    }
}
