public class ArrayLessThanException extends Exception {
    public ArrayLessThanException() {}
    public String toString () { return "Ошибка: массив меньше, чем необходимо.";}
    public String symbol;
}
