package com.gym.crm.bdd.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.DefaultDataTableCellTransformer;
import io.cucumber.java.DefaultDataTableEntryTransformer;
import io.cucumber.java.DefaultParameterTransformer;

import java.lang.reflect.Type;

public class CucumberJacksonConfigurer {

    @DefaultParameterTransformer
    @DefaultDataTableEntryTransformer
    @DefaultDataTableCellTransformer
    public Object transform(Object fromValue, Type toValueType) {
        ObjectMapper objectMapper = CucumberObjectMapper.instance();

        return objectMapper.convertValue(fromValue, objectMapper.constructType(toValueType));
    }

}
