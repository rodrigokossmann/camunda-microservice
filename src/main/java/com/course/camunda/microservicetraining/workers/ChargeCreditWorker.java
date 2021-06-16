package com.course.camunda.microservicetraining.workers;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.util.Arrays;

@Component
public class ChargeCreditWorker implements CommandLineRunner {

    private final ExternalTaskClient externalTaskClient;
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    public ChargeCreditWorker(ExternalTaskClient externalTaskClient) {
        this.externalTaskClient = externalTaskClient;
    }

    @Override
    public void run(String... args) throws Exception {
        externalTaskClient.subscribe("credit-charging")
                .lockDuration(10000L).handler(this::handleTask).open();
    }

    private void handleTask(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        //Here is where I do my service logic...
        try {
            LOG.info("Running external task {} for topic {}", externalTask.getId(), externalTask.getTopicName());
            VariableMap variableMap = Variables.createVariables();
            variableMap.put("clientCredit", 100);

            externalTaskService.complete(externalTask, variableMap);
            LOG.info("External task {} for topic {} completed", externalTask.getId(), externalTask.getTopicName());

        } catch (Exception ex) {
            Integer retries = externalTask.getRetries();
            if (retries==null) {
                retries = 3;
            } else {
                retries -= 1;
            }
            LOG.error("External task {} for topic {} failed", externalTask.getId(), externalTask.getTopicName());
            externalTaskService.handleFailure(externalTask, ex.getMessage(), Arrays.toString(ex.getStackTrace()), retries, 1000);
        }
    }
}