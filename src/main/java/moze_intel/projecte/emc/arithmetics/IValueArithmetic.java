package moze_intel.projecte.emc.arithmetics;

public interface IValueArithmetic<T extends Comparable<T>> {

    public boolean isZero(T value);

    public T getZero();

    public T add(T a, T b);

    public T mul(long a, T b);

    public T div(T a, long b);

    public T getFree();

    public boolean isFree(T value);
}
