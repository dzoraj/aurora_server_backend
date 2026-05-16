package rs.igapp.aurora.agent.runner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import rs.igapp.aurora.agent.config.AgentMode;
import rs.igapp.aurora.agent.config.AgentProperties;
import rs.igapp.aurora.agent.ingest.IngestClient;
import rs.igapp.aurora.agent.tail.AppendOnlyFileTailer;

@Component
@Order(0)
public class AgentRunner implements ApplicationRunner, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(AgentRunner.class);

    private final AgentProperties properties;
    private final IngestClient ingestClient;
    private final ConfigurableApplicationContext applicationContext;

    private ScheduledExecutorService fileScheduler;
    private AppendOnlyFileTailer tailer;
    private Thread stdinThread;

    public AgentRunner(
            AgentProperties properties,
            IngestClient ingestClient,
            ConfigurableApplicationContext applicationContext) {
        this.properties = properties;
        this.ingestClient = ingestClient;
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        switch (properties.getMode()) {
            case ONCE -> runOnce();
            case FILE -> runFile();
            case STDIN -> runStdin();
        }
    }

    private void runOnce() {
        String msg = "Aurora agent smoke test at " + Instant.now();
        log.info("Mode=ONCE: shipping single line");
        boolean ok = ingestClient.shipLine(msg);
        if (!ok) {
            log.error("Smoke ingest failed");
        }
        int code = SpringApplication.exit(applicationContext, () -> ok ? 0 : 1);
        System.exit(code);
    }

    private void runFile() throws IOException {
        if (properties.getFilePath() == null || !StringUtils.hasText(properties.getFilePath().toString())) {
            throw new IllegalStateException("aurora.agent.file-path is required when mode=FILE");
        }
        var path = properties.getFilePath().toAbsolutePath().normalize();
        var parent = path.getParent();
        if (parent != null) {
            java.nio.file.Files.createDirectories(parent);
        }
        if (!java.nio.file.Files.exists(path)) {
            java.nio.file.Files.createFile(path);
        }
        this.tailer = new AppendOnlyFileTailer(path);
        log.info("Mode=FILE: tailing {} every {} ms", path, properties.getPollIntervalMs());
        this.fileScheduler =
                Executors.newSingleThreadScheduledExecutor(
                        r -> {
                            Thread t = new Thread(r, "aurora-agent-file");
                            t.setDaemon(false);
                            return t;
                        });
        fileScheduler.scheduleWithFixedDelay(
                this::tickFile, 0, properties.getPollIntervalMs(), TimeUnit.MILLISECONDS);
    }

    private void tickFile() {
        try {
            for (String line : tailer.pollNewLines()) {
                if (!ingestClient.shipLine(line)) {
                    log.warn("Stopping file drain after failed ingest; will retry on next poll");
                    break;
                }
            }
        } catch (IOException ex) {
            log.error("Failed to read log file", ex);
        } catch (RuntimeException ex) {
            log.error("Unexpected error while tailing file", ex);
        }
    }

    private void runStdin() {
        log.info("Mode=STDIN: reading lines from stdin until EOF");
        this.stdinThread =
                new Thread(
                        () -> {
                            try (BufferedReader reader =
                                    new BufferedReader(
                                            new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    if (!line.isBlank() && !ingestClient.shipLine(line)) {
                                        log.warn("Stopping stdin after failed ingest");
                                        break;
                                    }
                                }
                            } catch (IOException ex) {
                                log.error("stdin read failed", ex);
                            }
                        },
                        "aurora-agent-stdin");
        stdinThread.setDaemon(false);
        stdinThread.start();
    }

    @Override
    public void destroy() {
        if (fileScheduler != null) {
            fileScheduler.shutdownNow();
        }
    }
}
