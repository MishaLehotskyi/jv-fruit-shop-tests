package core.basesyntax.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import core.basesyntax.dao.FruitDao;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReportGeneratorImplTest {
    private FruitDao fruitDao;
    private ReportGeneratorImpl reportGenerator;

    @BeforeEach
    void setup() {
        this.fruitDao = mock(FruitDao.class);
    }

    // ==================== CONSTRUCTOR TESTS ====================

    @Test
    void constructor_nullFruitDao_notOk() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> new ReportGeneratorImpl(null));

        assertTrue(exception.getMessage().contains("fruitDao"));
        assertTrue(exception.getMessage().contains("must not be null"));
    }

    @Test
    void constructor_validFruitDao_ok() {
        // Should not throw any exception
        ReportGeneratorImpl generator = new ReportGeneratorImpl(fruitDao);

        // Verify the instance is created successfully
        assertTrue(generator != null);
    }

    // ==================== GET REPORT METHOD TESTS ====================

    @Test
    void getReport_emptyFruits_ok() {
        when(fruitDao.getFruits()).thenReturn(new HashMap<>());
        reportGenerator = new ReportGeneratorImpl(fruitDao);

        String report = reportGenerator.getReport();

        assertEquals("fruit,quantity", report);
        verify(fruitDao, times(1)).getFruits();
    }

    @Test
    void getReport_singleFruit_ok() {
        Map<String, Integer> fruits = new LinkedHashMap<>();
        fruits.put("apple", 100);

        when(fruitDao.getFruits()).thenReturn(fruits);
        reportGenerator = new ReportGeneratorImpl(fruitDao);

        String report = reportGenerator.getReport();

        String expected = "fruit,quantity" + System.lineSeparator() + "apple,100";
        assertEquals(expected, report);
        verify(fruitDao, times(1)).getFruits();
    }

    @Test
    void getReport_multipleFruits_ok() {
        Map<String, Integer> fruits = new LinkedHashMap<>();
        fruits.put("apple", 100);
        fruits.put("banana", 50);
        fruits.put("orange", 75);

        when(fruitDao.getFruits()).thenReturn(fruits);
        reportGenerator = new ReportGeneratorImpl(fruitDao);

        String report = reportGenerator.getReport();

        String expected = "fruit,quantity"
                + System.lineSeparator() + "apple,100"
                + System.lineSeparator() + "banana,50"
                + System.lineSeparator() + "orange,75";
        assertEquals(expected, report);
        verify(fruitDao, times(1)).getFruits();
    }

    @Test
    void getReport_fruitsWithZeroQuantity_ok() {
        Map<String, Integer> fruits = new LinkedHashMap<>();
        fruits.put("apple", 0);
        fruits.put("banana", 50);

        when(fruitDao.getFruits()).thenReturn(fruits);
        reportGenerator = new ReportGeneratorImpl(fruitDao);

        String report = reportGenerator.getReport();

        String expected = "fruit,quantity"
                + System.lineSeparator() + "apple,0"
                + System.lineSeparator() + "banana,50";
        assertEquals(expected, report);
    }

    @Test
    void getReport_fruitsWithLargeQuantities_ok() {
        Map<String, Integer> fruits = new LinkedHashMap<>();
        fruits.put("apple", 1000000);
        fruits.put("banana", 999999);

        when(fruitDao.getFruits()).thenReturn(fruits);
        reportGenerator = new ReportGeneratorImpl(fruitDao);

        String report = reportGenerator.getReport();

        String expected = "fruit,quantity"
                + System.lineSeparator() + "apple,1000000"
                + System.lineSeparator() + "banana,999999";
        assertEquals(expected, report);
    }

    @Test
    void getReport_fruitNamesWithSpecialCharacters_ok() {
        Map<String, Integer> fruits = new LinkedHashMap<>();
        fruits.put("green apple", 100);
        fruits.put("lady-finger banana", 50);

        when(fruitDao.getFruits()).thenReturn(fruits);
        reportGenerator = new ReportGeneratorImpl(fruitDao);

        String report = reportGenerator.getReport();

        String expected = "fruit,quantity"
                + System.lineSeparator() + "green apple,100"
                + System.lineSeparator() + "lady-finger banana,50";
        assertEquals(expected, report);
    }

    @Test
    void getReport_multipleCallsSameData_ok() {
        Map<String, Integer> fruits = new LinkedHashMap<>();
        fruits.put("apple", 100);

        when(fruitDao.getFruits()).thenReturn(fruits);
        reportGenerator = new ReportGeneratorImpl(fruitDao);

        String report1 = reportGenerator.getReport();
        String report2 = reportGenerator.getReport();

        assertEquals(report1, report2);
        verify(fruitDao, times(2)).getFruits();
    }

    @Test
    void getReport_dataChangedBetweenCalls_ok() {
        Map<String, Integer> fruits1 = new LinkedHashMap<>();
        fruits1.put("apple", 100);

        Map<String, Integer> fruits2 = new LinkedHashMap<>();
        fruits2.put("apple", 150);
        fruits2.put("banana", 50);

        when(fruitDao.getFruits())
                .thenReturn(fruits1)
                .thenReturn(fruits2);

        reportGenerator = new ReportGeneratorImpl(fruitDao);

        String report1 = reportGenerator.getReport();
        String report2 = reportGenerator.getReport();

        String expected1 = "fruit,quantity" + System.lineSeparator() + "apple,100";
        String expected2 = "fruit,quantity"
                + System.lineSeparator() + "apple,150"
                + System.lineSeparator() + "banana,50";

        assertEquals(expected1, report1);
        assertEquals(expected2, report2);
        verify(fruitDao, times(2)).getFruits();
    }

    // ==================== HEADER FORMAT TESTS ====================

    @Test
    void getReport_headerFormat_ok() {
        when(fruitDao.getFruits()).thenReturn(new HashMap<>());
        reportGenerator = new ReportGeneratorImpl(fruitDao);

        String report = reportGenerator.getReport();

        assertTrue(report.startsWith("fruit,quantity"));
    }

    @Test
    void getReport_headerWithDataFormat_ok() {
        Map<String, Integer> fruits = new LinkedHashMap<>();
        fruits.put("apple", 100);

        when(fruitDao.getFruits()).thenReturn(fruits);
        reportGenerator = new ReportGeneratorImpl(fruitDao);

        String report = reportGenerator.getReport();

        String[] lines = report.split(System.lineSeparator());
        assertEquals(2, lines.length);
        assertEquals("fruit,quantity", lines[0]);
        assertEquals("apple,100", lines[1]);
    }

    // ==================== LINE SEPARATOR TESTS ====================

    @Test
    void getReport_usesSystemLineSeparator_ok() {
        Map<String, Integer> fruits = new LinkedHashMap<>();
        fruits.put("apple", 100);

        when(fruitDao.getFruits()).thenReturn(fruits);
        reportGenerator = new ReportGeneratorImpl(fruitDao);

        String report = reportGenerator.getReport();

        assertTrue(report.contains(System.lineSeparator()));
    }

    // ==================== ORDERING TESTS ====================

    @Test
    void getReport_preservesMapOrder_ok() {
        // LinkedHashMap preserves insertion order
        Map<String, Integer> fruits = new LinkedHashMap<>();
        fruits.put("zebra", 10);
        fruits.put("apple", 100);
        fruits.put("mango", 50);

        when(fruitDao.getFruits()).thenReturn(fruits);
        reportGenerator = new ReportGeneratorImpl(fruitDao);

        String report = reportGenerator.getReport();

        String expected = "fruit,quantity"
                + System.lineSeparator() + "zebra,10"
                + System.lineSeparator() + "apple,100"
                + System.lineSeparator() + "mango,50";
        assertEquals(expected, report);
    }

    // ==================== INTEGRATION-STYLE TESTS ====================

    @Test
    void getReport_typicalUsageScenario_ok() {
        // Simulate a typical scenario with various fruits
        Map<String, Integer> fruits = new LinkedHashMap<>();
        fruits.put("apple", 120);
        fruits.put("banana", 85);
        fruits.put("orange", 0);
        fruits.put("grape", 250);

        when(fruitDao.getFruits()).thenReturn(fruits);
        reportGenerator = new ReportGeneratorImpl(fruitDao);

        String report = reportGenerator.getReport();

        // Verify header
        assertTrue(report.startsWith("fruit,quantity"));

        // Verify all fruits are present
        assertTrue(report.contains("apple,120"));
        assertTrue(report.contains("banana,85"));
        assertTrue(report.contains("orange,0"));
        assertTrue(report.contains("grape,250"));

        // Verify line count (header + 4 data lines)
        String[] lines = report.split(System.lineSeparator());
        assertEquals(5, lines.length);
    }

    @Test
    void getReport_singleFruitNamedFruit_ok() {
        // Edge case: fruit named "fruit" (same as header column)
        Map<String, Integer> fruits = new LinkedHashMap<>();
        fruits.put("fruit", 50);

        when(fruitDao.getFruits()).thenReturn(fruits);
        reportGenerator = new ReportGeneratorImpl(fruitDao);

        String report = reportGenerator.getReport();

        String expected = "fruit,quantity" + System.lineSeparator() + "fruit,50";
        assertEquals(expected, report);
    }
}