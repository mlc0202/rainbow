package com.icitic.core;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.icitic.core.util.TestIntDate;
import com.icitic.core.util.TestPinYin;
import com.icitic.core.util.TestTemplate;
import com.icitic.core.util.TestUtils;
import com.icitic.core.util.ioc.TestIOC;

@RunWith(Suite.class)
@SuiteClasses({ TestUtils.class, TestIntDate.class, TestIOC.class, TestPinYin.class, TestTemplate.class })
public class TestSuite {

}
