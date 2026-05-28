package cl.joaedu.accessservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "capacity-service")
public interface CapacityClient {
    @PostMapping("/api/capacity/{branchId}/increment")
    void increment(@PathVariable("branchId") Long branchId);

    @PostMapping("/api/capacity/{branchId}/decrement")
    void decrement(@PathVariable("branchId") Long branchId);
}