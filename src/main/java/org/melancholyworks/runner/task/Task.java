package org.melancholyworks.runner.task;

import org.melancholyworks.runner.job.Job;
import org.melancholyworks.runner.Status;

import java.util.Date;
import java.util.Properties;

/**
 * @author ZarkTao
 */
public class Task {
    private Job instance;
    private String taskID;
    private Status status;
    private Date startTime;
    private Date endTime;
    private String result;
    private String command;

    public Task(Job instance, String taskID, String command) {
        this.instance = instance;
        this.command = command;
        this.taskID = taskID;
    }

    public String getTaskID() {
        return taskID;
    }

    public String getStatus() {
        return status.toString();
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public String getResult() {
        return result;
    }

    public String getCommand() {
        return command;
    }

    protected Job getInstance() {
        return instance;
    }

    protected void setStatus(Status status) {
        this.status = status;
    }

    public void setResult(String result) {
        this.result = result;
    }

    protected void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    protected void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    protected Properties getProperties() {
        return instance.getProperties();
    }
}
