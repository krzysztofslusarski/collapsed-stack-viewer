package pl.ks.profiling.gui.commons;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

@Getter
@Value
@Builder
public class Chart implements PageContent {
    private Object[][] data;
    private ChartType chartType;
    private String title;
    private String info;

    @Override
    public ContentType getType() {
        return ContentType.CHART;
    }

    public enum ChartType {
        PIE,
        LINE,
        POINTS,
    }
}
