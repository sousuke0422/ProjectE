package moze_intel.projecte.emc.pregenerated;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Map;

import moze_intel.projecte.emc.NormalizedSimpleStack;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class PregeneratedEMC {

    static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(NormalizedSimpleStack.class, new NSSJsonTypeAdapter().nullSafe())
            .enableComplexMapKeySerialization().setPrettyPrinting().create();

    public static boolean tryRead(File f, Map<NormalizedSimpleStack, Long> map) {
        try {
            Map<NormalizedSimpleStack, Long> m = read(f);
            map.clear();
            map.putAll(m);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<NormalizedSimpleStack, Long> read(File file) throws IOException {
        Type type = new TypeToken<Map<NormalizedSimpleStack, Long>>() {}.getType();
        FileReader reader = new FileReader(file);
        Map<NormalizedSimpleStack, Long> map = gson.fromJson(reader, type);
        reader.close();
        map.remove(null);
        return map;
    }

    public static void write(File file, Map<NormalizedSimpleStack, Long> map) throws IOException {
        Type type = new TypeToken<Map<NormalizedSimpleStack, Integer>>() {}.getType();
        FileWriter writer = new FileWriter(file);
        gson.toJson(map, type, writer);
        writer.close();
    }
}
