package pl.ks.collapsed.stack.viewer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.ks.collapsed.stack.viewer.creator.FlameGraphsCreator;
import pl.ks.collapsed.stack.viewer.creator.SelfTimeCreator;
import pl.ks.collapsed.stack.viewer.creator.TotalTimeCreator;
import pl.ks.profiling.gui.commons.Page;
import pl.ks.profiling.gui.commons.PageCreator;
import pl.ks.profiling.io.TempFileUtils;

@Slf4j
@RequiredArgsConstructor
class CollapsedStackPageCreator {
    List<Page> generatePages(String collapsedStackFile, String originalFileName) {
        CollapsedStackInfo collapsedStackInfo = parse(collapsedStackFile);

        List<PageCreator> pageCreators = List.of(
                new FlameGraphsCreator(collapsedStackFile),
                new TotalTimeCreator(collapsedStackInfo, originalFileName, collapsedStackFile),
                new SelfTimeCreator(collapsedStackInfo, originalFileName, collapsedStackFile)
        );

        return pageCreators.stream()
                .map(PageCreator::create)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private CollapsedStackInfo parse(String fileName) {
        Map<String, MethodInfo> methodInfos = new HashMap<>();
        long totalCount = 0;

        try (InputStream inputStream = new FileInputStream(TempFileUtils.getFilePath(fileName));
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
             ) {
            while (reader.ready()) {
                String line = reader.readLine();
                int delimiterChar = line.lastIndexOf(" ");
                String stack = line.substring(0, delimiterChar);
                String[] splittedStack = stack.split(";");
                String num = line.substring(delimiterChar + 1);
                long currentCount = Long.parseLong(num);
                totalCount += currentCount;

                Set<String> processed = new HashSet<>();
                for (int i = 0; i < splittedStack.length; i++) {
                    String methodOnStack = splittedStack[i];
                    MethodInfo methodInfo = methodInfos.computeIfAbsent(methodOnStack, name -> MethodInfo.builder().name(name).build());
                    if (processed.add(methodOnStack)) {
                        methodInfo.addTotalSamples(currentCount);
                    }
                    if (i == splittedStack.length - 1) {
                        methodInfo.addSelfSamples(currentCount);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Cannot create no thread collapsed stack", e);
            return null;
        }
        return CollapsedStackInfo.builder()
                .methods(methodInfos.values())
                .totalCount(totalCount)
                .build();
    }
}
