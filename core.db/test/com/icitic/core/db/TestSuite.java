package com.icitic.core.db;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.icitic.core.db.dao.TestDao;
import com.icitic.core.db.dao.TestNeoBean;
import com.icitic.core.db.incrementer.TestMaxIdIncrementer;
import com.icitic.core.db.incrementer.TestTableIncrementer;
import com.icitic.core.db.object.TestObjectManager;

@RunWith(Suite.class)
@SuiteClasses({ TestNeoBean.class, TestDao.class, TestMaxIdIncrementer.class, TestTableIncrementer.class,
		TestObjectManager.class })
public class TestSuite {

}
