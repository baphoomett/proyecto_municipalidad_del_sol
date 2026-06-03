package muni_del_valle.ms_integracion.consumer;

import muni_del_valle.ms_integracion.config.RabbitConfig;
import muni_del_valle.ms_integracion.dto.IntegrationEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class EventConsumer {
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);
    private final ObjectMapper mapper = new ObjectMapper();

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void receive(String payload) {
        try {
            IntegrationEvent event = mapper.readValue(payload, IntegrationEvent.class);
            logger.info("[ms_integracion] Evento recibido: tipo={} zona={} mensaje={}",
                    event.getTipo(), event.getZona(), event.getMensaje());
        } catch (Exception ex) {
            logger.warn("[ms_integracion] Payload no JSON o error parseando, raw: {}", payload);
        }
    }
}
