package net.camelapp.models;

import org.springframework.data.repository.CrudRepository;
import net.camelapp.models.Claim;

public interface ClaimRepository extends CrudRepository<Claim, String> {
}
