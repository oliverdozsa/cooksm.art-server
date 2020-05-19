package data.repositories.imp;

import io.ebean.EbeanServer;
import data.repositories.exceptions.NotFoundException;

class EbeanRepoUtils {
    public static <E> void assertEntityExists(EbeanServer ebean, Class<E> entityClass, Object id) {
        if (id == null) {
            throw new IllegalArgumentException("id is null!");
        }

        if (ebean.find(entityClass, id) == null) {
            String message = String.format("No such entity %s (%s) found!", entityClass.getName(), id);
            throw new NotFoundException(message);
        }
    }

    public static <E> void assertEntityDoesntExist(EbeanServer ebean, Class<E> entityClass, Object id) {
        if (id == null) {
            throw new IllegalArgumentException("id is null!");
        }

        if (ebean.find(entityClass, id) != null) {
            String message = String.format("Unexpected entity %s (%s) found!", entityClass.getName(), id);
            throw new NotFoundException(message);
        }
    }
}
