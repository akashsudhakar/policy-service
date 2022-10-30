package com.embea.policy.repository;

import com.embea.policy.dto.Policy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyRepo extends CrudRepository<Policy, String> {}
