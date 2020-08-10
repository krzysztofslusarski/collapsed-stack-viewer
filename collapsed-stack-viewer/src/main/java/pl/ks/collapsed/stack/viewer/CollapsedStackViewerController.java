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
package pl.ks.collapsed.stack.viewer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import pl.ks.flame.graph.ExternalFlameGraphExecutor;
import pl.ks.profiling.io.StorageUtils;
import pl.ks.profiling.io.TempFileUtils;
import pl.ks.profiling.web.commons.WelcomePage;

@Controller
@RequiredArgsConstructor
class CollapsedStackViewerController {
    private final CollapsedStackPageCreator collapsedStackPageCreator;
    private final ExternalFlameGraphExecutor externalFlameGraphExecutor;

    @GetMapping("/")
    String defualt() {
        return "upload";
    }

    @GetMapping("/upload")
    String upload() {
        return "upload";
    }

    @PostMapping("/upload")
    String upload(Model model,
                  @RequestParam("file") MultipartFile file,
                  @RequestParam("totalTimeThreshold") BigDecimal totalTimeThreshold,
                  @RequestParam("selfTimeThreshold") BigDecimal selfTimeThreshold) throws Exception {
        String originalFilename = file.getOriginalFilename();
        InputStream inputStream = StorageUtils.createCopy(TempFileUtils.TEMP_DIR, originalFilename, file.getInputStream());
        String uncompressedFileName = "collapsed-stack-" + UUID.randomUUID().toString() + ".log";
        IOUtils.copy(inputStream, new FileOutputStream(TempFileUtils.TEMP_DIR + uncompressedFileName));
        model.addAttribute("welcomePage", WelcomePage.builder()
                .pages(collapsedStackPageCreator.generatePages(uncompressedFileName, totalTimeThreshold, selfTimeThreshold))
                .build());
        return "welcome";
    }

    @GetMapping(value = "/image/{name}", produces = "image/svg+xml")
    @SneakyThrows
    @ResponseBody
    byte[] getImage(@PathVariable("name") String name) {
        return IOUtils.toByteArray(new FileInputStream(TempFileUtils.getFilePath(name)));
    }


    @GetMapping(value = "/flame-graph", produces = "image/svg+xml")
    @ResponseBody
    byte[] getFlameGraph(@RequestParam("collapsed") String collapsed) throws Exception {
        String collapsedFilePath = TempFileUtils.getFilePath(collapsed);
        String outputSvgFilePath = TempFileUtils.getFilePath(collapsed + ".svg");
        externalFlameGraphExecutor.generateFlameGraph(collapsedFilePath, outputSvgFilePath, "Flame graph", false, false);
        byte[] response = null;
        try (InputStream inputStream = new FileInputStream(outputSvgFilePath);) {
            response = IOUtils.toByteArray(inputStream);
        }
        Files.delete(Paths.get(outputSvgFilePath));
        return response;
    }

    @GetMapping(value = "/flame-graph-no-thread", produces = "image/svg+xml")
    @ResponseBody
    byte[] getFlameGraphNoThread(@RequestParam("collapsed") String collapsed) throws Exception {
        String collapsedFilepath = TempFileUtils.getFilePath(collapsed);
        String outputSvgFilePath = TempFileUtils.getFilePath(collapsed + ".no-thread.svg");
        String outputCollapsedFilePath = TempFileUtils.getFilePath(collapsed + "-no-thread.txt");

        try (InputStream inputStream = new FileInputStream(collapsedFilepath);
             OutputStream outputStream = new FileOutputStream(outputCollapsedFilePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));) {
            while (reader.ready()) {
                String line = reader.readLine();
                writer.write(line.substring(line.indexOf(";") + 1));
                writer.newLine();
            }
        }

