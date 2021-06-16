package com.course.camunda.microservicetraining.workers;

import com.camunda.consulting.client.api.MessageApi;
import com.camunda.consulting.client.model.CorrelationMessageDto;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class PaymentFailedWorker implements CommandLineRunner {

    private final MessageApi messageApi;
    private final ExternalTaskClient externalTaskClient;
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    public PaymentFailedWorker(MessageApi messageApi, ExternalTaskClient externalTaskClient) {
        this.messageApi = messageApi;
        this.externalTaskClient = externalTaskClient;
    }

    @Override
    public void run(String... args) throws Exception {
        externalTaskClient.subscribe("payment-done")
                .lockDuration(10000L).handler(this::handleTask).open();
    }

    private void handleTask(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        LOG.info("Running external task {} for topic {}", externalTask.getId(), externalTask.getTopicName());

        CorrelationMessageDto correlationMessageDto = new CorrelationMessageDto().
                messageName("PaymentCompleted").businessKey(externalTask.getBusinessKey());

        messageApi.deliverMessage(correlationMessageDto);

        externalTaskService.complete(externalTask);
        LOG.info("External task {} for topic {} completed", externalTask.getId(), externalTask.getTopicName());
    }
}
