package com.a2ui.backend.demo;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.a2ui.backend.protocol.A2uiEncoder;
import com.a2ui.backend.protocol.model.ComponentMessage;
import com.a2ui.backend.protocol.model.ComponentSpec;
import com.a2ui.backend.protocol.model.SurfaceMessage;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * Demo scenario for data query functionality.
 */
@Slf4j
@Component
public class DataQueryScenario implements DemoScenario {

    private final ConcurrentHashMap<String, Map<String, Object>> sessionState = new ConcurrentHashMap<>();

    private static final List<Map<String, Object>> SAMPLE_SALES_DATA = List.of(
        Map.of("month", "1月", "sales", 12000, "orders", 145, "avgOrderValue", 82.76),
        Map.of("month", "2月", "sales", 15000, "orders", 178, "avgOrderValue", 84.27),
        Map.of("month", "3月", "sales", 18000, "orders", 210, "avgOrderValue", 85.71),
        Map.of("month", "4月", "sales", 22000, "orders", 256, "avgOrderValue", 85.94),
        Map.of("month", "5月", "sales", 28000, "orders", 312, "avgOrderValue", 89.74),
        Map.of("month", "6月", "sales", 25000, "orders", 289, "avgOrderValue", 86.51),
        Map.of("month", "7月", "sales", 32000, "orders", 367, "avgOrderValue", 87.19),
        Map.of("month", "8月", "sales", 35000, "orders", 398, "avgOrderValue", 87.94),
        Map.of("month", "9月", "sales", 30000, "orders", 345, "avgOrderValue", 86.96),
        Map.of("month", "10月", "sales", 38000, "orders", 425, "avgOrderValue", 89.41),
        Map.of("month", "11月", "sales", 45000, "orders", 512, "avgOrderValue", 87.89),
        Map.of("month", "12月", "sales", 52000, "orders", 589, "avgOrderValue", 88.29)
    );

    @Override
    public String getName() {
        return "Data Query";
    }

    @Override
    public String getDescription() {
        return "Demonstrates data query with filter forms and chart visualization";
    }

    @Override
    public boolean matches(String userMessage) {
        if (userMessage == null) return false;
        String lowerMessage = userMessage.toLowerCase();

        // Exclude order-related messages
        if (lowerMessage.contains("order") || lowerMessage.contains("订单") ||
            lowerMessage.contains("购买") || lowerMessage.contains("商品")) {
            return false;
        }

        return lowerMessage.contains("sales") ||
               lowerMessage.contains("statistics") ||
               lowerMessage.contains("report") ||
               lowerMessage.contains("chart") ||
               lowerMessage.contains("销售") ||
               lowerMessage.contains("统计") ||
               lowerMessage.contains("报表") ||
               lowerMessage.contains("图表") ||
               lowerMessage.contains("折线图") ||
               lowerMessage.contains("生成") ||
               (lowerMessage.contains("数据") && !lowerMessage.contains("订单"));
    }

