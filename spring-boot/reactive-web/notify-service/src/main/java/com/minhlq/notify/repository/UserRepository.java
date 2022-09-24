package com.minhlq.notify.repository;

import com.minhlq.notify.entity.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveMongoRepository<User, String> {

  Mono<User> findByCardId(String cardId);
}
