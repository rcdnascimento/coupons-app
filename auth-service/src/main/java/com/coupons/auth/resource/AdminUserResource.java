package com.coupons.auth.resource;

import com.coupons.auth.domain.entity.User;
import com.coupons.auth.infra.persistence.UserRepository;
import com.coupons.auth.resource.dto.AdminUserSearchResponse;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/users")
public class AdminUserResource {

    private static final int MAX_RESULTS = 20;

    private final UserRepository userRepository;

    public AdminUserResource(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/search")
    public List<AdminUserSearchResponse> search(@RequestParam(value = "q", required = false) String q) {
        String trimmed = q == null ? "" : q.trim();
        if (trimmed.length() < 2) {
            return List.of();
        }
        List<User> found =
                userRepository.searchByNameOrEmailContaining(trimmed, PageRequest.of(0, MAX_RESULTS));
        return found.stream()
                .map(u -> new AdminUserSearchResponse(u.getId().toString(), u.getName(), u.getEmail()))
                .collect(Collectors.toList());
    }
}
