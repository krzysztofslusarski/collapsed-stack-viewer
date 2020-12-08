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
import pl.ks.collapsed.stack.viewer.CollapsedStackInfo;
import pl.ks.collapsed.stack.viewer.MethodInfo;
import pl.ks.collapsed.stack.viewer.pages.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class TotalTimeCreator implements PageCreator {
    private static final BigDecimal PERCENT_MULTIPLICAND = new BigDecimal(100);

    private final CollapsedStackInfo collapsedStackInfo;
    private final String collapsedStackFileName;
    private final BigDecimal totalTimeThreshold;
    private final BigDecimal selfTimeThreshold;

    @Override
    public Page create() {
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        BigDecimal totalCount = new BigDecimal(collapsedStackInfo.getTotalCount());

        List<MethodInfo> methodsToList = collapsedStackInfo.getMethods().stream()
                .filter(methodEntry -> overThreshold(totalCount, methodEntry))
                .sorted(Comparator.comparingLong(MethodInfo::getTotalSamples).reversed())
                .collect(Collectors.toList());
        return Page.builder()
                .fullName("Method total time")
                .menuName("Method total time")
                .icon(Page.Icon.STATS)
                .pageContents(List.of(
                        ProfilingResult.builder()
                                .filteredColumn(0)
                                .profilingEntries(
                                        methodsToList.stream()
                                                .map(methodEntry -> ProfilingEntry.builder()
                                                        .methodName(methodEntry.getName())
                                                        .percent(getPercent(methodEntry, totalCount, decimalFormat))
                                                        .samples(methodEntry.getTotalSamples())
                                                        .profilingLinks(ProfilingLinks.builder()
                                                                .fromMethodFlameGraph(LinkUtils.getFromMethodHref(collapsedStackFileName, methodEntry.getName()))
                                                                .toMethodFlameGraph(LinkUtils.getToMethodHref(collapsedStackFileName, methodEntry.getName()))
                                                                .fromMethodRoot(LinkUtils.getFromMethodRootHref(collapsedStackFileName, methodEntry.getName(), totalTimeThreshold, selfTimeThreshold))
                                                                .toMethodRoot(LinkUtils.getToMethodRootHref(collapsedStackFileName, methodEntry.getName(), totalTimeThreshold, selfTimeThreshold))
                                                                .build())
                                                        .build())
                                                .collect(Collectors.toList())
                                )
                                .build()
                ))
                .build();
    }

    private boolean overThreshold(BigDecimal totalCount, MethodInfo methodInfo) {
        return new BigDecimal(methodInfo.getTotalSamples()).divide(totalCount, 3, RoundingMode.HALF_EVEN).compareTo(totalTimeThreshold) > 0;
    }

    private static String getPercent(MethodInfo methodInfo, BigDecimal totalCount, DecimalFormat decimalFormat) {
        BigDecimal percent = new BigDecimal(methodInfo.getTotalSamples()).multiply(PERCENT_MULTIPLICAND).divide(totalCount, 2, RoundingMode.HALF_EVEN);
        return PageCreatorHelper.numToString(percent, decimalFormat);
    }
}
