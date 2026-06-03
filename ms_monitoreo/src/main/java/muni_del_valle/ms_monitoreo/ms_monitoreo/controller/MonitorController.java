package muni_del_valle.ms_monitoreo.ms_monitoreo.controller;

import muni_del_valle.ms_monitoreo.ms_monitoreo.dto.CreateFocusRequest;
import muni_del_valle.ms_monitoreo.ms_monitoreo.model.Focus;
import muni_del_valle.ms_monitoreo.ms_monitoreo.service.FocusService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/api/monitor")
public class MonitorController {

    private final FocusService focusService;

    public MonitorController(FocusService focusService) {
        this.focusService = focusService;
    }

    @PostMapping("/focus")
    public ResponseEntity<?> createFocus(@Valid @RequestBody CreateFocusRequest req, org.springframework.validation.BindingResult br) {
        if (br.hasErrors()) return ResponseEntity.badRequest().body(br.getAllErrors());
        Focus f = focusService.createFocus(req);
        return ResponseEntity.created(URI.create("/api/monitor/focus/" + f.getId())).body(f);
    }

    @GetMapping("/focus")
    public ResponseEntity<?> listFocus(@RequestParam Optional<String> status,
                                       @RequestParam Optional<Integer> page,
                                       @RequestParam Optional<Integer> size) {
        int p = page.orElse(0);
        int s = size.orElse(20);
        Pageable pageable = PageRequest.of(p, s);
        Page<Focus> res = focusService.listFocus(status, pageable);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/focus/{id}")
    public ResponseEntity<?> getFocus(@PathVariable Long id) {
        return focusService.getById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/stream")
    public SseEmitter stream() {
        return focusService.subscribe();
    }
}
