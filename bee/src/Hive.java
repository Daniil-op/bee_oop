import java.util.Random;

public class Hive {
    private Bee[] bees; // массив пчёл
    private int beeCount; // коли-во пчёл в улье
    private int capacity; //Максимальная вместимость улья (200)
    private double honey; //Текущий запас мёда
    private Random random; // генератор случайных чисел для яиц и превращений
    private int day; // дни для имитации

    private Bee[] deadBodies; //Трупы массив
    private int deadCount; //Трупы число

    private int idleCleanerDays; //Счётчик простоев. (труп тяжелее уборщика)
    private double totalHoneyCollected; //Собрали
    private double totalHoneyConsumed; // Съели
    private int totalDeaths; // всего смертей
    private int totalHungerDeaths; // всего смертей от голода
    private int larvaeHungerDeaths; // личинка
    private int droneHungerDeaths; // трутень
    private int workerHungerDeaths; // рабочий
    private int totalDroneCount; //Сколько всего родилось трутней
    private int totalWorkerCount; //Сколько всего родилось рабочих

    public Hive(int maxBees) { // создаём улей через конструктор
        capacity = maxBees; // запоминаем размер улья
        bees = new Bee[maxBees]; // массив для живых пчел
        beeCount = 0; // пчел пока нет
        honey = 3.0; // начальный запас — 3 кг мёда
        random = new Random(); // создаём генератор случайных чисел
        day = 0; // день 0, симуляция не началась
        deadBodies = new Bee[maxBees]; // массив для трупов
        deadCount = 0; // трупов нет
        idleCleanerDays = 0; // простоев не было
        totalHoneyCollected = 0; // ничего не собрано
        totalHoneyConsumed = 0; // ничего не съедено
        totalDeaths = 0; // никто не умирал
        totalHungerDeaths = 0; // от голода не умирали
        larvaeHungerDeaths = 0;
        droneHungerDeaths = 0;
        workerHungerDeaths = 0;
        totalDroneCount = 0;
        totalWorkerCount = 0;
    }

    public void addBee(Bee bee) { // добавляем пчёл в улей
        if (beeCount < capacity) { // проверяем есть ли место
            bees[beeCount] = bee; // кладём в первую свободную ячейку
            beeCount++; // увеличиваем счётчик живых

            if (bee.type.equals("drone")) {
                totalDroneCount++; // считаем рождённых трутней
            } else if (bee.type.equals("worker")) {
                totalWorkerCount++; // считаем рождённых рабочих
            }
        }
    }

    private void addDeadBody(Bee deadBee) { // добавляем мёртвых пчел в массив, обновляем все счётчики
        if (deadCount < capacity) { // если есть место для трупов
            deadBodies[deadCount] = deadBee; // кладём труп в массив
            deadCount++; // увеличиваем счётчик трупов
            totalDeaths++; // увеличиваем общий счётчик смертей

            if (deadBee.diedFromHunger) { // проверяем умерла ли от голода
                totalHungerDeaths++; // увеличиваем счётчик голодных смертей
                if (deadBee.type.equals("larva")) {
                    larvaeHungerDeaths++; // записываем в личинки
                } else if (deadBee.type.equals("drone")) {
                    droneHungerDeaths++; // записываем в трутни
                } else if (deadBee.type.equals("worker")) {
                    workerHungerDeaths++; // записываем в рабочие
                }
            }
        }
    }

    private void cleanHive() { // убираем улей, если мерт тело весит больше, то простой
        int cleanedToday = 0; // сколько трупов убрали сегодня
        int idleToday = 0; // сколько уборщиков простаивали сегодня

        for (int i = 0; i < beeCount && cleanedToday < deadCount; i++) {  //Идём по живым пчелам
            if (bees[i] != null && bees[i].isAlive &&
                    bees[i].type.equals("worker") && bees[i].workType.equals("cleaner")) {
                // если уборщик тяжелее трупа — может его убрать
                if (deadCount > cleanedToday && bees[i].weight > deadBodies[cleanedToday].weight) {
                    cleanedToday++; // успешно убрали один труп
                } else if (deadCount > cleanedToday) {
                    idleToday++; // труп тяжелее — уборщик простаивает
                }
            }
        }

        if (cleanedToday > 0) { // убираем трупы и обновляем счётчики уборки
            Bee[] newDead = new Bee[capacity]; // создаём новый массив для оставшихся трупов
            for (int i = cleanedToday; i < deadCount; i++) {
                newDead[i - cleanedToday] = deadBodies[i]; // копируем только неубранных
            }
            deadBodies = newDead; // заменяем старый массив новым
            deadCount = deadCount - cleanedToday; // обновляем количество трупов
        }

        idleCleanerDays = idleCleanerDays + idleToday; // прибавляем простои к общему счётчику
    }

