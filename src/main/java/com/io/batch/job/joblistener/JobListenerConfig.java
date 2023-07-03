package com.io.batch.job.joblistener;

import com.io.batch.job.logger.JobLoggerListener;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * 콘솔에 "Hello World Spring Batch" 로그를 찍는 잡
 * run: --spring.batch.job.names=jobListenerJob
 */
@Configuration
@RequiredArgsConstructor
public class JobListenerConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job jobListenerJob(Step jobListenerStep) {
        return jobBuilderFactory.get("jobListenerJob") // 이 Job의 이름을 지정
                .incrementer(new RunIdIncrementer()) // Job의 식별자인 id값 자동 증가
                .listener(new JobLoggerListener()) // Job의 리스너 등록
                .start(jobListenerStep) // 실행될 Step을 지정
                .build();
    }

    @Bean
    @JobScope
    public Step jobListenerStep(Tasklet jobListenerTasklet) {
        return stepBuilderFactory.get("jobListenerStep") // 이 Step의 이름을 지정
                .tasklet(jobListenerTasklet) // Step 이하의 작업 단위인 tasklet 지정
                .build();
    }

    @Bean
    @StepScope
    public Tasklet jobListenerTasklet() {
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution,
                                        ChunkContext chunkContext) throws Exception {
                System.out.println("Job Listener Tasklet");
                return RepeatStatus.FINISHED;
            }
        };
    }
}
