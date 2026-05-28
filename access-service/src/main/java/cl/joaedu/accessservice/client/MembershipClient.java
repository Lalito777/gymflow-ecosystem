package cl.joaedu.accessservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Map;

@FeignClient(name = "membership-service")
public interface MembershipClient {
    @GetMapping("/api/membership/status/{userId}")
    Map<String, Boolean> getStatus(@PathVariable("userId") Long userId);
}