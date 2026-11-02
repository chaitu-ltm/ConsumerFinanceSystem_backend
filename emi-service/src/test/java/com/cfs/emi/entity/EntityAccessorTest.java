package com.cfs.emi.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class EntityAccessorTest {

    @Test
    void loanAccountRetainsPublicNoArgAndExactAccessorSurface() throws Exception {
        verifyAccessors(LoanAccount.class, 14);
    }

    @Test
    void installmentRetainsPublicNoArgAndExactAccessorSurface() throws Exception {
        verifyAccessors(EmiInstallment.class, 11);
    }

    @Test
    void repaymentRetainsPublicNoArgAndExactAccessorSurface() throws Exception {
        verifyAccessors(Repayment.class, 10);
    }

    private void verifyAccessors(Class<?> type, int fieldCount) throws Exception {
        Object entity = type.getConstructor().newInstance();
        assertEquals(1, type.getConstructors().length);
        assertEquals(0, type.getConstructors()[0].getParameterCount());
        List<Field> fields = Arrays.stream(type.getDeclaredFields())
                .filter(field -> !field.isSynthetic() && !Modifier.isStatic(field.getModifiers()))
                .toList();
        assertEquals(fieldCount, fields.size());
        assertEquals(fieldCount * 2, Arrays.stream(type.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers())).count());

        for (Field field : fields) {
            String suffix = Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
            var getter = type.getMethod("get" + suffix);
            var setter = type.getMethod("set" + suffix, field.getType());
            assertEquals(field.getType(), getter.getReturnType());
            assertEquals(void.class, setter.getReturnType());
            assertNull(getter.invoke(entity), field.getName());
            Object value = sample(field.getType());
            setter.invoke(entity, value);
            assertSame(value, getter.invoke(entity), field.getName());
            setter.invoke(entity, new Object[]{null});
            assertNull(getter.invoke(entity), field.getName());
        }

        for (String method : List.of("equals", "hashCode", "toString")) {
            assertTrue(Arrays.stream(type.getDeclaredMethods()).noneMatch(m -> m.getName().equals(method)));
        }
    }

    private Object sample(Class<?> type) {
        if (type == Long.class) return 42L;
        if (type == Integer.class) return 12;
        if (type == String.class) return "reference";
        if (type == BigDecimal.class) return new BigDecimal("123.4500");
        if (type == LocalDate.class) return LocalDate.of(2026, 11, 2);
        if (type == LocalDateTime.class) return LocalDateTime.of(2026, 11, 2, 12, 30);
        if (type.isEnum()) return type.getEnumConstants()[0];
        throw new AssertionError("Unexpected field type: " + type);
    }
}
