package com.io.batch.job.filereadwrite;

import com.io.batch.domain.Accounts;
import com.io.batch.domain.Orders;
import com.io.batch.dto.Player;
import com.io.batch.dto.PlayerYears;
import com.io.batch.repository.AccountsRepository;
import com.io.batch.repository.OrdersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * csv파읽을 읽고 쓰기
 * run: --spring.batch.job.names=fileReadWriteJob
 */
@Configuration
@RequiredArgsConstructor
public class FileReadWriteConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    // 잡
    @Bean
    public Job fileReadWriteJob(Step fileReadWriteStep) {
        return jobBuilderFactory.get("fileReadWriteJob") // 이 Job의 이름을 지정
                .incrementer(new RunIdIncrementer()) // Job의 식별자인 id값 자동 증가
                .start(fileReadWriteStep) // 실행될 Step을 지정
                .build();
    }

    // 스텝
    @Bean
    @JobScope
    public Step fileReadWriteStep(ItemReader playerFlatFileItemReader,
                                  ItemProcessor playerPlayerYearsItemProcessor,
                                  ItemWriter playerYearsItemWriter) {
        return stepBuilderFactory.get("fileReadWriteStep") // 이 Step의 이름을 지정
                .<Player, PlayerYears>chunk(5)
                .reader(playerFlatFileItemReader)
                .processor(playerPlayerYearsItemProcessor)
                .writer(playerYearsItemWriter)
                .build();
    }

    // 리더
    @Bean
    @StepScope
    public FlatFileItemReader<Player> playerFlatFileItemReader() {
        return new FlatFileItemReaderBuilder<>()
                .name("playerFlatFileItemReader")
                .resource(new FileSystemResource("players.csv"))
                .lineTokenizer(new DelimitedLineTokenizer())
                .fieldSetMapper((FieldSetMapper)new PlayerFieldSetMapper())
                .linesToSkip(1)
                .build();
    }

    // 프로세서
    @Bean
    @StepScope
    public ItemProcessor<Player, PlayerYears> playerPlayerYearsItemProcessor() {
        return new ItemProcessor<Player, PlayerYears>() {
            @Override
            public PlayerYears process(Player item) throws Exception {
                return new PlayerYears(item);
            }
        };
    }

    // 라이터
    @Bean
    @StepScope
    public FlatFileItemWriter<PlayerYears> playerYearsItemWriter() {
        BeanWrapperFieldExtractor<PlayerYears> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{"ID", "lastName", "position", "yearsExperience"});
        fieldExtractor.afterPropertiesSet();

        DelimitedLineAggregator<PlayerYears> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");
        lineAggregator.setFieldExtractor(fieldExtractor);

        FileSystemResource outputResource = new FileSystemResource("player_output.txt");

        return new FlatFileItemWriterBuilder<PlayerYears>()
                .name("playerYearsItemWriter")
                .resource(outputResource)
                .lineAggregator(lineAggregator)
                .build();
    }
}
