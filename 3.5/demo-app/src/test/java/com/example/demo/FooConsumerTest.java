package com.example.demo;

import com.example.library.Bar;
import com.example.library.BarAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(classes = AppConfig.class, properties = {"debug=true"})
@ImportAutoConfiguration({BarAutoConfiguration.class, FooAutoConfiguration.class})
public class FooConsumerTest {

    @MockitoSpyBean
    private Bar bar;

    @Autowired
    ConsumerOfFoo consumerOfFoo;

    @Test
    public void spanishTest() {
        Mockito.when(bar.getGreeting()).thenReturn("Hola");
        String value = consumerOfFoo.sayHello();
        assertThat(value).contains("Hola");
    }
}
