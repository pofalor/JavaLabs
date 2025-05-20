package examples.TestInterface;

interface IFunc {
    public int getF();
}
interface IConst {
    public static final int verConst= 100;
}
class ClassInt implements IFunc, IConst {
    public int getF (){ return verConst;}
}
class TestInterface {
    public static void main (String[] args) {
        ClassInt cI= new ClassInt();
        System.out.println ("verConst= " + cI.getF());
        IFunc iF= cI;
        IConst iC= cI;
        System.out.println ("verConst= " + iF.getF());
        // System.out.println ("verConst= " + iC.getF()); // Error
        // Cannot find 'getF()' in 'IConst'
        System.out.println ("verConst= " + iC.verConst);
        System.out.println ("verConst= " + ClassInt.verConst);
    }
}
