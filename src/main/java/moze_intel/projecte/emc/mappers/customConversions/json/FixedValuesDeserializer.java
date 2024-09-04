package moze_intel.projecte.emc.mappers.customConversions.json;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

public class FixedValuesDeserializer implements JsonDeserializer<FixedValues> {

    @Override
    public FixedValues deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
        FixedValues fixed = new FixedValues();
        JsonObject o = json.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : o.entrySet()) {
            if (entry.getKey()
                .equals("before")) {
                fixed.setValueBefore = parseSetValueMap(
                    entry.getValue()
                        .getAsJsonObject());
            } else if (entry.getKey()
                .equals("after")) {
                    fixed.setValueAfter = parseSetValueMap(
                        entry.getValue()
                            .getAsJsonObject());
                } else if (entry.getKey()
                    .equals("conversion")) {
                        fixed.conversion = context.deserialize(
                            entry.getValue()
                                .getAsJsonArray(),
                            new TypeToken<List<CustomConversion>>() {}.getType());
                    } else {
                        throw new JsonParseException(
                            String.format("Can not parse \"%s\":%s in fixedValues", entry.getKey(), entry.getValue()));
                    }
        }
        return fixed;
    }

    Map<String, Long> parseSetValueMapFromObject(JsonObject o, String key) {
        if (o.has(key)) {
            return parseSetValueMap(o.getAsJsonObject(key));
        }
        return Maps.newHashMap();
    }

    Map<String, Long> parseSetValueMap(JsonObject o) {
        Map<String, Long> out = Maps.newHashMap();
        for (Map.Entry<String, JsonElement> entry : o.entrySet()) {
            JsonPrimitive primitive = entry.getValue()
                .getAsJsonPrimitive();
            if (primitive.isNumber()) {
                out.put(entry.getKey(), primitive.getAsLong());
                continue;
            } else if (primitive.isString()) {
                if (primitive.getAsString()
                    .toLowerCase()
                    .equals("free")) {
                    out.put(entry.getKey(), Long.MIN_VALUE); // TODO Get Value for 'free' from arithmetic?
                    continue;
                }
            }
            throw new JsonParseException("Could not parse " + o + " into 'free' or integer.");
        }
        return out;
    }
}
