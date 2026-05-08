import java.util.Random;

public abstract class Bee {
    int id;
    String type;        // queen-матка, drone-трутень, worker-рабочий, larva-личинка
    double weight;
    double age;
    boolean isAlive;
    String workType;    // forager, cleaner, none
    double maxAge;
    boolean diedFromHunger;

    static int nextId = 1; // используем это, чтобы счётчик был общий для всех пчёл
    static Random random = new Random();

    public Bee(String type, double weight, String workType) { // создаём конструктор для пчелы
        this.id = nextId;
        nextId++;
        this.type = type;
        this.weight = weight;
        this.age = 0;
        this.isAlive = true;
        this.workType = workType;
        this.diedFromHunger = false;
        this.maxAge = setMaxAge();
    }

    private double setMaxAge() { // задаём случайный продолжительность жизни
        if (type.equals("queen")) {
            return 3.0 + random.nextDouble() * 2.0;
        } else if (type.equals("worker")) {
            return 0.5 + random.nextDouble() * 0.3;
        } else if (type.equals("drone")) {
            return 0.3 + random.nextDouble() * 0.2;
        } else if (type.equals("larva")) {
            return 0.05;
        } else {
            return 1.0;
        }
    }

    public void ageOneDay() { // пчела умирает
        if (isAlive) {
            age = age + 1.0 / 365.0;
            if (age >= maxAge) {
                isAlive = false;
            }
        }
    }

    public abstract boolean canTransform(); // Используется в методе larvaeGrow(Hive). Может меняться пчела или нет
} // abstract - у всех наследников есть этот метод, но может не использоваться или меняться