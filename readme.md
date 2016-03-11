
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
2. Call RunnerUtils.addRunner to add your runner to the framework. It's sequence sensitive. Framework would choose the first runner which returns true in checkCommand() to run your command.
3. Create your Instance then submit, and it will run in Java thread pool.
4. You can get your job's status, result, log etc. from instance.
1. Job.setLogOutputStream to assign a outputStream to this job.
5. Implement the PersistenceHelper, then call RunnerUtils.setPersistenceHelper. If PersistenceHelper is defined in the framework, the framework will use these method to do persistence jobs. If not, the instances will be stored in memory.