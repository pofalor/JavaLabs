package examples;

class MyException extends Exception {
    //public MyException () {}
    public String toString () { return "MyException happened";}
}
class TestMyExc {
    static void Func() throws MyException {
        int m = 0, n = 10;
        if((m/n) == 0) throw new MyException ();
        System.out.print ("Next");
    }
    public static void main(String[] args) {
        try
        {Func ();}
        catch (MyException e) {System.out.println (e);}
    }
}
