package com.coupons.campaigns.infra.persistence;

import com.coupons.campaigns.domain.entity.Company;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, UUID> {

    boolean existsByCnpj(String cnpj);

    Optional<Company> findByCnpj(String cnpj);

    List<Company> findAllByOrderByNameAsc();
}
