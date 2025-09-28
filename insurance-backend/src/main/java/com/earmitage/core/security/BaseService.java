package com.earmitage.core.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.earmitage.core.security.repository.BaseEntity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class BaseService {

    @Autowired
    @PersistenceContext
    protected EntityManager entityManager;

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public <T extends BaseEntity> List<T> fetchByNativeQuery(final String sqlString) {
        return entityManager.createNativeQuery(sqlString).getResultList();
    }

    public <T extends BaseEntity> void saveNew(final T entity) {
        entityManager.persist(entity);
    }

}
