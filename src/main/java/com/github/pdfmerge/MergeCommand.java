package com.github.pdfmerge;

import io.quarkus.logging.Log;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * 输入参数：--input=/location/f1.pdf --input=/location/f2.pdf
 */
@Command(mixinStandardHelpOptions = true)
public class MergeCommand implements Callable<Integer> {

    /**
     * 根据参数设置，必须输入
     */
    @CommandLine.Option(names = {"-i", "--input"}, required = true)
    private List<String> inputFileNames;

    @CommandLine.Option(names = {"-o", "--output"}, defaultValue = "out.pdf")
    private String outputFileName;

    @Override
    public Integer call() {
        // 参数判断
        if (inputFileNames.size() < 2) {
            Log.error("At least two input files must be supplied");
            return 1;
        }
        Log.debugv("Will attempt to merge {0} into {1}", String.join(",", inputFileNames), outputFileName);

        // 逻辑
        var utility = new PDFMergerUtility();
        var paths = inputFileNames.stream().map(Paths::get).toList();
        try {
            for (Path p : paths) {
                utility.addSource(p.toFile());
            }
            utility.setDestinationFileName(outputFileName);
            utility.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
        } catch (IOException e) {
            Log.error(e.getMessage());
            return 1;
        }

        Log.info("Merged PDF is available at " + Paths.get(outputFileName).toAbsolutePath());
        return 0;
    }
}
