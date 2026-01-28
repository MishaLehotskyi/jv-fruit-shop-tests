package core.basesyntax.writer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FileWriterImplTest {
    private static final String VALID_REPORT = "fruit,quantity\napple,100\nbanana,50";
    private static final String EMPTY_REPORT = "";
    private static final String SINGLE_LINE_REPORT = "fruit,quantity";

    @TempDir
    private Path tempDir;

    private FileWriterImpl fileWriter;
    private Path testFile;

    @BeforeEach
    void setup() {
        this.fileWriter = new FileWriterImpl();
    }

    // ==================== VALID WRITE TESTS ====================

    @Test
    void write_validReportAndFileName_ok() throws IOException {
        testFile = tempDir.resolve("report.csv");

        fileWriter.write(VALID_REPORT, testFile.toString());

        // Verify file was created and contains correct content
        assertTrue(Files.exists(testFile));
        String content = Files.readString(testFile);
        assertEquals(VALID_REPORT, content);
    }

    @Test
    void write_emptyReport_ok() throws IOException {
        testFile = tempDir.resolve("empty-report.csv");

        fileWriter.write(EMPTY_REPORT, testFile.toString());

        assertTrue(Files.exists(testFile));
        String content = Files.readString(testFile);
        assertEquals(EMPTY_REPORT, content);
    }

    @Test
    void write_singleLineReport_ok() throws IOException {
        testFile = tempDir.resolve("single-line.csv");

        fileWriter.write(SINGLE_LINE_REPORT, testFile.toString());

        assertTrue(Files.exists(testFile));
        String content = Files.readString(testFile);
        assertEquals(SINGLE_LINE_REPORT, content);
    }

    @Test
    void write_reportWithSpecialCharacters_ok() throws IOException {
        testFile = tempDir.resolve("special-chars.csv");
        String specialReport = "fruit,quantity\ngreen-apple,100\nlady finger banana,50";

        fileWriter.write(specialReport, testFile.toString());

        assertTrue(Files.exists(testFile));
        String content = Files.readString(testFile);
        assertEquals(specialReport, content);
    }

    @Test
    void write_reportWithUnicodeCharacters_ok() throws IOException {
        testFile = tempDir.resolve("unicode.csv");
        String unicodeReport = "fruit,quantity\n🍎,100\n🍌,50";

        fileWriter.write(unicodeReport, testFile.toString());

        assertTrue(Files.exists(testFile));
        String content = Files.readString(testFile);
        assertEquals(unicodeReport, content);
    }

    @Test
    void write_largeReport_ok() throws IOException {
        testFile = tempDir.resolve("large-report.csv");
        StringBuilder largeReport = new StringBuilder("fruit,quantity\n");

        for (int i = 0; i < 1000; i++) {
            largeReport.append("fruit").append(i).append(",").append(i * 10).append("\n");
        }

        String reportContent = largeReport.toString();
        fileWriter.write(reportContent, testFile.toString());

        assertTrue(Files.exists(testFile));
        String content = Files.readString(testFile);
        assertEquals(reportContent, content);
    }

    // ==================== OVERWRITE TESTS ====================

    @Test
    void write_overwriteExistingFile_ok() throws IOException {
        testFile = tempDir.resolve("overwrite.csv");
        String initialContent = "initial content";
        String newContent = "new content";

        // Write initial content
        Files.writeString(testFile, initialContent);
        assertEquals(initialContent, Files.readString(testFile));

        // Overwrite with new content
        fileWriter.write(newContent, testFile.toString());

        // Verify content was replaced
        String content = Files.readString(testFile);
        assertEquals(newContent, content);
    }

    @Test
    void write_sameTwice_ok() throws IOException {
        testFile = tempDir.resolve("same-twice.csv");

        fileWriter.write(VALID_REPORT, testFile.toString());
        fileWriter.write(VALID_REPORT, testFile.toString());

        assertTrue(Files.exists(testFile));
        String content = Files.readString(testFile);
        assertEquals(VALID_REPORT, content);
    }

    // ==================== FILE NAME VARIATIONS TESTS ====================

    @Test
    void write_fileNameWithExtension_ok() throws IOException {
        testFile = tempDir.resolve("report.csv");

        fileWriter.write(VALID_REPORT, testFile.toString());

        assertTrue(Files.exists(testFile));
    }

    @Test
    void write_fileNameWithoutExtension_ok() throws IOException {
        testFile = tempDir.resolve("report");

        fileWriter.write(VALID_REPORT, testFile.toString());

        assertTrue(Files.exists(testFile));
    }

    @Test
    void write_fileNameWithDifferentExtension_ok() throws IOException {
        testFile = tempDir.resolve("report.txt");

        fileWriter.write(VALID_REPORT, testFile.toString());

        assertTrue(Files.exists(testFile));
    }

    @Test
    void write_fileInSubdirectory_ok() throws IOException {
        Path subDir = tempDir.resolve("subdir");
        Files.createDirectories(subDir);
        testFile = subDir.resolve("report.csv");

        fileWriter.write(VALID_REPORT, testFile.toString());

        assertTrue(Files.exists(testFile));
        String content = Files.readString(testFile);
        assertEquals(VALID_REPORT, content);
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Test
    void write_invalidPath_notOk() {
        // Use a path with invalid characters (null byte works on most systems)
        String invalidPath = "invalid\0path.csv";

        // Verify our code wraps the exception in RuntimeException
        assertThrows(RuntimeException.class,
                () -> fileWriter.write(VALID_REPORT, invalidPath));
    }

    @Test
    void write_nonExistentDirectory_notOk() {
        String pathInNonExistentDir = tempDir.resolve("nonexistent/dir/report.csv").toString();

        // Verify our code wraps the exception in RuntimeException
        assertThrows(RuntimeException.class,
                () -> fileWriter.write(VALID_REPORT, pathInNonExistentDir));
    }

    // ==================== NULL AND EMPTY PARAMETER TESTS ====================

    @Test
    void write_nullReport_notOk() {
        testFile = tempDir.resolve("null-report.csv");

        // Files.writeString throws exception for null content
        // Verify our code wraps it in RuntimeException
        assertThrows(RuntimeException.class,
                () -> fileWriter.write(null, testFile.toString()));
    }

    @Test
    void write_nullFileName_notOk() {
        // Files.writeString throws exception for null filename
        // Verify our code wraps it in RuntimeException
        assertThrows(RuntimeException.class,
                () -> fileWriter.write(VALID_REPORT, null));
    }

    // ==================== LINE SEPARATOR TESTS ====================

    @Test
    void write_reportWithWindowsLineEndings_ok() throws IOException {
        testFile = tempDir.resolve("windows-endings.csv");
        String windowsReport = "fruit,quantity\r\napple,100\r\nbanana,50";

        fileWriter.write(windowsReport, testFile.toString());

        assertTrue(Files.exists(testFile));
        String content = Files.readString(testFile);
        assertEquals(windowsReport, content);
    }

    @Test
    void write_reportWithUnixLineEndings_ok() throws IOException {
        testFile = tempDir.resolve("unix-endings.csv");
        String unixReport = "fruit,quantity\napple,100\nbanana,50";

        fileWriter.write(unixReport, testFile.toString());

        assertTrue(Files.exists(testFile));
        String content = Files.readString(testFile);
        assertEquals(unixReport, content);
    }

    @Test
    void write_reportWithMixedLineEndings_ok() throws IOException {
        testFile = tempDir.resolve("mixed-endings.csv");
        String mixedReport = "fruit,quantity\napple,100\r\nbanana,50\n";

        fileWriter.write(mixedReport, testFile.toString());

        assertTrue(Files.exists(testFile));
        String content = Files.readString(testFile);
        assertEquals(mixedReport, content);
    }

    // ==================== MULTIPLE FILES TESTS ====================

    @Test
    void write_multipleDifferentFiles_ok() throws IOException {
        Path file1 = tempDir.resolve("report1.csv");
        Path file2 = tempDir.resolve("report2.csv");
        String content1 = "fruit,quantity\napple,100";
        String content2 = "fruit,quantity\nbanana,50";

        fileWriter.write(content1, file1.toString());
        fileWriter.write(content2, file2.toString());

        assertTrue(Files.exists(file1));
        assertTrue(Files.exists(file2));
        assertEquals(content1, Files.readString(file1));
        assertEquals(content2, Files.readString(file2));
    }

    // ==================== WHITESPACE TESTS ====================

    @Test
    void write_reportWithLeadingWhitespace_ok() throws IOException {
        testFile = tempDir.resolve("leading-whitespace.csv");
        String whitespacedReport = "  fruit,quantity\n  apple,100";

        fileWriter.write(whitespacedReport, testFile.toString());

        assertTrue(Files.exists(testFile));
        String content = Files.readString(testFile);
        assertEquals(whitespacedReport, content);
    }

    @Test
    void write_reportWithTrailingWhitespace_ok() throws IOException {
        testFile = tempDir.resolve("trailing-whitespace.csv");
        String whitespacedReport = "fruit,quantity  \napple,100  ";

        fileWriter.write(whitespacedReport, testFile.toString());

        assertTrue(Files.exists(testFile));
        String content = Files.readString(testFile);
        assertEquals(whitespacedReport, content);
    }
}
