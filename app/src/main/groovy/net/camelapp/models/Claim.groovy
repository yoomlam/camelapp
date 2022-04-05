package net.camelapp.models

import groovy.transform.ToString
import org.hibernate.annotations.GenericGenerator

import javax.persistence.*

@Entity
@ToString
class Claim implements Serializable {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "uuid", updatable = false, nullable = false)
    String uuid

    Date createdAt = new Date()

    String submission_id
    String claimant_id

    String claim_id
    String contention_type

    @Enumerated(EnumType.STRING)
    ClaimStatus status = ClaimStatus.CREATED

//    List<Payload> payloads
}