    private void collectHoney() { // private так как симуляция сама решает когда его собирать
        double honeyCollected = 0; // сколько мёда собрали сегодня
        for (int i = 0; i < beeCount; i++) {
            if (bees[i] != null && bees[i].isAlive &&
                    bees[i].type.equals("worker") && bees[i].workType.equals("forager")) {
                double productivity = 0.03 * (1 - bees[i].age / bees[i].maxAge); //Расчёт продуктивности
                honeyCollected = honeyCollected + productivity; // суммируем сбор со всех добытчиков
            }
        }
        honey = honey + honeyCollected; // добавляем собранное в запасы улья
        totalHoneyCollected = totalHoneyCollected + honeyCollected; // считаем общий сбор за всё время
    }

    private void consumeHoney() { // то же самое, что и сбор, только едят сами по необходимости
        double honeyConsumed = 0; // сколько мёда съели сегодня
        for (int i = 0; i < beeCount; i++) {
            if (bees[i] != null && bees[i].isAlive) {
                honeyConsumed = honeyConsumed + bees[i].weight * 0.01; // каждая ест 1% от своего веса
            }
        }
        totalHoneyConsumed = totalHoneyConsumed + honeyConsumed; // считаем общее потребление

        if (honey >= honeyConsumed) {
            honey = honey - honeyConsumed; // мёда хватило — просто вычитаем
        } else {  //Если мёда не хватило — запасы обнуляются, ВСЕ пчелы умирают
            honey = 0; // запас мёда обнулён
            for (int i = 0; i < beeCount; i++) {
                if (bees[i] != null && bees[i].isAlive) {
                    bees[i].isAlive = false; // убиваем пчелу
                    bees[i].diedFromHunger = true; // помечаем что причина смерти — голод
                    addDeadBody(bees[i]); // добавляем в список трупов
                }
            }
        }
    }

    private void queenLayEggs() { // откладка яиц маткой
        // считаем живых маток
        int queenCount = 0;
        for (int i = 0; i < beeCount; i++) {
            if (bees[i] != null && bees[i].type.equals("queen") && bees[i].isAlive) {
                queenCount++; // нашли живую матку
            }
        }
        // считаем продуктивность (чем больше трупов тем хуже)
        double productivity = 1.0; // 100% если чисто
        if (deadCount > 10) {
            productivity = 0.5; // 50% — очень грязно
        } else if (deadCount > 5) {
            productivity = 0.7; // 70% — средне
        }
        // если мёда и маток хватает, то несёт яйца
        if (queenCount > 0 && honey > 3.0) {
            int eggsToLay = (int)((random.nextInt(5) + 3) * productivity); // от 3 до 7 яиц с учётом продуктивности
            for (int i = 0; i < eggsToLay && beeCount < capacity; i++) {
                addBee(new Larva(0.05 + random.nextDouble() * 0.05)); // создаём личинку со случайным весом
            }
        }
    }

    private void larvaeGrow() { // рост личинок и превращение во взрослых
        for (int i = 0; i < beeCount; i++) {
            if (bees[i] != null && bees[i].type.equals("larva") && bees[i].isAlive) {
                // набирает вес личинка
                bees[i].weight = bees[i].weight + 0.01; // прибавляем 0.01 кг
                // если соответствует требованиям, то превращается
                if (bees[i].canTransform()) { // проверяем готовность через метод из Larva
                    // случайный выбор, трутень или рабочий
                    if (random.nextDouble() < 0.2) { // 20% шанс
                        // стала трутнем
                        bees[i] = new Drone(0.2); // заменяем личинку на трутня
                        totalDroneCount++; // считаем нового трутня
                    } else { // 80% шанс
                        // становится рабочей
                        String type;
                        if (random.nextBoolean()) { // 50 на 50
                            type = "forager"; // добытчик
                        } else {
                            type = "cleaner"; // уборщик
                        }
                        bees[i] = new Worker(0.15, type); // создаём новый тип, чтобы не делать это вручную из-за разности конструктора и данных в нём
                        totalWorkerCount++; // считаем нового рабочего
                    }
                }
            }
        }
    }

