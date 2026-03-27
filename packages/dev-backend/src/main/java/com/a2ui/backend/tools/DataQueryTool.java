package com.a2ui.backend.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import lombok.extern.slf4j.Slf4j;

/**
 * AITool for querying sales data.
 *
 * This tool provides sample sales data that can be used by the LLM
 * to generate charts and reports. It demonstrates how to integrate
 * data retrieval capabilities with the A2UI system.
 */
@Slf4j
@Component
public class DataQueryTool {

    // Sample sales data by category
    private static final Map<String, List<Map<String, Object>>> SALES_BY_CATEGORY = Map.of(
        "Electronics", List.of(
            Map.of("product", "Wireless Earbuds", "sales", 12500, "units", 42),
            Map.of("product", "Smart Watch", "sales", 18900, "units", 32),
            Map.of("product", "Laptop Stand", "sales", 4500, "units", 51),
            Map.of("product", "USB-C Hub", "sales", 8200, "units", 68),
            Map.of("product", "Mechanical Keyboard", "sales", 11200, "units", 28)
        ),
        "Clothing", List.of(
            Map.of("product", "Winter Jacket", "sales", 15600, "units", 52),
            Map.of("product", "Running Shoes", "sales", 22300, "units", 89),
            Map.of("product", "Casual T-Shirt", "sales", 4500, "units", 150),
            Map.of("product", "Denim Jeans", "sales", 9800, "units", 65),
            Map.of("product", "Sports Hoodie", "sales", 7600, "units", 48)
        ),
        "Home & Garden", List.of(
            Map.of("product", "Smart LED Bulb", "sales", 3200, "units", 160),
            Map.of("product", "Indoor Plant Pot", "sales", 2100, "units", 105),
            Map.of("product", "Garden Hose", "sales", 4500, "units", 30),
            Map.of("product", "Kitchen Organizer", "sales", 3800, "units", 76),
            Map.of("product", "Door Mat", "sales", 1200, "units", 60)
        ),
        "Sports", List.of(
            Map.of("product", "Yoga Mat", "sales", 5600, "units", 112),
            Map.of("product", "Dumbbells Set", "sales", 12300, "units", 41),
            Map.of("product", "Resistance Bands", "sales", 2400, "units", 160),
            Map.of("product", "Jump Rope", "sales", 1800, "units", 120),
            Map.of("product", "Water Bottle", "sales", 3200, "units", 160)
        ),
        "Books", List.of(
            Map.of("product", "Programming Guide", "sales", 4500, "units", 90),
            Map.of("product", "Business Strategy", "sales", 3200, "units", 64),
            Map.of("product", "Self-Help Bestseller", "sales", 6800, "units", 136),
            Map.of("product", "Science Fiction Novel", "sales", 2100, "units", 70),
            Map.of("product", "Cookbook Collection", "sales", 1900, "units", 38)
        )
    );

    // Monthly trend data
    private static final List<Map<String, Object>> MONTHLY_TRENDS = List.of(
        Map.of("month", "Jan", "revenue", 125000, "orders", 1450, "growth", 5.2),
        Map.of("month", "Feb", "revenue", 138000, "orders", 1580, "growth", 10.4),
        Map.of("month", "Mar", "revenue", 152000, "orders", 1720, "growth", 10.1),
        Map.of("month", "Apr", "revenue", 145000, "orders", 1650, "growth", -4.6),
        Map.of("month", "May", "revenue", 168000, "orders", 1890, "growth", 15.9),
        Map.of("month", "Jun", "revenue", 182000, "orders", 2010, "growth", 8.3),
        Map.of("month", "Jul", "revenue", 195000, "orders", 2150, "growth", 7.1),
        Map.of("month", "Aug", "revenue", 210000, "orders", 2320, "growth", 7.7),
        Map.of("month", "Sep", "revenue", 188000, "orders", 2080, "growth", -10.5),
        Map.of("month", "Oct", "revenue", 225000, "orders", 2480, "growth", 19.7),
        Map.of("month", "Nov", "revenue", 268000, "orders", 2950, "growth", 19.1),
        Map.of("month", "Dec", "revenue", 312000, "orders", 3420, "growth", 16.4)
    );

