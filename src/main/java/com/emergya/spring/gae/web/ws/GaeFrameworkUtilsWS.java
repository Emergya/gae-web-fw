package com.emergya.spring.gae.web.ws;

import com.emergya.spring.gae.data.dao.BaseDao;
import com.emergya.spring.gae.data.model.BaseEntity;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
     * @param entityClassName
     * @return
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    @RequestMapping("updateSearchIndex")
    public Map<String, Object> updateSearchIndex(@RequestParam() String entityClassName)
            throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        Class<? extends BaseEntity> entityClass = (Class<? extends BaseEntity>) Class.forName(entityClassName);
        Class<? extends BaseDao> daoClass = BaseDao.getDaoForEntity(entityClass);

        int count = 0;
        BaseDao<BaseEntity> dao = daoClass.getConstructor().newInstance();
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
