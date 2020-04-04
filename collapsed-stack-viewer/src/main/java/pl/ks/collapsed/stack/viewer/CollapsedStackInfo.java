package pl.ks.collapsed.stack.viewer;

import java.util.Collection;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CollapsedStackInfo {
    private Collection<MethodInfo> methods;
    private long totalCount;
}
