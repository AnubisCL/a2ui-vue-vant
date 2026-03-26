package com.a2ui.backend.protocol;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import com.a2ui.backend.protocol.builder.A2uiMessageBuilder;
import com.a2ui.backend.protocol.builder.ComponentBuilder;
import com.a2ui.backend.protocol.builder.ComponentBuilder.FormField;
import com.a2ui.backend.protocol.model.ComponentMessage;
import com.a2ui.backend.protocol.model.ComponentSpec;
import com.a2ui.backend.protocol.model.DataModelMessage;
import com.a2ui.backend.protocol.model.DeleteSurfaceMessage;
import com.a2ui.backend.protocol.model.SurfaceMessage;

/**
 * Test suite for A2UI protocol message serialization.
 * Verifies that all message types serialize correctly to the expected JSON format.
 */
class A2uiProtocolTest {

    // ==================== Surface Message Tests ====================

    @Test
    @DisplayName("Surface message should serialize correctly")
    void testSurfaceMessage() {
        String json = A2uiMessageBuilder.createSurface("main", "Chat");
        assertNotNull(json);
        assertTrue(json.contains("\"type\":\"surface\""));
        assertTrue(json.contains("\"surfaceId\":\"main\""));
        assertTrue(json.contains("\"name\":\"Chat\""));
    }

    @Test
    @DisplayName("Surface message with metadata should include all fields")
    void testSurfaceMessageWithMetadata() {
        String json = A2uiMessageBuilder.createSurface("main", "Chat",
            Map.of("theme", "dark", "closable", true));

        assertNotNull(json);
        assertTrue(json.contains("\"theme\":\"dark\""));
        assertTrue(json.contains("\"closable\":true"));
    }

    @Test
    @DisplayName("Surface message of() factory should work")
    void testSurfaceMessageFactory() {
        SurfaceMessage msg = SurfaceMessage.of("test", "Test Surface");

        assertEquals("surface", msg.getType());
        assertEquals("test", msg.getSurfaceId());
        assertEquals("Test Surface", msg.getName());
        assertTrue(msg.isValid());
    }

    // ==================== Component Message Tests ====================

    @Test
    @DisplayName("Component message with Text should serialize correctly")
    void testTextComponent() {
        String json = A2uiMessageBuilder.appendText("main", "text1", "Hello World");

        assertNotNull(json);
        assertTrue(json.contains("\"type\":\"component\""));
        assertTrue(json.contains("\"surfaceId\":\"main\""));
        assertTrue(json.contains("\"componentId\":\"text1\""));
        assertTrue(json.contains("\"component\""));
        assertTrue(json.contains("\"type\":\"Text\""));
        assertTrue(json.contains("\"content\":\"Hello World\""));
        assertTrue(json.contains("\"position\":\"append\""));
    }

    @Test
    @DisplayName("Markdown component should include markdown flag")
    void testMarkdownComponent() {
        String json = A2uiMessageBuilder.appendMarkdown("main", "md1", "**Bold text**");

        assertNotNull(json);
        assertTrue(json.contains("\"markdown\":true"));
        assertTrue(json.contains("**Bold text**"));
    }

    @Test
    @DisplayName("Component message positions should be correct")
    void testComponentPositions() {
        ComponentSpec spec = ComponentSpec.text("Test");

        String append = A2uiEncoder.encode(ComponentMessage.append("main", "c1", spec));
        assertTrue(append.contains("\"position\":\"append\""));

        String prepend = A2uiEncoder.encode(ComponentMessage.prepend("main", "c1", spec));
        assertTrue(prepend.contains("\"position\":\"prepend\""));

        String replace = A2uiEncoder.encode(ComponentMessage.replace("main", "c1", spec));
        assertTrue(replace.contains("\"position\":\"replace\""));
    }

    // ==================== Component Builder Tests ====================

    @Test
    @DisplayName("ComponentBuilder should create valid Text component")
    void testComponentBuilderText() {
        ComponentSpec spec = ComponentBuilder.text("Hello").build();

        assertEquals("Text", spec.getType());
        assertEquals("Hello", spec.getProps().get("content"));
    }

