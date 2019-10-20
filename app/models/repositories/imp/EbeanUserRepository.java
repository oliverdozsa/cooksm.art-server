package models.repositories.imp;

import dto.UserCreateUpdateDto;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import models.DatabaseExecutionContext;
import models.entities.User;
import models.repositories.UserRepository;
import models.repositories.exceptions.BusinessLogicViolationException;
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

    @Inject
    public EbeanUserRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext, ValidatorFactory validatorFactory) {
        this.ebean = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
        this.validator = validatorFactory.getValidator();
    }

    @Override
    public CompletionStage<String> create(UserCreateUpdateDto dto) {
        return supplyAsync(() -> {
            Set<ConstraintViolation<UserCreateUpdateDto>> violations = validator.validate(dto);
            if(violations.size() > 0){
                throw new BusinessLogicViolationException("User to create is invalid!");
            }

            EbeanRepoUtils.assertEntityDoesntExist(ebean, User.class, dto.getEmail());

            User entity = new User();
            entity.setEmail(dto.getEmail());
            entity.setFullName(dto.getFullName());
            entity.setLastUpdate(Instant.now());

            ebean.save(entity);

            return entity.getEmail();
        }, executionContext);
    }

    @Override
    public CompletionStage<Void> update(UserCreateUpdateDto dto) {
        return supplyAsync(() -> {
            return null;
        }, executionContext);
    }
}
