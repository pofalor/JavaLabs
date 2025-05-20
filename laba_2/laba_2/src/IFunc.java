public interface IFunc {
    void CheckLessThan(String[] args) throws LessThanException;

    void CheckLen(String[] args) throws ArrayLessThanException;

    void CheckLiterals(String[] args) throws ArrayHasLiteralException;
}