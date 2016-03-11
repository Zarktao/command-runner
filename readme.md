
Runner aims for providing a simple multi-synchronized-task execution framework.

> * This is based on Java ExecutorService and FixedThreadPool.
> * The thought of job comes from Aliyun's odps-sdk-core.

### Base Thought
* Every task can be simplified as a single command with several properties. Like sql queries or commands in shell.
* ThreadPool provided by Java is simple and easy to use. (I'm not sure it is stable in JavaEE web application.)
* Change a synchronized task to asynchronous task.

### Constraint
* Every task sequence constitute a Instance. Each instance has a unique Job ID in same namespace.
* Every task has a status which can be checked at any time. When status changes, add a log line.
* Use Jackson's json lib to do json jobs.

### Usage
1. Define a subclass of AbstractTaskRunner to implement the method of executing the synchronized task. When run() method returns, the task will be marked as done.
2. Do 'new RunnerManager()' to manage the whole runner pool. Call runnerManager.addRunner() to add your defined runner to this manager. It's sequence sensitive. Manager would choose the first runner which returns true in getRunnerClass() to run your command.
3. Create your Instance then submit to your manager, and it will run in this Java thread pool.
4. You can get your job's status, result, log etc. from instance.
5. Job.setLogOutputStream to assign a outputStream to this job.
6. Implement the PersistenceHelper, then call RunnerManager.setPersistenceHelper. If PersistenceHelper is defined in the framework, the framework will use these method to do persistence jobs. If not, the instances will be temporarily stored in memory.