    @Override
    public Flux<String> execute(String userMessage, String sessionId) {
        log.info("Executing DataQueryScenario for session: {}", sessionId);

        String surfaceId = "main";
        Map<String, Object> filters = extractFilters(userMessage);

        if (filters.isEmpty()) {
            // Show form card with streaming effect
            return Flux.concat(
                // Step 1: Create surface
                Flux.just(createSurface(surfaceId))
                    .delayElements(Duration.ofMillis(300)),

                // Step 2: Show loading indicator
                Flux.just(createLoadingText(surfaceId, "📊 正在加载查询表单..."))
                    .delayElements(Duration.ofMillis(500)),

                // Step 3: Show form card
                Flux.fromIterable(createFormCard(surfaceId))
                    .delayElements(Duration.ofMillis(400))
            );
        } else {
            // Show chart with streaming effect
            boolean directChart = Boolean.TRUE.equals(filters.get("directChart"));

            Flux<String> chartFlow = Flux.concat(
                // Step 1: Create surface
                Flux.just(createSurface(surfaceId))
                    .delayElements(Duration.ofMillis(300)),

                // Step 2: Show loading indicator
                Flux.just(createLoadingText(surfaceId, "📈 正在分析数据..."))
                    .delayElements(Duration.ofMillis(600)),

                // Step 3: Show analyzing text
                Flux.just(createInfoText(surfaceId, "📊 正在统计销售数据..."))
                    .delayElements(Duration.ofMillis(500)),

                // Step 4: Show chart card + chart
                Flux.fromIterable(createChartCard(surfaceId, filters))
                    .delayElements(Duration.ofMillis(400))
            );

            // Step 5: Show data table (if not direct chart)
            if (!directChart) {
                chartFlow = Flux.concat(
                    chartFlow,
                    Flux.just(createInfoText(surfaceId, "📋 正在生成详细数据表..."))
                        .delayElements(Duration.ofMillis(500)),
                    Flux.fromIterable(createDataTableCard(surfaceId, filters))
                        .delayElements(Duration.ofMillis(400)),
                    // Final completion message
                    Flux.just(createSuccessText(surfaceId, "✅ 数据查询完成！"))
                        .delayElements(Duration.ofMillis(300))
                );
            } else {
                // Add completion for direct chart mode
                chartFlow = Flux.concat(
                    chartFlow,
                    Flux.just(createSuccessText(surfaceId, "✅ 图表生成完成！"))
                        .delayElements(Duration.ofMillis(300))
                );
            }

            sessionState.remove(sessionId);
            return chartFlow;
        }
    }

    private String createLoadingText(String surfaceId, String message) {
        return toJson(ComponentMessage.append(surfaceId,
            generateId("loading"),
            ComponentSpec.of("Text", Map.of(
                "content", "⏳ " + message,
                "size", "medium",
                "color", "#1890ff"
            ))
        ));
    }

    private String createInfoText(String surfaceId, String message) {
        return toJson(ComponentMessage.append(surfaceId,
            generateId("info"),
            ComponentSpec.of("Text", Map.of(
                "content", message,
                "size", "small",
                "color", "#666666"
            ))
        ));
    }

    private String createSuccessText(String surfaceId, String message) {
        return toJson(ComponentMessage.append(surfaceId,
            generateId("success"),
            ComponentSpec.of("Text", Map.of(
                "content", message,
                "size", "medium",
                "color", "#52c41a",
                "weight", "medium"
            ))
        ));
    }

    private Map<String, Object> extractFilters(String userMessage) {
        Map<String, Object> filters = new HashMap<>();
        String lowerMessage = userMessage.toLowerCase();

        // Direct chart request
        if (lowerMessage.contains("图表") || lowerMessage.contains("折线图") ||
            lowerMessage.contains("chart") || lowerMessage.contains("graph") ||
            lowerMessage.contains("可视化") || lowerMessage.contains("生成")) {
            filters.put("startDate", "2024-01-01");
            filters.put("endDate", "2024-12-31");
            filters.put("directChart", true);
            return filters;
        }

        // Date range detection
        if (lowerMessage.contains("q1") || lowerMessage.contains("第一季度")) {
            filters.put("startDate", "2024-01-01");
            filters.put("endDate", "2024-03-31");
        } else if (lowerMessage.contains("q2") || lowerMessage.contains("第二季度")) {
            filters.put("startDate", "2024-04-01");
            filters.put("endDate", "2024-06-30");
        } else if (lowerMessage.contains("q3") || lowerMessage.contains("第三季度")) {
            filters.put("startDate", "2024-07-01");
            filters.put("endDate", "2024-09-30");
        } else if (lowerMessage.contains("q4") || lowerMessage.contains("第四季度")) {
            filters.put("startDate", "2024-10-01");
            filters.put("endDate", "2024-12-31");
        } else if (lowerMessage.contains("year") || lowerMessage.contains("全年")) {
            filters.put("startDate", "2024-01-01");
            filters.put("endDate", "2024-12-31");
        } else if (lowerMessage.contains("上月")) {
            LocalDate lastMonth = LocalDate.now().minusMonths(1);
            filters.put("startDate", lastMonth.withDayOfMonth(1).toString());
            filters.put("endDate", lastMonth.withDayOfMonth(lastMonth.lengthOfMonth()).toString());
        } else if (lowerMessage.contains("本月")) {
            LocalDate now = LocalDate.now();
            filters.put("startDate", now.withDayOfMonth(1).toString());
            filters.put("endDate", now.withDayOfMonth(now.lengthOfMonth()).toString());
        }

        return filters;
    }

