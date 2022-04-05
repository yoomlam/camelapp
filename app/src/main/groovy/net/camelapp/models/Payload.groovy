package net.camelapp.models

import groovy.transform.ToString

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.validation.constraints.NotNull

//@Entity
@ToString
class Payload implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id

    Date createdAt = new Date()

//    @NotNull
    String submission_id

//    @NotNull
    String resultStatus

    Map<String, Object> results

    // Needed for DtoConverter
    Payload(){}

    // Created for ClaimProcessorA.java
    Payload(String submission_id, String resultStatus, HashMap<String, Object> results) {
        this.submission_id = submission_id
        this.resultStatus = resultStatus
        this.results = results
    }
}
