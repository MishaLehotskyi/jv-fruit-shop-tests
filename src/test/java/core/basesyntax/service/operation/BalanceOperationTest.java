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

public class BalanceOperationTest {
    private static final String VALID_FRUIT = "apple";
    private static final Integer VALID_QUANTITY = 100;
    private static final String EMPTY_FRUIT = "";
    private static final String SPACES_FRUIT = "   ";

    private FruitDao fruitDao;
    private BalanceOperation balanceOperation;

    @BeforeEach
    void setup() {
        this.fruitDao = mock(FruitDao.class);
        this.balanceOperation = new BalanceOperation(fruitDao);
    }

    // ==================== NULL TRANSACTION TESTS ====================

    @Test
    void handle_nullTransaction_notOk() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> balanceOperation.handle(null));

        assertTrue(exception.getMessage().contains("transaction"));
        assertTrue(exception.getMessage().contains("must not be null"));

        // Verify DAO was never called
        verify(fruitDao, never()).set(anyString(), anyInt());
    }

    // ==================== NULL FRUIT NAME TESTS ====================

    @Test
    void handle_transactionWithNullFruit_notOk() {
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.BALANCE,
                null,
                VALID_QUANTITY
        );

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> balanceOperation.handle(transaction));

        assertTrue(exception.getMessage().contains("transaction.fruit"));
        assertTrue(exception.getMessage().contains("must not be null or blank"));

        // Verify DAO was never called
        verify(fruitDao, never()).set(anyString(), anyInt());
    }

    @Test
    void handle_transactionWithEmptyFruit_notOk() {
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.BALANCE,
                EMPTY_FRUIT,
                VALID_QUANTITY
        );

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> balanceOperation.handle(transaction));

        assertTrue(exception.getMessage().contains("transaction.fruit"));
        assertTrue(exception.getMessage().contains("must not be null or blank"));

        // Verify DAO was never called
        verify(fruitDao, never()).set(anyString(), anyInt());
    }

    @Test
    void handle_transactionWithBlankFruit_notOk() {
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.BALANCE,
                SPACES_FRUIT,
                VALID_QUANTITY
        );

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> balanceOperation.handle(transaction));

        assertTrue(exception.getMessage().contains("transaction.fruit"));
        assertTrue(exception.getMessage().contains("must not be null or blank"));

        // Verify DAO was never called
        verify(fruitDao, never()).set(anyString(), anyInt());
    }

    // ==================== VALID TRANSACTION TESTS ====================

    @Test
    void handle_validTransaction_ok() {
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.BALANCE,
                VALID_FRUIT,
                VALID_QUANTITY
        );

        balanceOperation.handle(transaction);

        verify(fruitDao, times(1)).set(VALID_FRUIT, VALID_QUANTITY);
    }

    @Test
    void handle_transactionWithZeroQuantity_ok() {
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.BALANCE,
                VALID_FRUIT,
                0
        );

        balanceOperation.handle(transaction);

        verify(fruitDao, times(1)).set(VALID_FRUIT, 0);
    }

    @Test
    void handle_transactionWithLargeQuantity_ok() {
        Integer largeQuantity = 1000000;
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.BALANCE,
                VALID_FRUIT,
                largeQuantity
        );

        balanceOperation.handle(transaction);

        verify(fruitDao, times(1)).set(VALID_FRUIT, largeQuantity);
    }

    @Test
    void handle_transactionWithDifferentFruits_ok() {
        FruitTransaction transaction1 = new FruitTransaction(
                FruitTransaction.Operation.BALANCE,
                "apple",
                100
        );
        FruitTransaction transaction2 = new FruitTransaction(
                FruitTransaction.Operation.BALANCE,
                "banana",
                50
        );

        balanceOperation.handle(transaction1);
        balanceOperation.handle(transaction2);

        verify(fruitDao, times(1)).set("apple", 100);
        verify(fruitDao, times(1)).set("banana", 50);
    }

    @Test
    void handle_multipleSameFruitTransactions_ok() {
        FruitTransaction transaction1 = new FruitTransaction(
                FruitTransaction.Operation.BALANCE,
                VALID_FRUIT,
                100
        );
        FruitTransaction transaction2 = new FruitTransaction(
                FruitTransaction.Operation.BALANCE,
                VALID_FRUIT,
                200
        );

        balanceOperation.handle(transaction1);
        balanceOperation.handle(transaction2);

        verify(fruitDao, times(1)).set(VALID_FRUIT, 100);
        verify(fruitDao, times(1)).set(VALID_FRUIT, 200);
        verify(fruitDao, times(2)).set(anyString(), anyInt());
    }

    // ==================== FRUIT NAME VARIATIONS TESTS ====================

    @Test
    void handle_fruitWithWhitespace_ok() {
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.BALANCE,
                "  apple  ",
                VALID_QUANTITY
        );

        balanceOperation.handle(transaction);

        // Should pass the fruit name as-is (trimming is DAO's responsibility)
        verify(fruitDao, times(1)).set("  apple  ", VALID_QUANTITY);
    }

    @Test
    void handle_fruitWithSpecialCharacters_ok() {
        String specialFruit = "green-apple";
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.BALANCE,
                specialFruit,
                VALID_QUANTITY
        );

        balanceOperation.handle(transaction);

        verify(fruitDao, times(1)).set(specialFruit, VALID_QUANTITY);
    }

    @Test
    void handle_fruitWithSpaces_ok() {
        String spacedFruit = "lady finger banana";
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.BALANCE,
                spacedFruit,
                VALID_QUANTITY
        );

        balanceOperation.handle(transaction);

        verify(fruitDao, times(1)).set(spacedFruit, VALID_QUANTITY);
    }

    // ==================== OPERATION TYPE TESTS ====================

    @Test
    void handle_transactionWithDifferentOperationType_ok() {
        // Even though it's a SUPPLY operation, BalanceOperation should still process it
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.SUPPLY,
                VALID_FRUIT,
                VALID_QUANTITY
        );

        balanceOperation.handle(transaction);

        verify(fruitDao, times(1)).set(VALID_FRUIT, VALID_QUANTITY);
    }

    @Test
    void handle_transactionWithPurchaseOperation_ok() {
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.PURCHASE,
                VALID_FRUIT,
                VALID_QUANTITY
        );

        balanceOperation.handle(transaction);

        verify(fruitDao, times(1)).set(VALID_FRUIT, VALID_QUANTITY);
    }

    @Test
    void handle_transactionWithReturnOperation_ok() {
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.RETURN,
                VALID_FRUIT,
                VALID_QUANTITY
        );

        balanceOperation.handle(transaction);

        verify(fruitDao, times(1)).set(VALID_FRUIT, VALID_QUANTITY);
    }

    // ==================== NEGATIVE QUANTITY TESTS ====================

    @Test
    void handle_transactionWithNegativeQuantity_ok() {
        // BalanceOperation doesn't validate quantity - that's DAO's job
        Integer negativeQuantity = -10;
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.BALANCE,
                VALID_FRUIT,
                negativeQuantity
        );

        balanceOperation.handle(transaction);

        verify(fruitDao, times(1)).set(VALID_FRUIT, negativeQuantity);
    }
}