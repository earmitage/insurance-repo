package com.earmitage.core.security.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.earmitage.core.security.dto.SubscriptionDto;
import com.earmitage.core.security.repository.SubscriptionRepository;

@RestController
@RequestMapping(value = "${app.url}/subscriptions")
public class SubscriptionController {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @GetMapping("/uuid/{uuid}")
    public ResponseEntity<SubscriptionDto> getByUuid(@PathVariable String uuid) {
        return subscriptionRepository.findByUuid(uuid).map(SubscriptionDto::new).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{username}/")
    public List<SubscriptionDto> getByUsername(@PathVariable String username) {
        return subscriptionRepository.findByUserUsername(username).stream().map(SubscriptionDto::new).toList();
    }

}
