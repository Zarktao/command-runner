package org.melancholyworks.runner.example;

import org.melancholyworks.runner.task.AbstractTaskRunner;
import org.melancholyworks.runner.task.Task;

/**
 * @author ZarkTao
 */
public class SleepRunner extends AbstractTaskRunner {
    boolean isSuccess = false;
    boolean isStop = false;

    public SleepRunner(Task task) {
        super(task);
    }

    @Override
    public void setup() {

    }

    @Override
    public int run() {
        for (int i = 0; i < 5; i++) {
            if (isStop) {
                appendLogLine("killed");
                isSuccess = false;
                task.setResult("Dead");
                return -1;
            }
            try {
                Thread.sleep(1000L);
            } catch (Exception e) {
                appendLogLine("Interuptted by {0}", e);
                task.setResult("Dead");
                return -1;
            }
            appendLogLine("Sleep at {0}", i);
        }
        task.setResult("Awake!");
        isSuccess = true;
        return 0;
    }

    @Override
    public void teardown() {

    }

    @Override
    public boolean isSuccess() {
        return isSuccess;
    }

    @Override
    public void kill() {
        isStop = true;
    }

    @Override
    public boolean checkCommand() {
        return task.getCommand().equalsIgnoreCase("sleep");
    }
}
