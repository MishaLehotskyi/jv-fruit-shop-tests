package core.basesyntax.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class FruitDBTest {
    private static final String VALID_FRUIT = "apple";
    private static final Integer VALID_QUANTITY = 100;
    private static final Integer ZERO_QUANTITY = 0;
    private static final Integer NEGATIVE_QUANTITY = -10;

    private Map<String, Integer> fruits;
    private FruitDB fruitDB;

    @BeforeEach
    void setup() {
        this.fruits = new HashMap<>();
        this.fruitDB = new FruitDB(fruits);
    }

    @Test
    void constructor_emptyMap_ok() {
        FruitDB db = new FruitDB(new HashMap<>());

        assertTrue(db.getFruits().isEmpty());
    }

    @Test
    void constructor_prePopulatedMap_ok() {
        Map<String, Integer> prePopulated = new HashMap<>();
        prePopulated.put("apple", 50);
        prePopulated.put("banana", 30);

        FruitDB db = new FruitDB(prePopulated);

        assertEquals(50, db.getQuantity("apple"));
        assertEquals(30, db.getQuantity("banana"));
    }

    // ==================== SET QUANTITY METHOD TESTS ====================

    @Test
    void setQuantity_validFruitAndQuantity_ok() {
        fruitDB.setQuantity(VALID_FRUIT, VALID_QUANTITY);

        assertEquals(VALID_QUANTITY, fruitDB.getQuantity(VALID_FRUIT));
    }

    @Test
    void setQuantity_nullFruit_ok() {
        // Map allows null keys, so this should work
        fruitDB.setQuantity(null, VALID_QUANTITY);

        assertEquals(VALID_QUANTITY, fruitDB.getQuantity(null));
    }

    @Test
    void setQuantity_emptyStringFruit_ok() {
        fruitDB.setQuantity("", VALID_QUANTITY);

        assertEquals(VALID_QUANTITY, fruitDB.getQuantity(""));
    }

    @Test
    void setQuantity_nullQuantity_ok() {
        // Map allows null values, so this should work
        fruitDB.setQuantity(VALID_FRUIT, null);

        assertNull(fruitDB.getQuantity(VALID_FRUIT));
    }

    @Test
    void setQuantity_zeroQuantity_ok() {
        fruitDB.setQuantity(VALID_FRUIT, ZERO_QUANTITY);

        assertEquals(ZERO_QUANTITY, fruitDB.getQuantity(VALID_FRUIT));
    }

    @Test
    void setQuantity_negativeQuantity_ok() {
        // FruitDB doesn't validate, it just stores
        fruitDB.setQuantity(VALID_FRUIT, NEGATIVE_QUANTITY);

        assertEquals(NEGATIVE_QUANTITY, fruitDB.getQuantity(VALID_FRUIT));
    }

    @Test
    void setQuantity_updateExistingFruit_ok() {
        fruitDB.setQuantity(VALID_FRUIT, 50);
        fruitDB.setQuantity(VALID_FRUIT, VALID_QUANTITY);

        assertEquals(VALID_QUANTITY, fruitDB.getQuantity(VALID_FRUIT));
    }

    @Test
    void setQuantity_multipleFruits_ok() {
        fruitDB.setQuantity("apple", 100);
        fruitDB.setQuantity("banana", 50);
        fruitDB.setQuantity("orange", 75);

        assertEquals(100, fruitDB.getQuantity("apple"));
        assertEquals(50, fruitDB.getQuantity("banana"));
        assertEquals(75, fruitDB.getQuantity("orange"));
    }

    // ==================== GET QUANTITY METHOD TESTS ====================

    @Test
    void getQuantity_existingFruit_ok() {
        fruitDB.setQuantity(VALID_FRUIT, VALID_QUANTITY);

        assertEquals(VALID_QUANTITY, fruitDB.getQuantity(VALID_FRUIT));
    }

    @Test
    void getQuantity_nonExistingFruit_ok() {
        Integer quantity = fruitDB.getQuantity("nonexistent");

        assertNull(quantity);
    }

    @Test
    void getQuantity_nullFruit_ok() {
        Integer quantity = fruitDB.getQuantity(null);

        assertNull(quantity);
    }

    @Test
    void getQuantity_emptyStringFruit_ok() {
        Integer quantity = fruitDB.getQuantity("");

        assertNull(quantity);
    }

    @Test
    void getQuantity_afterUpdate_ok() {
        fruitDB.setQuantity(VALID_FRUIT, 50);
        fruitDB.setQuantity(VALID_FRUIT, VALID_QUANTITY);

        assertEquals(VALID_QUANTITY, fruitDB.getQuantity(VALID_FRUIT));
    }

    // ==================== GET FRUITS METHOD TESTS ====================

    @Test
    void getFruits_emptyDatabase_ok() {
        Map<String, Integer> result = fruitDB.getFruits();

        assertTrue(result.isEmpty());
    }

    @Test
    void getFruits_singleFruit_ok() {
        fruitDB.setQuantity(VALID_FRUIT, VALID_QUANTITY);

        Map<String, Integer> result = fruitDB.getFruits();

        assertEquals(1, result.size());
        assertEquals(VALID_QUANTITY, result.get(VALID_FRUIT));
    }

    @Test
    void getFruits_multipleFruits_ok() {
        fruitDB.setQuantity("apple", 100);
        fruitDB.setQuantity("banana", 50);
        fruitDB.setQuantity("orange", 75);

        Map<String, Integer> result = fruitDB.getFruits();

        assertEquals(3, result.size());
        assertEquals(100, result.get("apple"));
        assertEquals(50, result.get("banana"));
        assertEquals(75, result.get("orange"));
    }

    @Test
    void getFruits_returnsImmutableCopy_ok() {
        fruitDB.setQuantity(VALID_FRUIT, VALID_QUANTITY);

        Map<String, Integer> result = fruitDB.getFruits();

        // Verify it's a different instance (copy)
        assertNotSame(fruits, result);

        // Verify modifying the returned map doesn't affect the database
        Exception exception = assertThrows(UnsupportedOperationException.class,
                () -> result.put("banana", 50));

        // Original database should be unchanged
        assertNull(fruitDB.getQuantity("banana"));
    }

    @Test
    void getFruits_afterOperations_ok() {
        fruitDB.setQuantity("apple", 100);
        fruitDB.setQuantity("banana", 50);
        fruitDB.setQuantity("apple", 120); // update

        Map<String, Integer> result = fruitDB.getFruits();

        assertEquals(2, result.size());
        assertEquals(120, result.get("apple"));
        assertEquals(50, result.get("banana"));
    }

    @Test
    void getFruits_multipleCalls_ok() {
        fruitDB.setQuantity(VALID_FRUIT, VALID_QUANTITY);

        Map<String, Integer> result1 = fruitDB.getFruits();
        Map<String, Integer> result2 = fruitDB.getFruits();

        // Each call should return a new copy
        assertNotSame(result1, result2);

        // But with the same contents
        assertEquals(result1, result2);
    }

    // ==================== INTEGRATION TESTS ====================

    @Test
    void integration_setGetSequence_ok() {
        // Set initial values
        fruitDB.setQuantity("apple", 100);
        fruitDB.setQuantity("banana", 50);

        // Get individual quantities
        assertEquals(100, fruitDB.getQuantity("apple"));
        assertEquals(50, fruitDB.getQuantity("banana"));

        // Update values
        fruitDB.setQuantity("apple", 150);

        // Verify update
        assertEquals(150, fruitDB.getQuantity("apple"));
        assertEquals(50, fruitDB.getQuantity("banana"));

        // Get all fruits
        Map<String, Integer> allFruits = fruitDB.getFruits();
        assertEquals(2, allFruits.size());
        assertEquals(150, allFruits.get("apple"));
        assertEquals(50, allFruits.get("banana"));
    }

    @Test
    void integration_externalMapModification_affectsDatabase_ok() {
        // Modify the original map directly
        fruits.put("external", 999);

        // Should be reflected in the database since it's using the same reference
        assertEquals(999, fruitDB.getQuantity("external"));
    }
}
