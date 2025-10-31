package example.backend_mini_app.base;

@FunctionalInterface
public interface Task<T> {
    T run() throws Exception;
}