package data.repositories.imp;

import lombokized.dto.UserCreateUpdateDto;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import data.DatabaseExecutionContext;
import data.entities.User;
import data.repositories.UserRepository;
import data.repositories.exceptions.BusinessLogicViolationException;
import play.Logger;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class EbeanUserRepository implements UserRepository {
    private EbeanServer ebean;
    private DatabaseExecutionContext executionContext;
    private Validator validator;

    private static final Logger.ALogger logger = Logger.of(EbeanUserRepository.class);

    @Inject
    public EbeanUserRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext, ValidatorFactory validatorFactory) {
        this.ebean = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
        this.validator = validatorFactory.getValidator();
    }

    @Override
    public CompletionStage<User> createOrUpdate(UserCreateUpdateDto dto) {
        logger.debug("createOrUpdate(): dto = {}", dto.toString());
        return supplyAsync(() -> {
            User existing = findByDto(dto);
            if (existing == null) {
                return create(dto);
            } else {
                return update(dto, existing.getId());
            }
        }, executionContext);
    }

    @Override
    public CompletionStage<User> byId(Long id) {
        return supplyAsync(() -> {
            EbeanRepoUtils.assertEntityExists(ebean, User.class, id);
            return ebean.find(User.class, id);
        }, executionContext);
    }

    private User create(UserCreateUpdateDto dto) {
        logger.debug("create(): dto = {}", dto.toString());

        validate(dto);

        User entity = new User();
        entity.setEmail(dto.getEmail());
        entity.setFullName(dto.getFullName());
        entity.setLastUpdate(Instant.now());
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
