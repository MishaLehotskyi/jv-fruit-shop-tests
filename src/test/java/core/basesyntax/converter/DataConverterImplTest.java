package core.basesyntax.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.basesyntax.model.FruitTransaction;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DataConverterImplTest {
    private static final String EMPTY_LINE = "";
    private static final String SPACES_LINE = "     ";
    private static final String WRONG_FORMAT_LINE = "b apple 100";
    private static final String UNKNOWN_OPERATION_LINE = "zxc,apple,100";
    private static final String NON_INTEGER_QUANTITY_LINE = "b,apple,banana";
    private static final String NEGATIVE_INTEGER_QUANTITY_LINE = "b,apple,-10";
    private static final String VALID_LINE = "b,apple,100";
    private static final FruitTransaction fruitTransaction = new FruitTransaction(
            FruitTransaction.Operation.BALANCE,
            "apple",
            100
    );
    private static final List<FruitTransaction> FRUIT_TRANSACTIONS_EXPECTED_LIST = List.of(
            fruitTransaction
    );

    private DataConverterImpl dataConverter;
    private List<String> report;

    @BeforeEach
    void setup() {
        this.dataConverter = new DataConverterImpl();
        this.report = new ArrayList<>();
    }

    @Test
    void convertToTransaction_nullReport_notOk() {
        Exception exception = assertThrows(NullPointerException.class,
                () -> this.dataConverter.convertToTransaction(null));
        assertTrue(exception.getMessage().contains("report must not be null"));
    }

    @Test
    void convertToTransaction_nullLineReport_notOk() {
        report.add(null);
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> this.dataConverter.convertToTransaction(this.report));
        assertTrue(exception.getMessage().contains("Line 1"));
        assertTrue(exception.getMessage().contains("empty or null"));
    }

    @Test
    void convertToTransaction_emptyLineReport_notOk() {
        report.add(EMPTY_LINE);
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> this.dataConverter.convertToTransaction(this.report));
        assertTrue(exception.getMessage().contains("Line 1"));
        assertTrue(exception.getMessage().contains("empty or null"));
    }

    @Test
    void convertToTransaction_spacesLineReport_notOk() {
        report.add(SPACES_LINE);
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> this.dataConverter.convertToTransaction(this.report));
        assertTrue(exception.getMessage().contains("Line 1"));
        assertTrue(exception.getMessage().contains("empty or null"));
    }

    @Test
    void convertToTransaction_wrongFormatLineReport_notOk() {
        report.add(WRONG_FORMAT_LINE);
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> this.dataConverter.convertToTransaction(this.report));
        assertTrue(exception.getMessage().contains("Line 1"));
        assertTrue(exception.getMessage().contains("exactly 3 fields"));
    }

    @Test
    void convertToTransaction_unknownOperationLineReport_notOk() {
        report.add(UNKNOWN_OPERATION_LINE);
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> this.dataConverter.convertToTransaction(this.report));
        assertTrue(exception.getMessage().contains("Unknown operation code"));
        assertTrue(exception.getMessage().contains("zxc"));
        assertTrue(exception.getMessage().contains("line 1"));
    }

    @Test
    void convertToTransaction_nonIntegerQuantityLineReport_notOk() {
        report.add(NON_INTEGER_QUANTITY_LINE);
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> this.dataConverter.convertToTransaction(this.report));
        assertTrue(exception.getMessage().contains("Invalid quantity"));
        assertTrue(exception.getMessage().contains("banana"));
        assertTrue(exception.getMessage().contains("line 1"));
        assertTrue(exception.getMessage().contains("must be an integer"));
    }

    @Test
    void convertToTransaction_negativeIntegerQuantityLineReport_notOk() {
        report.add(NEGATIVE_INTEGER_QUANTITY_LINE);
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> this.dataConverter.convertToTransaction(this.report));
        assertTrue(exception.getMessage().contains("Negative quantity"));
        assertTrue(exception.getMessage().contains("-10"));
        assertTrue(exception.getMessage().contains("line 1"));
    }

    @Test
    void convertToTransaction_validLineReport_ok() {
        report.add(VALID_LINE);
        List<FruitTransaction> fruitTransactions =
                this.dataConverter.convertToTransaction(this.report);
        assertEquals(FRUIT_TRANSACTIONS_EXPECTED_LIST, fruitTransactions);
    }
}