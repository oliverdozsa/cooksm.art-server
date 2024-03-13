package data.repositories.imp;

import data.entities.User;
import data.repositories.UserRepository;
import data.repositories.exceptions.BusinessLogicViolationException;
import io.ebean.EbeanServer;
import lombokized.dto.UserCreateUpdateDto;
import play.Logger;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.Instant;
import java.util.Set;

public class EbeanUserRepository implements UserRepository {
    private EbeanServer ebean;
    private Validator validator;

    private static final Logger.ALogger logger = Logger.of(EbeanUserRepository.class);

    @Inject
    public EbeanUserRepository(EbeanServer ebean, ValidatorFactory validatorFactory) {
        this.ebean = ebean;
        this.validator = validatorFactory.getValidator();
    }

    @Override
    public User createOrUpdate(UserCreateUpdateDto dto) {
        logger.debug("createOrUpdate(): dto = {}", dto.toString());
        User existing = findByDto(dto);
        if (existing == null) {
            return create(dto);
        } else {
            return update(dto, existing.getId());
        }
    }

    @Override
    public User byId(Long id) {
        logger.info("byId(): id = {}", id);
        EbeanRepoUtils.assertEntityExists(ebean, User.class, id);
        return ebean.find(User.class, id);
    }

    @Override
    public void delete(Long id) {
        logger.info("delete(): id = {}", id);
        EbeanRepoUtils.assertEntityExists(ebean, User.class, id);
        int count = ebean.delete(User.class, id);
        if (count != 1) {
            throw new BusinessLogicViolationException("Failed to delete user with id = " + id);
        }
    }

    private User create(UserCreateUpdateDto dto) {
        logger.debug("create(): dto = {}", dto.toString());

        validate(dto);

        User entity = new User();
        entity.setEmail(dto.getEmail());
        entity.setFullName(dto.getFullName());
        entity.setLastUpdate(Instant.now());
        entity.setPicture(dto.getPicture());
        setSocialId(dto, entity);

        ebean.save(entity);

        return entity;
    }

    private User update(UserCreateUpdateDto dto, Long entityId) {
        logger.debug("update(): dto = {}", dto.toString());

        validate(dto);

        User entity = new User();
        entity.setId(entityId);
        entity.setEmail(dto.getEmail());
        entity.setFullName(dto.getFullName());
        entity.setLastUpdate(Instant.now());
        entity.setPicture(dto.getPicture());
        setSocialId(dto, entity);

        ebean.update(entity);

        return entity;
    }

    private User findByDto(UserCreateUpdateDto dto) {
        User existing = findByProperty("email", dto.getEmail());

        if (existing == null && dto.getGoogleUserId() != null) {
            existing = findByProperty("googleUserId", dto.getGoogleUserId());
        }

        if (existing == null && dto.getFacebookUserId() != null) {
            existing = findByProperty("facebookUserId", dto.getFacebookUserId());
        }

        return existing;
    }

    private User findByProperty(String property, String value) {
        return ebean.createQuery(User.class)
                .where()
                .eq(property, value)
                .findOne();
    }

    private void validate(UserCreateUpdateDto dto) {
        Set<ConstraintViolation<UserCreateUpdateDto>> violations = validator.validate(dto);
        if (violations.size() > 0) {
            throw new BusinessLogicViolationException("User dto is invalid!");
        }
    }

    private void setSocialId(UserCreateUpdateDto dto, User entity) {
        if (dto.getFacebookUserId() != null) {
            entity.setFacebookUserId(dto.getFacebookUserId());
        }

        if (dto.getGoogleUserId() != null) {
            entity.setGoogleUserId(dto.getGoogleUserId());
        }
    }
}
