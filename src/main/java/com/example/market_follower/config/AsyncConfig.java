package com.example.market_follower.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 50MB 응답을 고려한 보수적인 설정
        executor.setCorePoolSize(2);        // 코어 스레드 수 감소 (메모리 고려)
        executor.setMaxPoolSize(5);         // 최대 스레드 수 감소
        executor.setQueueCapacity(10);      // 큐 용량 감소 (대용량 응답 고려)

        executor.setThreadNamePrefix("CandleAsync-");
        executor.setAwaitTerminationSeconds(120);  // 대용량 처리 시간 고려하여 증가
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 대용량 처리를 위한 긴 유휴 시간
        executor.setKeepAliveSeconds(300);   // 5분으로 증가
        executor.setAllowCoreThreadTimeOut(false);  // 코어 스레드 유지

        // 큐가 가득 찰 경우 호출자 스레드에서 실행
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        return executor;
    }

    // DB 조회 전용 스레드 풀 (대용량 응답 최적화)
    @Bean(name = "dbQueryExecutor")
    public Executor dbQueryExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // DB 조회는 더 보수적으로 설정
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(5);
        executor.setThreadNamePrefix("DbQuery-");
        executor.setAwaitTerminationSeconds(180);   // 3분
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setKeepAliveSeconds(600);          // 10분

        executor.initialize();
        return executor;
    }
}