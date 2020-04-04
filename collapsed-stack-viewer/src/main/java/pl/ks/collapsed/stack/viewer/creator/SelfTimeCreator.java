package pl.ks.collapsed.stack.viewer.creator;

import static pl.ks.profiling.gui.commons.TableWithLinks.Link.of;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import pl.ks.collapsed.stack.viewer.CollapsedStackInfo;
import pl.ks.collapsed.stack.viewer.MethodInfo;
import pl.ks.profiling.gui.commons.Page;
import pl.ks.profiling.gui.commons.PageCreator;
import pl.ks.profiling.gui.commons.PageCreatorHelper;
import pl.ks.profiling.gui.commons.TableWithLinks;

@RequiredArgsConstructor
public class SelfTimeCreator implements PageCreator {
    private static final BigDecimal PERCENT_MULTIPLICAND = new BigDecimal(100);
    private static final BigDecimal THRESHOLD = new BigDecimal("0.0001");

    private final CollapsedStackInfo collapsedStackInfo;
    private final String appName;
    private final String collapsedStackFileName;

    @Override
    public Page create() {
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        BigDecimal totalCount = new BigDecimal(collapsedStackInfo.getTotalCount());

        List<MethodInfo> methodsToList = collapsedStackInfo.getMethods().stream()
                .filter(methodEntry -> overThreshold(totalCount, methodEntry))
                .sorted(Comparator.comparingLong(MethodInfo::getSelfSamples).reversed())
                .collect(Collectors.toList());
        return Page.builder()
                .fullName("Method self time")
                .menuName("Method self time")
                .icon(Page.Icon.STATS)
                .pageContents(List.of(
                        TableWithLinks.builder()
                                .header(List.of("Method", "Self  time %", "No of samples", "Callee flame graph", "Callers flame graph"))
                                .filteredColumn(0)
                                .table(methodsToList.stream()
                                        .map(methodEntry -> List.of(
                                                of(methodEntry.getName()),
                                                of(getPercent(methodEntry, totalCount, decimalFormat)),
                                                of(String.valueOf(methodEntry.getSelfSamples())),
                                                of(LinkUtils.getFromMethodHref(collapsedStackFileName, methodEntry.getName()), "Callee"),
                                                of(LinkUtils.getToMethodHref(collapsedStackFileName, methodEntry.getName()), "Callers")
                                        ))
                                        .collect(Collectors.toList())
                                )
                                .build()
                ))
                .build();
    }

    private boolean overThreshold(BigDecimal totalCount, MethodInfo methodInfo) {
        return new BigDecimal(methodInfo.getSelfSamples()).divide(totalCount, 3, RoundingMode.HALF_EVEN).compareTo(THRESHOLD) > 0;
    }

    private static String getPercent(MethodInfo methodInfo, BigDecimal totalCount, DecimalFormat decimalFormat) {
        BigDecimal percent = new BigDecimal(methodInfo.getSelfSamples()).multiply(PERCENT_MULTIPLICAND).divide(totalCount, 2, RoundingMode.HALF_EVEN);
        return PageCreatorHelper.numToString(percent, decimalFormat);
    }
}
