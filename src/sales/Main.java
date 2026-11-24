package sales;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        Path path = Path.of(args.length > 0 ? args[0] : "data/sample.csv");
        SalesLoader loader = new SalesLoader();
        List<Sale> sales = loader.load(path);
        SalesAnalyticsService service = new SalesAnalyticsService();

        System.out.println("Загружено продаж: " + sales.size());
        for (String error : loader.getErrors()) {
            System.out.println("Ошибка данных -> " + error);
        }

        System.out.println("Топ-3 продажи:");
        for (Sale sale : service.topSales(sales, 3)) {
            System.out.println("  " + sale);
        }

        System.out.println("Максимальная продажа: "
                + service.maxSale(sales).map(Sale::toString).orElse("продаж нет"));

        System.out.println("Суммы по менеджерам:");
        for (Map.Entry<String, Double> entry : service.totalsByManager(sales).entrySet()) {
            System.out.println("  " + entry.getKey() + " = " + entry.getValue());
        }

        System.out.println("Средняя сумма по региону North: " + service.averageByRegion(sales, "North"));

        System.out.println("Количество продаж от " + SalesRules.MIN_AMOUNT_FOR_COUNT + " по регионам:");
        for (Map.Entry<String, Long> entry : service.countedByRegion(sales).entrySet()) {
            System.out.println("  " + entry.getKey() + " = " + entry.getValue());
        }

        System.out.println("Порядок сортировки:");
        for (Sale sale : service.sortedByAmountThenId(sales)) {
            System.out.println("  " + sale);
        }
    }
}
