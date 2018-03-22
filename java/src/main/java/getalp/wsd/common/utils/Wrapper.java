package getalp.wsd.common.utils;

public class Wrapper<T>
{
    public Wrapper(T obj) { this.obj = obj; }
    public Wrapper() { this(null); }
    public T obj;
    public String toString() { return obj.toString(); }
}
