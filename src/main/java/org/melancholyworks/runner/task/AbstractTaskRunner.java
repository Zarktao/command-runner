package org.melancholyworks.runner.task;

import org.melancholyworks.runner.Status;
import org.melancholyworks.runner.log.LogWriter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;

/**
 * @author ZarkTao
 */
public abstract class AbstractTaskRunner {
    protected Task task = null;
    private LogWriter writer = null;

    public AbstractTaskRunner(Task task) {
        this.task = task;
        if (task != null && task.getInstance() != null)
            writer = task.getInstance().getRunner().getLogWriter();
    }

    public void execute() {
        int result = -1;
        task.setStatus(Status.WAITING);
        appendLogLine("Status change to waiting.");
        try {
            setup();
        } catch (Exception e) {
            appendLogLine("Error occurred when setup command. Error: {0}", e.getMessage());
            task.setStatus(Status.FAILED);
            StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            task.setResult(writer.toString());
            return;
        }
        task.setStatus(Status.RUNNING);
        task.setStartTime(new Date());
        appendLogLine("Command start.");
        try {
            result = run();
            if (isSuccess() && result != -1) {
                task.setStatus(Status.SUCCESS);
                appendLogLine("Command finished successfully.");
            } else {
                task.setStatus(Status.FAILED);
                appendLogLine("Command finished failed.");
            }
        } catch (Exception e) {
            appendLogLine("Error occurred when running command. Error: {0}.", e.getMessage());
            task.setStatus(Status.FAILED);
            task.setResult(e.getMessage());
            task.setEndTime(new Date());
            return;
        }
        try {
            teardown();
        } catch (Exception e) {
            appendLogLine("Error occurred when teardown command.");
        }
        task.setEndTime(new Date());
    }

    /**
     * Things should be done before command running.
     */
    public abstract void setup();

    /**
     * Define how to synchronized run this command.
     * Lines of log can be added in this method.
     * If there is a result, put it into runner context.
     *
     * @return 0-success, 1-failed
     */
    public abstract int run();

    /**
     * Things should be done after command running.
     */
    public abstract void teardown();

    /**
     * Is this command finished successfully.
     */
    public abstract boolean isSuccess();

    protected void appendLogLine(String line, Object... objects) {
        if (writer != null) {
            writer.appendLine(getPrefix() + MessageFormat.format(line, objects));
        }
    }

    protected void appendEngineLog(String logText) {
        if (writer != null) {
            writer.appendLine(getPrefix() + "\n" + logText);
        }
    }

    private String getPrefix() {
        return "TASK_ID[" + task.getTaskID() + "] COMMAND[" + task.getCommand() + "] ";
    }

    /**
     * Check if the command in the context compatible with this runner.
     *
     * @return true if this runner can execute this command.
     */
    public abstract boolean checkCommand();
}
