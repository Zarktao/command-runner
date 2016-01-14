package org.melancholyworks.runner;

import org.melancholyworks.runner.task.AbstractTaskRunner;
import org.melancholyworks.runner.task.Task;

import java.lang.reflect.Constructor;
import java.util.LinkedHashSet;

/**
 * @author ZarkTao
 */
public class RunnerUtils {
    protected static LinkedHashSet<Class<? extends AbstractTaskRunner>> runnerList = new LinkedHashSet<>();

    public static Class getRunnerClass(String command) {
        if (command != null && !command.equals("")) {
            for (Class c : runnerList) {
                try {
                    Constructor constructor = c.getConstructor(Task.class);
                    AbstractTaskRunner runner = (AbstractTaskRunner) constructor.newInstance(new Task(null, null, command));
                    if (runner.checkCommand())
                        return c;
                } catch (Exception e) {
                    throw new RuntimeException("Runner error. Do not change the constructor of AbstractTaskRunner.");
                }
            }
        } else {
            throw new IllegalArgumentException("Command can not be null");
        }
        return null;
    }

    public static void addRunner(Class<? extends AbstractTaskRunner> runnerClass) {
        runnerList.add(runnerClass);
    }

    public static void setPersistenceHelper(PersistenceHelper helper) {
        RunnersManager.setPersistenceHelper(helper);
    }
}
