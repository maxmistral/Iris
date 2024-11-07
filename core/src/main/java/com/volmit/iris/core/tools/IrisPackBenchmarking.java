package com.volmit.iris.core.tools;


import com.volmit.iris.Iris;
import com.volmit.iris.core.pregenerator.PregenTask;
import com.volmit.iris.engine.framework.Engine;
import com.volmit.iris.engine.object.IrisDimension;
import com.volmit.iris.util.collection.KList;
import com.volmit.iris.util.collection.KMap;
import com.volmit.iris.util.exceptions.IrisException;
import com.volmit.iris.util.format.Form;
import com.volmit.iris.util.math.Position2;

import com.volmit.iris.util.scheduling.J;
import com.volmit.iris.util.scheduling.PrecisionStopwatch;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.*;


public class IrisPackBenchmarking {
    @Getter
    public static IrisPackBenchmarking instance;
     public static boolean benchmarkInProgress = false;
     private IrisDimension IrisDimension;
     private int radius;
     private boolean finished = false;
    PrecisionStopwatch stopwatch;

    public IrisPackBenchmarking(IrisDimension dimension, int r) {
        instance = this;
        this.IrisDimension = dimension;
        this.radius = r;
        runBenchmark();
    }

    private void runBenchmark() {
        this.stopwatch = new PrecisionStopwatch();
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.submit(() -> {
            Iris.info("Setting up benchmark environment ");
            benchmarkInProgress = true;
            File file = new File("benchmark");
            if (file.exists()) {
                deleteDirectory(file.toPath());
            }
            createBenchmark();
            while (!IrisToolbelt.isIrisWorld(Bukkit.getWorld("benchmark"))) {
                J.sleep(1000);
                Iris.debug("Iris PackBenchmark: Waiting...");
            }
            Iris.info("Starting Benchmark!");
            stopwatch.begin();
            startBenchmark();
        });

    }

    public boolean getBenchmarkInProgress() {
        return benchmarkInProgress;
    }

    public void finishedBenchmark(KList<Integer> cps) {
        try {
            String time = Form.duration(stopwatch.getMillis());
            Engine engine = IrisToolbelt.access(Bukkit.getWorld("benchmark")).getEngine();
            Iris.info("-----------------");
            Iris.info("Results:");
            Iris.info("- Total time: " + time);
            Iris.info("- Average CPS: " + calculateAverage(cps));
            Iris.info("  - Median CPS: " + calculateMedian(cps));
            Iris.info("  - Highest CPS: " + findHighest(cps));
            Iris.info("  - Lowest CPS: " + findLowest(cps));
            Iris.info("-----------------");
            Iris.info("Creating a report..");
            File profilers = new File("plugins" + File.separator + "Iris" + File.separator + "packbenchmarks");
            profilers.mkdir();

            File results = new File("plugins " + File.separator + "Iris", IrisDimension.getName() + LocalDateTime.now(Clock.systemDefaultZone()) + ".txt");
            results.createNewFile();
            KMap<String, Double> metrics = engine.getMetrics().pull();
            try (FileWriter writer = new FileWriter(results)) {
                writer.write("-----------------\n");
                writer.write("Results:\n");
                writer.write("Dimension: " + IrisDimension.getName() + "\n");
                writer.write("- Date of Benchmark: " +  LocalDateTime.now(Clock.systemDefaultZone()) + "\n");
                writer.write("\n");
                writer.write("Metrics");
                for (String m : metrics.k()) {
                    double i = metrics.get(m);
                    writer.write("- " + m + ": " + i);
                }
                writer.write("- " + metrics);
                writer.write("Benchmark: " +  LocalDateTime.now(Clock.systemDefaultZone()) + "\n");
                writer.write("- Total time: " + time + "\n");
                writer.write("- Average CPS: " + calculateAverage(cps) + "\n");
                writer.write("  - Median CPS: " + calculateMedian(cps) + "\n");
                writer.write("  - Highest CPS: " + findHighest(cps) + "\n");
                writer.write("  - Lowest CPS: " + findLowest(cps) + "\n");
                writer.write("-----------------\n");
                Iris.info("Finished generating a report!");
            } catch (IOException e) {
                Iris.error("An error occurred writing to the file.");
                e.printStackTrace();
            }

            Bukkit.getServer().unloadWorld("benchmark", true);
            stopwatch.end();
        } catch (Exception e) {
            Iris.error("Something has gone wrong!");
            e.printStackTrace();
        }
    }
     private void createBenchmark(){
        try {
            IrisToolbelt.createWorld()
                    .dimension(IrisDimension.getName())
                    .name("benchmark")
                    .seed(1337)
                    .studio(false)
                    .benchmark(true)
                    .create();
        } catch (IrisException e) {
            throw new RuntimeException(e);
        }
    }

     private void startBenchmark(){
        int x = 0;
        int z = 0;
            IrisToolbelt.pregenerate(PregenTask
                    .builder()
                    .gui(false)
                    .center(new Position2(x, z))
                    .width(5)
                    .height(5)
                    .build(), Bukkit.getWorld("benchmark")
            );
    }

    private double calculateAverage(KList<Integer> list) {
        double sum = 0;
        for (int num : list) {
            sum += num;
        }
        return sum / list.size();
    }

    private double calculateMedian(KList<Integer> list) {
        Collections.sort(list);
        int middle = list.size() / 2;

        if (list.size() % 2 == 1) {
            return list.get(middle);
        } else {
            return (list.get(middle - 1) + list.get(middle)) / 2.0;
        }
    }

    private int findLowest(KList<Integer> list) {
        return Collections.min(list);
    }

    private int findHighest(KList<Integer> list) {
        return Collections.max(list);
    }

    private boolean deleteDirectory(Path dir) {
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}