    private void ageAllBees() {  // старение на 1 день, если после одного дня умерла, то добавляем в список трупов
        for (int i = 0; i < beeCount; i++) {
            if (bees[i] != null && bees[i].isAlive) {
                bees[i].ageOneDay(); // вызываем метод старения у каждой живой пчелы
                if (!bees[i].isAlive) { // если после старения умерла
                    addDeadBody(bees[i]); // добавляем в трупы
                }
            }
        }
    }

    private void removeDeadBees() { // убираем из массива живых пчел, остаются только Alive = true
        int newCount = 0; // новый счётчик живых
        for (int i = 0; i < beeCount; i++) {
            if (bees[i].isAlive) { // если пчела жива
                bees[newCount] = bees[i]; // сдвигаем живых в начало массива
                newCount++; // увеличиваем счётчик
            }
        }
        beeCount = newCount; // обновляем количество живых пчел
    }

    public void nextDay() { // симуляция дня, именно такой порядок
        day++; // увеличиваем номер дня
        consumeHoney(); // 1. сначала все едят мёд
        collectHoney(); // 2. потом добытчики собирают новый мёд
        cleanHive(); // 3. уборщики выносят трупы
        queenLayEggs(); // 4. матка откладывает яйца
        larvaeGrow(); // 5. личинки растут и превращаются
        ageAllBees(); // 6. все пчелы стареют на день
        removeDeadBees(); // 7. убираем мёртвых из списка живых
    }

    private int getLarvaCount() { // получаем число личинок
        int count = 0;
        for (int i = 0; i < beeCount; i++) {
            if (bees[i] != null && bees[i].type.equals("larva") && bees[i].isAlive) {
                count++;
            }
        }
        return count;
    }

    private int getDroneCount() { // получаем число трутней
        int count = 0;
        for (int i = 0; i < beeCount; i++) {
            if (bees[i] != null && bees[i].type.equals("drone") && bees[i].isAlive) {
                count++;
            }
        }
        return count;
    }

    private int getForagerCount() { // получаем число добытчиков
        int count = 0;
        for (int i = 0; i < beeCount; i++) {
            if (bees[i] != null && bees[i].isAlive &&
                    bees[i].type.equals("worker") && bees[i].workType.equals("forager")) {
                count++;
            }
        }
        return count;
    }

    private int getCleanerCount() { // получаем число уборщиков
        int count = 0;
        for (int i = 0; i < beeCount; i++) {
            if (bees[i] != null && bees[i].isAlive &&
                    bees[i].type.equals("worker") && bees[i].workType.equals("cleaner")) {
                count++;
            }
        }
        return count;
    }

    public void showStatistics() { // выводим статистику по дням
        System.out.println();
        System.out.println("День " + day);
        System.out.println("Личинок: " + getLarvaCount());
        System.out.println("Трутней: " + getDroneCount());
        System.out.println("Рабочих-добытчиков: " + getForagerCount());
        System.out.println("Рабочих-уборщиков: " + getCleanerCount());
        System.out.println("Всего живых пчел: " +
                (getForagerCount() + getCleanerCount() + getLarvaCount() + getDroneCount()));
        System.out.println("Запасы меда: " + String.format("%.2f", honey) + " кг");
        System.out.println("Собрано меда всего: " + String.format("%.2f", totalHoneyCollected) + " кг");
        System.out.println("Потреблено меда всего: " + String.format("%.2f", totalHoneyConsumed) + " кг");
        System.out.println("Дней простоя уборщиков: " + idleCleanerDays);
        System.out.println("Мертвых пчел в улье: " + deadCount);
        System.out.println("Всего смертей: " + totalDeaths);
        System.out.println("Смертей от голода: " + totalHungerDeaths);
    }

