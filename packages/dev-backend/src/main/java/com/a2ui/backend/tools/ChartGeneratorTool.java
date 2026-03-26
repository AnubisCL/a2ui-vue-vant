package com.a2ui.backend.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * AITool for generating chart configurations.
 *
 * This tool generates ECharts-compatible chart option objects that can be
 * used in A2UI Chart components. It supports various chart types including
 * line, bar, pie, and combination charts.
 *
 * Usage in LangChain4j:
 * <pre>
 * &#64;Tool("Generate a chart from data")
 * public String generateChart(
 *     &#64;P("Chart type: line, bar, pie") String chartType,
 *     &#64;P("Chart title") String title,
 *     &#64;P("Data in JSON format") String data
 * )
 * </pre>
 */
@Slf4j
@Component
public class ChartGeneratorTool {

    // Default color palette
    private static final List<String> DEFAULT_COLORS = List.of(
        "#5470c6", "#91cc75", "#fac858", "#ee6666", "#73c0de",
        "#3ba272", "#fc8452", "#9a60b4", "#ea7ccc", "#48b8d0"
    );

    /**
     * Generate a chart configuration from data.
     *
     * @param chartType Type of chart: "line", "bar", "pie", "area", "scatter"
     * @param title     Chart title
     * @param dataJson  Data in JSON format (will be parsed)
     * @return JSON string containing ECharts option
     */
    public String generateChart(String chartType, String title, String dataJson) {
        log.info("Generating chart: type={}, title={}", chartType, title);

        Map<String, Object> option = new HashMap<>();

        // Title
        option.put("title", Map.of(
            "text", title != null ? title : "Chart",
            "left", "center"
        ));

        // Tooltip
        option.put("tooltip", Map.of(
            "trigger", "pie".equals(chartType) ? "item" : "axis",
            "confine", true
        ));

        // Legend
        option.put("legend", Map.of(
            "bottom", 10,
            "left", "center"
        ));

        // Generate chart-specific configuration
        switch (chartType.toLowerCase()) {
            case "line":
                option.putAll(createLineChartConfig(dataJson));
                break;
            case "bar":
                option.putAll(createBarChartConfig(dataJson));
                break;
            case "pie":
                option.putAll(createPieChartConfig(dataJson));
                break;
            case "area":
                option.putAll(createAreaChartConfig(dataJson));
                break;
            case "scatter":
                option.putAll(createScatterChartConfig(dataJson));
                break;
            default:
                option.putAll(createBarChartConfig(dataJson));
        }

        // Color palette
        option.put("color", DEFAULT_COLORS);

        return formatAsJson(option);
    }

    /**
     * Generate a sales performance chart with multiple metrics.
     *
     * @param title    Chart title
     * @param months   List of month labels
     * @param sales    Sales data values
     * @param orders   Order count values (optional)
     * @return JSON string containing ECharts option
     */
    public String generateSalesChart(String title, List<String> months,
                                      List<Integer> sales, List<Integer> orders) {
        log.info("Generating sales chart with {} data points", months.size());

        Map<String, Object> option = new HashMap<>();

        option.put("title", Map.of(
            "text", title != null ? title : "Sales Performance",
            "left", "center"
        ));

        option.put("tooltip", Map.of(
            "trigger", "axis",
            "axisPointer", Map.of("type", "cross")
        ));

        option.put("legend", Map.of(
            "data", orders != null ? List.of("Sales", "Orders") : List.of("Sales"),
            "bottom", 10
        ));

        option.put("grid", Map.of(
            "left", "3%",
            "right", "4%",
            "bottom", "15%",
            "containLabel", true
        ));

        option.put("xAxis", Map.of(
            "type", "category",
            "data", months,
            "boundaryGap", false
        ));

        option.put("yAxis", List.of(
            Map.of(
                "type", "value",
                "name", "Sales ($)",
                "position", "left"
            ),
            Map.of(
                "type", "value",
                "name", "Orders",
                "position", "right"
            )
        ));

        List<Map<String, Object>> series = new ArrayList<>();
        series.add(Map.of(
            "name", "Sales",
            "type", "line",
            "data", sales,
            "smooth", true,
            "areaStyle", Map.of("opacity", 0.3),
            "itemStyle", Map.of("color", "#5470c6")
        ));

        if (orders != null && !orders.isEmpty()) {
            series.add(Map.of(
                "name", "Orders",
                "type", "bar",
                "yAxisIndex", 1,
                "data", orders,
                "itemStyle", Map.of("color", "#91cc75")
            ));
        }

        option.put("series", series);

        return formatAsJson(option);
    }

