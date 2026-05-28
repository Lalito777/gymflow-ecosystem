package cl.joaedu.notificationservice.controller;

import cl.joaedu.notificationservice.model.Notification;
import cl.joaedu.notificationservice.repository.NotificationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/notify")
public class NotificationController {
    private final NotificationRepository repository;

    public NotificationController(NotificationRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<Notification> send(@RequestBody Notification n) {
        n.setCreadoEn(LocalDateTime.now());
        return new ResponseEntity<>(repository.save(n), HttpStatus.CREATED);
    }
}