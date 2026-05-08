import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class BeeSimulation extends Application {

    private Hive hive; // улей
    private int dayNumber = 0; // текущий день

    private TextArea statsArea; // поле с текстом статистики
    private Label dayLabel; // надпись с номером дня

    // линии на графике популяции
    private XYChart.Series<Number, Number> foragersData = new XYChart.Series<>(); // добытчики
    private XYChart.Series<Number, Number> cleanersData = new XYChart.Series<>(); // уборщики
    private XYChart.Series<Number, Number> dronesData = new XYChart.Series<>(); // трутни
    private XYChart.Series<Number, Number> larvaeData = new XYChart.Series<>(); // личинки

    // столбцы на графике мёда
    private XYChart.Series<String, Number> collectedData = new XYChart.Series<>(); // собрано
    private XYChart.Series<String, Number> consumedData = new XYChart.Series<>(); // съедено

    public static void main(String[] args) {
        launch(args); // запуск окна
    }

    @Override
    public void start(Stage stage) {
        // создаём улей на 200 мест
        hive = new Hive(200);
        hive.addBee(new Queen(0.5)); // матка
        for (int i = 0; i < 6; i++) hive.addBee(new Drone(0.2)); // 6 трутней

        for (int i = 0; i < 12; i++) hive.addBee(new Worker(0.25, "cleaner")); // 12 уборщиков

        for (int i = 0; i < 11; i++) hive.addBee(new Worker(0.04, "forager")); // 11 добытчиков

        for (int i = 0; i < 13; i++) hive.addBee(new Larva(0.1)); // 13 личинок

        stage.setTitle("Симуляция улья"); // заголовок окна

        // кнопки управления
        Button nextButton = new Button("Следующий день");
        Button runButton = new Button("30 дней сразу");
        Button analysisButton = new Button("Полный анализ");
        dayLabel = new Label("День: 0");

        HBox buttons = new HBox(10, nextButton, runButton, analysisButton, dayLabel);
        buttons.setPadding(new Insets(10)); // отступы вокруг кнопок

        // поле для вывода статистики
        statsArea = new TextArea();
        statsArea.setEditable(false); // только чтение
        statsArea.setPrefRowCount(8); // высота 8 строк

        // популяция график
        foragersData.setName("Добытчики");
        cleanersData.setName("Уборщики");
        dronesData.setName("Трутни");
        larvaeData.setName("Личинки");

        NumberAxis xPop = new NumberAxis(); // ось X — дни
        NumberAxis yPop = new NumberAxis(); // ось Y — штуки
        xPop.setLabel("День");
        yPop.setLabel("Количество");

        LineChart<Number, Number> popChart = new LineChart<>(xPop, yPop);
        popChart.setTitle("Популяция");
        popChart.getData().addAll(foragersData, cleanersData, dronesData, larvaeData);
        popChart.setCreateSymbols(false); // без точек на линиях
        popChart.setPrefHeight(250); // высота графика

        // график мёда
        collectedData.setName("Собрано");
        consumedData.setName("Потреблено");

        CategoryAxis xHoney = new CategoryAxis(); // ось X — дни (категории)
        NumberAxis yHoney = new NumberAxis(); // ось Y — кг
        xHoney.setLabel("День");
        yHoney.setLabel("кг");

        BarChart<String, Number> honeyChart = new BarChart<>(xHoney, yHoney);
        honeyChart.setTitle("Мёд");
        honeyChart.getData().addAll(collectedData, consumedData);
        honeyChart.setPrefHeight(250); // высота графика
        honeyChart.setBarGap(2); // зазор между столбцами

        // раскладка — два графика рядом
        HBox charts = new HBox(10, popChart, honeyChart);
        HBox.setHgrow(popChart, Priority.ALWAYS); // растянуть по ширине
        HBox.setHgrow(honeyChart, Priority.ALWAYS);
        charts.setPadding(new Insets(0, 10, 10, 10));

        // главная панель — всё сверху вниз
        VBox root = new VBox(10, buttons, statsArea, charts);
        root.setPadding(new Insets(10));

        updateStats(); // показать день 0

        // обработчик кнопок
        nextButton.setOnAction(e -> { // следующий день
            dayNumber++;
            hive.nextDay(); // прожить один день
            updateStats(); // обновить текст и линии
            addHoneyData(); // добавить столбец мёда если надо
        });

        runButton.setOnAction(e -> { // 30 дней сразу
            // очистить старые графики
            foragersData.getData().clear();
            cleanersData.getData().clear();
            dronesData.getData().clear();
            larvaeData.getData().clear();
            collectedData.getData().clear();
            consumedData.getData().clear();

            // пересоздать улей заново
            hive = new Hive(200);
            hive.addBee(new Queen(0.5));
            for (int i = 0; i < 6; i++) hive.addBee(new Drone(0.2));
            for (int i = 0; i < 12; i++) hive.addBee(new Worker(0.25, "cleaner"));
            for (int i = 0; i < 11; i++) hive.addBee(new Worker(0.04, "forager"));
            for (int i = 0; i < 13; i++) hive.addBee(new Larva(0.1));

            dayNumber = 0;
            updateStats(); // показать день 0
            addHoneyData();

            // прогнать 30 дней
            for (int d = 1; d <= 30; d++) {
                dayNumber = d;
                hive.nextDay();
                updateStats();
                addHoneyData();
            }
            showFullAnalysis(); // показать итоги
        });

        analysisButton.setOnAction(e -> showFullAnalysis()); // полный анализ

        Scene scene = new Scene(root, 900, 650); // размер окна
        stage.setScene(scene);
        stage.show(); // показать окно
    }

    // добавляет столбцы мёда (каждые 5 дней и день 0)
    private void addHoneyData() {
        if (dayNumber == 0 || dayNumber % 5 == 0) {
            HiveSnapshot snap = hive.getSnapshot();
            String label = "д" + dayNumber; // метка "д0", "д5", "д10"...
            collectedData.getData().add(new XYChart.Data<>(label, snap.collected));
            consumedData.getData().add(new XYChart.Data<>(label, snap.consumed));
        }
    }

    // обновляет текст статистики и линии популяции
    private void updateStats() {
        // текст
        StringBuilder sb = new StringBuilder();
        sb.append("++Статистика улья++\n");
        sb.append(hive.getStatisticsText());
        statsArea.setText(sb.toString());
        dayLabel.setText("День: " + dayNumber);

        // линии на графике
        HiveSnapshot snap = hive.getSnapshot();
        Number day = dayNumber;
        foragersData.getData().add(new XYChart.Data<>(day, snap.foragers));
        cleanersData.getData().add(new XYChart.Data<>(day, snap.cleaners));
        dronesData.getData().add(new XYChart.Data<>(day, snap.drones));
        larvaeData.getData().add(new XYChart.Data<>(day, snap.larvae));
    }

    // полный анализ по всем пунктам задания
    private void showFullAnalysis() {
        HiveSnapshot snap = hive.getSnapshot();

        StringBuilder sb = new StringBuilder();
        sb.append("++Детальный анализ++\n\n");
        // количество пчёл по типам
        sb.append("Количество пчёл:\n");
        sb.append("Личинок: ").append(snap.larvae).append("\n");
        sb.append("Трутней: ").append(snap.drones).append("\n");
        sb.append("Рабочих-добытчиков: ").append(snap.foragers).append("\n");
        sb.append("Рабочих-уборщиков: ").append(snap.cleaners).append("\n");
        sb.append("Всего живых: ").append(snap.larvae + snap.drones + snap.foragers + snap.cleaners).append("\n\n");

        // баланс мёда
        sb.append("Баланс мёда:\n");
        sb.append("Собрано: ").append(String.format("%.2f", snap.collected)).append(" кг\n");
        sb.append("Потреблено: ").append(String.format("%.2f", snap.consumed)).append(" кг\n");
        if (snap.consumed > 0) {
            double eff = snap.collected / snap.consumed;
            sb.append("Эффективность: ").append(String.format("%.2f", eff)).append("\n");
            if (eff > 1) sb.append("Собирают больше чем едят\n");
            else sb.append("Едят больше чем собирают\n");
        }
        sb.append("\n");

        // эффективность трутней
        sb.append("Трутни:\n");
        sb.append("Живых: ").append(snap.drones).append("\n");
        sb.append("Всего рабочих: ").append(snap.totalWorkers).append("\n");
        if (snap.totalWorkers > 0) {
            double ratio = (double)snap.drones / snap.totalWorkers * 100;
            sb.append("Соотношение: ").append(String.format("%.1f%%", ratio)).append("\n");
            if (ratio > 15) sb.append("Трутней много\n");
            else if (ratio < 5) sb.append("Трутней мало\n");
            else sb.append("Трутней в норме\n");
        }
        sb.append("\n");

        // смерти от голода
        sb.append("Смерти от голода:\n");
        sb.append("Всего смертей: ").append(snap.deaths).append("\n");
        sb.append("От голода: ").append(snap.hungerDeaths).append("\n");
        if (snap.deaths > 0) {
            double pct = (double)snap.hungerDeaths / snap.deaths * 100;
            sb.append("Процент: ").append(String.format("%.1f%%", pct)).append("\n");
        }
        if (snap.hungerDeaths > 0) {
            sb.append("Личинок: ").append(snap.larvaeHunger).append("\n");
            sb.append("Трутней: ").append(snap.droneHunger).append("\n");
            sb.append("Рабочих: ").append(snap.workerHunger).append("\n");
        }
        sb.append("\n");

        // простой уборщиков и трупы в улье
        sb.append("Простой уборщиков: ").append(snap.idleDays).append(" дней\n");
        sb.append("Трупов в улье: ").append(snap.deadCount).append("\n");

        statsArea.setText(sb.toString()); // вывести анализ в текстовое поле
    }
}

// класс для передачи данных из улья в интерфейс
class HiveSnapshot {
    int larvae;
    int drones;
    int foragers;
    int cleaners;
    double collected;
    double consumed;
    double honey;
    int deaths;
    int hungerDeaths;
    int larvaeHunger;
    int droneHunger;
    int workerHunger;
    int totalWorkers;
    int deadCount;
    int idleDays;
}