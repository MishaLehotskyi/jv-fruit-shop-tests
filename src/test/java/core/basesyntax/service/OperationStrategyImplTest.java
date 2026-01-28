package core.basesyntax.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import core.basesyntax.model.FruitTransaction;
import core.basesyntax.service.operation.OperationHandler;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OperationStrategyImplTest {
    private Map<FruitTransaction.Operation, OperationHandler> operationHandlers;
    private OperationHandler balanceHandler;
    private OperationHandler supplyHandler;
    private OperationHandler purchaseHandler;
    private OperationHandler returnHandler;

    @BeforeEach
    void setup() {
        this.balanceHandler = mock(OperationHandler.class);
        this.supplyHandler = mock(OperationHandler.class);
        this.purchaseHandler = mock(OperationHandler.class);
        this.returnHandler = mock(OperationHandler.class);

        this.operationHandlers = new HashMap<>();
        this.operationHandlers.put(FruitTransaction.Operation.BALANCE, balanceHandler);
        this.operationHandlers.put(FruitTransaction.Operation.SUPPLY, supplyHandler);
        this.operationHandlers.put(FruitTransaction.Operation.PURCHASE, purchaseHandler);
        this.operationHandlers.put(FruitTransaction.Operation.RETURN, returnHandler);
    }

    // ==================== CONSTRUCTOR TESTS ====================

    @Test
    void constructor_nullOperationHandlers_notOk() {
        Exception exception = assertThrows(NullPointerException.class,
                () -> new OperationStrategyImpl(null));

        assertTrue(exception.getMessage().contains("operationHandlers"));
        assertTrue(exception.getMessage().contains("must not be null"));
    }

    @Test
    void constructor_emptyOperationHandlers_ok() {
        Map<FruitTransaction.Operation, OperationHandler> emptyMap = new HashMap<>();

        OperationStrategyImpl strategy = new OperationStrategyImpl(emptyMap);

        assertNotNull(strategy);
    }

    @Test
    void constructor_validOperationHandlers_ok() {
        OperationStrategyImpl strategy = new OperationStrategyImpl(operationHandlers);

        assertNotNull(strategy);
    }

    // ==================== GET METHOD - NULL OPERATION TESTS ====================

    @Test
    void get_nullOperation_notOk() {
        OperationStrategyImpl strategy = new OperationStrategyImpl(operationHandlers);

        Exception exception = assertThrows(NullPointerException.class,
                () -> strategy.get(null));

        assertTrue(exception.getMessage().contains("operation"));
        assertTrue(exception.getMessage().contains("must not be null"));
    }

    // ==================== GET METHOD - VALID OPERATIONS TESTS ====================

    @Test
    void get_balanceOperation_ok() {
        OperationStrategyImpl strategy = new OperationStrategyImpl(operationHandlers);

        OperationHandler handler = strategy.get(FruitTransaction.Operation.BALANCE);

        assertSame(balanceHandler, handler);
    }

    @Test
    void get_supplyOperation_ok() {
        OperationStrategyImpl strategy = new OperationStrategyImpl(operationHandlers);

        OperationHandler handler = strategy.get(FruitTransaction.Operation.SUPPLY);

        assertSame(supplyHandler, handler);
    }

    @Test
    void get_purchaseOperation_ok() {
        OperationStrategyImpl strategy = new OperationStrategyImpl(operationHandlers);

        OperationHandler handler = strategy.get(FruitTransaction.Operation.PURCHASE);

        assertSame(purchaseHandler, handler);
    }

    @Test
    void get_returnOperation_ok() {
        OperationStrategyImpl strategy = new OperationStrategyImpl(operationHandlers);

        OperationHandler handler = strategy.get(FruitTransaction.Operation.RETURN);

        assertSame(returnHandler, handler);
    }

    // ==================== GET METHOD - MISSING HANDLER TESTS ====================

    @Test
    void get_operationWithoutHandler_notOk() {
        Map<FruitTransaction.Operation, OperationHandler> incompleteMap = new HashMap<>();
        incompleteMap.put(FruitTransaction.Operation.BALANCE, balanceHandler);
        // SUPPLY, PURCHASE, and RETURN handlers are missing

        OperationStrategyImpl strategy = new OperationStrategyImpl(incompleteMap);

        Exception exception = assertThrows(IllegalStateException.class,
                () -> strategy.get(FruitTransaction.Operation.SUPPLY));

        assertTrue(exception.getMessage().contains("No handler registered"));
        assertTrue(exception.getMessage().contains("operation"));
        assertTrue(exception.getMessage().contains("SUPPLY"));
    }

    @Test
    void get_emptyMapAnyOperation_notOk() {
        Map<FruitTransaction.Operation, OperationHandler> emptyMap = new HashMap<>();
        OperationStrategyImpl strategy = new OperationStrategyImpl(emptyMap);

        Exception exception = assertThrows(IllegalStateException.class,
                () -> strategy.get(FruitTransaction.Operation.BALANCE));

        assertTrue(exception.getMessage().contains("No handler registered"));
        assertTrue(exception.getMessage().contains("operation"));
        assertTrue(exception.getMessage().contains("BALANCE"));
    }

    // ==================== GET METHOD - MULTIPLE CALLS TESTS ====================

    @Test
    void get_sameOperationMultipleTimes_ok() {
        OperationStrategyImpl strategy = new OperationStrategyImpl(operationHandlers);

        OperationHandler handler1 = strategy.get(FruitTransaction.Operation.BALANCE);
        OperationHandler handler2 = strategy.get(FruitTransaction.Operation.BALANCE);

        assertSame(handler1, handler2);
        assertSame(balanceHandler, handler1);
    }

    @Test
    void get_differentOperationsSequentially_ok() {
        OperationStrategyImpl strategy = new OperationStrategyImpl(operationHandlers);

        OperationHandler handler1 = strategy.get(FruitTransaction.Operation.BALANCE);
        assertSame(balanceHandler, handler1);
        OperationHandler handler2 = strategy.get(FruitTransaction.Operation.SUPPLY);
        assertSame(supplyHandler, handler2);
        OperationHandler handler3 = strategy.get(FruitTransaction.Operation.PURCHASE);
        assertSame(purchaseHandler, handler3);
        OperationHandler handler4 = strategy.get(FruitTransaction.Operation.RETURN);
        assertSame(returnHandler, handler4);
    }

    // ==================== MAP MODIFICATION TESTS ====================

    @Test
    void get_afterExternalMapModification_reflectsChanges() {
        // Using the same map reference that can be modified externally
        OperationStrategyImpl strategy = new OperationStrategyImpl(operationHandlers);

        // Verify initial state
        OperationHandler handler1 = strategy.get(FruitTransaction.Operation.BALANCE);
        assertSame(balanceHandler, handler1);

        // Modify the external map
        OperationHandler newBalanceHandler = mock(OperationHandler.class);
        operationHandlers.put(FruitTransaction.Operation.BALANCE, newBalanceHandler);

        // Strategy should reflect the change (since it holds a reference to the same map)
        OperationHandler handler2 = strategy.get(FruitTransaction.Operation.BALANCE);
        assertSame(newBalanceHandler, handler2);
    }

    @Test
    void get_afterExternalMapRemoval_throwsException() {
        OperationStrategyImpl strategy = new OperationStrategyImpl(operationHandlers);

        // Remove handler from external map
        operationHandlers.remove(FruitTransaction.Operation.BALANCE);

        Exception exception = assertThrows(IllegalStateException.class,
                () -> strategy.get(FruitTransaction.Operation.BALANCE));

        assertTrue(exception.getMessage().contains("No handler registered"));
        assertTrue(exception.getMessage().contains("BALANCE"));
    }

    // ==================== PARTIAL MAP TESTS ====================

    @Test
    void constructor_partialHandlersMap_ok() {
        Map<FruitTransaction.Operation, OperationHandler> partialMap = new HashMap<>();
        partialMap.put(FruitTransaction.Operation.BALANCE, balanceHandler);
        partialMap.put(FruitTransaction.Operation.SUPPLY, supplyHandler);
        // PURCHASE and RETURN are missing

        OperationStrategyImpl strategy = new OperationStrategyImpl(partialMap);

        // Should work for registered operations
        assertSame(balanceHandler, strategy.get(FruitTransaction.Operation.BALANCE));
        assertSame(supplyHandler, strategy.get(FruitTransaction.Operation.SUPPLY));

        // Should fail for unregistered operations
        assertThrows(IllegalStateException.class,
                () -> strategy.get(FruitTransaction.Operation.PURCHASE));
        assertThrows(IllegalStateException.class,
                () -> strategy.get(FruitTransaction.Operation.RETURN));
    }

    // ==================== NULL HANDLER VALUE TESTS ====================

    @Test
    void get_operationMappedToNull_notOk() {
        Map<FruitTransaction.Operation, OperationHandler> mapWithNull = new HashMap<>();
        mapWithNull.put(FruitTransaction.Operation.BALANCE, null);

        OperationStrategyImpl strategy = new OperationStrategyImpl(mapWithNull);

        Exception exception = assertThrows(IllegalStateException.class,
                () -> strategy.get(FruitTransaction.Operation.BALANCE));

        assertTrue(exception.getMessage().contains("No handler registered"));
        assertTrue(exception.getMessage().contains("BALANCE"));
    }

    // ==================== ALL OPERATIONS COVERAGE TEST ====================

    @Test
    void get_allOperations_ok() {
        OperationStrategyImpl strategy = new OperationStrategyImpl(operationHandlers);

        // Verify all operations can be retrieved
        Map<FruitTransaction.Operation, OperationHandler> retrievedHandlers = new HashMap<>();

        for (FruitTransaction.Operation operation : FruitTransaction.Operation.values()) {
            OperationHandler handler = strategy.get(operation);
            assertNotNull(handler);
            retrievedHandlers.put(operation, handler);
        }

        // Verify we got all 4 operations
        assertEquals(4, retrievedHandlers.size());
        assertSame(balanceHandler, retrievedHandlers.get(FruitTransaction.Operation.BALANCE));
        assertSame(supplyHandler, retrievedHandlers.get(FruitTransaction.Operation.SUPPLY));
        assertSame(purchaseHandler, retrievedHandlers.get(FruitTransaction.Operation.PURCHASE));
        assertSame(returnHandler, retrievedHandlers.get(FruitTransaction.Operation.RETURN));
    }
}
