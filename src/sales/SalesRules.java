package sales;

public final class SalesRules {
    public static final double MIN_AMOUNT_FOR_COUNT = 35;

    private SalesRules() {
    }

    public static boolean isCounted(Sale sale) {
        return sale.getAmount() >= MIN_AMOUNT_FOR_COUNT;
    }
}
