package muni_del_valle.ms_monitoreo.ms_monitoreo.service;

import muni_del_valle.ms_monitoreo.ms_monitoreo.dto.CreateFocusRequest;
import muni_del_valle.ms_monitoreo.ms_monitoreo.model.Focus;
import muni_del_valle.ms_monitoreo.ms_monitoreo.model.FocusStatus;
import muni_del_valle.ms_monitoreo.ms_monitoreo.repository.FocusRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.locationtech.jts.io.WKTWriter;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class FocusService {

    private final FocusRepository focusRepository;
    private final AmqpTemplate amqpTemplate;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public FocusService(FocusRepository focusRepository, AmqpTemplate amqpTemplate) {
        this.focusRepository = focusRepository;
        this.amqpTemplate = amqpTemplate;
    }

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L); // no timeout
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        return emitter;
    }

    @Transactional
    public Focus createFocus(CreateFocusRequest req) {
        Focus f = FocusFactory.createFocus(req);
        Focus saved = focusRepository.save(f);
        // publish to SSE subscribers
        publishEvent("focus-created", saved);

        // notify ms_alertas asynchronously (do not block request)
        try {
            WKTWriter writer = new WKTWriter();
            String wkt = writer.write(saved.getGeometry());
            var payload = new java.util.HashMap<String, Object>();
            payload.put("reportId", saved.getReportId());
            payload.put("geometry", wkt);
            payload.put("description", saved.getDescription());
            payload.put("severity", saved.getSeverity() != null ? saved.getSeverity().name() : null);
            // publish to RabbitMQ exchange (non-blocking)
            amqpTemplate.convertAndSend("alerts.exchange", "alerts.new", payload);
        } catch (Exception ex) {
            // ignore conversion/notification errors to keep createFocus reliable
        }
        return saved;
    }

    public Page<Focus> listFocus(Optional<String> statusOpt, Pageable pageable) {
        if (statusOpt.isPresent()) {
            try {
                FocusStatus st = FocusStatus.valueOf(statusOpt.get());
                return focusRepository.findByStatus(st, pageable);
            } catch (Exception ex) {
                return focusRepository.findAll(pageable);
            }
        }
        return focusRepository.findAll(pageable);
    }

    public Optional<Focus> getById(Long id) { return focusRepository.findById(id); }

    private void publishEvent(String eventName, Object data) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (IOException ex) {
                emitters.remove(emitter);
            }
        }
    }
}
