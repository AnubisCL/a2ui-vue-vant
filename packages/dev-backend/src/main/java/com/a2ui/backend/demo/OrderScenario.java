package com.a2ui.backend.demo;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
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
 * Demo scenario for order management functionality.
 *
 * This scenario demonstrates:
 * - Displaying lists using Card components
 * - Order details view
 * - Status updates and actions
 *
 * Keywords: "order", "purchase", "product", "order list", "my orders"
 */
@Slf4j
@Component
public class OrderScenario implements DemoScenario {

    // Session state for tracking selected order
    private final ConcurrentHashMap<String, String> selectedOrders = new ConcurrentHashMap<>();

    // Sample orders data
    private static final List<Map<String, Object>> SAMPLE_ORDERS = List.of(
        Map.<String, Object>of(
            "orderId", "ORD-2024-001",
            "status", "Delivered",
            "statusColor", "#52c41a",
            "totalAmount", 1299.00,
            "itemCount", 3,
            "orderDate", "2024-01-15 14:30",
            "deliveryDate", "2024-01-18 16:45",
            "items", List.of(
                Map.of("name", "Wireless Earbuds Pro", "quantity", 1, "price", 299.00),
                Map.of("name", "USB-C Charging Cable (2-pack)", "quantity", 2, "price", 49.99),
                Map.of("name", "Phone Case - Black", "quantity", 1, "price", 29.99)
            ),
            "shippingAddress", "123 Main St, Apt 4B, New York, NY 10001",
            "trackingNumber", "TRK123456789"
        ),
        Map.<String, Object>of(
            "orderId", "ORD-2024-002",
            "status", "Shipped",
            "statusColor", "#1890ff",
            "totalAmount", 599.00,
            "itemCount", 1,
            "orderDate", "2024-01-20 09:15",
            "deliveryDate", "Expected: 2024-01-25",
            "items", List.of(
                Map.of("name", "Smart Watch Series 5", "quantity", 1, "price", 599.00)
            ),
            "shippingAddress", "456 Oak Ave, Los Angeles, CA 90001",
            "trackingNumber", "TRK987654321"
        ),
        Map.<String, Object>of(
            "orderId", "ORD-2024-003",
            "status", "Processing",
            "statusColor", "#faad14",
            "totalAmount", 2150.00,
            "itemCount", 4,
            "orderDate", "2024-01-22 11:45",
            "deliveryDate", "Expected: 2024-01-28",
            "items", List.of(
                Map.of("name", "Laptop Stand", "quantity", 1, "price", 89.00),
                Map.of("name", "Mechanical Keyboard RGB", "quantity", 1, "price", 159.00),
                Map.of("name", "27\" 4K Monitor", "quantity", 1, "price", 1799.00),
                Map.of("name", "Mouse Pad XL", "quantity", 1, "price", 39.00)
            ),
            "shippingAddress", "789 Pine Rd, Chicago, IL 60601",
            "trackingNumber", "Pending"
        ),
        Map.<String, Object>of(
            "orderId", "ORD-2024-004",
            "status", "Pending Payment",
            "statusColor", "#ff4d4f",
            "totalAmount", 89.99,
            "itemCount", 2,
            "orderDate", "2024-01-23 16:20",
            "deliveryDate", "Awaiting Payment",
            "items", List.of(
                Map.of("name", "Wireless Mouse", "quantity", 1, "price", 49.99),
                Map.of("name", "Mouse Pad", "quantity", 1, "price", 19.99)
            ),
            "shippingAddress", "321 Elm St, Seattle, WA 98101",
            "trackingNumber", "N/A"
        )
    );

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public String getName() {
        return "Order Management";
    }

    @Override
    public String getDescription() {
        return "Demonstrates order listing with card components and order detail views";
    }

