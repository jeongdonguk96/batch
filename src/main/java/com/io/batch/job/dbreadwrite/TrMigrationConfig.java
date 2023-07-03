package com.io.batch.job.dbreadwrite;

import com.io.batch.domain.Accounts;
import com.io.batch.domain.Orders;
import com.io.batch.job.logger.JobLoggerListener;
import com.io.batch.repository.AccountsRepository;
import com.io.batch.repository.OrdersRepository;
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
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * 주문 테이블(orders) -> 정산 테이블(accounts) 이관
 * run: --spring.batch.job.names=trMigrationJob
 */
@Configuration
@RequiredArgsConstructor
public class TrMigrationConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final OrdersRepository ordersRepository;
    private final AccountsRepository accountsRepository;

    // 잡
    @Bean
    public Job trMigrationJob(Step trMigrationStep) {
        return jobBuilderFactory.get("trMigrationJob") // 이 Job의 이름을 지정
                .incrementer(new RunIdIncrementer()) // Job의 식별자인 id값 자동 증가
                .start(trMigrationStep) // 실행될 Step을 지정
                .build();
    }

    // 스텝
    @Bean
    @JobScope
    public Step trMigrationStep(ItemReader trOrdersReader,
                                ItemProcessor trOrderProcessor,
                                RepositoryItemWriter trOrdersWriter) {
        return stepBuilderFactory.get("trMigrationStep") // 이 Step의 이름을 지정
                .<Orders, Accounts> chunk(5) // chunk: 처리할 데이터의 트랜잭션 수, <가져올 엔티티, write할 엔티티>
                .reader(trOrdersReader)
                .processor(trOrderProcessor)
                .writer(trOrdersWriter)
                .build();
    }

    // 리더
    @Bean
    @StepScope
    public RepositoryItemReader<Orders> trOrdersReader() {
        return new RepositoryItemReaderBuilder<Orders>()
                .name("trOrdersReader") // 아이템 리더명 지정
                .repository(ordersRepository) // 사용할 레파지토리
                .methodName("findAll") // 사용할 매서드
                .pageSize(5) // 통상 chunk 사이즈와 동일
                .arguments(Arrays.asList()) // 파라미터
                .sorts(Collections.singletonMap("id", Sort.Direction.ASC)) // 정렬
                .build();
    }

    // 프로세서
    @Bean
    @StepScope
    public ItemProcessor<Orders, Accounts> trOrderProcessor() {
        return new ItemProcessor<Orders, Accounts>() {
            @Override
            public Accounts process(Orders item) throws Exception {
                return new Accounts(item);
            }
        };
    }

    // 라이터
    @Bean
    @StepScope
    public RepositoryItemWriter<Accounts> trOrdersWriter() {
        return new RepositoryItemWriterBuilder<Accounts>()
                .repository(accountsRepository) // 사용할 레파지토리
                .methodName("save") // 사용할 매서드
                .build();
    }
}
