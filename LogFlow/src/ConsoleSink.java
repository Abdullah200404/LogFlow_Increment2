public class ConsoleSink implements Sink<Object> {

    @Override
    public void consume(Object item) {
        if (item instanceof LogRecord record) {
            System.out.printf("[%s] %s %s %s -> Status: %d (%d bytes)%n",
                    record.timestamp(),
                    record.clientIp(),
                    record.method(),
                    record.path(),
                    record.status(),
                    record.bytes()
            );
        } else if (item != null) {
            System.out.println(item);
        }
    }
}