    @Test
    @DisplayName("ComponentBuilder should create valid Chart component")
    void testComponentBuilderChart() {
        Map<String, Object> chartOption = Map.of(
            "xAxis", Map.of("type", "category"),
            "yAxis", Map.of("type", "value"),
            "series", List.of(Map.of("type", "line", "data", List.of(10, 20, 30)))
        );

        ComponentSpec spec = ComponentBuilder.chart(chartOption).build();

        assertEquals("Chart", spec.getType());
        assertNotNull(spec.getProps().get("option"));
    }

    @Test
    @DisplayName("ComponentBuilder should create valid Card component")
    void testComponentBuilderCard() {
        ComponentSpec spec = ComponentBuilder.card("Title", "Card content").build();

        assertEquals("Card", spec.getType());
        assertEquals("Title", spec.getProps().get("title"));
        assertEquals("Card content", spec.getProps().get("content"));
    }

    @Test
    @DisplayName("ComponentBuilder should create valid Button component")
    void testComponentBuilderButton() {
        ComponentSpec spec = ComponentBuilder.button("Click Me", "submit").build();

        assertEquals("Button", spec.getType());
        assertEquals("Click Me", spec.getProps().get("label"));
        assertEquals("submit", spec.getProps().get("action"));
    }

    @Test
    @DisplayName("ComponentBuilder should create Form with fields")
    void testComponentBuilderForm() {
        List<FormField> fields = List.of(
            FormField.text("username", "Username").required().placeholder("Enter username"),
            FormField.number("age", "Age").defaultValue(18)
        );

        ComponentSpec spec = ComponentBuilder.form(fields).build();

        assertEquals("Form", spec.getType());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> propsFields = (List<Map<String, Object>>) spec.getProps().get("fields");
        assertEquals(2, propsFields.size());
        assertEquals("username", propsFields.get(0).get("name"));
        assertEquals(true, propsFields.get(0).get("required"));
        assertEquals(18, propsFields.get(1).get("defaultValue"));
    }

    @Test
    @DisplayName("ComponentBuilder withProp should add properties")
    void testComponentBuilderWithProp() {
        ComponentSpec spec = ComponentBuilder.text("Hello")
            .withProp("className", "highlight")
            .withProp("visible", true)
            .build();

        assertEquals("highlight", spec.getProps().get("className"));
        assertEquals(true, spec.getProps().get("visible"));
    }

    // ==================== Data Model Tests ====================

    @Test
    @DisplayName("DataModel message should serialize correctly")
    void testDataModelMessage() {
        String json = A2uiMessageBuilder.updateDataModel("main",
            Map.of("userName", "John", "balance", 100));

        assertNotNull(json);
        assertTrue(json.contains("\"type\":\"dataModel\""));
        assertTrue(json.contains("\"userName\":\"John\""));
        assertTrue(json.contains("\"balance\":100"));
        assertTrue(json.contains("\"mergeStrategy\":\"merge\""));
    }

    @Test
    @DisplayName("DataModel replace strategy should be correct")
    void testDataModelReplace() {
        String json = A2uiMessageBuilder.replaceDataModel("main",
            Map.of("key", "value"));

        assertTrue(json.contains("\"mergeStrategy\":\"replace\""));
    }

    // ==================== Delete Surface Tests ====================

    @Test
    @DisplayName("DeleteSurface message should serialize correctly")
    void testDeleteSurfaceMessage() {
        String json = A2uiMessageBuilder.deleteSurface("modal1");

        assertNotNull(json);
        assertTrue(json.contains("\"type\":\"deleteSurface\""));
        assertTrue(json.contains("\"surfaceId\":\"modal1\""));
    }

    // ==================== JSONL Encoding Tests ====================

    @Test
    @DisplayName("JSONL encoding should produce correct format")
    void testJsonlEncoding() {
        List<Object> messages = List.of(
            SurfaceMessage.of("main", "Chat"),
            ComponentMessage.append("main", "text1", ComponentSpec.text("Hello"))
        );

        String jsonl = A2uiEncoder.encodeAsJsonl(messages);

        String[] lines = jsonl.split("\n");
        assertEquals(2, lines.length);
        assertTrue(lines[0].contains("\"type\":\"surface\""));
        assertTrue(lines[1].contains("\"type\":\"component\""));
    }

