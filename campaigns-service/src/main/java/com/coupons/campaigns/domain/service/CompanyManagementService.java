package com.coupons.campaigns.domain.service;

import com.coupons.campaigns.domain.entity.Company;
import com.coupons.campaigns.domain.exception.BadRequestException;
import com.coupons.campaigns.infra.persistence.CompanyRepository;
import com.coupons.campaigns.infra.resource.dto.CreateCompanyRequest;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyManagementService {

    private final CompanyRepository companyRepository;

    public CompanyManagementService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Transactional
    public Company create(CreateCompanyRequest request) {
        String cnpj = normalizeCnpj(request.getCnpj());
        if (companyRepository.existsByCnpj(cnpj)) {
            throw new BadRequestException("CNPJ já cadastrado para outra empresa");
        }

        Company c = new Company();
        c.setName(request.getName().trim());
        c.setCnpj(cnpj);
        c.setLogoUrl(blankToNull(request.getLogoUrl()));
        return companyRepository.save(c);
    }

    @Transactional(readOnly = true)
    public List<Company> list() {
        return companyRepository.findAllByOrderByNameAsc();
    }

    private String normalizeCnpj(String input) {
        if (input == null) return "";
        String digits = input.replaceAll("\\D+", "");
        if (digits.length() != 14) {
            throw new BadRequestException("cnpj deve conter 14 dígitos numéricos");
        }
        return digits;
    }

    private String blankToNull(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }
}
