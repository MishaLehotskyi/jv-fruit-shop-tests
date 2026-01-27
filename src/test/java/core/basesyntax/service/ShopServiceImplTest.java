package core.basesyntax.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import core.basesyntax.model.FruitTransaction;
import core.basesyntax.service.operation.OperationHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class ShopServiceImplTest {
    private static final String VALID_FRUIT = "apple";
    private static final Integer VALID_QUANTITY = 100;

    private OperationStrategy operationStrategy;
    private OperationHandler balanceHandler;
    private OperationHandler supplyHandler;
    private OperationHandler purchaseHandler;
    private OperationHandler returnHandler;
    private ShopServiceImpl shopService;

    @BeforeEach
    void setup() {
        this.operationStrategy = mock(OperationStrategy.class);
        this.balanceHandler = mock(OperationHandler.class);
        this.supplyHandler = mock(OperationHandler.class);
        this.purchaseHandler = mock(OperationHandler.class);
        this.returnHandler = mock(OperationHandler.class);

        // Setup default behavior for strategy
        when(operationStrategy.get(FruitTransaction.Operation.BALANCE))
                .thenReturn(balanceHandler);
        when(operationStrategy.get(FruitTransaction.Operation.SUPPLY))
                .thenReturn(supplyHandler);
        when(operationStrategy.get(FruitTransaction.Operation.PURCHASE))
                .thenReturn(purchaseHandler);
        when(operationStrategy.get(FruitTransaction.Operation.RETURN))
                .thenReturn(returnHandler);

        this.shopService = new ShopServiceImpl(operationStrategy);
    }

    // ==================== CONSTRUCTOR TESTS ====================

    @Test
    void constructor_nullOperationStrategy_notOk() {
        Exception exception = assertThrows(NullPointerException.class,
                () -> new ShopServiceImpl(null));

        assertTrue(exception.getMessage().contains("operationStrategy"));
        assertTrue(exception.getMessage().contains("must not be null"));
    }

    @Test
    void constructor_validOperationStrategy_ok() {
        ShopServiceImpl service = new ShopServiceImpl(operationStrategy);

        assertTrue(service != null);
    }

    // ==================== PROCESS METHOD - NULL LIST TESTS ====================

    @Test
    void process_nullTransactionsList_notOk() {
        Exception exception = assertThrows(NullPointerException.class,
                () -> shopService.process(null));

        assertTrue(exception.getMessage().contains("transactions"));
        assertTrue(exception.getMessage().contains("must not be null"));

        // Verify no handlers were called
        verify(balanceHandler, never()).handle(any());
        verify(supplyHandler, never()).handle(any());
        verify(purchaseHandler, never()).handle(any());
        verify(returnHandler, never()).handle(any());
    }

    // ==================== PROCESS METHOD - EMPTY LIST TESTS ====================

    @Test
    void process_emptyTransactionsList_ok() {
        List<FruitTransaction> transactions = new ArrayList<>();

        shopService.process(transactions);

        // Verify no handlers were called
        verify(balanceHandler, never()).handle(any());
        verify(supplyHandler, never()).handle(any());
        verify(purchaseHandler, never()).handle(any());
        verify(returnHandler, never()).handle(any());
    }

    // ==================== PROCESS METHOD - NULL TRANSACTION TESTS ====================

    @Test
    void process_listWithNullTransaction_notOk() {
        List<FruitTransaction> transactions = new ArrayList<>();
        transactions.add(null);

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> shopService.process(transactions));

        assertTrue(exception.getMessage().contains("Transaction at index"));
        assertTrue(exception.getMessage().contains("1"));
        assertTrue(exception.getMessage().contains("is null"));

        // Verify no handlers were called
        verify(balanceHandler, never()).handle(any());
    }

    @Test
    void process_listWithNullTransactionAtSecondPosition_notOk() {
        List<FruitTransaction> transactions = new ArrayList<>();
        transactions.add(new FruitTransaction(
                FruitTransaction.Operation.BALANCE, VALID_FRUIT, VALID_QUANTITY));
        transactions.add(null);

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> shopService.process(transactions));

        assertTrue(exception.getMessage().contains("Transaction at index"));
        assertTrue(exception.getMessage().contains("2"));
        assertTrue(exception.getMessage().contains("is null"));

        // First transaction should have been processed
        verify(balanceHandler, times(1)).handle(any());
    }

    // ==================== PROCESS METHOD - NULL OPERATION TESTS ====================

    @Test
    void process_transactionWithNullOperation_notOk() {
        List<FruitTransaction> transactions = new ArrayList<>();
        FruitTransaction transaction = new FruitTransaction(null, VALID_FRUIT, VALID_QUANTITY);
        transactions.add(transaction);

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> shopService.process(transactions));

        assertTrue(exception.getMessage().contains("Operation at transaction index"));
        assertTrue(exception.getMessage().contains("1"));
        assertTrue(exception.getMessage().contains("is null"));

        // Verify no handlers were called
        verify(balanceHandler, never()).handle(any());
    }

    @Test
    void process_secondTransactionWithNullOperation_notOk() {
        List<FruitTransaction> transactions = new ArrayList<>();
        transactions.add(new FruitTransaction(
                FruitTransaction.Operation.BALANCE, VALID_FRUIT, VALID_QUANTITY));
        transactions.add(new FruitTransaction(null, "banana", 50));

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> shopService.process(transactions));

        assertTrue(exception.getMessage().contains("Operation at transaction index"));
        assertTrue(exception.getMessage().contains("2"));
        assertTrue(exception.getMessage().contains("is null"));

        // First transaction should have been processed
        verify(balanceHandler, times(1)).handle(any());
    }

    // ==================== PROCESS METHOD - SINGLE TRANSACTION TESTS ====================

    @Test
    void process_singleBalanceTransaction_ok() {
        List<FruitTransaction> transactions = new ArrayList<>();
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.BALANCE, VALID_FRUIT, VALID_QUANTITY);
        transactions.add(transaction);

        shopService.process(transactions);

        verify(operationStrategy, times(1)).get(FruitTransaction.Operation.BALANCE);
        verify(balanceHandler, times(1)).handle(transaction);
        verify(supplyHandler, never()).handle(any());
        verify(purchaseHandler, never()).handle(any());
        verify(returnHandler, never()).handle(any());
    }

    @Test
    void process_singleSupplyTransaction_ok() {
        List<FruitTransaction> transactions = new ArrayList<>();
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.SUPPLY, VALID_FRUIT, VALID_QUANTITY);
        transactions.add(transaction);

        shopService.process(transactions);

        verify(operationStrategy, times(1)).get(FruitTransaction.Operation.SUPPLY);
        verify(supplyHandler, times(1)).handle(transaction);
        verify(balanceHandler, never()).handle(any());
    }

    @Test
    void process_singlePurchaseTransaction_ok() {
        List<FruitTransaction> transactions = new ArrayList<>();
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.PURCHASE, VALID_FRUIT, VALID_QUANTITY);
        transactions.add(transaction);

        shopService.process(transactions);

        verify(operationStrategy, times(1)).get(FruitTransaction.Operation.PURCHASE);
        verify(purchaseHandler, times(1)).handle(transaction);
        verify(balanceHandler, never()).handle(any());
    }

    @Test
    void process_singleReturnTransaction_ok() {
        List<FruitTransaction> transactions = new ArrayList<>();
        FruitTransaction transaction = new FruitTransaction(
                FruitTransaction.Operation.RETURN, VALID_FRUIT, VALID_QUANTITY);
        transactions.add(transaction);

        shopService.process(transactions);

        verify(operationStrategy, times(1)).get(FruitTransaction.Operation.RETURN);
        verify(returnHandler, times(1)).handle(transaction);
        verify(balanceHandler, never()).handle(any());
    }

    // ==================== PROCESS METHOD - MULTIPLE TRANSACTIONS TESTS ====================

    @Test
    void process_multipleTransactionsSameType_ok() {
        List<FruitTransaction> transactions = new ArrayList<>();
        FruitTransaction transaction1 = new FruitTransaction(
                FruitTransaction.Operation.BALANCE, "apple", 100);
        FruitTransaction transaction2 = new FruitTransaction(
                FruitTransaction.Operation.BALANCE, "banana", 50);
        transactions.add(transaction1);
        transactions.add(transaction2);

        shopService.process(transactions);

        verify(operationStrategy, times(2)).get(FruitTransaction.Operation.BALANCE);
        verify(balanceHandler, times(1)).handle(transaction1);
        verify(balanceHandler, times(1)).handle(transaction2);
    }

    @Test
    void process_multipleTransactionsDifferentTypes_ok() {
        List<FruitTransaction> transactions = new ArrayList<>();
        FruitTransaction balanceTransaction = new FruitTransaction(
                FruitTransaction.Operation.BALANCE, "apple", 100);
        FruitTransaction supplyTransaction = new FruitTransaction(
                FruitTransaction.Operation.SUPPLY, "banana", 50);
        FruitTransaction purchaseTransaction = new FruitTransaction(
                FruitTransaction.Operation.PURCHASE, "orange", 30);
        FruitTransaction returnTransaction = new FruitTransaction(
                FruitTransaction.Operation.RETURN, "grape", 20);

        transactions.add(balanceTransaction);
        transactions.add(supplyTransaction);
        transactions.add(purchaseTransaction);
        transactions.add(returnTransaction);

        shopService.process(transactions);

        verify(balanceHandler, times(1)).handle(balanceTransaction);
        verify(supplyHandler, times(1)).handle(supplyTransaction);
        verify(purchaseHandler, times(1)).handle(purchaseTransaction);
        verify(returnHandler, times(1)).handle(returnTransaction);
    }

    @Test
    void process_manyTransactions_ok() {
        List<FruitTransaction> transactions = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            transactions.add(new FruitTransaction(
                    FruitTransaction.Operation.BALANCE, "fruit" + i, i * 10));
        }

        shopService.process(transactions);

        verify(operationStrategy, times(10)).get(FruitTransaction.Operation.BALANCE);
        verify(balanceHandler, times(10)).handle(any());
    }

    // ==================== PROCESS METHOD - MIXED VALID AND INVALID TESTS ====================

    @Test
    void process_validThenNullTransaction_stopsAtNull() {
        List<FruitTransaction> transactions = new ArrayList<>();
        FruitTransaction validTransaction = new FruitTransaction(
                FruitTransaction.Operation.BALANCE, VALID_FRUIT, VALID_QUANTITY);
        transactions.add(validTransaction);
        transactions.add(null);
        transactions.add(new FruitTransaction(
                FruitTransaction.Operation.SUPPLY, "banana", 50));

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> shopService.process(transactions));

        assertTrue(exception.getMessage().contains("Transaction at index"));
        assertTrue(exception.getMessage().contains("2"));

        // Only first transaction should have been processed
        verify(balanceHandler, times(1)).handle(validTransaction);
        verify(supplyHandler, never()).handle(any());
    }

    // ==================== PROCESS METHOD - ORDER PRESERVATION TESTS ====================

    @Test
    void process_maintainsTransactionOrder_ok() {
        List<FruitTransaction> transactions = new ArrayList<>();
        FruitTransaction transaction1 = new FruitTransaction(
                FruitTransaction.Operation.BALANCE, "apple", 100);
        FruitTransaction transaction2 = new FruitTransaction(
                FruitTransaction.Operation.SUPPLY, "banana", 50);
        FruitTransaction transaction3 = new FruitTransaction(
                FruitTransaction.Operation.PURCHASE, "orange", 30);

        transactions.add(transaction1);
        transactions.add(transaction2);
        transactions.add(transaction3);

        shopService.process(transactions);

        // Verify order by using InOrder
        org.mockito.InOrder inOrder = org.mockito.Mockito.inOrder(
                balanceHandler, supplyHandler, purchaseHandler);

        inOrder.verify(balanceHandler).handle(transaction1);
        inOrder.verify(supplyHandler).handle(transaction2);
        inOrder.verify(purchaseHandler).handle(transaction3);
    }

    // ==================== INTEGRATION WITH STRATEGY TESTS ====================

    @Test
    void process_callsStrategyForEachTransaction_ok() {
        List<FruitTransaction> transactions = new ArrayList<>();
        transactions.add(new FruitTransaction(
                FruitTransaction.Operation.BALANCE, "apple", 100));
        transactions.add(new FruitTransaction(
                FruitTransaction.Operation.SUPPLY, "banana", 50));

        shopService.process(transactions);

        verify(operationStrategy, times(1)).get(FruitTransaction.Operation.BALANCE);
        verify(operationStrategy, times(1)).get(FruitTransaction.Operation.SUPPLY);
    }
}