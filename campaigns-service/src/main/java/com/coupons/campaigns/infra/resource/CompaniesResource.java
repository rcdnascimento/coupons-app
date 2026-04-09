package com.coupons.campaigns.infra.resource;

import com.coupons.campaigns.domain.entity.Company;
import com.coupons.campaigns.domain.service.CompanyManagementService;
import com.coupons.campaigns.infra.resource.dto.CompanyResponse;
import com.coupons.campaigns.infra.resource.dto.CreateCompanyRequest;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/companies")
public class CompaniesResource {

    private final CompanyManagementService companyManagementService;

    public CompaniesResource(CompanyManagementService companyManagementService) {
        this.companyManagementService = companyManagementService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyResponse create(@Valid @RequestBody CreateCompanyRequest request) {
        Company created = companyManagementService.create(request);
        return CompanyResponse.from(created);
    }

    @GetMapping
    public List<CompanyResponse> list() {
        return companyManagementService.list().stream().map(CompanyResponse::from).collect(Collectors.toList());
    }
}
