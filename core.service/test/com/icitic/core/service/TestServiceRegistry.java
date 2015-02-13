package com.icitic.core.service;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.icitic.core.service.internal.ServiceRegistry;

public class TestServiceRegistry {
	
	private ServiceRegistry registry;
	
	@Before
	public void setUp() {
		registry = new ServiceRegistry();
	}

	@Test
	public void testFindServiceId() {
		assertEquals("scheduler.wait", registry.findServiceId("com.icitic.scheduler.wait.api.WaitService", true));
		assertEquals("scheduler.wait", registry.findServiceId("com.icitic.scheduler.wait.impl.WaitServiceImpl", false));
		assertEquals("scheduler.wait", registry.findServiceId("com.icitic.scheduler.api.wait.WaitService", true));
		assertEquals("scheduler.wait", registry.findServiceId("com.icitic.scheduler.impl.wait.WaitServiceImpl", false));
	}

}
