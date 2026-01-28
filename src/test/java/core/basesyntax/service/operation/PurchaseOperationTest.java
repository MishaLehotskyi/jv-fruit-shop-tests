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

public class PurchaseOperationTest {
    private static final String VALID_FRUIT = "apple";
    private static final Integer VALID_QUANTITY = 100;
    private static final String EMPTY_FRUIT = "";
    private static final String SPACES_FRUIT = "   ";

    private FruitDao fruitDao;
    private PurchaseOperation purchaseOperation;

    @BeforeEach
    void setup() {
        this.fruitDao = mock(FruitDao.class);
        this.purchaseOperation = new PurchaseOperation(fruitDao);
    }

    // ==================== NULL TRANSACTION TESTS ====================

    @Test
    void handle_nullTransaction_notOk() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> purchaseOperation.handle(null));

        assertTrue(exception.getMessage().contains("transaction"));
        assertTrue(exception.getMessage().contains("must not be null"));

        // Verify DAO was never called
        verify(fruitDao, never()).remove(anyString(), anyInt());
    }

    // ==================== NULL FRUIT NAME TESTS ====================

    @Test
    void handle_transactionWithNullFruit_notOk() {
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.PURCHASE,
                null,
                VALID_QUANTITY
        );

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> purchaseOperation.handle(transaction));

        assertTrue(exception.getMessage().contains("transaction.fruit"));
        assertTrue(exception.getMessage().contains("must not be null or blank"));

        // Verify DAO was never called
        verify(fruitDao, never()).remove(anyString(), anyInt());
    }

    @Test
    void handle_transactionWithEmptyFruit_notOk() {
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.PURCHASE,
                EMPTY_FRUIT,
                VALID_QUANTITY
        );

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> purchaseOperation.handle(transaction));

        assertTrue(exception.getMessage().contains("transaction.fruit"));
        assertTrue(exception.getMessage().contains("must not be null or blank"));

        // Verify DAO was never called
        verify(fruitDao, never()).remove(anyString(), anyInt());
    }

    @Test
    void handle_transactionWithBlankFruit_notOk() {
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.PURCHASE,
                SPACES_FRUIT,
                VALID_QUANTITY
        );

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> purchaseOperation.handle(transaction));

        assertTrue(exception.getMessage().contains("transaction.fruit"));
        assertTrue(exception.getMessage().contains("must not be null or blank"));

        // Verify DAO was never called
        verify(fruitDao, never()).remove(anyString(), anyInt());
    }

    // ==================== VALID TRANSACTION TESTS ====================

    @Test
    void handle_validTransaction_ok() {
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.PURCHASE,
                VALID_FRUIT,
                VALID_QUANTITY
        );

        purchaseOperation.handle(transaction);

        verify(fruitDao, times(1)).remove(VALID_FRUIT, VALID_QUANTITY);
    }

    @Test
    void handle_transactionWithZeroQuantity_ok() {
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.PURCHASE,
                VALID_FRUIT,
                0
        );

        purchaseOperation.handle(transaction);

        verify(fruitDao, times(1)).remove(VALID_FRUIT, 0);
    }

    @Test
    void handle_transactionWithLargeQuantity_ok() {
        Integer largeQuantity = 1000000;
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.PURCHASE,
                VALID_FRUIT,
                largeQuantity
        );

        purchaseOperation.handle(transaction);

        verify(fruitDao, times(1)).remove(VALID_FRUIT, largeQuantity);
    }

    @Test
    void handle_transactionWithDifferentFruits_ok() {
        FruitTransaction transaction1 = new FruitTransaction(
                FruitTransaction.Operation.PURCHASE,
                "apple",
                100
        );
        FruitTransaction transaction2 = new FruitTransaction(
                FruitTransaction.Operation.PURCHASE,
                "banana",
                50
        );

        purchaseOperation.handle(transaction1);
        purchaseOperation.handle(transaction2);

        verify(fruitDao, times(1)).remove("apple", 100);
        verify(fruitDao, times(1)).remove("banana", 50);
    }

    @Test
    void handle_multipleSameFruitTransactions_ok() {
        FruitTransaction transaction1 = new FruitTransaction(
                FruitTransaction.Operation.PURCHASE,
                VALID_FRUIT,
                100
        );
        FruitTransaction transaction2 = new FruitTransaction(
                FruitTransaction.Operation.PURCHASE,
                VALID_FRUIT,
                200
        );

        purchaseOperation.handle(transaction1);
        purchaseOperation.handle(transaction2);

        verify(fruitDao, times(1)).remove(VALID_FRUIT, 100);
        verify(fruitDao, times(1)).remove(VALID_FRUIT, 200);
        verify(fruitDao, times(2)).remove(anyString(), anyInt());
    }

    // ==================== FRUIT NAME VARIATIONS TESTS ====================

    @Test
    void handle_fruitWithWhitespace_ok() {
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.PURCHASE,
                "  apple  ",
                VALID_QUANTITY
        );

        purchaseOperation.handle(transaction);

        // Should pass the fruit name as-is (trimming is DAO's responsibility)
        verify(fruitDao, times(1)).remove("  apple  ", VALID_QUANTITY);
    }

    @Test
    void handle_fruitWithSpecialCharacters_ok() {
        String specialFruit = "green-apple";
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.PURCHASE,
                specialFruit,
                VALID_QUANTITY
        );

        purchaseOperation.handle(transaction);

        verify(fruitDao, times(1)).remove(specialFruit, VALID_QUANTITY);
    }

    @Test
    void handle_fruitWithSpaces_ok() {
        String spacedFruit = "lady finger banana";
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.PURCHASE,
                spacedFruit,
                VALID_QUANTITY
        );

        purchaseOperation.handle(transaction);

        verify(fruitDao, times(1)).remove(spacedFruit, VALID_QUANTITY);
    }

    // ==================== OPERATION TYPE TESTS ====================

    @Test
    void handle_transactionWithDifferentOperationType_ok() {
        // Even though it's a BALANCE operation, PurchaseOperation should still process it
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.BALANCE,
                VALID_FRUIT,
                VALID_QUANTITY
        );

        purchaseOperation.handle(transaction);

        verify(fruitDao, times(1)).remove(VALID_FRUIT, VALID_QUANTITY);
    }

    @Test
    void handle_transactionWithSupplyOperation_ok() {
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.SUPPLY,
                VALID_FRUIT,
                VALID_QUANTITY
        );

        purchaseOperation.handle(transaction);

        verify(fruitDao, times(1)).remove(VALID_FRUIT, VALID_QUANTITY);
    }

    @Test
    void handle_transactionWithReturnOperation_ok() {
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.RETURN,
                VALID_FRUIT,
                VALID_QUANTITY
        );

        purchaseOperation.handle(transaction);

        verify(fruitDao, times(1)).remove(VALID_FRUIT, VALID_QUANTITY);
    }

    // ==================== NEGATIVE QUANTITY TESTS ====================

    @Test
    void handle_transactionWithNegativeQuantity_ok() {
        // PurchaseOperation doesn't validate quantity - that's DAO's job
        Integer negativeQuantity = -10;
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.PURCHASE,
                VALID_FRUIT,
                negativeQuantity
        );

        purchaseOperation.handle(transaction);

        verify(fruitDao, times(1)).remove(VALID_FRUIT, negativeQuantity);
    }
}
