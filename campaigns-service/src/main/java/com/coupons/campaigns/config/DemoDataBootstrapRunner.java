package com.coupons.campaigns.config;

import com.coupons.campaigns.domain.CampaignStatus;
import com.coupons.campaigns.domain.entity.Campaign;
import com.coupons.campaigns.domain.entity.Company;
import com.coupons.campaigns.domain.entity.Coupon;
import com.coupons.campaigns.domain.service.CampaignCouponService;
import com.coupons.campaigns.domain.service.CampaignManagementService;
import com.coupons.campaigns.domain.service.CompanyManagementService;
import com.coupons.campaigns.domain.service.CouponCrudService;
import com.coupons.campaigns.infra.persistence.CampaignCouponRepository;
import com.coupons.campaigns.infra.persistence.CampaignRepository;
import com.coupons.campaigns.infra.persistence.CompanyRepository;
import com.coupons.campaigns.infra.persistence.CouponRepository;
import com.coupons.campaigns.infra.resource.dto.CreateCompanyRequest;
import com.coupons.campaigns.infra.resource.dto.CreateCouponRequest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Seed idempotente: exatamente 1 campanha por empresa (Uber e iFood), com 2 prêmios (R$ 50 e R$ 20).
 * Imagens: {@code classpath:seed-uploads/} → BFF {@code /api/uploads/images/...}.
 */
