package com.yoloo.backend.util;

import com.googlecode.objectify.impl.translate.opt.joda.JodaTimeTranslators;
import com.googlecode.objectify.util.Closeable;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.yoloo.backend.util.TestObjectifyService.fact;

@RunWith(JUnit4.class)
public class TestBase extends GAETestBase {

    private Closeable rootService;

    @Before
    public void setUp() {
        this.setUpObjectifyFactory(new TestObjectifyFactory());
        JodaTimeTranslators.add(fact());
    }

    @After
    public void tearDown() {
        rootService.close();
        rootService = null;
    }

    protected void setUpObjectifyFactory(TestObjectifyFactory factory) {
        if (rootService != null) {
            rootService.close();
        }

        TestObjectifyService.setFactory(factory);
        rootService = TestObjectifyService.begin();
    }
}
