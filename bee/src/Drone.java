public class Drone extends Bee {
    public Drone(double weight) {
        super("drone", weight, "none");
    } // вызываем из родителя к дочернему через super

    @Override // предписание, чтобы узнать об ошибке сразу после компиляции
    public boolean canTransform() {
        return false;
    }
}