@Component
public class DemoDataBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataBootstrapRunner.class);

    private static final String UBER_TITLE = "Até R$ 50,00 na Uber";
    private static final String IFOOD_TITLE = "Até R$ 50,00 no iFood";

    private static final String UBER_LOGO = "/api/uploads/images/uber-logo.png";
    private static final String UBER_BG = "/api/uploads/images/uber-campaign-background.png";
    private static final String IFOOD_LOGO = "/api/uploads/images/ifood-logo.jpg";
    private static final String IFOOD_BG = "/api/uploads/images/ifood-campaign-background.jpg";

    private static final String CNPJ_UBER = "17895646000187";
    private static final String CNPJ_IFOOD = "14380200000121";

    private final boolean enabled;
    private final int pointsCost;
    private final CompanyManagementService companyManagementService;
    private final CouponCrudService couponCrudService;
    private final CampaignManagementService campaignManagementService;
    private final CampaignCouponService campaignCouponService;
    private final CompanyRepository companyRepository;
    private final CouponRepository couponRepository;
    private final CampaignRepository campaignRepository;
    private final CampaignCouponRepository campaignCouponRepository;

    public DemoDataBootstrapRunner(
            @Value("${campaigns.demo-seed.enabled:true}") boolean enabled,
            @Value("${campaigns.demo-seed.points-cost:10}") int pointsCost,
            CompanyManagementService companyManagementService,
            CouponCrudService couponCrudService,
            CampaignManagementService campaignManagementService,
            CampaignCouponService campaignCouponService,
            CompanyRepository companyRepository,
            CouponRepository couponRepository,
            CampaignRepository campaignRepository,
            CampaignCouponRepository campaignCouponRepository) {
        this.enabled = enabled;
        this.pointsCost = pointsCost;
        this.companyManagementService = companyManagementService;
        this.couponCrudService = couponCrudService;
        this.campaignManagementService = campaignManagementService;
        this.campaignCouponService = campaignCouponService;
        this.companyRepository = companyRepository;
        this.couponRepository = couponRepository;
        this.campaignRepository = campaignRepository;
        this.campaignCouponRepository = campaignCouponRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }

        Instant start = Instant.now().minus(1, ChronoUnit.HOURS);
        Instant end = Instant.now().plus(14, ChronoUnit.DAYS);
        Instant distribution = end.plus(1, ChronoUnit.DAYS);
        Instant couponExpires = Instant.now().plus(120, ChronoUnit.DAYS);
        Instant visibleUntil = distribution.plus(45, ChronoUnit.DAYS);
        int cost = Math.max(0, pointsCost);

        Company uber = ensureCompany("Uber", CNPJ_UBER, UBER_LOGO);
        seedCampaign(
                UBER_TITLE,
                "Inscreva-se e concorra a descontos na Uber.\n"
                        + "Há 2 prêmios: R$ 50 e R$ 20 off na corrida.\n"
                        + "Use seus pontos e embarque nessa.",
                uber,
                UBER_BG,
                cost,
                start,
                end,
                distribution,
                visibleUntil,
                couponExpires,
                new PrizeSpec[] {
                    new PrizeSpec("UBER-R50-001", "R$ 50,00 de desconto em corridas Uber"),
                    new PrizeSpec("UBER-R20-001", "R$ 20,00 de desconto em corridas Uber")
                });

        Company ifood = ensureCompany("iFood", CNPJ_IFOOD, IFOOD_LOGO);
        seedCampaign(
                IFOOD_TITLE,
                "Peça e concorra a descontos no iFood.\n"
                        + "São 2 cupons: R$ 50 e R$ 20 off no pedido.\n"
                        + "Inscreva-se com pontos e boa sorte.",
                ifood,
                IFOOD_BG,
                cost,
                start,
                end,
                distribution,
                visibleUntil,
                couponExpires,
                new PrizeSpec[] {
                    new PrizeSpec("IFOOD-R50-001", "R$ 50,00 de desconto no iFood"),
                    new PrizeSpec("IFOOD-R20-001", "R$ 20,00 de desconto no iFood")
                });
    }

    private void seedCampaign(
            String title,
            String description,
            Company company,
            String imageUrl,
            int pointsCost,
            Instant start,
            Instant end,
            Instant distribution,
            Instant visibleUntil,
            Instant couponExpires,
            PrizeSpec[] prizes) {
        for (PrizeSpec prize : prizes) {
            ensureCoupon(prize.code, prize.title, couponExpires);
        }

        List<Campaign> forCompany = campaignRepository.findByCompanyIdOrderByCreatedAtAsc(company.getId());
        Campaign keeper =
                forCompany.stream()
                        .filter(c -> title.equals(c.getTitle()))
                        .findFirst()
                        .orElse(forCompany.isEmpty() ? null : forCompany.get(0));

        if (keeper == null) {
            keeper = createCampaign(
                    title, description, company, imageUrl, pointsCost, start, end, distribution, visibleUntil);
            syncPrizes(keeper, prizes);
            log.info("Seed criado: '{}' ({} prêmios, {} pontos)", title, prizes.length, pointsCost);
        } else {
            applyCampaignFields(
                    keeper, title, description, imageUrl, pointsCost, start, end, distribution, visibleUntil);
            campaignRepository.save(keeper);
            syncPrizes(keeper, prizes);
            log.info("Seed atualizado: '{}' ({} prêmios)", title, prizes.length);
        }

        UUID keeperId = keeper.getId();
        for (Campaign extra : forCompany) {
            if (!extra.getId().equals(keeperId)) {
                retireCampaign(extra);
            }
        }
    }

    private Campaign createCampaign(
            String title,
            String description,
            Company company,
            String imageUrl,
            int pointsCost,
            Instant start,
            Instant end,
            Instant distribution,
            Instant visibleUntil) {
        Campaign campaign = new Campaign();
        applyCampaignFields(
                campaign, title, description, imageUrl, pointsCost, start, end, distribution, visibleUntil);
        campaign.setCompanyId(company.getId());
        return campaignManagementService.createCampaign(campaign);
    }

    private void applyCampaignFields(
            Campaign campaign,
            String title,
            String description,
            String imageUrl,
            int pointsCost,
            Instant start,
            Instant end,
            Instant distribution,
            Instant visibleUntil) {
        campaign.setTitle(title);
        campaign.setDescription(description);
        campaign.setSubscriptionsStartAt(start);
        campaign.setSubscriptionsEndAt(end);
        campaign.setDistributionAt(distribution);
        campaign.setVisibleUntil(visibleUntil);
        campaign.setImageUrl(imageUrl);
        campaign.setPointsCost(pointsCost);
        campaign.setStatus(CampaignStatus.ACTIVE);
    }

    private void retireCampaign(Campaign campaign) {
        campaign.setStatus(CampaignStatus.CLOSED);
        campaign.setVisibleUntil(Instant.now().minus(1, ChronoUnit.DAYS));
        campaignRepository.save(campaign);
        log.info("Campanha de seed antiga retirada: '{}' ({})", campaign.getTitle(), campaign.getId());
    }

    private void syncPrizes(Campaign campaign, PrizeSpec[] prizes) {
        Set<String> desiredCodes = new HashSet<>();
        for (PrizeSpec prize : prizes) {
            desiredCodes.add(prize.code);
        }

        for (var link : campaignCouponRepository.findByCampaignIdOrderByPriorityAsc(campaign.getId())) {
            Coupon coupon = couponRepository.findById(link.getCouponId()).orElse(null);
            if (coupon == null || desiredCodes.contains(coupon.getCode())) {
                continue;
            }
            try {
                campaignCouponService.removeCouponFromCampaign(campaign.getId(), coupon.getId());
            } catch (RuntimeException ex) {
                log.warn(
                        "Não foi possível remover cupom legado {} da campanha {}: {}",
                        coupon.getCode(),
                        campaign.getId(),
                        ex.getMessage());
            }
        }

        int priority = 1;
        for (PrizeSpec prize : prizes) {
            Coupon coupon = couponRepository.findByCode(prize.code).orElse(null);
            if (coupon == null) {
                continue;
            }
            if (campaignCouponRepository.existsByCampaignIdAndCouponId(campaign.getId(), coupon.getId())) {
                priority++;
                continue;
            }
            // Um cupom só pode estar em uma campanha (UK global). Solta de campanhas antigas.
            detachCouponFromOtherCampaigns(campaign.getId(), coupon);
            try {
                Coupon draft = new Coupon();
                draft.setCode(prize.code);
                campaignCouponService.addCoupon(campaign.getId(), draft, priority);
            } catch (RuntimeException ex) {
                log.warn(
                        "Não foi possível vincular cupom {} à campanha {}: {}",
                        prize.code,
                        campaign.getId(),
                        ex.getMessage());
            }
            priority++;
        }
    }

    private void detachCouponFromOtherCampaigns(UUID keeperCampaignId, Coupon coupon) {
        campaignCouponRepository
                .findByCouponId(coupon.getId())
                .ifPresent(
                        link -> {
                            if (link.getCampaignId().equals(keeperCampaignId)) {
                                return;
                            }
                            try {
                                campaignCouponService.removeCouponFromCampaign(
                                        link.getCampaignId(), coupon.getId());
                                log.info(
                                        "Cupom {} desvinculado da campanha antiga {} para o seed",
                                        coupon.getCode(),
                                        link.getCampaignId());
                            } catch (RuntimeException ex) {
                                log.warn(
                                        "Não foi possível desvincular cupom {} da campanha {}: {}",
                                        coupon.getCode(),
                                        link.getCampaignId(),
                                        ex.getMessage());
                            }
                        });
    }

    private Company ensureCompany(String name, String cnpj, String logoUrl) {
        return companyRepository
                .findByCnpj(cnpj)
                .orElseGet(
                        () -> {
                            CreateCompanyRequest req = new CreateCompanyRequest();
                            req.setName(name);
                            req.setCnpj(cnpj);
                            req.setLogoUrl(logoUrl);
                            Company created = companyManagementService.create(req);
                            log.info("Empresa de seed criada: {}", name);
                            return created;
                        });
    }

    private void ensureCoupon(String code, String title, Instant expiresAt) {
        if (couponRepository.existsByCode(code)) {
            return;
        }
        CreateCouponRequest req = new CreateCouponRequest();
        req.setCode(code);
        req.setTitle(title);
        req.setExpiresAt(expiresAt);
        couponCrudService.create(req);
    }

    private static final class PrizeSpec {
        private final String code;
        private final String title;

        private PrizeSpec(String code, String title) {
            this.code = code;
            this.title = title;
        }
    }
}
