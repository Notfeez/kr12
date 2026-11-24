package sales;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SalesLoader {
    private final List<String> errors = new ArrayList<>();

    public List<Sale> load(Path path) throws IOException {
        List<Sale> sales = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            try {
                Sale sale = parseLine(line);
                if (!ids.add(sale.getId())) {
                    throw new SalesException("Повтор номера продажи " + sale.getId());
                }
                sales.add(sale);
            } catch (SalesException e) {
                errors.add("Строка " + (i + 1) + ": " + e.getMessage());
            }
        }
        return sales;
    }

    public List<String> getErrors() {
        return List.copyOf(errors);
    }

    private Sale parseLine(String line) {
        String[] parts = line.split(";", -1);
        if (parts.length != 4) {
            throw new SalesException("Ожидалось 4 поля, получено " + parts.length);
        }
        return new Sale(parts[0], parts[1], parts[2], parseAmount(parts[3]));
    }

    private double parseAmount(String text) {
        if (text == null || text.isBlank()) {
            throw new SalesException("Сумма не указана");
        }
        try {
            return Double.parseDouble(text.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new SalesException("Неверный формат суммы: " + text);
        }
    }
}
