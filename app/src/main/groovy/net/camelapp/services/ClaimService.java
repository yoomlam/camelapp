package net.camelapp.services;

import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import net.camelapp.models.ClaimRepository;
import net.camelapp.models.Claim;
import net.camelapp.models.ClaimStatus;

import java.util.ArrayList;
import java.util.List;

@Service
public class ClaimService {
    @Autowired
    private ClaimRepository claimRepository;

    public List<Claim> getAllClaims() {
        List<Claim> claimRecords = new ArrayList<>();
        claimRepository.findAll().forEach(claimRecords::add);
        return claimRecords;
    }

    public Claim addClaim(Claim claimRecord) {
        return claimRepository.save(claimRecord);
    }

    public Claim getClaim(String id) {
        return claimRepository.findById(id).get();
    }

    public Claim claimDetail(Exchange exchange) {
        String id = exchange.getIn().getHeader("id").toString();
        return getClaim(id);
    }

    public Claim updateStatus(String id, ClaimStatus status) {
        Claim claim = getClaim(id);
        claim.setStatus(status);
        claimRepository.save(claim);
        return claim;
    }
}
