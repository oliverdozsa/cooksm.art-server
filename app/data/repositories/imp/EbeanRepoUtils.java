package data.repositories.imp;

import io.ebean.EbeanServer;
import data.repositories.exceptions.NotFoundException;

import java.util.Collection;
import java.util.List;

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

    public static <E> void assertEntitiesExist(
            EbeanServer ebean, Class<E> entityClass, String idField, Collection<?> ids) {
        if(ids == null || ids.size() <= 0) {
            throw new IllegalArgumentException("ids is invalid!");
        }

        int count = ebean.createQuery(entityClass)
                .where()
                .in(idField, ids)
                .findCount();

        if(count != ids.size()) {
            String message = String.format("Not all entities exist %s (%s)!", entityClass.getName(), ids);
            throw new NotFoundException(message);
        }
    }
}
