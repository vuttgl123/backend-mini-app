package example.backend_mini_app.base;

public record Result<T>(boolean success, T value, Throwable error) {
    public static <T> Result<T> ok(T value) { return new Result<>(true, value, null); }
    public static <T> Result<T> fail(Throwable error) { return new Result<>(false, null, error); }
}