package core.basesyntax.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.basesyntax.db.FruitDB;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FruitDaoImplTest {
    private static final String VALID_FRUIT = "apple";
    private static final String EMPTY_FRUIT = "";
    private static final String SPACES_FRUIT = "   ";
    private static final Integer VALID_QUANTITY = 100;
    private static final Integer NEGATIVE_QUANTITY = -10;
    private static final Integer ZERO_QUANTITY = 0;

    private FruitDB fruitDB;
    private FruitDaoImpl fruitDao;

    @BeforeEach
    void setup() {
        Map<String, Integer> fruits = new HashMap<>();
        this.fruitDB = new FruitDB(fruits);
        this.fruitDao = new FruitDaoImpl(fruitDB);
    }

    // ==================== SET METHOD TESTS ====================

    @Test
    void set_validFruitAndQuantity_ok() {
        fruitDao.set(VALID_FRUIT, VALID_QUANTITY);

        assertEquals(VALID_QUANTITY, fruitDB.getQuantity(VALID_FRUIT));
    }

    @Test
    void set_nullFruit_notOk() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> fruitDao.set(null, VALID_QUANTITY));

        assertTrue(exception.getMessage().contains("Fruit name"));
        assertTrue(exception.getMessage().contains("must not be null or blank"));
    }

    @Test
    void set_emptyFruit_notOk() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> fruitDao.set(EMPTY_FRUIT, VALID_QUANTITY));

        assertTrue(exception.getMessage().contains("Fruit name"));
        assertTrue(exception.getMessage().contains("must not be null or blank"));
    }

    @Test
    void set_blankFruit_notOk() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> fruitDao.set(SPACES_FRUIT, VALID_QUANTITY));

        assertTrue(exception.getMessage().contains("Fruit name"));
        assertTrue(exception.getMessage().contains("must not be null or blank"));
    }

    @Test
    void set_nullQuantity_notOk() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> fruitDao.set(VALID_FRUIT, null));

        assertTrue(exception.getMessage().contains("Quantity"));
        assertTrue(exception.getMessage().contains("must not be null"));
    }

    @Test
    void set_negativeQuantity_notOk() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> fruitDao.set(VALID_FRUIT, NEGATIVE_QUANTITY));

        assertTrue(exception.getMessage().contains("Quantity"));
        assertTrue(exception.getMessage().contains("must not be negative"));
        assertTrue(exception.getMessage().contains(NEGATIVE_QUANTITY.toString()));
    }

    @Test
    void set_zeroQuantity_ok() {
        fruitDao.set(VALID_FRUIT, ZERO_QUANTITY);

        assertEquals(ZERO_QUANTITY, fruitDB.getQuantity(VALID_FRUIT));
    }

    @Test
    void set_updateExistingFruit_ok() {
        fruitDao.set(VALID_FRUIT, 50);
        fruitDao.set(VALID_FRUIT, VALID_QUANTITY);

        assertEquals(VALID_QUANTITY, fruitDB.getQuantity(VALID_FRUIT));
    }

    // ==================== ADD METHOD TESTS ====================

    @Test
    void add_validFruitAndQuantity_ok() {
        fruitDao.add(VALID_FRUIT, VALID_QUANTITY);

        assertEquals(VALID_QUANTITY, fruitDB.getQuantity(VALID_FRUIT));
    }

    @Test
    void add_toExistingFruit_ok() {
        fruitDao.set(VALID_FRUIT, 50);
        fruitDao.add(VALID_FRUIT, 30);

        assertEquals(80, fruitDB.getQuantity(VALID_FRUIT));
    }

    @Test
    void add_toNonExistingFruit_ok() {
        fruitDao.add("banana", 25);

        assertEquals(25, fruitDB.getQuantity("banana"));
    }

    @Test
    void add_nullFruit_notOk() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> fruitDao.add(null, VALID_QUANTITY));

        assertTrue(exception.getMessage().contains("Fruit name"));
        assertTrue(exception.getMessage().contains("must not be null or blank"));
    }

    @Test
    void add_emptyFruit_notOk() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> fruitDao.add(EMPTY_FRUIT, VALID_QUANTITY));

        assertTrue(exception.getMessage().contains("Fruit name"));
        assertTrue(exception.getMessage().contains("must not be null or blank"));
    }

    @Test
    void add_blankFruit_notOk() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> fruitDao.add(SPACES_FRUIT, VALID_QUANTITY));

        assertTrue(exception.getMessage().contains("Fruit name"));
        assertTrue(exception.getMessage().contains("must not be null or blank"));
    }

    @Test
    void add_nullQuantity_notOk() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> fruitDao.add(VALID_FRUIT, null));

        assertTrue(exception.getMessage().contains("Quantity"));
        assertTrue(exception.getMessage().contains("must not be null"));
    }

    @Test
    void add_negativeQuantity_notOk() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> fruitDao.add(VALID_FRUIT, NEGATIVE_QUANTITY));

        assertTrue(exception.getMessage().contains("Quantity"));
        assertTrue(exception.getMessage().contains("must not be negative"));
        assertTrue(exception.getMessage().contains(NEGATIVE_QUANTITY.toString()));
    }

    @Test
    void add_zeroQuantity_ok() {
        fruitDao.set(VALID_FRUIT, 50);
        fruitDao.add(VALID_FRUIT, ZERO_QUANTITY);

        assertEquals(50, fruitDB.getQuantity(VALID_FRUIT));
    }

    // ==================== REMOVE METHOD TESTS ====================

    @Test
    void remove_validQuantityFromExisting_ok() {
        fruitDao.set(VALID_FRUIT, VALID_QUANTITY);
        fruitDao.remove(VALID_FRUIT, 30);

        assertEquals(70, fruitDB.getQuantity(VALID_FRUIT));
    }

    @Test
    void remove_entireQuantity_ok() {
        fruitDao.set(VALID_FRUIT, VALID_QUANTITY);
        fruitDao.remove(VALID_FRUIT, VALID_QUANTITY);

        assertEquals(0, fruitDB.getQuantity(VALID_FRUIT));
    }

    @Test
    void remove_moreThanAvailable_notOk() {
        fruitDao.set(VALID_FRUIT, 50);

        Exception exception = assertThrows(RuntimeException.class,
                () -> fruitDao.remove(VALID_FRUIT, 60));

        assertTrue(exception.getMessage().contains("Cannot remove"));
        assertTrue(exception.getMessage().contains("60"));
        assertTrue(exception.getMessage().contains(VALID_FRUIT));
        assertTrue(exception.getMessage().contains("negative balance"));
        assertTrue(exception.getMessage().contains("Current: 50"));
    }

    @Test
    void remove_fromNonExistingFruit_notOk() {
        Exception exception = assertThrows(RuntimeException.class,
                () -> fruitDao.remove("nonexistent", 10));

        assertTrue(exception.getMessage().contains("Cannot remove"));
        assertTrue(exception.getMessage().contains("10"));
        assertTrue(exception.getMessage().contains("nonexistent"));
        assertTrue(exception.getMessage().contains("negative balance"));
        assertTrue(exception.getMessage().contains("Current: 0"));
    }

    @Test
    void remove_nullFruit_notOk() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> fruitDao.remove(null, VALID_QUANTITY));

        assertTrue(exception.getMessage().contains("Fruit name"));
        assertTrue(exception.getMessage().contains("must not be null or blank"));
    }

    @Test
    void remove_emptyFruit_notOk() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> fruitDao.remove(EMPTY_FRUIT, VALID_QUANTITY));

        assertTrue(exception.getMessage().contains("Fruit name"));
        assertTrue(exception.getMessage().contains("must not be null or blank"));
    }

    @Test
    void remove_blankFruit_notOk() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> fruitDao.remove(SPACES_FRUIT, VALID_QUANTITY));

        assertTrue(exception.getMessage().contains("Fruit name"));
        assertTrue(exception.getMessage().contains("must not be null or blank"));
    }

    @Test
    void remove_nullQuantity_notOk() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> fruitDao.remove(VALID_FRUIT, null));

        assertTrue(exception.getMessage().contains("Quantity"));
        assertTrue(exception.getMessage().contains("must not be null"));
    }

    @Test
    void remove_negativeQuantity_notOk() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> fruitDao.remove(VALID_FRUIT, NEGATIVE_QUANTITY));

        assertTrue(exception.getMessage().contains("Quantity"));
        assertTrue(exception.getMessage().contains("must not be negative"));
        assertTrue(exception.getMessage().contains(NEGATIVE_QUANTITY.toString()));
    }

    @Test
    void remove_zeroQuantity_ok() {
        fruitDao.set(VALID_FRUIT, 50);
        fruitDao.remove(VALID_FRUIT, ZERO_QUANTITY);

        assertEquals(50, fruitDB.getQuantity(VALID_FRUIT));
    }

    // ==================== GET FRUITS METHOD TESTS ====================

    @Test
    void getFruits_emptyDatabase_ok() {
        Map<String, Integer> fruits = fruitDao.getFruits();

        assertTrue(fruits.isEmpty());
    }

    @Test
    void getFruits_singleFruit_ok() {
        fruitDao.set(VALID_FRUIT, VALID_QUANTITY);

        Map<String, Integer> fruits = fruitDao.getFruits();

        assertEquals(1, fruits.size());
        assertEquals(VALID_QUANTITY, fruits.get(VALID_FRUIT));
    }

    @Test
    void getFruits_multipleFruits_ok() {
        fruitDao.set("apple", 100);
        fruitDao.set("banana", 50);
        fruitDao.set("orange", 75);

        Map<String, Integer> fruits = fruitDao.getFruits();

        assertEquals(3, fruits.size());
        assertEquals(100, fruits.get("apple"));
        assertEquals(50, fruits.get("banana"));
        assertEquals(75, fruits.get("orange"));
    }

    @Test
    void getFruits_afterOperations_ok() {
        fruitDao.set("apple", 100);
        fruitDao.add("apple", 50);
        fruitDao.remove("apple", 30);
        fruitDao.set("banana", 200);

        Map<String, Integer> fruits = fruitDao.getFruits();

        assertEquals(2, fruits.size());
        assertEquals(120, fruits.get("apple"));
        assertEquals(200, fruits.get("banana"));
    }
}
