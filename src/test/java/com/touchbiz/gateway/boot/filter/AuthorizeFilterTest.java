package com.touchbiz.gateway.boot.filter;

import com.touchbiz.gateway.ApplicationTest;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

public class AuthorizeFilterTest extends ApplicationTest {

    @Autowired
    private AuthorizeFilter authorizeFilter;

    @Getter
    @Autowired
    private WebApplicationContext context;

    @Test
    public void filter() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
                .webAppContextSetup(getContext())
                 .build();
        mockMvc.perform(MockMvcRequestBuilders
                .post("/api/v1/test")
                .accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .param("username","admin")
                .param("password","admin123"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }
}