        externalFlameGraphExecutor.generateFlameGraph(outputCollapsedFilePath, outputSvgFilePath, "Flame graph - no thread division", false, false);
        byte[] response = null;
        try (InputStream inputStream = new FileInputStream(outputSvgFilePath);) {
            response = IOUtils.toByteArray(inputStream);
        }
        Files.delete(Paths.get(outputSvgFilePath));
        Files.delete(Paths.get(outputCollapsedFilePath));
        return response;
    }

    @GetMapping(value = "/flame-graph-hotspot-limited", produces = "image/svg+xml")
    @ResponseBody
    byte[] getFlameGraphHotspotLimited(@RequestParam("limit") int limit, @RequestParam("collapsed") String collapsed) throws Exception {
        UUID newUuid = UUID.randomUUID();
        String outputCollapsedFilePath = TempFileUtils.getFilePath(newUuid + "-method.txt");
        String outputSvgFilePath = TempFileUtils.getFilePath(newUuid + "-method.svg");
        try (InputStream inputStream = new FileInputStream(TempFileUtils.getFilePath(collapsed));
             OutputStream fromOutStream = new FileOutputStream(outputCollapsedFilePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
             BufferedWriter toWriter = new BufferedWriter(new OutputStreamWriter(fromOutStream));
        ) {
            while (reader.ready()) {
                String line = reader.readLine();
                int delimiterChar = line.lastIndexOf(" ");
                String stack = line.substring(0, delimiterChar);
                String num = line.substring(delimiterChar + 1);

                String[] splittedStack = stack.split(";");
                for (int i = Math.max(0, splittedStack.length - limit); i < splittedStack.length; i++) {
                    String frame = splittedStack[i];
                    toWriter.write(frame);
                    if (i != splittedStack.length -1) {
                        toWriter.write(";");
                    }
                }

                toWriter.write(" ");
                toWriter.write(num);
                toWriter.newLine();
            }
        }

        externalFlameGraphExecutor.generateFlameGraph(outputCollapsedFilePath, outputSvgFilePath, "Flame graph - hotspot", true, true);
        byte[] response = null;
        try (InputStream inputStream = new FileInputStream(outputSvgFilePath);) {
            response = IOUtils.toByteArray(inputStream);
        }
        Files.delete(Paths.get(outputSvgFilePath));
        Files.delete(Paths.get(outputCollapsedFilePath));
        return response;
    }

    @GetMapping(value = "/flame-graph-hotspot", produces = "image/svg+xml")
    @ResponseBody
    byte[] getFlameGraphHotspot(@RequestParam("collapsed") String collapsed) throws Exception {
        String collapsedFileName = TempFileUtils.getFilePath(collapsed);
        String outputSvgFilePath = TempFileUtils.getFilePath(collapsed + ".hotspot.svg");
        externalFlameGraphExecutor.generateFlameGraph(collapsedFileName, outputSvgFilePath, "Flame graph - hotspot", true, true);
        byte[] response = null;
        try (InputStream inputStream = new FileInputStream(outputSvgFilePath);) {
            response = IOUtils.toByteArray(inputStream);
        }
        Files.delete(Paths.get(outputSvgFilePath));
        return response;
    }


    @GetMapping(value = "/from-method", produces = "image/svg+xml")
    @ResponseBody
    byte[] fromMethod(@RequestParam("collapsed") String collapsed, @RequestParam("method") String method) throws Exception {
        UUID newUuid = UUID.randomUUID();
        String outputCollapsedFilePath = TempFileUtils.getFilePath(newUuid + "-method.txt");
        String outputSvgFilePath = TempFileUtils.getFilePath(newUuid + "-method.svg");
        try (InputStream inputStream = new FileInputStream(TempFileUtils.getFilePath(collapsed));
             OutputStream fromOutStream = new FileOutputStream(outputCollapsedFilePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
             BufferedWriter fromWriter = new BufferedWriter(new OutputStreamWriter(fromOutStream));
        ) {
            while (reader.ready()) {
                String line = reader.readLine();
                int delimiterChar = line.lastIndexOf(" ");
                String stack = line.substring(0, delimiterChar);
                int pos = stack.indexOf(method);
                if (pos < 0) {
                    continue;
                }
                String fromStack = stack.substring(pos);
                String num = line.substring(delimiterChar + 1);
                fromWriter.write(fromStack);
                fromWriter.write(" ");
                fromWriter.write(num);
                fromWriter.newLine();
            }
        }
        externalFlameGraphExecutor.generateFlameGraph(outputCollapsedFilePath, outputSvgFilePath, "Callee", false, false);
        byte[] response = null;
        try (InputStream inputStream = new FileInputStream(outputSvgFilePath);) {
            response = IOUtils.toByteArray(inputStream);
        }
        Files.delete(Paths.get(outputSvgFilePath));
        Files.delete(Paths.get(outputCollapsedFilePath));
        return response;
    }

    @GetMapping(value = "/to-method", produces = "image/svg+xml")
    @ResponseBody
    byte[] toMethod(@RequestParam("collapsed") String collapsed, @RequestParam("method") String method) throws Exception {
        UUID newUuid = UUID.randomUUID();
        String outputCollapsedFilePath = TempFileUtils.getFilePath(newUuid + "-method.txt");
        String outputSvgFilePath = TempFileUtils.getFilePath(newUuid + "-method.svg");
        try (InputStream inputStream = new FileInputStream(TempFileUtils.getFilePath(collapsed));
             OutputStream fromOutStream = new FileOutputStream(outputCollapsedFilePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
             BufferedWriter toWriter = new BufferedWriter(new OutputStreamWriter(fromOutStream));
        ) {
            while (reader.ready()) {
                String line = reader.readLine();
                int delimiterChar = line.lastIndexOf(" ");
                String stack = line.substring(0, delimiterChar);
                int pos = stack.indexOf(method);
                if (pos < 0) {
                    continue;
                }
                String toStack = stack.substring(0, pos + method.length());
                String num = line.substring(delimiterChar + 1);

                String[] splittedStack = toStack.split(";");
                for (int i = splittedStack.length; i > 0; i--) {
                    String frame = splittedStack[i - 1];
                    toWriter.write(frame);
                    if (i != 1) {
                        toWriter.write(";");
                    }
                }
                toWriter.write(" ");
                toWriter.write(num);
                toWriter.newLine();
            }
        }
        externalFlameGraphExecutor.generateFlameGraph(outputCollapsedFilePath, outputSvgFilePath, "Callers", true, false);
        byte[] response = null;
        try (InputStream inputStream = new FileInputStream(outputSvgFilePath);) {
            response = IOUtils.toByteArray(inputStream);
        }
        Files.delete(Paths.get(outputCollapsedFilePath));
        Files.delete(Paths.get(outputSvgFilePath));
        return response;
    }
}
