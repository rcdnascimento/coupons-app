package com.coupons.bff.resource;

import com.coupons.bff.infra.gateway.campaigns.CampaignsGateway;
import com.coupons.bff.infra.resource.dto.CompanyResponse;
import com.coupons.bff.infra.resource.dto.CreateCompanyRequest;
import java.util.List;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/companies")
public class CompaniesProxyResource {

    private final CampaignsGateway campaignsGateway;

    public CompaniesProxyResource(CampaignsGateway campaignsGateway) {
        this.campaignsGateway = campaignsGateway;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CompanyResponse> create(@Valid @RequestBody CreateCompanyRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(campaignsGateway.createCompany(body));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CompanyResponse>> list() {
        return ResponseEntity.ok(campaignsGateway.listCompanies());
    }
}
