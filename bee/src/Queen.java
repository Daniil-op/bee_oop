public class Queen extends Bee {
    public Queen(double weight) {
        super("queen", weight, "none");
    }// вызываем из родителя к дочернему через super

    @Override // предписание, чтобы узнать об ошибке сразу после компиляции
    public boolean canTransform() {
        return false;
    }
}