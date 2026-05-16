package rs.igapp.aurora.agent.tail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads newly appended complete lines => Intended for small log files
 */
public final class AppendOnlyFileTailer {

    private final Path path;
    private long readOffset;
    private final StringBuilder carry = new StringBuilder();

    public AppendOnlyFileTailer(Path path) {
        this.path = path;
    }

    public List<String> pollNewLines() throws IOException {
        if (!Files.isRegularFile(path)) {
            return List.of();
        }
        byte[] full = Files.readAllBytes(path);
        long newLen = full.length;
        if (newLen < readOffset) {
            readOffset = 0;
            carry.setLength(0);
        }
        if (newLen <= readOffset) {
            return List.of();
        }
        String delta =
                new String(full, (int) readOffset, (int) (newLen - readOffset), StandardCharsets.UTF_8);
        readOffset = newLen;
        carry.append(delta);
        List<String> lines = new ArrayList<>();
        int nl;
        while ((nl = indexOfNewline(carry)) >= 0) {
            String line = carry.substring(0, nl).replace("\r", "");
            carry.delete(0, nl + 1);
            if (!line.isBlank()) {
                lines.add(line);
            }
        }
        return lines;
    }

    private static int indexOfNewline(StringBuilder sb) {
        for (int i = 0; i < sb.length(); i++) {
            if (sb.charAt(i) == '\n') {
                return i;
            }
        }
        return -1;
    }
}
