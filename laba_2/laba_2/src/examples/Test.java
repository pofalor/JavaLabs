package examples;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
class Dan {
    public int xSup, xSub;
    public boolean b;
    public Dan (int Xsub, int Xsup, boolean B) {xSub= Xsub; xSup= Xsup; b= B;}
}
class SuperClass {
    int x;
    private boolean b;
    public SuperClass (int X, boolean B) {x= X; b= B;}
    public boolean getB () {return b;}
}
class SubClass extends SuperClass {// Подкласс (sub class)
    int x;
    public SubClass (int Xsup, int Xsub, boolean B) // Конструктор
    {
        super (Xsub, B); // Вызов конструктора базового класса
        x= Xsup;
    }
    // Получить объект типа Dan с данными
    public Dan get () {return new Dan(x, super.x, super.getB());}
}
class Test {
    public static void main (String[] args) {
        SuperClass supC= new SuperClass (5, true );// Создать объект суперкласса
        System.out.println ("supC.x= "+ supC.x + " supC.b= " + supC.getB());
        // Создать объект подкласса
        SubClass subC= new SubClass (55, 555, false );
        // Получить объект данных
        Dan d= subC.get();
        System.out.println ("subC.x= "+ d.xSub + " supC.x= "+ d.xSup + " subC.b= " + d.b);
    }
}
