package net.camelapp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import net.camelapp.models.Claim;
import net.camelapp.models.ClaimStatus;
import net.camelapp.models.Payload;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class ClaimProcessorA {

    private final ClaimService claimService;

    // Autowired constructor allows field to be final
    // @Autowired
    // ClaimProcessorA(ClaimService claimService){
    //     this.claimService = claimService;
    // }

    public Payload process(Claim claim) {
        HashMap<String, Object> results = new HashMap<String, Object>();
        results.put("bp_systolic", 120);
        results.put("bp_diastolic", 80);
        results.put("rrd_pdf_path", "rrd/hypertension/" + claim.getSubmission_id() + ".pdf");

        claimService.updateStatus(claim.getUuid(), ClaimStatus.DONE_RRD);
        return new Payload(claim.getSubmission_id(), "Success", results);
    }
}