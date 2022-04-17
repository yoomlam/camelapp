package net.camelapp.services

import org.apache.camel.Exchange
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import net.camelapp.models.ClaimRepository
import net.camelapp.models.Claim
import net.camelapp.models.ClaimStatus

@Service
class ClaimService {
    @Autowired
    private final ClaimRepository claimRepository

    List<Claim> getAllClaims() {
        List<Claim> claimRecords = []
        claimRepository.findAll().forEach(claimRecords::add)
        claimRecords
    }

    Claim addClaim(Claim claimRecord) {
        claimRepository.save(claimRecord)
    }

    Claim getClaim(String id) {
        claimRepository.findById(id).get()
    }

    // Alternative to `getClaim(String id)` is to pass in the Exchange.
    // Downside: this class becomes dependent on Camel's Exchange class.
    Claim claimDetail(Exchange exchange) {
        String id = exchange.getIn().getHeader('id')
        getClaim(id)
    }

    Claim updateStatus(String id, ClaimStatus status) {
        Claim claim = getClaim(id)
        claim.setStatus(status)
        claimRepository.save(claim)
        claim
    }
}
