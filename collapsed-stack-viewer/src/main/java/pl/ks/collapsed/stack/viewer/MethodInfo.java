package pl.ks.collapsed.stack.viewer;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MethodInfo {
    private String name;
    private long selfSamples;
    private long totalSamples;

    void addSelfSamples(long selfSamples) {
        this.selfSamples += selfSamples;
    }

    void addTotalSamples(long totalSamples) {
        this.totalSamples += totalSamples;
    }
}
