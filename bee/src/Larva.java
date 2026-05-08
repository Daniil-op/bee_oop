public class Larva extends Bee {
    public Larva(double weight) {
        super("larva", weight, "none");
    }// вызываем из родителя к дочернему через super

    @Override // предписание, чтобы узнать об ошибке сразу после компиляции
    public boolean canTransform() {
        return age >= 0.05;
    }
}