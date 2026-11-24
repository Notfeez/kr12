package sales;

import java.util.Objects;

public class Sale {
    private final String id;
    private final String manager;
    private final String region;
    private final double amount;

    public Sale(String id, String manager, String region, double amount) {
        if (id == null || id.isBlank()) {
            throw new SalesException("Номер продажи пуст");
        }
        if (manager == null || manager.isBlank()) {
            throw new SalesException("Менеджер не указан");
        }
        if (region == null || region.isBlank()) {
            throw new SalesException("Регион не указан");
        }
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0) {
            throw new SalesException("Сумма продажи должна быть положительным числом");
        }
        this.id = id.trim();
        this.manager = manager.trim();
        this.region = region.trim();
        this.amount = amount;
    }

    public String getId() {
        return id;
    }

    public String getManager() {
        return manager;
    }

    public String getRegion() {
        return region;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Sale)) {
            return false;
        }
        Sale other = (Sale) o;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return id + " | " + manager + " | " + region + " | " + amount;
    }
}
