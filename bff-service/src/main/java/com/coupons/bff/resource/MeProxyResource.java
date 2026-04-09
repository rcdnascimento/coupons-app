package com.coupons.bff.resource;

import com.coupons.bff.infra.gateway.ledger.LedgerGateway;
import com.coupons.bff.infra.gateway.profile.ProfileGateway;
import com.coupons.bff.infra.resource.dto.BalanceResponse;
import com.coupons.bff.infra.resource.dto.MeProfileResponse;
import com.coupons.bff.infra.resource.dto.ProfileResponse;
import com.coupons.bff.security.SecurityRequestSupport;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class MeProxyResource {

    private final ProfileGateway profileGateway;
    private final LedgerGateway ledgerGateway;

    public MeProxyResource(ProfileGateway profileGateway, LedgerGateway ledgerGateway) {
        this.profileGateway = profileGateway;
        this.ledgerGateway = ledgerGateway;
    }

    @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MeProfileResponse> profile(
            @RequestParam(required = false) String name, @RequestParam(required = false) String email) {
        UUID userId = SecurityRequestSupport.requireUserId();
        ProfileResponse profile = profileGateway.getByUserId(userId.toString());
        MeProfileResponse out = new MeProfileResponse();
        out.setUserId(profile.getUserId());
        out.setName(name);
        out.setEmail(email);
        out.setReferralCode(profile.getReferralCode());
        return ResponseEntity.ok(out);
    }

    @GetMapping(value = "/balance", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BalanceResponse> balance() {
        UUID userId = SecurityRequestSupport.requireUserId();
        return ResponseEntity.ok(ledgerGateway.getBalance(userId.toString()));
    }
}
