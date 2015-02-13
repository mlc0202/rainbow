package com.icitic.core.bundle;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.icitic.core.platform.BundleAncestor;
import com.icitic.core.platform.ProjectClassLoader;

public class TestBundleAncestor {

	private Bundle[] bundles = new Bundle[10];
	private BundleAncestor ancestor;

	@Before
	public void setup() throws IOException {
		BundleClassLoader bc = new ProjectClassLoader(new File("."));
		for (int i = 0; i < 10; i++) {
			BundleData data = new BundleData();
			data.setId(Integer.toString(i));
			bundles[i] = new Bundle(data, bc);
		}
		ancestor = new BundleAncestor();
	}

	@Test
	public void test1() {
		bundles[0].setParents(ImmutableList.of(bundles[1], bundles[2]));

		ancestor.addParent(bundles[1]);
		ancestor.addParent(bundles[0]);
		
		List<Bundle> parents = ancestor.getParents();
		assertEquals(1, parents.size());
		assertSame(bundles[0], parents.get(0));
		List<Bundle> ancestors = ancestor.getAncestors();
		assertEquals(3, ancestors.size());
		assertSame(bundles[2], ancestors.get(0));
		assertSame(bundles[1], ancestors.get(1));
		assertSame(bundles[0], ancestors.get(2));
	}

	@Test
	public void test2() {
		bundles[1].setParents(ImmutableList.of(bundles[4]));
		bundles[2].setParents(ImmutableList.of(bundles[3]));
		bundles[3].setParents(ImmutableList.of(bundles[4]));
		bundles[4].setParents(ImmutableList.of(bundles[5]));
		bundles[0].setParents(ImmutableList.of(bundles[3]));
		
		ancestor.addParent(bundles[1]);
		ancestor.addParent(bundles[2]);
		ancestor.addParent(bundles[5]);
		
		List<Bundle> parents = ancestor.getParents();
		assertEquals(2, parents.size());
		assertTrue(parents.contains(bundles[1]));
		assertTrue(parents.contains(bundles[2]));
		List<Bundle> ancestors = ancestor.getAncestors();
		assertEquals(5, ancestors.size());
		assertSame(bundles[5], ancestors.get(0));
		assertSame(bundles[4], ancestors.get(1));
		assertSame(bundles[3], ancestors.get(2));
		assertTrue(ancestors.contains(bundles[1]));
		assertTrue(ancestors.contains(bundles[2]));
	}
}
