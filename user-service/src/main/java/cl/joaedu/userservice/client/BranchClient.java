package cl.joaedu.userservice.client;

import cl.joaedu.userservice.dto.BranchResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "branch-service", url = "${BRANCH_SERVICE_URL:http://localhost:8081}")
public interface BranchClient {

    @GetMapping("/api/branches")
    List<BranchResponse> getAllBranches();

    @GetMapping("/api/branches/{id}")
    BranchResponse getBranchById(@PathVariable("id") Long id);
}