/*
 * Copyright 2020 Krzysztof Slusarski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.ks.collapsed.stack.viewer.creator;

import lombok.RequiredArgsConstructor;
import pl.ks.collapsed.stack.viewer.pages.Page;
import pl.ks.collapsed.stack.viewer.pages.PageCreator;
import pl.ks.collapsed.stack.viewer.pages.TableWithLinks;

import java.util.List;

import static pl.ks.collapsed.stack.viewer.pages.TableWithLinks.Link.of;

@RequiredArgsConstructor
public class FlameGraphsCreator implements PageCreator {
    private final String collapsedStackFile;
    private final String chartName;

    @Override
    public Page create() {
        return Page.builder()
                .fullName(chartName)
                .menuName(chartName)
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
