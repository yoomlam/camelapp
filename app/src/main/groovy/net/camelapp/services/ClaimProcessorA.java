package net.camelapp.services;

import org.jruby.embed.ScriptingContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import net.camelapp.models.Claim;
import net.camelapp.models.ClaimStatus;
import net.camelapp.models.Payload;

import java.util.HashMap;

@Service
public class ClaimProcessorA {

    @Autowired
    ClaimService claimService;

    public Claim claimFactory() {
        return Claim.builder().submission_id("subm1").claimant_id("vet2").build();
    }

    public Payload process(Claim claim) {
        HashMap<String, Object> results = new HashMap<String, Object>();
        results.put("bp_systolic", 120);
        results.put("bp_diastolic", 80);
        results.put("rrd_pdf_path", "rrd/hypertension/" + claim.getSubmission_id() + ".pdf");

        claimService.updateStatus(claim.getUuid(), ClaimStatus.DONE_RRD);
        return new Payload(claim.getSubmission_id(), "Success", results);
    }
}
