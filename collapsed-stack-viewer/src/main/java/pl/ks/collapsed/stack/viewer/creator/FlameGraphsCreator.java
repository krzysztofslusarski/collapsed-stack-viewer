package pl.ks.collapsed.stack.viewer.creator;

import static pl.ks.profiling.gui.commons.TableWithLinks.Link.of;

import java.util.List;
import lombok.RequiredArgsConstructor;
import pl.ks.profiling.gui.commons.Page;
import pl.ks.profiling.gui.commons.PageCreator;
import pl.ks.profiling.gui.commons.TableWithLinks;

@RequiredArgsConstructor
public class FlameGraphsCreator implements PageCreator {
    private final String collapsedStackFile;

    @Override
    public Page create() {
        return Page.builder()
                .fullName("Flame graphs")
                .menuName("Flame graphs")
                .icon(Page.Icon.STATS)
                .pageContents(List.of(
                        TableWithLinks.builder()
                                .header(List.of("Type"))
                                .table(
                                        List.of(
                                                List.of(of(LinkUtils.getFlameGraphHref(collapsedStackFile), "Flame graph")),
                                                List.of(of(LinkUtils.getNoThreadFlameGraphHref(collapsedStackFile), "Flame graph with no thread division")),
                                                List.of(of(LinkUtils.getHotspotFlameGraphHref(collapsedStackFile), "Hotspot flame graph")),
                                                List.of(of(LinkUtils.getHotspotLLimitedFlameGraphHref(collapsedStackFile, 10), "Hotspot flame graph - depth = 10")),
                                                List.of(of(LinkUtils.getHotspotLLimitedFlameGraphHref(collapsedStackFile, 20), "Hotspot flame graph - depth = 20")),
                                                List.of(of(LinkUtils.getHotspotLLimitedFlameGraphHref(collapsedStackFile, 30), "Hotspot flame graph - depth = 30"))
                                        )
                                )
                                .build()
                ))
                .build();
    }
}
