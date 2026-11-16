package com.github.nicolaskrier.experimental.mistral.ai.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.session.SessionService;
import org.springframework.ai.session.advisor.SessionAdvisors;
import org.springframework.ai.session.compaction.TurnCountTrigger;
import org.springframework.ai.session.compaction.WindowCompactionStrategy;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

@SpringBootApplication
class ToolsExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(ToolsExample.class);

    private static final String SEARCHED_POPE_KEY = "searched_pope";

    private static final String SESSION_ID = UUID.randomUUID().toString();

    @Value("classpath:/prompts/system-prompt.txt")
    private Resource systemPromptResource;

    @Value("classpath:/prompts/user-templated-prompt.txt")
    private Resource userTemplatedPromptResource;

    @Value("classpath:/prompts/user-prompt.txt")
    private Resource userPromptResource;

    @Value("${searched-pope:current}")
    private String searchedPope;

    @Value("${previous-searched-popes-number:0}")
    private int previousSearchedPopesNumber;

    @Bean
    ToolCallback currentDateTimeToolCallback() {
        return FunctionToolCallback.builder("currentDateTime", () -> Instant.now().toString())
                .description("Get current date and time with UTC time zone in the ISO-8601 calendar system.")
                .build();
    }

    @Bean
    ChatClient chatClient(ChatClient.Builder chatClientBuilder, SessionService sessionService, PopeSearchTools popeSearchTools, ToolCallback currentDateTimeToolCallback) {
        var sessionAdvisors = SessionAdvisors.builder(sessionService)
                .compaction(new TurnCountTrigger(4), WindowCompactionStrategy.builder().lastTurns(2).build())
                .build();

        return chatClientBuilder.defaultSystem(systemPromptResource)
                .defaultTools(currentDateTimeToolCallback, popeSearchTools)
                .defaultAdvisors(sessionAdvisors)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    @Bean
    ApplicationRunner applicationRunner(ChatClient chatClient) {
        return _ -> {
            searchPope(chatClient);
            searchPreviousPopes(chatClient);
        };
    }

    private void searchPope(ChatClient chatClient) {
        var prompt = PromptTemplate.builder()
                .resource(userTemplatedPromptResource)
                .variables(Map.of(SEARCHED_POPE_KEY, searchedPope))
                .build()
                .create();
        var chatClientRequestSpec = chatClient.prompt(prompt);
        var pope = searchPope(chatClientRequestSpec);
        LOGGER.info("The {} pope is: {}", searchedPope, pope);
    }

    private void searchPreviousPopes(ChatClient chatClient) {
        IntStream.range(0, previousSearchedPopesNumber).forEach(_ -> {
            var chatClientRequestSpec = chatClient.prompt().user(userPromptResource);
            var pope = searchPope(chatClientRequestSpec);
            LOGGER.info("The previous pope is: {}", pope);
        });
    }

    private static Pope searchPope(ChatClient.ChatClientRequestSpec chatClientRequestSpec) {
        return chatClientRequestSpec.advisors(SessionAdvisors.session(SESSION_ID))
                .call()
                .entity(Pope.class, entityParamSpec -> entityParamSpec.useProviderStructuredOutput().validateSchema());
    }

    static void main(String[] args) {
        SpringApplication.run(ToolsExample.class, args);
    }

}
