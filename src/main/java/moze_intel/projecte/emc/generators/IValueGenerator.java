package moze_intel.projecte.emc.generators;

import java.util.Map;

public interface IValueGenerator<T, V extends Comparable<V>> {

    public Map<T, V> generateValues();
}
