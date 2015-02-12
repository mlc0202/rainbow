package com.icitic.core.db.incrementer;

import java.util.concurrent.atomic.AtomicInteger;

import com.icitic.core.db.dao.Dao;
import com.icitic.core.db.dao.SqlBuilder;

/**
 * 简单的Incrementer，使用这个Incrementer的代码不支持集群部署
 * 
 * @author lijinghui
 *
 */
public class SimpleIncrementer extends AbstractIncrementer {

    private AtomicInteger seed;
    
	public SimpleIncrementer(Dao dao, String entityName) {
		int value = dao.queryForInt(SqlBuilder.select("max(id)").from(entityName));
        seed = new AtomicInteger(value + 1);
	}

	@Override
	protected long getNextKey() {
	    return seed.getAndIncrement();
	}
}
