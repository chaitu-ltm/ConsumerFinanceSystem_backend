package com.cfs.auth;

import com.cfs.auth.config.DataInitializer;
import com.cfs.auth.controller.AdminUserController;
import com.cfs.auth.service.AuthServiceImpl;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ConstructorInjectionTests {
    @ParameterizedTest
    @ValueSource(classes = {AuthServiceImpl.class, DataInitializer.class, AdminUserController.class})
    void springResolvesGeneratedConstructorAndInjectsFinalDependencies(Class<?> type) {
        var fields = Arrays.stream(type.getDeclaredFields())
                .filter(field -> Modifier.isFinal(field.getModifiers())
                        && !Modifier.isStatic(field.getModifiers())).toList();
        assertEquals(1, type.getConstructors().length);
        assertArrayEquals(fields.stream().map(field -> field.getType()).toArray(Class<?>[]::new),
                type.getConstructors()[0].getParameterTypes());
        try (var context = new AnnotationConfigApplicationContext()) {
            for (var field : fields) {
                context.getBeanFactory().registerSingleton(field.getName(), mock(field.getType()));
            }
            context.register(type);
            context.refresh();
            var bean = context.getBean(type);
            for (var field : fields) {
                assertSame(context.getBean(field.getName()), ReflectionTestUtils.getField(bean, field.getName()));
            }
        }
    }
}
