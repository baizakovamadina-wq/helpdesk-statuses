package com.example.helpdesk.config;

import com.example.helpdesk.domain.HoldRule;
import com.example.helpdesk.domain.Rule;
import com.example.helpdesk.domain.TransitionRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RulesConfig {

    @Bean
    public Rule rule() {
        Rule transitionRule = new TransitionRule();
        Rule holdRule = new HoldRule();

        return (from, to) -> {
            transitionRule.check(from, to);
            holdRule.check(from, to);
        };
    }
}