package com.a2ui.backend.agent.general;

import com.a2ui.backend.tools.ChartGeneratorTool;
import com.a2ui.backend.tools.DataQueryTool;
import lombok.RequiredArgsConstructor;

/**
 * Tool container for GeneralAgent.
 * Aggregates all tools that GeneralAgent can use.
 */
@RequiredArgsConstructor
public class GeneralAgentTools {

    private final DataQueryTool dataQueryTool;
    private final ChartGeneratorTool chartGeneratorTool;

    public DataQueryTool getDataQueryTool() {
        return dataQueryTool;
    }

    public ChartGeneratorTool getChartGeneratorTool() {
        return chartGeneratorTool;
    }
}
