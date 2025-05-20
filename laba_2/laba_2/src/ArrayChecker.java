import java.util.Arrays;

public class ArrayChecker implements IConst, IFunc {
    public void CheckLessThan(String[] args) throws LessThanException {
        for (String x : args) {
            var numb = Integer.parseInt(x);
            if (numb < minAllowed) {
                throw new LessThanException();
            }
        }
    }

    public void CheckLen(String[] args) throws ArrayLessThanException {
        if (args.length < minAllowedLen) {
            throw new ArrayLessThanException();
        }
    }

    public void CheckLiterals(String[] args) throws ArrayHasLiteralException {
        for (String x: args){
            if (x.matches(notAllowedSymbols)) { // Проверка на наличие литер
                throw new ArrayHasLiteralException();
            }
        }
    }
}
