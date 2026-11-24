package sales;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class SalesAnalyticsService {
    private static final Comparator<Sale> BY_AMOUNT_THEN_ID =
            Comparator.comparingDouble(Sale::getAmount).reversed()
                    .thenComparing(Sale::getId);

    public List<Sale> topSales(List<Sale> sales, int limit) {
        checkSales(sales);
        if (limit < 0) {
            throw new SalesException("Количество продаж не может быть отрицательным");
        }
        return sales.stream()
                .filter(SalesRules::isCounted)
                .sorted(BY_AMOUNT_THEN_ID)
                .limit(limit)
                .collect(Collectors.toList());
    }

    public Optional<Sale> maxSale(List<Sale> sales) {
        checkSales(sales);
        return sales.stream().min(BY_AMOUNT_THEN_ID);
    }

    public Map<String, Double> totalsByManager(List<Sale> sales) {
        checkSales(sales);
        return sales.stream()
                .collect(Collectors.groupingBy(
                        Sale::getManager,
                        TreeMap::new,
                        Collectors.summingDouble(Sale::getAmount)));
    }

    public double averageByRegion(List<Sale> sales, String region) {
        checkSales(sales);
        if (region == null || region.isBlank()) {
            throw new SalesException("Регион не указан");
        }
        String wanted = region.trim();
        return sales.stream()
                .filter(sale -> sale.getRegion().equalsIgnoreCase(wanted))
                .mapToDouble(Sale::getAmount)
                .average()
                .orElse(0.0);
    }

    public Map<String, Long> countedByRegion(List<Sale> sales) {
        checkSales(sales);
        return sales.stream()
                .filter(SalesRules::isCounted)
                .collect(Collectors.groupingBy(
                        Sale::getRegion,
                        TreeMap::new,
                        Collectors.counting()));
    }

    public List<Sale> sortedByAmountThenId(List<Sale> sales) {
        checkSales(sales);
        return sales.stream().sorted(BY_AMOUNT_THEN_ID).collect(Collectors.toList());
    }

    private void checkSales(List<Sale> sales) {
        if (sales == null) {
            throw new SalesException("Список продаж не передан");
        }
        if (sales.stream().anyMatch(Objects::isNull)) {
            throw new SalesException("В списке продаж есть пустой элемент");
        }
    }
}