    private List<String> createFormCard(String surfaceId) {
        List<String> messages = new ArrayList<>();

        // Card component
        Map<String, Object> cardProps = new LinkedHashMap<>();
        cardProps.put("title", "📊 数据查询");
        cardProps.put("bordered", true);
        cardProps.put("elevated", true);
        cardProps.put("content", "请选择筛选条件查看销售数据：");

        messages.add(toJson(ComponentMessage.append(surfaceId,
            generateId("card"),
            ComponentSpec.of("Card", cardProps)
        )));

        // Form component
        List<Map<String, Object>> fields = List.of(
            Map.of(
                "name", "dateRange",
                "label", "时间范围",
                "type", "select",
                "required", false,
                "options", Map.of("options", List.of(
                    Map.of("value", "q1", "label", "第一季度 (1-3月)"),
                    Map.of("value", "q2", "label", "第二季度 (4-6月)"),
                    Map.of("value", "q3", "label", "第三季度 (7-9月)"),
                    Map.of("value", "q4", "label", "第四季度 (10-12月)"),
                    Map.of("value", "year", "label", "全年"),
                    Map.of("value", "lastMonth", "label", "上月"),
                    Map.of("value", "thisMonth", "label", "本月")
                ))
            ),
            Map.of(
                "name", "category",
                "label", "产品类别",
                "type", "select",
                "required", false,
                "options", Map.of("options", List.of(
                    Map.of("value", "all", "label", "全部分类"),
                    Map.of("value", "electronics", "label", "电子产品"),
                    Map.of("value", "clothing", "label", "服装"),
                    Map.of("value", "home", "label", "家居园艺"),
                    Map.of("value", "sports", "label", "运动"),
                    Map.of("value", "books", "label", "图书")
                ))
            )
        );

        messages.add(toJson(ComponentMessage.append(surfaceId,
            generateId("form"),
            ComponentSpec.of("Form", Map.of(
                "fields", fields,
                "submitLabel", "查询数据",
                "action", "data-query-submit"
            ))
        )));

        return messages;
    }

    private List<String> createChartCard(String surfaceId, Map<String, Object> filters) {
        List<String> messages = new ArrayList<>();
        List<Map<String, Object>> filteredData = filterData(filters);

        List<String> months = new ArrayList<>();
        List<Integer> salesValues = new ArrayList<>();
        List<Integer> orderValues = new ArrayList<>();

        for (Map<String, Object> row : filteredData) {
            months.add((String) row.get("month"));
            salesValues.add((Integer) row.get("sales"));
            orderValues.add((Integer) row.get("orders"));
        }

        int totalSales = salesValues.stream().mapToInt(Integer::intValue).sum();
        int totalOrders = orderValues.stream().mapToInt(Integer::intValue).sum();

        // Card
        Map<String, Object> cardProps = new LinkedHashMap<>();
        cardProps.put("title", "📈 销售趋势图表");
        cardProps.put("bordered", true);
        cardProps.put("elevated", true);
        cardProps.put("content", String.format("**总销售额:** ¥%,d  |  **总订单数:** %,d", totalSales, totalOrders));

        messages.add(toJson(ComponentMessage.append(surfaceId,
            generateId("card"),
            ComponentSpec.of("Card", cardProps)
        )));

        // Chart
        Map<String, Object> chartOption = Map.of(
            "title", Map.of("text", "销售趋势", "left", "center"),
            "tooltip", Map.of("trigger", "axis"),
            "legend", Map.of("data", List.of("销售额", "订单数"), "bottom", "0"),
            "grid", Map.of("left", "3%", "right", "4%", "bottom", "15%", "containLabel", true),
            "xAxis", Map.of("type", "category", "data", months, "axisLabel", Map.of("interval", 0)),
            "yAxis", List.of(
                Map.of("type", "value", "name", "销售额(元)"),
                Map.of("type", "value", "name", "订单数")
            ),
            "series", List.of(
                Map.of(
                    "name", "销售额",
                    "data", salesValues,
                    "type", "line",
                    "smooth", true,
                    "itemStyle", Map.of("color", "#5470c6"),
                    "areaStyle", Map.of("color", "rgba(84, 112, 198, 0.2)")
                ),
                Map.of(
                    "name", "订单数",
                    "data", orderValues,
                    "type", "bar",
                    "yAxisIndex", 1,
                    "itemStyle", Map.of("color", "#91cc75")
                )
            )
        );

        messages.add(toJson(ComponentMessage.append(surfaceId,
            generateId("chart"),
            ComponentSpec.of("Chart", Map.of("option", chartOption, "height", 350))
        )));

        return messages;
    }

