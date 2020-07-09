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
package pl.ks.flame.graph;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.IOUtils;

@RequiredArgsConstructor
public class ExternalFlameGraphExecutor {
    private final String flameGraphPath;

    public void generateFlameGraph(String inputFile, String outputFile, String title, boolean reversed, boolean hotspot)  {
        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(new File(flameGraphPath));
        List<String> command = new ArrayList<>();
        command.add("./flamegraph.pl");
        if (hotspot) {
            command.add("--reverse");
        }
        if (reversed) {
            command.add("--inverted");
        }
        command.addAll(List.of("--fontsize", "14", "--width", "1900", "--hash", "--color", "java", "--title", title, inputFile));

        builder.command(command);
        try (InputStream inputStream = builder.start().getInputStream();
             OutputStream outputStream = new FileOutputStream(outputFile)) {
            IOUtils.copy(inputStream, outputStream);
        } catch (Exception e) {
            throw new RuntimeException("Cannot generate flame graph", e);
        }
    }
}
