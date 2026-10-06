import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserStage implements Stage {

    private int totalLines = 0;
    private int errorLines = 0;

    // Common Log Format + User-Agent
    private static final Pattern CLF_PATTERN = Pattern.compile(
            "^(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+\\[([^]]+)]\\s+\"([^\"]*)\"\\s+(\\d{3})\\s+(\\S+)(?:\\s+\"([^\"]*)\"\\s+\"([^\"]*)\")?.*$"
    );

    @Override
    public void process(String rawLine, Emitter emitter) throws Exception {

        totalLines++;

        // Empty / null line
        if (rawLine == null || rawLine.trim().isEmpty()) {
            errorLines++;
            return;
        }

        try {
            Matcher matcher = CLF_PATTERN.matcher(rawLine.trim());

            if (matcher.matches()) {

                String clientIp = matcher.group(1);
                String timestampText = matcher.group(4);
                String request = matcher.group(5);
                int status = Integer.parseInt(matcher.group(6));

                long bytes = 0L;
                if (!matcher.group(7).equals("-")) {
                    bytes = Long.parseLong(matcher.group(7));
                }

                String userAgent = matcher.group(9);

                if (userAgent == null || userAgent.isBlank()) {
                    userAgent = "-";
                }

                // GET /login HTTP/1.1
                String[] requestParts = request.split("\\s+", 3);

                if (requestParts.length < 2) {
                    errorLines++;
                    return;
                }

                String method = requestParts[0];
                String path = requestParts[1];

                Instant timestamp = parseTimestamp(timestampText);

                LogRecord record = new LogRecord(
                        timestamp,
                        clientIp,
                        method,
                        path,
                        status,
                        bytes,
                        userAgent,
                        Map.of(),
                        rawLine
                );

                emitter.emit(record);
                return;
            }

            /*
             * دعم الـ format البسيط الموجود في مشروعك الحالي:
             *
             * IP METHOD PATH STATUS
             *
             * مثال:
             * 192.168.1.10 GET /login 200
             */
            String[] parts = rawLine.trim().split("\\s+");

            if (parts.length >= 4) {

                String clientIp = parts[0];
                String method = parts[1];
                String path = parts[2];
                int status = Integer.parseInt(parts[3]);

                LogRecord record = new LogRecord(
                        Instant.now(),
                        clientIp,
                        method,
                        path,
                        status,
                        0L,
                        "-",
                        Map.of(),
                        rawLine
                );

                emitter.emit(record);
                return;
            }

            errorLines++;

        } catch (NumberFormatException | DateTimeParseException e) {
            errorLines++;
        }
    }

    private Instant parseTimestamp(String timestampText) {

        // ISO timestamp
        try {
            return Instant.parse(timestampText);
        } catch (DateTimeParseException ignored) {
        }

        // Common Log Format timestamp:
        // 06/Oct/2026:14:14:36 +0300
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss xx");

        return OffsetDateTime
                .parse(timestampText, formatter)
                .toInstant();
    }

    public int getTotalLines() {
        return totalLines;
    }

    public int getErrorLines() {
        return errorLines;
    }
}