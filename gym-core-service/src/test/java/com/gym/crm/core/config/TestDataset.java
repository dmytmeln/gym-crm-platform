package com.gym.crm.core.config;

import org.springframework.test.context.jdbc.Sql;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(TYPE)
@Inherited
@Sql(scripts = {"classpath:datasets/cleanup-all.sql", "classpath:datasets/seed-data.sql"})
public @interface TestDataset {
}