    /**
     * Generate a category distribution pie chart.
     *
     * @param title      Chart title
     * @param categories Category labels
     * @param values     Category values
     * @return JSON string containing ECharts option
     */
    public String generatePieChart(String title, List<String> categories, List<Integer> values) {
        log.info("Generating pie chart with {} categories", categories.size());

        Map<String, Object> option = new HashMap<>();

        option.put("title", Map.of(
            "text", title != null ? title : "Distribution",
            "left", "center"
        ));

        option.put("tooltip", Map.of(
            "trigger", "item",
            "formatter", "{a} <br/>{b}: {c} ({d}%)"
        ));

        option.put("legend", Map.of(
            "orient", "vertical",
            "left", "left",
            "top", "middle"
        ));

        // Build data array
        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 0; i < categories.size() && i < values.size(); i++) {
            data.add(Map.of(
                "name", categories.get(i),
                "value", values.get(i)
            ));
        }

        option.put("series", List.of(
            Map.of(
                "name", "Category",
                "type", "pie",
                "radius", List.of("40%", "70%"),
                "center", List.of("60%", "50%"),
                "avoidLabelOverlap", false,
                "label", Map.of(
                    "show", true,
                    "position", "outside"
                ),
                "emphasis", Map.of(
                    "label", Map.of(
                        "show", true,
                        "fontSize", 16,
                        "fontWeight", "bold"
                    )
                ),
                "data", data
            )
        ));

        option.put("color", DEFAULT_COLORS);