    /**
     * Query sales data for a given time period.
     *
     * @param startDate Start date in YYYY-MM-DD format
     * @param endDate   End date in YYYY-MM-DD format
     * @param category  Product category filter (optional)
     * @return JSON string containing sales data
     */
    @Tool("Query sales data for a given time period")
    public String querySalesData(
        @P("Start date in YYYY-MM-DD format") String startDate,
        @P("End date in YYYY-MM-DD format") String endDate,
        @P("Product category filter, optional") String category
    ) {
        log.info("Querying sales data: startDate={}, endDate={}, category={}",
            startDate, endDate, category);

        Map<String, Object> result = new HashMap<>();
        result.put("query", Map.of(
            "startDate", startDate,
            "endDate", endDate,
            "category", category != null ? category : "All"
        ));

        // Get category-specific data
        if (category != null && SALES_BY_CATEGORY.containsKey(category)) {
            result.put("products", SALES_BY_CATEGORY.get(category));
        } else {
            result.put("products", getAllProducts());
        }

        // Calculate summary
        List<Map<String, Object>> products = (List<Map<String, Object>>) result.get("products");
        int totalSales = products.stream()
            .mapToInt(p -> (Integer) p.get("sales"))
            .sum();
        int totalUnits = products.stream()
            .mapToInt(p -> (Integer) p.get("units"))
            .sum();

        result.put("summary", Map.of(
            "totalSales", totalSales,
            "totalUnits", totalUnits,
            "averagePerProduct", totalSales / products.size(),
            "productCount", products.size()
        ));

        // Return as JSON-like string
        return formatAsJson(result);
    }

    /**
     * Query monthly sales trends.
     *
     * @param year Year to query (e.g., "2024")
     * @return JSON string containing monthly trend data
     */
    @Tool("Query monthly sales trends for a given year")
    public String queryMonthlyTrends(
        @P("Year to query, e.g. 2024") String year
    ) {
        log.info("Querying monthly trends for year: {}", year);

        Map<String, Object> result = new HashMap<>();
        result.put("year", year);
        result.put("data", MONTHLY_TRENDS);

        // Calculate annual summary
        int totalRevenue = MONTHLY_TRENDS.stream()
            .mapToInt(m -> (Integer) m.get("revenue"))
            .sum();
        int totalOrders = MONTHLY_TRENDS.stream()
            .mapToInt(m -> (Integer) m.get("orders"))
            .sum();
        double avgGrowth = MONTHLY_TRENDS.stream()
            .mapToDouble(m -> ((Number) m.get("growth")).doubleValue())
            .average()
            .orElse(0.0);

        result.put("annualSummary", Map.of(
            "totalRevenue", totalRevenue,
            "totalOrders", totalOrders,
            "averageMonthlyGrowth", String.format("%.1f%%", avgGrowth),
            "bestMonth", "December",
            "bestMonthRevenue", 312000
        ));

        return formatAsJson(result);
    }

    /**
     * Get available product categories.
     *
     * @return JSON string containing category list
     */
    @Tool("Get available product categories")
    public String getCategories() {
        Map<String, Object> result = new HashMap<>();
        result.put("categories", SALES_BY_CATEGORY.keySet());
        return formatAsJson(result);
    }

    /**
     * Get top performing products.
     *
     * @param limit Maximum number of products to return
     * @return JSON string containing top products
     */
    @Tool("Get top performing products by sales")
    public String getTopProducts(
        @P("Maximum number of products to return") int limit
    ) {
        List<Map<String, Object>> allProducts = getAllProducts();

        // Sort by sales descending
        allProducts.sort((a, b) ->
            ((Integer) b.get("sales")).compareTo((Integer) a.get("sales")));

        // Take top N
        List<Map<String, Object>> topProducts = allProducts.subList(0,
            Math.min(limit, allProducts.size()));

        Map<String, Object> result = new HashMap<>();
        result.put("topProducts", topProducts);
        result.put("limit", limit);

        return formatAsJson(result);
    }

    private List<Map<String, Object>> getAllProducts() {
        List<Map<String, Object>> all = new ArrayList<>();
        SALES_BY_CATEGORY.values().forEach(all::addAll);
        return all;
    }

    private String formatAsJson(Map<String, Object> data) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) json.append(", ");
            first = false;
            json.append("\"").append(entry.getKey()).append("\": ");
            json.append(valueToJson(entry.getValue()));
        }

        json.append("}");
        return json.toString();
    }

    private String valueToJson(Object value) {
        if (value == null) return "null";
        if (value instanceof String) return "\"" + value + "\"";
        if (value instanceof Number || value instanceof Boolean) return value.toString();
        if (value instanceof List<?> list) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : list) {
                if (!first) sb.append(", ");
                first = false;
                sb.append(valueToJson(item));
            }
            sb.append("]");
            return sb.toString();
        }
        if (value instanceof Map<?, ?> map) {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) sb.append(", ");
                first = false;
                sb.append("\"").append(entry.getKey()).append("\": ");
                sb.append(valueToJson(entry.getValue()));
            }
            sb.append("}");
            return sb.toString();
        }
        return "\"" + value.toString() + "\"";
    }
}