    public void showFullAnalysis() { // детальная статистика после всех дней, либо выхода из цикла
        System.out.println();
        System.out.println("ПОЛНЫЙ АНАЛИЗ");
        System.out.println("Личинок: " + getLarvaCount());
        System.out.println("Трутней: " + getDroneCount());
        System.out.println("Рабочих-добытчиков: " + getForagerCount());
        System.out.println("Рабочих-уборщиков: " + getCleanerCount());
        System.out.println("Всего живых пчел: " +
                (getForagerCount() + getCleanerCount() + getLarvaCount() + getDroneCount()));
        System.out.println();
        System.out.println("Баланс меда:");
        System.out.println("Всего собрано: " + String.format("%.2f", totalHoneyCollected) + " кг");
        System.out.println("Всего потреблено: " + String.format("%.2f", totalHoneyConsumed) + " кг");
        double balance = totalHoneyCollected - totalHoneyConsumed;
        System.out.println("Разница: " + String.format("%.2f", balance) + " кг");

        if (totalHoneyConsumed > 0) {
            double efficiency = totalHoneyCollected / totalHoneyConsumed;
            System.out.println("Эффективность: " + String.format("%.2f", efficiency));
            if (efficiency > 1) {
                System.out.println("Собирают больше чем едят");
            } else {
                System.out.println("Едят больше чем собирают");
            }
        }

        System.out.println();
        int liveDrones = getDroneCount();
        double droneRatio = 0;
        if (totalWorkerCount > 0) {
            droneRatio = (double)liveDrones / totalWorkerCount;
        }
        System.out.println("Эффективность трутней:");
        System.out.println("Живых трутней: " + liveDrones);
        System.out.println("Всего было рабочих: " + totalWorkerCount);
        System.out.println("Соотношение: " + String.format("%.2f%%", droneRatio * 100));
        if (droneRatio > 0.15) {
            System.out.println("Трутней слишком много");
        } else if (droneRatio < 0.05) {
            System.out.println("Трутней мало");
        } else {
            System.out.println("Трутней в норме");
        }

        System.out.println();
        System.out.println("Смертность от голода:");
        System.out.println("Всего смертей: " + totalDeaths);
        System.out.println("От голода: " + totalHungerDeaths);
        if (totalDeaths > 0) {
            System.out.println("Процент: " +
                    String.format("%.2f%%", (double)totalHungerDeaths / totalDeaths * 100));
        }
    }

    public String getStatisticsText() { // метод для интерфейса — отдаёт статистику текстом
        StringBuilder sb = new StringBuilder();
        sb.append("Личинок: ").append(getLarvaCount()).append("\n");
        sb.append("Трутней: ").append(getDroneCount()).append("\n");
        sb.append("Рабочих-добытчиков: ").append(getForagerCount()).append("\n");
        sb.append("Рабочих-уборщиков: ").append(getCleanerCount()).append("\n");
        sb.append("Всего живых пчел: ").append(
                getForagerCount() + getCleanerCount() + getLarvaCount() + getDroneCount()).append("\n");
        sb.append(String.format("Запасы меда: %.2f кг\n", honey));
        sb.append(String.format("Собрано меда всего: %.2f кг\n", totalHoneyCollected));
        sb.append(String.format("Потреблено меда всего: %.2f кг\n", totalHoneyConsumed));
        sb.append("Дней простоя уборщиков: ").append(idleCleanerDays).append("\n");
        sb.append("Мертвых пчел в улье: ").append(deadCount).append("\n");
        sb.append("Всего смертей: ").append(totalDeaths).append("\n");
        sb.append("Смертей от голода: ").append(totalHungerDeaths).append("\n");
        return sb.toString();
    }

    public HiveSnapshot getSnapshot() { // метод для анализа — собирает все данные в один объект
        HiveSnapshot s = new HiveSnapshot();
        s.larvae = getLarvaCount();
        s.drones = getDroneCount();
        s.foragers = getForagerCount();
        s.cleaners = getCleanerCount();
        s.collected = totalHoneyCollected;
        s.consumed = totalHoneyConsumed;
        s.honey = honey;
        s.deaths = totalDeaths;
        s.hungerDeaths = totalHungerDeaths;
        s.larvaeHunger = larvaeHungerDeaths;
        s.droneHunger = droneHungerDeaths;
        s.workerHunger = workerHungerDeaths;
        s.totalWorkers = totalWorkerCount;
        s.deadCount = deadCount;
        s.idleDays = idleCleanerDays;
        return s;
    }
}