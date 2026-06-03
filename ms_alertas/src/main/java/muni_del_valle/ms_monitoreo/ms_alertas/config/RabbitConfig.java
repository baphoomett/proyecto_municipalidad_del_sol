package muni_del_valle.ms_monitoreo.ms_alertas.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String EXCHANGE = "alerts.exchange";
    public static final String QUEUE = "alerts.queue";
    public static final String ROUTING_KEY = "alerts.new";

    @Bean
    public TopicExchange alertsExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue alertsQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public Binding binding(Queue alertsQueue, TopicExchange alertsExchange) {
        return BindingBuilder.bind(alertsQueue).to(alertsExchange).with(ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
