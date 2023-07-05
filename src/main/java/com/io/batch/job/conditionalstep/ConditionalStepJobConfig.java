package com.io.batch.job.conditionalstep;

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
 * step을 조건으로 분기할 때 사용하는 방법
 * run: --spring.batch.job.names=conditionalStepJob
 */
@Configuration
@RequiredArgsConstructor
public class ConditionalStepJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    // 잡
    @Bean
    public Job conditionalStepJob(Step conditionalStepStep,
                                  Step conditionalAllStepStep,
                                  Step conditionalCompliteStepStep,
                                  Step conditionalFailStepStep) {
        return jobBuilderFactory.get("conditionalStepJob") // 이 Job의 이름을 지정
                .incrementer(new RunIdIncrementer()) // Job의 식별자인 id값 자동 증가
                .start(conditionalStepStep)
                    .on("FAILED").to(conditionalFailStepStep)
                .from(conditionalStepStep)
                    .on("COMLETED").to(conditionalCompliteStepStep)
                .from(conditionalStepStep)
                    .on("*").to(conditionalAllStepStep)
                .end()
                .build();
    }

    // 스텝 1
    @Bean
    @JobScope
    public Step conditionalStepStep() {
        return stepBuilderFactory.get("conditionalStepStep")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("First Step Start");
                        return RepeatStatus.FINISHED;
                    }
                })
                .build();
    }

    // 스텝 전체
    @Bean
    @JobScope
    public Step conditionalAllStepStep() {
        return stepBuilderFactory.get("conditionalAllStepStep")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("conditionalAllStepStep Start");
                        return RepeatStatus.FINISHED;
                    }
                })
                .build();
    }

    // 스텝 성공
    @Bean
    @JobScope
    public Step conditionalCompliteStepStep() {
        return stepBuilderFactory.get("conditionalCompliteStepStep")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("conditionalCompliteStepStep Start");
                        return RepeatStatus.FINISHED;
                    }
                })
                .build();
    }

    // 스텝 실패
    @Bean
    @JobScope
    public Step conditionalFailStepStep() {
        return stepBuilderFactory.get("conditionalFailStepStep")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("conditionalFailStepStep Start");
                        return RepeatStatus.FINISHED;
                    }
                })
                .build();
    }

}