    @Override
    public boolean matches(String userMessage) {
        if (userMessage == null) {
            return false;
        }
        String lowerMessage = userMessage.toLowerCase();
        return lowerMessage.contains("order") ||
               lowerMessage.contains("purchase") ||
               lowerMessage.contains("product") ||
               lowerMessage.contains("buy") ||
               lowerMessage.contains("my orders") ||
               // Chinese keywords
               lowerMessage.contains("订单") ||
               lowerMessage.contains("购买") ||
               lowerMessage.contains("商品") ||
               lowerMessage.contains("我的订单");
    }

    @Override
    public Flux<String> execute(String userMessage, String sessionId) {
        log.info("Executing OrderScenario for session: {}", sessionId);

        String surfaceId = "main";
        String specificOrderId = extractOrderId(userMessage);

        if (specificOrderId != null) {
            // Show order details with streaming effect
            return Flux.concat(
                // Step 1: Create surface
                Flux.just(createSurface(surfaceId))
                    .delayElements(Duration.ofMillis(300)),

                // Step 2: Show loading
                Flux.just(createLoadingText(surfaceId, "📋 正在加载订单详情..."))
                    .delayElements(Duration.ofMillis(500)),

                // Step 3: Show order detail
                Flux.just(createOrderDetail(surfaceId, specificOrderId))
                    .delayElements(Duration.ofMillis(400)),

                // Step 4: Show back button
                Flux.just(createBackButton(surfaceId))
                    .delayElements(Duration.ofMillis(200)),

                // Step 5: Show divider
                Flux.just(createDivider(surfaceId))
                    .delayElements(Duration.ofMillis(100))
            );
        } else {
            // Show order list with streaming effect
            return Flux.concat(
                // Step 1: Create surface
                Flux.just(createSurface(surfaceId))
                    .delayElements(Duration.ofMillis(300)),

                // Step 2: Show loading
                Flux.just(createLoadingText(surfaceId, "📦 正在查询订单列表..."))
                    .delayElements(Duration.ofMillis(500)),

                // Step 3: Show header
                Flux.just(createOrderListHeader(surfaceId))
                    .delayElements(Duration.ofMillis(400)),

                // Step 4: Show order cards one by one
                Flux.fromIterable(createOrderCards(surfaceId))
                    .delayElements(Duration.ofMillis(300)),

                // Step 5: Show divider
                Flux.just(createDivider(surfaceId))
                    .delayElements(Duration.ofMillis(100))
            );
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

    /**
     * Extract order ID from user message if present.
     */
    private String extractOrderId(String userMessage) {
        if (userMessage == null) return null;

        String upperMessage = userMessage.toUpperCase();
        for (Map<String, Object> order : SAMPLE_ORDERS) {
            String orderId = (String) order.get("orderId");
            if (upperMessage.contains(orderId)) {
                return orderId;
            }
        }

        // Check for "details" or numbers
        if (userMessage.contains("detail") || userMessage.contains("详情")) {
            // Try to extract order number
            for (int i = 1; i <= SAMPLE_ORDERS.size(); i++) {
                if (userMessage.contains(String.valueOf(i))) {
                    return (String) SAMPLE_ORDERS.get(i - 1).get("orderId");
                }
            }
        }

        return null;
    }

    private String createSurface(String surfaceId) {
        return toJson(SurfaceMessage.of(surfaceId, "Order Management"));
    }

    private String createOrderListHeader(String surfaceId) {
        return toJson(ComponentMessage.append(surfaceId,
            generateId("text"),
            ComponentSpec.of("Text", Map.of(
                "content", "## My Orders\n\n" +
                    "Here are your recent orders. Click on an order to view details.\n\n" +
                    "**Total Orders:** " + SAMPLE_ORDERS.size(),
                "markdown", true
            ))
        ));
    }

    private List<String> createOrderCards(String surfaceId) {
        List<String> cards = new ArrayList<>();

        for (Map<String, Object> order : SAMPLE_ORDERS) {
            String orderId = (String) order.get("orderId");
            String status = (String) order.get("status");
            String statusColor = (String) order.get("statusColor");
            Double totalAmount = (Double) order.get("totalAmount");
            Integer itemCount = (Integer) order.get("itemCount");
            String orderDate = (String) order.get("orderDate");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) order.get("items");
            String firstItem = items.isEmpty() ? "Unknown" : (String) items.get(0).get("name");
            String itemSummary = items.size() > 1
                ? firstItem + " and " + (items.size() - 1) + " more"
                : firstItem;

            StringBuilder cardContent = new StringBuilder();
            cardContent.append("**").append(firstItem).append("**\n\n");
            if (items.size() > 1) {
                cardContent.append("*+ ").append(items.size() - 1).append(" more items*\n\n");
            }
            cardContent.append("**Total:** $").append(String.format("%.2f", totalAmount)).append("\n");
            cardContent.append("**Date:** ").append(orderDate).append("\n");
            cardContent.append("**Status:** ").append(status);

            Map<String, Object> cardProps = new HashMap<>();
            cardProps.put("title", orderId);
            cardProps.put("content", cardContent.toString());
            cardProps.put("bordered", true);
            cardProps.put("hoverable", true);

            // Add action button
            cardProps.put("actions", List.of(
                Map.of(
                    "label", "View Details",
                    "action", "order:" + orderId,
                    "type", "primary"
                )
            ));

            cards.add(toJson(ComponentMessage.append(surfaceId,
                generateId("card"),
                ComponentSpec.of("Card", cardProps)
            )));
        }

        return cards;
    }

    private String createOrderDetail(String surfaceId, String orderId) {
        Map<String, Object> order = SAMPLE_ORDERS.stream()
            .filter(o -> orderId.equals(o.get("orderId")))
            .findFirst()
            .orElse(null);

        if (order == null) {
            return toJson(ComponentMessage.append(surfaceId,
                generateId("text"),
                ComponentSpec.of("Text", Map.of(
                    "content", "**Order Not Found**\n\n" +
                        "The order " + orderId + " could not be found.",
                    "markdown", true
                ))
            ));
        }

        StringBuilder content = new StringBuilder();
        content.append("## Order Details\n\n");
        content.append("**Order ID:** ").append(order.get("orderId")).append("\n\n");

        // Status badge
        content.append("**Status:** `").append(order.get("status")).append("`\n\n");
        content.append("---\n\n");

        // Items
        content.append("### Items\n\n");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) order.get("items");
        for (Map<String, Object> item : items) {
            content.append("- **").append(item.get("name")).append("**\n");
            content.append("  Qty: ").append(item.get("quantity"));
            content.append(" | $").append(String.format("%.2f", item.get("price"))).append("\n\n");
        }

        // Totals
        content.append("---\n\n");
        content.append("### Summary\n\n");
        content.append("**Items:** ").append(order.get("itemCount")).append("\n");
        content.append("**Total Amount:** $").append(String.format("%.2f", order.get("totalAmount"))).append("\n\n");

        // Dates
        content.append("---\n\n");
        content.append("### Timeline\n\n");
        content.append("**Order Date:** ").append(order.get("orderDate")).append("\n");
        content.append("**Delivery:** ").append(order.get("deliveryDate")).append("\n\n");

        // Shipping
        content.append("---\n\n");
        content.append("### Shipping\n\n");
        content.append("**Address:** ").append(order.get("shippingAddress")).append("\n");
        content.append("**Tracking:** ").append(order.get("trackingNumber")).append("\n");

        return toJson(ComponentMessage.append(surfaceId,
            generateId("text"),
            ComponentSpec.of("Text", Map.of(
                "content", content.toString(),
                "markdown", true
            ))
        ));
    }

    private String createBackButton(String surfaceId) {
        return toJson(ComponentMessage.append(surfaceId,
            generateId("button"),
            ComponentSpec.of("Button", Map.of(
                "label", "< Back to Order List",
                "action", "order:list",
                "type", "default"
            ))
        ));
    }

    private String createDivider(String surfaceId) {
        return toJson(ComponentMessage.append(surfaceId,
            generateId("divider"),
            ComponentSpec.of("Divider", Map.of())
        ));
    }

    private String generateId(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String toJson(Object message) {
        return A2uiEncoder.encode(message);
    }
}
