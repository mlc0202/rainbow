package com.icitic.core.platform;

import java.util.Map;

import com.google.common.base.Predicate;
import com.icitic.core.bundle.Bundle;
import com.icitic.core.bundle.BundleData;
import com.icitic.core.util.XmlBinder;

public interface BundleLoader {

    public static final XmlBinder<BundleData> binder = new XmlBinder<BundleData>(BundleData.class);

    public Map<String, Bundle> loadBundle(Predicate<String> exist);

}
