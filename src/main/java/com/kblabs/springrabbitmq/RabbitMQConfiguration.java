package com.kblabs.springrabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.rabbit.listener.exception.ListenerExecutionFailedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ErrorHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

@Configuration
public class RabbitMQConfiguration {

    @Value("${rabbitmq.queue}")
    private String queueName;

    @Value("${rabbitmq.exchange}")
    private String topicExchangeName;

    @Value("${rabbitmq.routingkey}")
    private String routingKey;

    @Value("${rabbitmq.concurrency}")
    private int concurrency;

    @Bean
    Queue queue() {
        return new Queue(queueName, true, false, false, null);
    }

    @Bean
    DirectExchange exchange() {
        return new DirectExchange(topicExchangeName);
    }

    @Bean
    Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }

    @Bean
    SimpleMessageListenerContainer container(ConnectionFactory connectionFactory,
                                             MessageListenerAdapter listenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setConcurrentConsumers(concurrency);
        container.setQueueNames(queueName);
        container.setErrorHandler(errorHandler());
        container.setMessageListener(listenerAdapter);
        return container;
    }

    @Bean
    MessageListenerAdapter listenerAdapter(Consumer consumer) {
        return new MessageListenerAdapter(consumer, "receiveMessage");
    }


    @Bean
    public ErrorHandler errorHandler() {
        return new ConditionalRejectingErrorHandler(new FatalExceptionStrategy());
    }


    public static class FatalExceptionStrategy extends ConditionalRejectingErrorHandler.DefaultExceptionStrategy {

        protected transient Logger log = Logger.getLogger(this.getClass().getName());

        @Override
        public boolean isFatal(Throwable t) {
            if (t.getCause() instanceof ListenerExecutionFailedException) {
                ListenerExecutionFailedException lefe = (ListenerExecutionFailedException) t;
                log.log (Level.SEVERE,"Failed to process inbound message from queue "
                        + lefe.getFailedMessage().getMessageProperties().getConsumerQueue()
                        + "; failed message: " + lefe.getFailedMessage(), t);
            }
            return true;
            // TODO super.isFatal(t); decide whether an exception should be considered to be fatal and the message
            //  should not be requeued.
        }

        /*
        private boolean isCauseFatal(Throwable cause) {
            return cause instanceof org.springframework.amqp.support.converter.MessageConversionException
            || cause instanceof MessageConversionException
            || cause instanceof MethodArgumentResolutionException || cause instanceof NoSuchMethodException
            || cause instanceof ClassCastException  || this.isUserCauseFatal(cause);
        }
        */

    }

}
