public class ArrayHasLiteralException extends Exception {
    public ArrayHasLiteralException() {}
    public String toString () { return "Ошибка: в строке есть литеры.";}
    public String symbol;
}