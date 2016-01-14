package org.melancholyworks.runner;

import org.melancholyworks.runner.job.Job;
import org.melancholyworks.runner.job.JobRunner;
import org.melancholyworks.runner.task.AbstractTaskRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author ZarkTao
 */
public class RunnersManager {
    private static ExecutorService _POOL = Executors.newFixedThreadPool(1000);
    private static Map<String, Future<Job>> futureMap = new HashMap<>();
    private static Map<String, Job> instanceMap = new HashMap<>();
    private static PersistenceHelper persistenceHelper = null;
    private static PersistenceListener persistenceListener = new PersistenceListener();

    public static String submit(Job instance) {
        JobRunner runner = instance.getRunner();
        runner.setFinishListener(persistenceListener);
        Future<Job> future = _POOL.submit(runner);
        futureMap.put(instance.getJobID(), future);
        instanceMap.put(instance.getJobID(), instance);
        return instance.getJobID();
    }

    public static void setPersistenceHelper(PersistenceHelper helper) {
        persistenceHelper = helper;
    }

    public static void kill(Job instance) {
        if (futureMap.containsKey(instance.getJobID())) {
            futureMap.get(instance.getJobID()).cancel(true);
        }
    }

    private static Job getInstance(String instanceID) {
        Job instance = null;
        if (instanceMap.containsKey(instanceID)) {
            try {
                instance = instanceMap.get(instanceID);
            } catch (Exception ignored) {
            }
        } else if (persistenceHelper != null) {
            String instanceStr = persistenceHelper.getInstance(instanceID);
            instance = Job.fromString(instanceStr);
        }
        if (instance != null) {
            return instance;
        } else {
            throw new IllegalArgumentException("InstanceID not exists.");
        }
    }

    private static Job getMemoryInstance(String instanceID) {
        Job instance = null;
        if (futureMap.containsKey(instanceID)) {
            try {
                instance = instanceMap.get(instanceID);
            } catch (Exception ignored) {
            }
        }
        if (instance != null) {
            return instance;
        } else {
            throw new IllegalArgumentException("InstanceID not exists.");
        }
    }

    private static void idCheck(String instanceID) {
        if (instanceID != null && !instanceID.equals("")) {
            if (instanceMap.containsKey(instanceID))
                throw new IllegalArgumentException("InstanceID duplicated.");
            if (persistenceHelper != null) {
                List<String> idList = persistenceHelper.getInstanceIDList();
                for (String id : idList) {
                    if (instanceID.equals(id)) {
                        throw new IllegalArgumentException("InstanceID duplicated.");
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Context must has an instanceID");
        }
    }

    private static class PersistenceListener implements FinishListener {
        @Override
        public void onFinish(String instanceId) throws Exception {
            persistence(instanceId);
        }
    }

    private static void persistence(String instanceID) {
        if (persistenceHelper != null) {
            persistenceHelper.save(getMemoryInstance(instanceID).toString());
            futureMap.remove(instanceID);
            instanceMap.remove(instanceID);
        }
    }

    public static void addRunner(Class<? extends AbstractTaskRunner> runnerClass) {
        RunnerUtils.addRunner(runnerClass);
    }

    public static void shutdown() {
        _POOL.shutdownNow();
    }
}
