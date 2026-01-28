package core.basesyntax.service.operation;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import core.basesyntax.dao.FruitDao;
import core.basesyntax.model.FruitTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SupplyOperationTest {
    private static final String VALID_FRUIT = "apple";
    private static final Integer VALID_QUANTITY = 100;
    private static final String EMPTY_FRUIT = "";
    private static final String SPACES_FRUIT = "   ";

    private FruitDao fruitDao;
    private SupplyOperation supplyOperation;

    @BeforeEach
    void setup() {
        this.fruitDao = mock(FruitDao.class);
        this.supplyOperation = new SupplyOperation(fruitDao);
    }

    // ==================== NULL TRANSACTION TESTS ====================

    @Test
    void handle_nullTransaction_notOk() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> supplyOperation.handle(null));

        assertTrue(exception.getMessage().contains("transaction"));
        assertTrue(exception.getMessage().contains("must not be null"));

        // Verify DAO was never called
        verify(fruitDao, never()).add(anyString(), anyInt());
    }

    // ==================== NULL FRUIT NAME TESTS ====================

    @Test
    void handle_transactionWithNullFruit_notOk() {
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.SUPPLY,
                null,
                VALID_QUANTITY
        );

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> supplyOperation.handle(transaction));

        assertTrue(exception.getMessage().contains("transaction.fruit"));
        assertTrue(exception.getMessage().contains("must not be null or blank"));

        // Verify DAO was never called
        verify(fruitDao, never()).add(anyString(), anyInt());
    }

    @Test
    void handle_transactionWithEmptyFruit_notOk() {
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.SUPPLY,
                EMPTY_FRUIT,
                VALID_QUANTITY
        );

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> supplyOperation.handle(transaction));

        assertTrue(exception.getMessage().contains("transaction.fruit"));
        assertTrue(exception.getMessage().contains("must not be null or blank"));

        // Verify DAO was never called
        verify(fruitDao, never()).add(anyString(), anyInt());
    }

    @Test
    void handle_transactionWithBlankFruit_notOk() {
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.SUPPLY,
                SPACES_FRUIT,
                VALID_QUANTITY
        );

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> supplyOperation.handle(transaction));

        assertTrue(exception.getMessage().contains("transaction.fruit"));
        assertTrue(exception.getMessage().contains("must not be null or blank"));

        // Verify DAO was never called
        verify(fruitDao, never()).add(anyString(), anyInt());
    }

    // ==================== VALID TRANSACTION TESTS ====================

    @Test
    void handle_validTransaction_ok() {
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.SUPPLY,
                VALID_FRUIT,
                VALID_QUANTITY
        );

        supplyOperation.handle(transaction);

        verify(fruitDao, times(1)).add(VALID_FRUIT, VALID_QUANTITY);
    }

    @Test
    void handle_transactionWithZeroQuantity_ok() {
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.SUPPLY,
                VALID_FRUIT,
                0
        );

        supplyOperation.handle(transaction);

        verify(fruitDao, times(1)).add(VALID_FRUIT, 0);
    }

    @Test
    void handle_transactionWithLargeQuantity_ok() {
        Integer largeQuantity = 1000000;
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.SUPPLY,
                VALID_FRUIT,
                largeQuantity
        );

        supplyOperation.handle(transaction);

        verify(fruitDao, times(1)).add(VALID_FRUIT, largeQuantity);
    }

    @Test
    void handle_transactionWithDifferentFruits_ok() {
        FruitTransaction transaction1 = new FruitTransaction(
                FruitTransaction.Operation.SUPPLY,
                "apple",
                100
        );
        FruitTransaction transaction2 = new FruitTransaction(
                FruitTransaction.Operation.SUPPLY,
                "banana",
                50
        );

        supplyOperation.handle(transaction1);
        supplyOperation.handle(transaction2);

        verify(fruitDao, times(1)).add("apple", 100);
        verify(fruitDao, times(1)).add("banana", 50);
    }

    @Test
    void handle_multipleSameFruitTransactions_ok() {
        FruitTransaction transaction1 = new FruitTransaction(
                FruitTransaction.Operation.SUPPLY,
                VALID_FRUIT,
                100
        );
        FruitTransaction transaction2 = new FruitTransaction(
                FruitTransaction.Operation.SUPPLY,
                VALID_FRUIT,
                200
        );

        supplyOperation.handle(transaction1);
        supplyOperation.handle(transaction2);

        verify(fruitDao, times(1)).add(VALID_FRUIT, 100);
        verify(fruitDao, times(1)).add(VALID_FRUIT, 200);
        verify(fruitDao, times(2)).add(anyString(), anyInt());
    }

    // ==================== FRUIT NAME VARIATIONS TESTS ====================

    @Test
    void handle_fruitWithWhitespace_ok() {
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.SUPPLY,
                "  apple  ",
                VALID_QUANTITY
        );

        supplyOperation.handle(transaction);

        // Should pass the fruit name as-is (trimming is DAO's responsibility)
        verify(fruitDao, times(1)).add("  apple  ", VALID_QUANTITY);
    }

    @Test
    void handle_fruitWithSpecialCharacters_ok() {
        String specialFruit = "green-apple";
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.SUPPLY,
                specialFruit,
                VALID_QUANTITY
        );

        supplyOperation.handle(transaction);

        verify(fruitDao, times(1)).add(specialFruit, VALID_QUANTITY);
    }

    @Test
    void handle_fruitWithSpaces_ok() {
        String spacedFruit = "lady finger banana";
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.SUPPLY,
                spacedFruit,
                VALID_QUANTITY
        );

        supplyOperation.handle(transaction);

        verify(fruitDao, times(1)).add(spacedFruit, VALID_QUANTITY);
    }

    // ==================== OPERATION TYPE TESTS ====================

    @Test
    void handle_transactionWithDifferentOperationType_ok() {
        // Even though it's a BALANCE operation, SupplyOperation should still process it
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.BALANCE,
                VALID_FRUIT,
                VALID_QUANTITY
        );

        supplyOperation.handle(transaction);

        verify(fruitDao, times(1)).add(VALID_FRUIT, VALID_QUANTITY);
    }

    @Test
    void handle_transactionWithPurchaseOperation_ok() {
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.PURCHASE,
                VALID_FRUIT,
                VALID_QUANTITY
        );

        supplyOperation.handle(transaction);

        verify(fruitDao, times(1)).add(VALID_FRUIT, VALID_QUANTITY);
    }

    @Test
    void handle_transactionWithReturnOperation_ok() {
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.RETURN,
                VALID_FRUIT,
                VALID_QUANTITY
        );

        supplyOperation.handle(transaction);

        verify(fruitDao, times(1)).add(VALID_FRUIT, VALID_QUANTITY);
    }

    // ==================== NEGATIVE QUANTITY TESTS ====================

    @Test
    void handle_transactionWithNegativeQuantity_ok() {
        // SupplyOperation doesn't validate quantity - that's DAO's job
        Integer negativeQuantity = -10;
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.SUPPLY,
                VALID_FRUIT,
                negativeQuantity
        );

        supplyOperation.handle(transaction);

        verify(fruitDao, times(1)).add(VALID_FRUIT, negativeQuantity);
    }
}
