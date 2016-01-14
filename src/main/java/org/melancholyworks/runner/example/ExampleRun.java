package org.melancholyworks.runner.example;

import org.codehaus.jackson.map.ObjectMapper;
import org.melancholyworks.runner.RunnersManager;
import org.melancholyworks.runner.job.Job;
import org.melancholyworks.runner.task.Task;

import java.io.IOException;
import java.util.Properties;

/**
 * @author ZarkTao
 */
public class ExampleRun {
    public static void main(String[] args) throws InterruptedException, IOException {
        RunnersManager.addRunner(SleepRunner.class);
        String instanceID = "job-1";
        Job instance = new Job(instanceID, "demo", new String[]{"sleep"}, new Properties());
        instance.submit();
        while (true) {
            if (instance.isEnd()) {
                System.out.println("============== JSON ===============");
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(instance);
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
    }
}
