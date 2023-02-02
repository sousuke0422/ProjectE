package moze_intel.projecte.emc.mappers.customConversions.json;

import java.util.Map;

import moze_intel.projecte.emc.NormalizedSimpleStack;

import com.google.common.collect.Maps;

public class CustomConversion {

    public int count = 1;
    public String output;
    public Map<String, Integer> ingredients;
    public transient boolean evalOD = false;

    public static CustomConversion getFor(int count, NormalizedSimpleStack output,
            Map<NormalizedSimpleStack, Integer> ingredients) {
        CustomConversion conversion = new CustomConversion();
        conversion.count = count;
        conversion.output = output.json();
        conversion.ingredients = Maps.newHashMap();
        for (Map.Entry<NormalizedSimpleStack, Integer> entry : ingredients.entrySet()) {
            conversion.ingredients.put(entry.getKey().json(), entry.getValue());
        }
        return conversion;
    }
}