        return formatAsJson(option);
    }

    /**
     * Generate a comparison bar chart.
     *
     * @param title    Chart title
     * @param labels   X-axis labels
     * @param series   List of series data (each with name and values)
     * @return JSON string containing ECharts option
     */
    public String generateComparisonChart(String title, List<String> labels,
                                           List<Map<String, Object>> seriesData) {
        log.info("Generating comparison chart with {} labels", labels.size());

        Map<String, Object> option = new HashMap<>();

        option.put("title", Map.of(
            "text", title != null ? title : "Comparison",
            "left", "center"
        ));

        option.put("tooltip", Map.of(
            "trigger", "axis",
            "axisPointer", Map.of("type", "shadow")
        ));

        List<String> legendData = new ArrayList<>();
        for (Map<String, Object> s : seriesData) {
            legendData.add((String) s.get("name"));
        }

        option.put("legend", Map.of(
            "data", legendData,
            "bottom", 10
        ));

        option.put("grid", Map.of(
            "left", "3%",
            "right", "4%",
            "bottom", "15%",
            "containLabel", true
        ));

        option.put("xAxis", Map.of(
            "type", "category",
            "data", labels
        ));

        option.put("yAxis", Map.of(
            "type", "value"
        ));

        List<Map<String, Object>> series = new ArrayList<>();
        for (int i = 0; i < seriesData.size(); i++) {
            Map<String, Object> s = seriesData.get(i);
            series.add(Map.of(
                "name", s.get("name"),
                "type", "bar",
                "data", s.get("values"),
                "itemStyle", Map.of("color", DEFAULT_COLORS.get(i % DEFAULT_COLORS.size()))
            ));
        }

        option.put("series", series);

        return formatAsJson(option);
    }

    private Map<String, Object> createLineChartConfig(String dataJson) {
        Map<String, Object> config = new HashMap<>();
        SimpleData data = parseSimpleData(dataJson);

        config.put("xAxis", Map.of(
            "type", "category",
            "data", data.labels,
            "boundaryGap", false
        ));

        config.put("yAxis", Map.of(
            "type", "value"
        ));

        config.put("series", List.of(
            Map.of(
                "type", "line",
                "data", data.values,
                "smooth", true,
                "symbol", "circle",
                "symbolSize", 8
            )
        ));

        return config;
    }

    private Map<String, Object> createBarChartConfig(String dataJson) {
        Map<String, Object> config = new HashMap<>();
        SimpleData data = parseSimpleData(dataJson);

        config.put("xAxis", Map.of(
            "type", "category",
            "data", data.labels
        ));

        config.put("yAxis", Map.of(
            "type", "value"
        ));

        config.put("series", List.of(
            Map.of(
                "type", "bar",
                "data", data.values,
                "barMaxWidth", 60
            )
        ));

        return config;
    }

    private Map<String, Object> createPieChartConfig(String dataJson) {
        Map<String, Object> config = new HashMap<>();
        SimpleData data = parseSimpleData(dataJson);

        List<Map<String, Object>> pieData = new ArrayList<>();
        for (int i = 0; i < data.labels.size() && i < data.values.size(); i++) {
            pieData.add(Map.of(
                "name", data.labels.get(i),
                "value", data.values.get(i)
            ));
        }

        config.put("series", List.of(
            Map.of(
                "type", "pie",
                "radius", "60%",
                "data", pieData,
                "emphasis", Map.of(
                    "itemStyle", Map.of(
                        "shadowBlur", 10,
                        "shadowOffsetX", 0,
                        "shadowColor", "rgba(0, 0, 0, 0.5)"
                    )
                )
            )
        ));

        return config;
    }

    private Map<String, Object> createAreaChartConfig(String dataJson) {
        Map<String, Object> config = new HashMap<>();
        SimpleData data = parseSimpleData(dataJson);

        config.put("xAxis", Map.of(
            "type", "category",
            "data", data.labels,
            "boundaryGap", false
        ));

        config.put("yAxis", Map.of(
            "type", "value"
        ));

        config.put("series", List.of(
            Map.of(
                "type", "line",
                "data", data.values,
                "smooth", true,
                "areaStyle", Map.of("opacity", 0.5)
            )
        ));

        return config;
    }

    private Map<String, Object> createScatterChartConfig(String dataJson) {
        Map<String, Object> config = new HashMap<>();
        SimpleData data = parseSimpleData(dataJson);

        config.put("xAxis", Map.of("type", "value"));
        config.put("yAxis", Map.of("type", "value"));

        // Convert to scatter data [x, y] pairs
        List<List<Integer>> scatterData = new ArrayList<>();
        for (int i = 0; i < data.values.size(); i++) {
            scatterData.add(List.of(i + 1, (Integer) data.values.get(i)));
        }

        config.put("series", List.of(
            Map.of(
                "type", "scatter",
                "data", scatterData,
                "symbolSize", 10
            )
        ));

        return config;
    }

    private SimpleData parseSimpleData(String dataJson) {
        SimpleData data = new SimpleData();

        if (dataJson == null || dataJson.isEmpty()) {
            // Default sample data
            data.labels = List.of("A", "B", "C", "D", "E");
            data.values = List.of(10, 20, 30, 25, 15);
            return data;
        }

        // Simple parsing - extract labels and values from JSON-like string
        // Format expected: {"labels":["A","B","C"],"values":[10,20,30]}
        try {
            String cleanJson = dataJson.trim();
            if (cleanJson.startsWith("{") && cleanJson.endsWith("}")) {
                // Extract labels
                int labelsStart = cleanJson.indexOf("\"labels\":[");
                if (labelsStart >= 0) {
                    int labelsEnd = cleanJson.indexOf("]", labelsStart);
                    String labelsStr = cleanJson.substring(
                        labelsStart + "\"labels\":[".length(), labelsEnd);
                    data.labels = parseStringArray(labelsStr);
                }

                // Extract values
                int valuesStart = cleanJson.indexOf("\"values\":[");
                if (valuesStart >= 0) {
                    int valuesEnd = cleanJson.indexOf("]", valuesStart);
                    String valuesStr = cleanJson.substring(
                        valuesStart + "\"values\":[".length(), valuesEnd);
                    data.values = parseIntArray(valuesStr);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse data JSON, using defaults: {}", e.getMessage());
            data.labels = List.of("A", "B", "C", "D", "E");
            data.values = List.of(10, 20, 30, 25, 15);
        }

        if (data.labels.isEmpty()) {
            data.labels = List.of("A", "B", "C", "D", "E");
        }
        if (data.values.isEmpty()) {
            data.values = List.of(10, 20, 30, 25, 15);
        }

        return data;
    }

    private List<String> parseStringArray(String str) {
        List<String> result = new ArrayList<>();
        String[] parts = str.replace("\"", "").split(",");
        for (String part : parts) {
            result.add(part.trim());
        }
        return result;
    }

    private List<Integer> parseIntArray(String str) {
        List<Integer> result = new ArrayList<>();
        String[] parts = str.split(",");
        for (String part : parts) {
            try {
                result.add(Integer.parseInt(part.trim()));
            } catch (NumberFormatException e) {
                result.add(0);
            }
        }
        return result;
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
        if (value instanceof String) return "\"" + escapeJson((String) value) + "\"";
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
        return "\"" + escapeJson(value.toString()) + "\"";
    }

    private String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    private static class SimpleData {
        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
    }
}
