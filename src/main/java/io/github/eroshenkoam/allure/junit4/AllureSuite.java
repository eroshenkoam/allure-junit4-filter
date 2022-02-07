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

import io.qameta.allure.testfilter.FileTestPlanSupplier;
import io.qameta.allure.testfilter.TestPlan;
import io.qameta.allure.testfilter.TestPlanUnknown;
import io.qameta.allure.testfilter.TestPlanV1_0;
import org.junit.Test;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author eroshenkoam (Artem Eroshenko).
 */
public class AllureSuite extends Suite {

    private static final char UNIX_SEPARATOR = '/';
    private static final char DOT_SYMBOL = '.';

    private static final String CLASS_SUFFIX = ".class";
    private static final String FALLBACK_CLASSPATH_PROPERTY = "java.class.path";

    public AllureSuite(final Class<?> klass, final RunnerBuilder builder) throws InitializationError {
        super(builder, klass, findAllTestClasses());
        final TestPlan testPlan = new FileTestPlanSupplier().supply().orElse(new TestPlanUnknown());

        if (testPlan instanceof TestPlanV1_0) {
            final Filter filter = new AllureFilter((TestPlanV1_0) testPlan);
            try {
                filter(filter);
            } catch (NoTestsRemainException e) {
                throw new InitializationError(e);
            }

        }
    }

    private static Class<?>[] findAllTestClasses() {
        final List<Path> classRoots = splitClassPath(getClasspath()).stream()
                .map(Paths::get)
                .filter(AllureSuite::isNotJar)
                .collect(Collectors.toList());
        final List<String> classFiles = new ArrayList<>();

        for (final Path classRoot : classRoots) {
            classFiles.addAll(getRelativeClassFiles(classRoot));
        }

        final List<String> classNames = classFiles.stream()
                .map(AllureSuite::classNameFromFile)
                .collect(Collectors.toList());

        final List<Class<?>> classes = classNames.stream()
                .map(AllureSuite::readClass)
                .filter(AllureSuite::isTestClass)
                .collect(Collectors.toList());

        return classes.toArray(new Class[]{});
    }

    private static List<String> getRelativeClassFiles(final Path root) {
        try {
            return Files.walk(root)
                    .filter(Files::isRegularFile)
                    .filter(Files::isReadable)
                    .filter(AllureSuite::isNotInnerClass)
                    .filter(AllureSuite::isClassFile)
                    .map(root::relativize)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private static String classNameFromFile(final String classFileName) {
        final String result = replaceFileSeparators(cutOffExtension(classFileName));
        return result.charAt(0) == '.' ? result.substring(1) : result;
    }

    private static boolean isTestClass(final Class<?> clazz) {
        if (isAbstractClass(clazz)) {
            return false;
        }
        return hasInheritanceTestMethods(clazz);
    }

    private static boolean hasInheritanceTestMethods(final Class<?> clazz) {
        Class<?> possibleClass = clazz;
        while (possibleClass != null) {
            if (hasTestMethods(possibleClass)) {
                return true;
            }
            possibleClass = possibleClass.getSuperclass();
        }
        return false;
    }

    private static boolean hasTestMethods(final Class<?> clazz) {
        return Arrays.stream(clazz.getMethods())
                .anyMatch(method -> method.isAnnotationPresent(Test.class));
    }

    private static boolean isAbstractClass(final Class<?> clazz) {
        return (clazz.getModifiers() & Modifier.ABSTRACT) != 0;
    }

    private static Class<?> readClass(final String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private static boolean isNotInnerClass(final Path classFilePath) {
        return !classFilePath.getFileName().toString().contains("$");
    }

    private static boolean isClassFile(final Path classFilePath) {
        return classFilePath.getFileName().toString().endsWith(CLASS_SUFFIX);
    }

    private static boolean isNotJar(final Path classRoot) {
        final String rootName = classRoot.getFileName().toString();
        return !rootName.endsWith(".jar") && !rootName.endsWith(".JAR");
    }

    private static String getClasspath() {
        return System.getProperty(FALLBACK_CLASSPATH_PROPERTY);
    }

    private static List<String> splitClassPath(final String classPath) {
        final String separator = System.getProperty("path.separator");
        return Arrays.asList(classPath.split(separator));
    }

    private static String replaceFileSeparators(final String s) {
        String result = s.replace(File.separatorChar, DOT_SYMBOL);
        if (File.separatorChar != UNIX_SEPARATOR) {
            result = result.replace(UNIX_SEPARATOR, DOT_SYMBOL);
        }
        return result;
    }

    private static String cutOffExtension(final String classFileName) {
        return classFileName.substring(0, classFileName.length() - CLASS_SUFFIX.length());
    }

}
