package sales;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SelfCheck {
    private static int passed = 0;
    private static int failed = 0;
    private static final SalesAnalyticsService SERVICE = new SalesAnalyticsService();

    public static void main(String[] args) throws Exception {
        typicalTopSales();
        thresholdBoundary();
        topLimitBoundary();
        maxSaleOptional();
        maxSaleTie();
        totalsByManager();
        averageByRegion();
        countedByRegion();
        twoFieldComparator();
        sourceNotChanged();
        emptyAndNullInput();
        wrongSaleData();
        csvLoading();
        System.out.println("Пройдено: " + passed + ", провалено: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static List<Sale> base() {
        List<Sale> sales = new ArrayList<>();
        sales.add(new Sale("S-1", "Иванов", "North", 120));
        sales.add(new Sale("S-2", "Петров", "South", 35));
        sales.add(new Sale("S-3", "Иванов", "North", 34.99));
        sales.add(new Sale("S-4", "Сидорова", "East", 250));
        sales.add(new Sale("S-5", "Петров", "North", 35));
        return sales;
    }

    private static void typicalTopSales() {
        List<Sale> top = SERVICE.topSales(base(), 2);
        check("типовой топ-2", top.size() == 2 && top.get(0).getId().equals("S-4") && top.get(1).getId().equals("S-1"));
    }

    private static void thresholdBoundary() {
        List<Sale> top = SERVICE.topSales(base(), 10);
        boolean includesExact = top.stream().anyMatch(s -> s.getId().equals("S-2"));
        boolean excludesBelow = top.stream().noneMatch(s -> s.getId().equals("S-3"));
        check("граница 35: ровно 35 входит, 34.99 нет", includesExact && excludesBelow && top.size() == 4);
    }

    private static void topLimitBoundary() {
        List<Sale> sales = base();
        boolean zero = SERVICE.topSales(sales, 0).isEmpty();
        boolean more = SERVICE.topSales(sales, 100).size() == 4;
        boolean negative = fails(() -> SERVICE.topSales(sales, -1));
        check("лимит топа: 0, больше размера, отрицательный", zero && more && negative);
    }

    private static void maxSaleOptional() {
        Optional<Sale> max = SERVICE.maxSale(base());
        boolean found = max.isPresent() && max.get().getId().equals("S-4");
        boolean empty = SERVICE.maxSale(new ArrayList<>()).isEmpty();
        check("максимальная продажа возвращает Optional", found && empty);
    }

    private static void maxSaleTie() {
        List<Sale> sales = new ArrayList<>();
        sales.add(new Sale("B-2", "Иванов", "North", 100));
        sales.add(new Sale("A-1", "Петров", "South", 100));
        check("при равных суммах побеждает меньший номер",
                SERVICE.maxSale(sales).get().getId().equals("A-1"));
    }

    private static void totalsByManager() {
        Map<String, Double> totals = SERVICE.totalsByManager(base());
        boolean ok = Math.abs(totals.get("Иванов") - 154.99) < 0.0001
                && Math.abs(totals.get("Петров") - 70) < 0.0001
                && Math.abs(totals.get("Сидорова") - 250) < 0.0001;
        totals.clear();
        check("суммы по менеджерам и независимость Map",
                ok && SERVICE.totalsByManager(base()).size() == 3 && SERVICE.totalsByManager(new ArrayList<>()).isEmpty());
    }

    private static void averageByRegion() {
        double north = SERVICE.averageByRegion(base(), " north ");
        boolean ok = Math.abs(north - (120 + 34.99 + 35) / 3) < 0.0001;
        boolean none = SERVICE.averageByRegion(base(), "West") == 0.0;
        boolean blank = fails(() -> SERVICE.averageByRegion(base(), " "));
        boolean nullRegion = fails(() -> SERVICE.averageByRegion(base(), null));
        check("среднее по региону: обычный, нет продаж, пустой регион", ok && none && blank && nullRegion);
    }

    private static void countedByRegion() {
        Map<String, Long> counted = SERVICE.countedByRegion(base());
        boolean ok = counted.get("North") == 2 && counted.get("South") == 1 && counted.get("East") == 1;
        check("дополнительный агрегат: количество продаж от 35 по регионам", ok && counted.size() == 3);
    }

    private static void twoFieldComparator() {
        List<Sale> sales = new ArrayList<>();
        sales.add(new Sale("C", "Иванов", "North", 50));
        sales.add(new Sale("A", "Иванов", "North", 50));
        sales.add(new Sale("B", "Иванов", "North", 70));
        List<Sale> sorted = SERVICE.sortedByAmountThenId(sales);
        check("Comparator: сумма по убыванию, затем номер",
                sorted.get(0).getId().equals("B") && sorted.get(1).getId().equals("A") && sorted.get(2).getId().equals("C"));
    }

    private static void sourceNotChanged() {
        List<Sale> sales = base();
        List<Sale> before = new ArrayList<>(sales);
        SERVICE.topSales(sales, 3);
        SERVICE.sortedByAmountThenId(sales);
        SERVICE.totalsByManager(sales);
        SERVICE.countedByRegion(sales);
        List<Sale> top = SERVICE.topSales(sales, 3);
        top.clear();
        check("исходный список не изменяется", sales.equals(before) && sales.size() == 5);
    }

    private static void emptyAndNullInput() {
        List<Sale> empty = new ArrayList<>();
        boolean emptyOk = SERVICE.topSales(empty, 3).isEmpty()
                && SERVICE.totalsByManager(empty).isEmpty()
                && SERVICE.countedByRegion(empty).isEmpty()
                && SERVICE.averageByRegion(empty, "North") == 0.0;
        boolean nullList = fails(() -> SERVICE.topSales(null, 1));
        List<Sale> withNull = new ArrayList<>();
        withNull.add(null);
        boolean nullElement = fails(() -> SERVICE.maxSale(withNull));
        check("пустой список, null список и null элемент", emptyOk && nullList && nullElement);
    }

    private static void wrongSaleData() {
        boolean a = fails(() -> new Sale("", "Иванов", "North", 10));
        boolean b = fails(() -> new Sale("X", null, "North", 10));
        boolean c = fails(() -> new Sale("X", "Иванов", " ", 10));
        boolean d = fails(() -> new Sale("X", "Иванов", "North", 0));
        boolean e = fails(() -> new Sale("X", "Иванов", "North", -1));
        boolean f = fails(() -> new Sale("X", "Иванов", "North", Double.NaN));
        check("некорректная продажа отклоняется", a && b && c && d && e && f);
    }

    private static void csvLoading() throws Exception {
        Path file = Files.createTempFile("sales", ".csv");
        Files.writeString(file, String.join("\n",
                "id;manager;region;amount",
                "A-1;Иванов;North;100",
                "A-1;Дубль;North;5",
                "A-2;Петров;South;",
                "A-3;Петров;South;abc",
                "A-4;Петров;South;-3",
                "A-5;;South;10",
                "A-6;Петров;South",
                "A-7;Сидорова;East;12,5"));
        SalesLoader loader = new SalesLoader();
        List<Sale> sales = loader.load(file);
        Files.delete(file);
        check("загрузка CSV: 2 корректные строки, 6 ошибок", sales.size() == 2 && loader.getErrors().size() == 6);
    }

    private static boolean fails(Runnable action) {
        try {
            action.run();
            return false;
        } catch (SalesException e) {
            return true;
        }
    }

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("PASS " + name);
        } else {
            failed++;
            System.out.println("FAIL " + name);
        }
    }
}
