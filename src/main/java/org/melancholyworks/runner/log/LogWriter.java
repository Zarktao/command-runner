package org.melancholyworks.runner.log;

import org.melancholyworks.runner.job.Job;

import java.io.*;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author ZarkTao
 */
public class LogWriter extends PrintWriter {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final OutputStream DEFAULT_OUTPUT = System.out;
    private static final String NO_VALUE = "-";
    private Job instance;
    private ByteArrayOutputStream streamData;
    private PrintWriter streamDataWriter;

    public LogWriter(Job instance) {
        super(instance.getLogOutputStream() == null ? DEFAULT_OUTPUT : instance.getLogOutputStream(), true);
        this.instance = instance;
        streamData = new ByteArrayOutputStream();
        streamDataWriter = new PrintWriter(streamData);
    }

    private String getPrefix() {
        StringBuilder builder = new StringBuilder();
        builder.append(DATE_FORMAT.format(new Date())).append(" ");
        builder.append("USER[").append(instance.getUser() == null ? NO_VALUE : instance.getUser()).append("] ");
        builder.append("NAMESPACE[").append(instance.getNamespace() == null ? NO_VALUE : instance.getNamespace()).append("] ");
        builder.append("INSTANCE_ID[").append(instance.getJobID() == null ? NO_VALUE : instance.getJobID()).append("] ");
        //builder.append("PRIORITY[").append(job.getPriority()).append("] ");
        return builder.toString();
    }

    public void appendLine(String line, Object... objects) {
        String toPrint = getPrefix() + MessageFormat.format(line, objects) + "\n";
        append(toPrint);
        streamDataWriter.append(toPrint);
        flush();
    }

    public void finish() {
        streamDataWriter.flush();
        instance.setLog(streamData.toString());
        streamDataWriter.close();
        try {
            streamData.close();
        } catch (IOException ignore) {
        }
    }
}
