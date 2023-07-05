package com.io.batch.job.multistep;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * step이 2개 이상일 때 사용하는 방법
 * run: --spring.batch.job.names=multiStepJob
 */
@Configuration
@RequiredArgsConstructor
public class MultiStepJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    // 잡
    @Bean
    public Job multiStepJob(Step multiStepStep1,

                            Step multiStepStep2,
                            Step multiStepStep3 ) {
        return jobBuilderFactory.get("multiStepJob") // 이 Job의 이름을 지정
                .incrementer(new RunIdIncrementer()) // Job의 식별자인 id값 자동 증가
                .start(multiStepStep1) // 실행될 Step을 지정
                .next(multiStepStep2)
                .next(multiStepStep3)
                .build();
    }

    // 스텝 1
    @Bean
    @JobScope
    public Step multiStepStep1() {
        return stepBuilderFactory.get("multiStepStep1")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("First Step Start");

                        return RepeatStatus.FINISHED;
                    }
                })
                .build();
    }

    // 스텝 2
    @Bean
    @JobScope
    public Step multiStepStep2() {
        return stepBuilderFactory.get("multiStepStep2")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("Second Step Start");

                        return RepeatStatus.FINISHED;
                    }
                })
                .build();
    }

    // 스텝 3
    @Bean
    @JobScope
    public Step multiStepStep3() {
        return stepBuilderFactory.get("multiStepStep3")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("Third Step Start");

                        return RepeatStatus.FINISHED;
                    }
                })
                .build();
    }
}
