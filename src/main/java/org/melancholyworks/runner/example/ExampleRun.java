package org.melancholyworks.runner.example;

import org.codehaus.jackson.map.ObjectMapper;
import org.melancholyworks.runner.RunnerException;
import org.melancholyworks.runner.RunnersManager;
import org.melancholyworks.runner.job.Job;
import org.melancholyworks.runner.task.Task;

import java.io.IOException;
import java.util.Properties;

/**
 * @author ZarkTao
 */
public class ExampleRun {
    public static void main(String[] args) throws InterruptedException, IOException, RunnerException {
        RunnersManager manager = new RunnersManager();
        manager.addRunner(SleepRunner.class);
        String instanceID = "job-1";
        Job instance = new Job(instanceID, "demo", new String[]{"sleep"}, new Properties());
        instance.submitTo(manager);
        Thread.sleep(1000L);
        manager.shutdown();
        while (true) {
            if (instance.isEnd()) {
                System.out.println("============== JSON ===============");
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(instance);
                System.out.println(json);
                Job restore = mapper.readValue(json, Job.class);
                System.out.println("============== LOG ===============");
                System.out.println(restore.getLog());
                System.out.println("============== Result ===============");
                for (Task task : restore.getTasks()) {
                    System.out.println(restore.getTaskResults().get(task.getTaskID()));
                }
                break;
            }
            Thread.sleep(1000L);
        }
        //manager.shutdown();
    }
}
