/*
 *
 * QametaSoftware OÜ
 * ______________
 *
 * This software is copyrighted. It may only be used for the purposes stipulated
 * in the license agreement.
 * Any usage without QametaSoftware OÜ consent is inadmissible and liable to prosecution.
 *
 * QametaSoftware OÜ has installed the copyright notice stipulated herein
 * in the computer program. In the event of a user being authorized to duplicate
 * the computer program, this copyright notice always is to be included.
 * The copyright notice must neither be changed nor destroyed.
 *
 * All rights reserved.
 * This program is globally copyrighted Copyright (C) 2020 QametaSoftware OÜ
 */
package io.github.eroshenkoam.allure.junit4;

import io.qameta.allure.AllureId;
import io.qameta.allure.testfilter.TestPlanV1_0;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

import java.util.Objects;
import java.util.Optional;

/**
 * @author eroshenkoam (Artem Eroshenko).
 */
public class AllureFilter extends Filter {

    private final TestPlanV1_0 testPlan;

    public AllureFilter(final TestPlanV1_0 testPlan) {
        this.testPlan = testPlan;
    }

    @Override
    public String describe() {
        final Object[] ids = testPlan.getTests().stream()
                .map(TestPlanV1_0.TestCase::getId).toArray();
        return String.format("include ids: %s", ids);
    }

    @Override
    public boolean shouldRun(final Description description) {
        if (Objects.isNull(description.getMethodName())) {
            return true;
        }
        final Optional<String> allureId = Optional
                .ofNullable(description.getAnnotation(AllureId.class))
                .map(AllureId::value);
        final String selector = getSelector(description.getClassName(), description.getMethodName());
        if (allureId.isPresent()) {
            final String id = allureId.get();
            return testPlan.getTests().stream()
                    .map(TestPlanV1_0.TestCase::getId)
                    .anyMatch(id::equals);
        } else {
            return testPlan.getTests().stream()
                    .map(TestPlanV1_0.TestCase::getSelector)
                    .anyMatch(selector::equals);
        }
    }

    private String getSelector(final String className, final String methodName) {
        return String.format("%s.%s",
                className,
                methodName);
    }

}
