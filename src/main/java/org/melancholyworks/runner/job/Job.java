package org.melancholyworks.runner.job;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonSetter;
import org.codehaus.jackson.map.ObjectMapper;
import org.melancholyworks.runner.RunnerException;
import org.melancholyworks.runner.RunnersManager;
import org.melancholyworks.runner.Status;
import org.melancholyworks.runner.task.Task;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * @author ZarkTao
 */
public class Job {
    private String jobID;
    private String user;
    private String namespace;
    private int priority;//TODO:priority
    private Status status;
    private Task[] tasks;
    private Date submitTime;
    private Date startTime;
    private Date endTime;
    private Properties properties;
    private String log;
    private int point;
    @JsonIgnore
    private JobRunner runner;
    @JsonIgnore
    private OutputStream logOutputStream;

    public Job(String jobID, String user, String[] commands, Properties properties) {
        this(jobID, user, null, 9, commands, properties);
    }

    public Job(String jobID, String user, String namespace, String[] commands, Properties properties) {
        this(jobID, user, namespace, 9, commands, properties);
    }

    public Job(String jobID, String user, int priority, String[] commands, Properties properties) {
        this(jobID, user, null, priority, commands, properties);
    }

    public Job(String jobID, String user, String namespace, int priority, String[] commands, Properties properties) {
        //TODO:jobID check
        this.jobID = jobID;
        this.user = user;
        this.namespace = namespace;
        this.priority = priority;
        this.properties = properties;
        this.point = 0;
        setTasks(commands);
    }

    //for json mapper to restore this
    private Job(){}

    //for json mapper to restore this
    @JsonSetter
    private void setTasks(Task[] tasks){
        this.tasks = tasks;
    }

    private void setTasks(String[] commands) {
        LinkedList<String> strList = new LinkedList<>();
        for (String s : commands)
            if (StringUtils.isNotEmpty(s))
                strList.add(s);
        tasks = new Task[strList.size()];
        for (int i = 0; i < tasks.length; i++) {
            tasks[i] = new Task(this, jobID + "#_" + i, strList.get(i));
        }
    }

    protected void setStatus(Status status) {
        this.status = status;
    }

    public void submitTo(RunnersManager manager) {
        submitTime = new Date();
        prepareToRun(manager);
        manager.submit(this);
    }

    public void kill() throws RunnerException {
        if (runner == null)
            throw new RunnerException("Job not be submitted can not be killed.");
        else if (status.isEnd())
            throw new RunnerException("Job is already stopped.");
        runner.stop();
    }

    private void prepareToRun(RunnersManager manager) {
        runner = new JobRunner(this, manager);
    }

    public JobRunner getRunner() {
        return runner;
    }

    public void setLogOutputStream(OutputStream logOutputStream) {
        this.logOutputStream = logOutputStream;
    }

    public OutputStream getLogOutputStream() {
        return this.logOutputStream;
    }

    public String getJobID() {
        return jobID;
    }

    public String getUser() {
        return user;
    }

    public String getNamespace() {
        return namespace;
    }

    public int getPriority() {
        return priority;
    }

    public String getStatus() {
        return status.toString();
    }

    public Task[] getTasks() {
        return tasks;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public Date getSubmitTime() {
        return submitTime;
    }

    public String getLog() {
        return log;
    }

    public int getPoint() {
        return point;
    }

    protected void setPoint(int point) {
        this.point = point;
    }

    @JsonIgnore
    public int getCount() {
        if (tasks != null)
            return tasks.length;
        return 0;
    }

    public Properties getProperties() {
        return properties;
    }

    protected void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    protected void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (IOException e) {
            return "";
        }
    }

    public static Job fromString(String s) {
        try {
            return new ObjectMapper().readValue(s, Job.class);
        } catch (IOException e) {
            return null;
        }
    }

    @JsonIgnore
    public boolean isEnd() {
        return status.isEnd();
    }

    @JsonIgnore
    public Map<String, String> getTaskResults() {
        LinkedHashMap<String, String> resultMap = new LinkedHashMap<>();
        for (Task task : tasks) {
            resultMap.put(task.getTaskID(), task.getResult());
        }
        return resultMap;
    }

    public void setLog(String log) {
        this.log = log;
    }
}