    @Test
    @DisplayName("JSONL with newline should end with newline")
    void testJsonlWithNewline() {
        List<Object> messages = List.of(
            SurfaceMessage.of("main", "Chat")
        );

        String jsonl = A2uiEncoder.encodeAsJsonlWithNewline(messages);

        assertTrue(jsonl.endsWith("\n"));
    }

    // ==================== Sequence Builder Tests ====================

    @Test
    @DisplayName("SequenceBuilder should build complete message sequence")
    void testSequenceBuilder() {
        String jsonl = A2uiMessageBuilder.SequenceBuilder.create()
            .surface("main", "Chat")
            .append("main", "text1", ComponentBuilder.text("Hello"))
            .data("main", Map.of("count", 0))
            .build();

        String[] lines = jsonl.split("\n");
        assertEquals(3, lines.length);
        assertTrue(lines[0].contains("\"type\":\"surface\""));
        assertTrue(lines[1].contains("\"type\":\"component\""));
        assertTrue(lines[2].contains("\"type\":\"dataModel\""));
    }

    // ==================== Validation Tests ====================

    @Test
    @DisplayName("ComponentMessage validation should work correctly")
    void testComponentMessageValidation() {
        ComponentMessage valid = ComponentMessage.append("main", "c1", ComponentSpec.text("Test"));
        assertTrue(valid.isValid());

        ComponentMessage noComponentId = new ComponentMessage();
        noComponentId.setType("component");
        noComponentId.setSurfaceId("main");
        noComponentId.setComponent(ComponentSpec.text("Test"));
        assertFalse(noComponentId.isValid());
    }

    // ==================== Quick Helper Tests ====================

    @Test
    @DisplayName("Quick helpers should produce correct JSON")
    void testQuickHelpers() {
        String card = A2uiMessageBuilder.appendCard("main", "card1", "Title", "Content");
        assertTrue(card.contains("\"type\":\"Card\""));
        assertTrue(card.contains("\"title\":\"Title\""));

        String chart = A2uiMessageBuilder.appendChart("main", "chart1", Map.of("series", List.of()));
        assertTrue(chart.contains("\"type\":\"Chart\""));

        String divider = A2uiMessageBuilder.appendDivider("main", "div1");
        assertTrue(divider.contains("\"type\":\"Divider\""));
    }

    @Test
    @DisplayName("generateId should produce unique IDs")
    void testGenerateId() {
        String id1 = A2uiMessageBuilder.generateId();
        String id2 = A2uiMessageBuilder.generateId();
        assertNotEquals(id1, id2);

        String prefixed = A2uiMessageBuilder.generateId("chart");
        assertTrue(prefixed.startsWith("chart_"));
    }

    // ==================== Decoder Tests ====================

    @Test
    @DisplayName("Decoder should parse JSON correctly")
    void testDecoder() {
        SurfaceMessage original = SurfaceMessage.of("test", "Test");
        String json = A2uiEncoder.encode(original);

        SurfaceMessage decoded = A2uiEncoder.decode(json, SurfaceMessage.class);

        assertEquals(original.getSurfaceId(), decoded.getSurfaceId());
        assertEquals(original.getName(), decoded.getName());
        assertEquals(original.getType(), decoded.getType());
    }

    // ==================== Null Handling Tests ====================

    @Test
    @DisplayName("Null values should be excluded from JSON")
    void testNullExclusion() {
        ComponentSpec spec = ComponentSpec.builder()
            .type("Text")
            .build();

        String json = A2uiEncoder.encode(spec);

        assertTrue(json.contains("\"type\":\"Text\""));
        // props with null should not be in JSON
        assertFalse(json.contains("\"props\":null"));
    }

    @Test
    @DisplayName("Encoder should throw on null message")
    void testNullMessage() {
        assertThrows(A2uiEncoder.A2uiEncodingException.class, () -> {
            A2uiEncoder.encode(null);
        });
    }
}
