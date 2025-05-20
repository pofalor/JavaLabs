package examples;

class TestExc {
    static void Func ( ) { int m=10, n=0; n= m/n; }
    public static void main (String[] args) {
        try {
            Func ();}
        catch (ArithmeticException e) {
            System.out.println ("ArithmeticException happened");}
        catch (Exception e) {
            System.out.println ("Exception happened");}
        finally {
            System.out.println ("finally");}
    }
}