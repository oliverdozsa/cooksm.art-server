package models.repositories.imp;

import io.ebean.EbeanServer;

public class EbeanRepoUtils {
    public static <E> void checkEntity(EbeanServer ebean, Class<E> entityClass, Object id) {
        if (id == null) {
            throw new IllegalArgumentException("id is null!");
        }

        if (ebean.find(entityClass, id) == null) {
            String message = String.format("No entity %s with id %s found", entityClass.getName(), id);
            throw new IllegalArgumentException(message);
        }
    }
}
