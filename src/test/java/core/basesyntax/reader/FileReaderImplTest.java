package core.basesyntax.reader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FileReaderImplTest {
    private static final String VALID_HEADER = "type,fruit,quantity";
    private static final String VALID_LINE_1 = "b,apple,100";
    private static final String VALID_LINE_2 = "s,banana,50";
    private static final String EMPTY_STRING = "";
    private static final String SPACES_STRING = "   ";

    @TempDir
    private Path tempDir;

    private FileReaderImpl fileReader;
    private Path testFile;

    @BeforeEach
    void setup() {
        this.fileReader = new FileReaderImpl();
    }

    // ==================== NULL AND BLANK SOURCE TESTS ====================

    @Test
    void read_nullSource_notOk() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> fileReader.read(null));

        assertTrue(exception.getMessage().contains("source"));
        assertTrue(exception.getMessage().contains("must not be null or blank"));
    }

    @Test
    void read_emptySource_notOk() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> fileReader.read(EMPTY_STRING));

        assertTrue(exception.getMessage().contains("source"));
        assertTrue(exception.getMessage().contains("must not be null or blank"));
    }

    @Test
    void read_blankSource_notOk() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> fileReader.read(SPACES_STRING));

        assertTrue(exception.getMessage().contains("source"));
        assertTrue(exception.getMessage().contains("must not be null or blank"));
    }

    // ==================== INVALID PATH TESTS ====================

    @Test
    void read_invalidPath_notOk() {
        // Use a path with invalid characters
        // (this varies by OS, but null byte works on most)
        String invalidPath = "invalid\0path.csv";

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> fileReader.read(invalidPath));

        assertTrue(exception.getMessage().contains("Invalid path"));
        assertTrue(exception.getMessage().contains(invalidPath));
    }

    // ==================== NON-EXISTENT FILE TESTS ====================

    @Test
    void read_nonExistentFile_notOk() {
        String nonExistentPath = tempDir.resolve("nonexistent.csv").toString();

        Exception exception = assertThrows(UncheckedIOException.class,
                () -> fileReader.read(nonExistentPath));

        assertTrue(exception.getMessage().contains("Error reading file"));
        assertTrue(exception.getMessage().contains(nonExistentPath));
    }

    // ==================== EMPTY FILE TESTS ====================

    @Test
    void read_emptyFile_ok() throws IOException {
        testFile = tempDir.resolve("empty.csv");
        Files.writeString(testFile, "");

        List<String> result = fileReader.read(testFile.toString());

        assertTrue(result.isEmpty());
    }

    // ==================== FILE WITH HEADER TESTS ====================

    @Test
    void read_fileWithHeaderOnly_ok() throws IOException {
        testFile = tempDir.resolve("header-only.csv");
        Files.writeString(testFile, VALID_HEADER);

        List<String> result = fileReader.read(testFile.toString());

        assertTrue(result.isEmpty());
    }

    @Test
    void read_fileWithHeaderAndData_ok() throws IOException {
        testFile = tempDir.resolve("with-header.csv");
        String content = VALID_HEADER + "\n" + VALID_LINE_1 + "\n" + VALID_LINE_2;
        Files.writeString(testFile, content);

        List<String> result = fileReader.read(testFile.toString());

        assertEquals(2, result.size());
        assertEquals(VALID_LINE_1, result.get(0));
        assertEquals(VALID_LINE_2, result.get(1));
    }

    @Test
    void read_fileWithHeaderDifferentCase_ok() throws IOException {
        testFile = tempDir.resolve("header-case.csv");
        String content = "TYPE,FRUIT,QUANTITY\n" + VALID_LINE_1 + "\n" + VALID_LINE_2;
        Files.writeString(testFile, content);

        List<String> result = fileReader.read(testFile.toString());

        assertEquals(2, result.size());
        assertEquals(VALID_LINE_1, result.get(0));
        assertEquals(VALID_LINE_2, result.get(1));
    }

    @Test
    void read_fileWithHeaderAndWhitespace_ok() throws IOException {
        testFile = tempDir.resolve("header-whitespace.csv");
        String content = "  type,fruit,quantity  \n" + VALID_LINE_1 + "\n" + VALID_LINE_2;
        Files.writeString(testFile, content);

        List<String> result = fileReader.read(testFile.toString());

        assertEquals(2, result.size());
        assertEquals(VALID_LINE_1, result.get(0));
        assertEquals(VALID_LINE_2, result.get(1));
    }

    // ==================== FILE WITHOUT HEADER TESTS ====================

    @Test
    void read_fileWithoutHeader_ok() throws IOException {
        testFile = tempDir.resolve("no-header.csv");
        String content = VALID_LINE_1 + "\n" + VALID_LINE_2;
        Files.writeString(testFile, content);

        List<String> result = fileReader.read(testFile.toString());

        assertEquals(2, result.size());
        assertEquals(VALID_LINE_1, result.get(0));
        assertEquals(VALID_LINE_2, result.get(1));
    }

    // ==================== WHITESPACE HANDLING TESTS ====================

    @Test
    void read_fileWithLeadingTrailingWhitespace_ok() throws IOException {
        testFile = tempDir.resolve("whitespace.csv");
        String content = VALID_HEADER + "\n  " + VALID_LINE_1 + "  \n\t" + VALID_LINE_2 + "\t";
        Files.writeString(testFile, content);

        List<String> result = fileReader.read(testFile.toString());

        assertEquals(2, result.size());
        assertEquals(VALID_LINE_1, result.get(0));
        assertEquals(VALID_LINE_2, result.get(1));
    }

    @Test
    void read_fileWithEmptyLines_ok() throws IOException {
        testFile = tempDir.resolve("empty-lines.csv");
        String content = VALID_HEADER + "\n" + VALID_LINE_1 + "\n\n\n" + VALID_LINE_2 + "\n\n";
        Files.writeString(testFile, content);

        List<String> result = fileReader.read(testFile.toString());

        assertEquals(2, result.size());
        assertEquals(VALID_LINE_1, result.get(0));
        assertEquals(VALID_LINE_2, result.get(1));
    }

    @Test
    void read_fileWithBlankLines_ok() throws IOException {
        testFile = tempDir.resolve("blank-lines.csv");
        String content = VALID_HEADER + "\n" + VALID_LINE_1 + "\n   \n\t\t\n" + VALID_LINE_2;
        Files.writeString(testFile, content);

        List<String> result = fileReader.read(testFile.toString());

        assertEquals(2, result.size());
        assertEquals(VALID_LINE_1, result.get(0));
        assertEquals(VALID_LINE_2, result.get(1));
    }

    // ==================== DUPLICATE HEADER TESTS ====================

    @Test
    void read_fileWithDuplicateHeaderInMiddle_ok() throws IOException {
        testFile = tempDir.resolve("duplicate-header.csv");
        String content = VALID_HEADER + "\n" + VALID_LINE_1 + "\n"
                + VALID_HEADER + "\n" + VALID_LINE_2;
        Files.writeString(testFile, content);

        List<String> result = fileReader.read(testFile.toString());

        // The duplicate header in the middle should be filtered out
        assertEquals(2, result.size());
        assertEquals(VALID_LINE_1, result.get(0));
        assertEquals(VALID_LINE_2, result.get(1));
    }

    @Test
    void read_fileWithMultipleDuplicateHeaders_ok() throws IOException {
        testFile = tempDir.resolve("multiple-headers.csv");
        String content = VALID_HEADER + "\n"
                + VALID_LINE_1 + "\n"
                + "TYPE,FRUIT,QUANTITY\n"
                + VALID_LINE_2 + "\n"
                + "  type,fruit,quantity  ";
        Files.writeString(testFile, content);

        List<String> result = fileReader.read(testFile.toString());

        // All headers should be filtered out
        assertEquals(2, result.size());
        assertEquals(VALID_LINE_1, result.get(0));
        assertEquals(VALID_LINE_2, result.get(1));
    }

    // ==================== SINGLE LINE TESTS ====================

    @Test
    void read_fileWithSingleDataLine_ok() throws IOException {
        testFile = tempDir.resolve("single-line.csv");
        Files.writeString(testFile, VALID_LINE_1);

        List<String> result = fileReader.read(testFile.toString());

        assertEquals(1, result.size());
        assertEquals(VALID_LINE_1, result.get(0));
    }

    @Test
    void read_fileWithSingleDataLineAndHeader_ok() throws IOException {
        testFile = tempDir.resolve("single-with-header.csv");
        String content = VALID_HEADER + "\n" + VALID_LINE_1;
        Files.writeString(testFile, content);

        List<String> result = fileReader.read(testFile.toString());

        assertEquals(1, result.size());
        assertEquals(VALID_LINE_1, result.get(0));
    }

    // ==================== LARGE FILE TESTS ====================

    @Test
    void read_fileWithManyLines_ok() throws IOException {
        testFile = tempDir.resolve("many-lines.csv");
        StringBuilder content = new StringBuilder(VALID_HEADER + "\n");

        for (int i = 0; i < 100; i++) {
            content.append("b,fruit").append(i).append(",").append(i * 10).append("\n");
        }

        Files.writeString(testFile, content.toString());

        List<String> result = fileReader.read(testFile.toString());

        assertEquals(100, result.size());
        assertEquals("b,fruit0,0", result.get(0));
        assertEquals("b,fruit99,990", result.get(99));
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    void read_fileWithOnlyWhitespace_ok() throws IOException {
        testFile = tempDir.resolve("only-whitespace.csv");
        Files.writeString(testFile, "   \n\t\t\n   \n");

        List<String> result = fileReader.read(testFile.toString());

        assertTrue(result.isEmpty());
    }

    @Test
    void read_fileWithHeaderAndOnlyWhitespace_ok() throws IOException {
        testFile = tempDir.resolve("header-and-whitespace.csv");
        String content = VALID_HEADER + "\n   \n\t\t\n   ";
        Files.writeString(testFile, content);

        List<String> result = fileReader.read(testFile.toString());

        assertTrue(result.isEmpty());
    }

    @Test
    void read_validPathMultipleTimes_ok() throws IOException {
        testFile = tempDir.resolve("reread.csv");
        String content = VALID_HEADER + "\n" + VALID_LINE_1;
        Files.writeString(testFile, content);

        // Read the same file multiple times
        List<String> result1 = fileReader.read(testFile.toString());
        List<String> result2 = fileReader.read(testFile.toString());

        assertEquals(result1, result2);
        assertEquals(1, result1.size());
        assertEquals(VALID_LINE_1, result1.get(0));
    }
}
