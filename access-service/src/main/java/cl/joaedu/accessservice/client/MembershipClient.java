package cl.joaedu.accessservice.client;

import cl.joaedu.accessservice.dto.MembershipStatusDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "membership-service")
public interface MembershipClient {
    @GetMapping("/api/membership/status/{userId}")
    MembershipStatusDto getStatus(@PathVariable("userId") Long userId);
}
