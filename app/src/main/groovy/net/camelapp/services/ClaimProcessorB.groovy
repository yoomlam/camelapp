package net.camelapp.services

import net.camelapp.models.Claim
import net.camelapp.models.ClaimStatus
import net.camelapp.models.Payload
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ClaimProcessorB {

    @Autowired
    final ClaimService claimService

    Payload process(Claim claim) {
        def results = [
            'p_systolic'  : 110,
            'bp_diastolic': 70,
            'rrd_pdf_path': "rrd/hypertension/${claim.submission_id}.pdf"
        ]

        claimService.updateStatus(claim.uuid, ClaimStatus.DONE_RRD)
        new Payload(claim.submission_id, 'Success', results)
    }
}
