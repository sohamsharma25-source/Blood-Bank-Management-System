package bloodbank.util;

import java.util.concurrent.atomic.AtomicLong;

public class IdGenerator {
    private final String prefix;
    private final AtomicLong counter;

    public IdGenerator(String prefix, long start) {
        this.prefix = prefix;
        counter = new AtomicLong(start);
    }

    public String next() {
        return prefix + String.format("%04d", counter.getAndIncrement());
    }
}