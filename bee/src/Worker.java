public class Worker extends Bee {
    public Worker(double weight, String workType) {
        super("worker", weight, workType);
    }// вызываем из родителя к дочернему через super

    @Override // предписание, чтобы узнать об ошибке сразу после компиляции
    public boolean canTransform() { return false; }
}