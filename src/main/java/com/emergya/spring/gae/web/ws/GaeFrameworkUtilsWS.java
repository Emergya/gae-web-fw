package com.emergya.spring.gae.web.ws;

import com.emergya.spring.gae.data.dao.BaseDao;
import com.emergya.spring.gae.data.model.BaseEntity;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * WARNING: This request path must be protected for SUPERADMIN roles in the apps Spring Security config.
 *
 * @author lroman
 */
@RestController
@RequestMapping("_gae_fw")
public class GaeFrameworkUtilsWS extends BaseRestWebService {

    /**
     * Allows to refresh the search index associated to an entity.
     *
     * Useful after schema changes.
     *
     *
     * @param entityClassName the full name of the entity class whose related index is to be updated.
     * @return A map containing information about the updated index
     * @throws ClassNotFoundException the provided class name doesn't exist in the classpath
     * @throws NoSuchMethodException a valid default constructor for the entity's dao class doesn't exist
     */
    @RequestMapping("updateSearchIndex")
    public final Map<String, Object> updateSearchIndex(@RequestParam() String entityClassName)
            throws ClassNotFoundException, NoSuchMethodException {

        Class<? extends BaseEntity> entityClass = (Class<? extends BaseEntity>) Class.forName(entityClassName);
        Class<? extends BaseDao> daoClass = BaseDao.getDaoForEntity(entityClass);

        int count = 0;
        BaseDao<BaseEntity> dao;
        try {
            dao = daoClass.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(GaeFrameworkUtilsWS.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        List<BaseEntity> list = dao.list();
        for (BaseEntity e : list) {
            dao.save(e); // This will recreate the index.
            count++;
        }

        HashMap<String, Object> result = new HashMap<>();

        result.put("reindexedClass", entityClass);
        result.put("reindexedEntitiesCount", count);
        result.put("success", true);

        return result;
    }
}
