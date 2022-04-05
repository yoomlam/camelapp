package net.camelapp.services;

import org.jruby.embed.ScriptingContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import net.camelapp.models.Claim;
import net.camelapp.models.ClaimStatus;
import net.camelapp.models.Payload;

import java.util.HashMap;

@Service
public class ClaimProcessorD {

    @Autowired
    ClaimService claimService;

    public Claim claimFactory() {
        return Claim.builder().submission_id("subm1").claimant_id("vet2").build();
    }

    ScriptingContainer container = new ScriptingContainer();

    public Payload process(Claim claim) {
        container.put("submission_id", claim.getSubmission_id());
        String rubyScript = "results = Hash[" +
                "'p_systolic' => 130," +
                "'bp_diastolic' => 75," +
                "'rrd_pdf_path' => \"rrd/hypertension/#{submission_id}.pdf\"," +
                "];" +
                "require 'java';" +
                "java_import 'net.camelapp.models.Payload';" +
                "Payload.new(submission_id, 'SUCCESS', results)";

        Payload rubyResults = (Payload) container.runScriptlet(rubyScript);

        claimService.updateStatus(claim.getUuid(), ClaimStatus.DONE_RRD);
        return rubyResults;
    }
}
