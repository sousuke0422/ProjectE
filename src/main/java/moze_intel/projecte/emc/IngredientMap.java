package moze_intel.projecte.emc;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Maps;

public class IngredientMap<T> {

    protected HashMap<T, Integer> ingredientsWithAmount = Maps.newHashMap();

    public void addIngredient(T stackNorm, int amount) {
        int count = amount;
        if (ingredientsWithAmount.containsKey(stackNorm)) {
            count += ingredientsWithAmount.get(stackNorm);
        }
        ingredientsWithAmount.put(stackNorm, count);
    }

    public Map<T, Integer> getMap() {
        return Maps.newHashMap(ingredientsWithAmount);
    }

    public String toString() {
        return ingredientsWithAmount.toString();
    }
}
