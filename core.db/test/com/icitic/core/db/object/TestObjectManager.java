package com.icitic.core.db.object;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.icitic.core.db.internal.ObjectManagerImpl;
import com.icitic.core.db.internal.ObjectNameRule;
import com.icitic.core.extension.ExtensionRegistry;

public class TestObjectManager {

    private static ObjectManager objectManager;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        ExtensionRegistry.registerExtensionPoint(null, ObjectType.class);
        ExtensionRegistry.registerExtension(null, ObjectType.class, new DeviceManager());
        ExtensionRegistry.registerExtension(null, ObjectType.class, new CodeManager());
        objectManager = new ObjectManagerImpl();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testTranslateListOfObject() {
        List<Person> list = ImmutableList.of(new Person("韦小宝", 1, "1"), new Person("陈近南", 2, "1"), new Person("阿珂", 3,
                "2"));
        List<ObjectNameRule> rules = objectManager.getObjectNameRule(Person.class);
        objectManager.listSetName(list, rules);
        Person person = list.get(0);
        assertEquals("iPod", person.getDeviceName());
        assertEquals("男", person.getGender());
        person = list.get(1);
        assertEquals("iPhone", person.getDeviceName());
        assertEquals("男", person.getGender());
        person = list.get(2);
        assertEquals("iPad", person.getDeviceName());
        assertEquals("女", person.getGender());
    }

}
