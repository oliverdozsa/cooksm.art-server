package models.repositories.imp;

import lombokized.dto.UserCreateUpdateDto;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import models.DatabaseExecutionContext;
import models.entities.User;
import models.repositories.UserRepository;
import models.repositories.exceptions.BusinessLogicViolationException;
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
    public CompletionStage<Long> createOrUpdate(UserCreateUpdateDto dto) {
        logger.debug("createOrUpdate(): dto = {}", dto.toString());
        return supplyAsync(() -> {
            User existing = findByEmail(dto.getEmail());
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

    private Long create(UserCreateUpdateDto dto) {
        logger.debug("create(): dto = {}", dto.toString());

        validate(dto);

        User entity = new User();
        entity.setEmail(dto.getEmail());
        entity.setFullName(dto.getFullName());
        entity.setLastUpdate(Instant.now());

        ebean.save(entity);

        return entity.getId();
    }

    private Long update(UserCreateUpdateDto dto, Long entityId) {
        logger.debug("update(): dto = {}", dto.toString());

        validate(dto);

        User entity = new User();
        entity.setId(entityId);
        entity.setEmail(dto.getEmail());
        entity.setFullName(dto.getFullName());
        entity.setLastUpdate(Instant.now());

        ebean.update(entity);

        return entity.getId();
    }

    private User findByEmail(String email) {
        return ebean.createQuery(User.class)
                .where()
                .eq("email", email)
                .findOne();

    }

    private void validate(UserCreateUpdateDto dto){
        Set<ConstraintViolation<UserCreateUpdateDto>> violations = validator.validate(dto);
        if (violations.size() > 0) {
            throw new BusinessLogicViolationException("User dto is invalid!");
        }
    }
}
