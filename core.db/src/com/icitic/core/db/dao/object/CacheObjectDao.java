package com.icitic.core.db.dao.object;

import com.icitic.core.cache.CacheConfig;
import com.icitic.core.cache.CacheManager;
import com.icitic.core.model.object.IIdObject;
import com.icitic.core.util.ioc.DisposableBean;
import com.icitic.core.util.ioc.Inject;

/**
 * 封装一个具体对象的数据库操作类，该对象实现IIdObject接口。
 * 
 * 派生类必须由Context容器管理
 * 
 * @author lijinghui
 * 
 * @param <I>
 * @param <T>
 */
public abstract class CacheObjectDao<I, T extends IIdObject<I>> extends IdDao<I, T> implements DisposableBean {

    protected CacheManager cacheManager;

    @Inject
    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    protected CacheObjectDao(Class<T> clazz) {
        super(clazz);
    }

    protected String getCacheName() {
        return getEntityName();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        createCache();
    }

    protected abstract void createCache();

    public CacheConfig getCacheConfig() {
        return null;
    }

    @Override
    public void destroy() throws Exception {
        cacheManager.destoryCache(getCacheName());
    }

}