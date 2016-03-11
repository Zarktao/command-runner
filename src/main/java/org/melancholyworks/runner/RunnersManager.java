package org.melancholyworks.runner;

import org.melancholyworks.runner.job.Job;
import org.melancholyworks.runner.job.JobRunner;
import org.melancholyworks.runner.task.AbstractTaskRunner;
import org.melancholyworks.runner.task.Task;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ZarkTao
 */
public class RunnersManager {
    private LinkedHashSet<Class<? extends AbstractTaskRunner>> runnerList;
    private static final String DEFAULT_RUNNER_NAME = "CommandRunner";
    private static final int DEFAULT_POOL_SIZE = 1000;

    private String runnerName;
    private int poolSize;
    private boolean useDaemonThread = false;

    private ExecutorService _POOL;
    private Map<String, Future<Job>> futureMap = new HashMap<>();
    private Map<String, Job> instanceMap = new HashMap<>();
    private PersistenceHelper persistenceHelper = null;
    private PersistenceListener persistenceListener = new PersistenceListener();

    public RunnersManager() {
        this(DEFAULT_POOL_SIZE);
    }

    public RunnersManager(int poolSize) {
        this(DEFAULT_RUNNER_NAME, poolSize, false);
    }

    public RunnersManager(String runnerName) {
        this(runnerName, DEFAULT_POOL_SIZE, false);
    }

    public RunnersManager(String runnerName, int poolSize) {
        this(runnerName, poolSize, false);
    }

    public RunnersManager(String runnerName, int poolSize, boolean useDaemonThread) {
        this.runnerName = runnerName;
        this.poolSize = poolSize;
        this.useDaemonThread = useDaemonThread;
        this.runnerList = new LinkedHashSet<>();
        ThreadFactory threadFactory = new RunnerThreadFactory(runnerName, useDaemonThread);
        _POOL = Executors.newFixedThreadPool(poolSize, threadFactory);
    }

    public String submit(Job instance) {
        JobRunner runner = instance.getRunner();
        runner.setFinishListener(persistenceListener);
        Future<Job> future = _POOL.submit(runner);
        futureMap.put(instance.getJobID(), future);
        instanceMap.put(instance.getJobID(), instance);
        return instance.getJobID();
    }

    public void setPersistenceHelper(PersistenceHelper helper) {
        persistenceHelper = helper;
    }

    public void kill(Job instance) {
        if (futureMap.containsKey(instance.getJobID())) {
            futureMap.get(instance.getJobID()).cancel(true);
        }
    }

    private Job getInstance(String instanceID) {
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

    private Job getMemoryInstance(String instanceID) {
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

    private void idCheck(String instanceID) {
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

    private class PersistenceListener implements FinishListener {
        @Override
        public void onFinish(String instanceId) throws Exception {
            persistence(instanceId);
        }
    }

    private void persistence(String instanceID) {
        if (persistenceHelper != null) {
            persistenceHelper.save(getMemoryInstance(instanceID).toString());
            futureMap.remove(instanceID);
            instanceMap.remove(instanceID);
        }
    }

    public void shutdown() throws RunnerException {
        if (_POOL.isShutdown() || _POOL.isTerminated())
            throw new IllegalStateException("Runner manager already shutdown.");
        for (String i : futureMap.keySet()) {
            Future<Job> f = futureMap.get(i);
            Job job = instanceMap.get(i);
            if (!f.isDone() && !f.isCancelled()) {
                try {
                    job.kill();
                } catch (RunnerException e) {
                    throw new RunnerException("Runner manager shutdown failed. Because job[ " + i + " ] kill failed." + e.getMessage(), e);
                } finally {
                    if (!f.cancel(true)) {
                        throw new RunnerException("Runner manager shutdown failed. Because job[ " + i + " ] thread kill failed.");
                    }
                }
            }
        }
        _POOL.shutdownNow();
        if (!_POOL.isShutdown()) {
            throw new RunnerException("Runner manager shutdown failed because of unknown reason.");
        }
    }

    public String getRunnerName() {
        return runnerName;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public boolean isUseDaemonThread() {
        return useDaemonThread;
    }

    public Class getRunnerClass(String command) {
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

    public synchronized void addRunner(Class<? extends AbstractTaskRunner> runnerClass) {
        runnerList.add(runnerClass);
    }

    private static class RunnerThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;
        private boolean useDaemonThread;

        private RunnerThreadFactory(String runnerName, boolean useDaemonThread) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = runnerName + "-" +
                    poolNumber.getAndIncrement() +
                    "-thread-";
            this.useDaemonThread = useDaemonThread;
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (!t.isDaemon())
                t.setDaemon(useDaemonThread);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
