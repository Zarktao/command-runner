package org.melancholyworks.runner.job;

import org.melancholyworks.runner.RunnerUtils;
import org.melancholyworks.runner.FinishListener;
import org.melancholyworks.runner.Status;
import org.melancholyworks.runner.log.LogWriter;
import org.melancholyworks.runner.task.AbstractTaskRunner;
import org.melancholyworks.runner.task.Task;

import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.Date;
import java.util.concurrent.Callable;

/**
 * @author ZarkTao
 */
public class JobRunner implements Callable<Job> {
    private LogWriter logWriter;
    private Job instance = null;
    private FinishListener finishListener = null;

    public JobRunner(Job instance) {
        this.instance = instance;
        this.instance.setStatus(Status.WAITING);
        this.logWriter = new LogWriter(instance);
    }

    public void setFinishListener(FinishListener listener) {
        this.finishListener = listener;
    }

    @Override
    public Job call() throws Exception {
        appendLogLine("Commands start to run.");
        instance.setStatus(Status.RUNNING);
        instance.setStartTime(new Date());
        try {
            for (Task task : instance.getTasks()) {
                appendLogLine("Command({0}) start. Total [{1}].", instance.getPoint() + 1, instance.getCount());
                Class runnerClass = RunnerUtils.getRunnerClass(task.getCommand());
                Constructor<AbstractTaskRunner> constructor = runnerClass.getConstructor(Task.class);
                AbstractTaskRunner runner = constructor.newInstance(task);
                runner.execute();
                Status status = Status.fromString(task.getStatus());
                if (status == Status.FAILED) {
                    appendLogLine("Commands failed at [{0}].", instance.getPoint() + 1);
                    instance.setStatus(Status.FAILED);
                    break;
                } else if (status == Status.KILLED) {
                    appendLogLine("Commands are killed at [{0}].", instance.getPoint() + 1);
                    instance.setStatus(Status.KILLED);
                    break;
                }
                appendLogLine("Command({0}) finished successfully.", instance.getPoint() + 1);
                instance.setPoint(instance.getPoint() + 1);
            }
        } catch (Exception e) {
            appendLogLine("Commands failed at [{0}].", instance.getPoint() + 1);
            instance.setStatus(Status.FAILED);
        }
        if (instance.getStatus().equals(Status.RUNNING.toString())) {
            instance.setStatus(Status.SUCCESS);
            logWriter.finish();
        }
        if (finishListener != null)
            finishListener.onFinish(instance.getJobID());
        instance.setEndTime(new Date());
        return instance;
    }

    protected void appendLogLine(String line, Object... objects) {
        logWriter.appendLine(MessageFormat.format(line, objects));
    }

    public Job getInstance() {
        return instance;
    }

    public LogWriter getLogWriter() {
        return logWriter;
    }
}
