public class LessThanException extends Exception {
    public LessThanException () {}
    public String toString () { return "Ошибка: в массиве присутствует значение меньше требуемого.";}
    public int xNumber;
}
