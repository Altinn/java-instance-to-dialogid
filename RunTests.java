import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RunTests {
    private static final Pattern TEST_CASE_PATTERN = Pattern.compile(
        "\\{\\s*\"instanceId\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"created\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"dialogId\"\\s*:\\s*\"([^\"]+)\"\\s*\\}",
        Pattern.DOTALL
    );

    private RunTests() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.err.println("Usage:");
            System.err.println("  java -cp . RunTests");
            System.err.println("  java -cp . RunTests <path-to-testdata.json>");
            System.exit(1);
        }

        Path inputPath = args.length == 1 ? Path.of(args[0]) : Path.of("testdata.json");
        run(inputPath);
    }

    private static void run(Path inputPath) throws IOException {
        String content = Files.readString(inputPath);
        Matcher matcher = TEST_CASE_PATTERN.matcher(content);

        int checked = 0;
        int matches = 0;
        int failed = 0;

        while (matcher.find()) {
            String rawInstanceId = matcher.group(1);
            String created = matcher.group(2);
            String expectedDialogId = matcher.group(3);

            String actualDialogId = convert(rawInstanceId, created);
            checked++;

            if (actualDialogId.equals(expectedDialogId)) {
                matches++;
                continue;
            }

            failed++;
            System.out.println(
                "Mismatch: instanceId=" + rawInstanceId
                    + ", created=" + created
                    + ", expected=" + expectedDialogId
                    + ", actual=" + actualDialogId
            );
        }

        if (checked == 0) {
            throw new IllegalArgumentException("No test cases found in " + inputPath);
        }

        System.out.println(
            "Checked " + checked + " instance ids/created, "
                + matches + " matches, "
                + failed + " failed"
        );
    }

    private static String convert(String rawInstanceId, String timestampText) {
        String uuidText = extractUuid(rawInstanceId);
        UUID instanceId = UUID.fromString(uuidText);
        Instant timestamp = Instant.parse(timestampText);
        return InstanceToDialogId.convert(instanceId, timestamp).toString();
    }

    private static String extractUuid(String rawInstanceId) {
        int slashIndex = rawInstanceId.lastIndexOf('/');
        return slashIndex >= 0 ? rawInstanceId.substring(slashIndex + 1) : rawInstanceId;
    }
}