    private List<String> createDataTableCard(String surfaceId, Map<String, Object> filters) {
        List<String> messages = new ArrayList<>();
        List<Map<String, Object>> filteredData = filterData(filters);

        int totalSales = filteredData.stream().mapToInt(m -> (Integer) m.get("sales")).sum();
        int totalOrders = filteredData.stream().mapToInt(m -> (Integer) m.get("orders")).sum();
        double avgOrderValue = (double) totalSales / totalOrders;

        // Card
        Map<String, Object> cardProps = new LinkedHashMap<>();
        cardProps.put("title", "📋 详细数据");
        cardProps.put("bordered", true);

        messages.add(toJson(ComponentMessage.append(surfaceId,
            generateId("card"),
            ComponentSpec.of("Card", cardProps)
        )));

        // Table
        List<Map<String, Object>> columns = List.of(
            Map.of("key", "month", "title", "月份", "width", 80),
            Map.of("key", "sales", "title", "销售额", "width", 120),
            Map.of("key", "orders", "title", "订单数", "width", 100),
            Map.of("key", "avgOrderValue", "title", "客单价", "width", 100)
        );

        List<Map<String, Object>> tableData = new ArrayList<>();
        tableData.add(Map.of(
            "month", "合计",
            "sales", String.format("¥%,d", totalSales),
            "orders", totalOrders,
            "avgOrderValue", String.format("¥%.2f", avgOrderValue)
        ));
        tableData.addAll(filteredData);

        messages.add(toJson(ComponentMessage.append(surfaceId,
            generateId("table"),
            ComponentSpec.of("Table", Map.of(
                "columns", columns,
                "dataSource", tableData,
                "pagination", Map.of("pageSize", 12)
            ))
        )));

        return messages;
    }

    private String createSurface(String surfaceId) {
        return toJson(SurfaceMessage.of(surfaceId, "Data Query"));
    }

    private List<Map<String, Object>> filterData(Map<String, Object> filters) {
        String startDate = (String) filters.getOrDefault("startDate", "2024-01-01");
        String endDate = (String) filters.getOrDefault("endDate", "2024-12-31");

        int startMonth = extractMonth(startDate);
        int endMonth = extractMonth(endDate);

        if (startMonth <= 1 && endMonth >= 12) {
            return SAMPLE_SALES_DATA;
        }

        List<Map<String, Object>> filtered = new ArrayList<>();
        for (int i = startMonth - 1; i <= Math.min(endMonth - 1, SAMPLE_SALES_DATA.size() - 1); i++) {
            filtered.add(SAMPLE_SALES_DATA.get(i));
        }
        return filtered;
    }

    private int extractMonth(String dateStr) {
        if (dateStr == null) return 1;
        String[] parts = dateStr.split("-");
        if (parts.length >= 2) {
            try {
                return Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                return 1;
            }
        }
        return 1;
    }

    private String generateId(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String toJson(Object message) {
        return A2uiEncoder.encode(message);
    }
}
