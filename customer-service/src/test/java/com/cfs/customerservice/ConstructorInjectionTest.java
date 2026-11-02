package com.cfs.customerservice;

import com.cfs.customerservice.controller.AdminKycController;
import com.cfs.customerservice.controller.CustomerController;
import com.cfs.customerservice.service.AdminKycServiceImpl;
import com.cfs.customerservice.service.CustomerServiceImpl;
import com.cfs.customerservice.service.KycReadinessEvaluator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ConstructorInjectionTest {
    @ParameterizedTest
    @ValueSource(classes = {CustomerServiceImpl.class, AdminKycServiceImpl.class,
            KycReadinessEvaluator.class, CustomerController.class, AdminKycController.class})
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
