package practice.batch.chunks;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
public class ChunkStepConfiguration {

    private final StepBuilderFactory stepBuilderFactory;
    private final JobBuilderFactory jobBuilderFactory;

    // Job 설정
    @Bean(name = "chunkJob")
    public Job bookOrderJob() throws Exception {
        return jobBuilderFactory.get("chunkJob")
                .start(chunkStep()) // "chunkStep"이라는 이름의 Step을 시작점으로 설정
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<String> reader() {
        return new ListItemReader<>(Arrays.asList("one", "two", "three", "four", "five"));
    }

    @Bean
    @StepScope
    public ItemProcessor<String, String> processor() {
        return item -> item + " processed";
    }

    @Bean
    @StepScope
    public ItemWriter<String> writer() {
        return items -> items.forEach(System.out::println);
    }

    @Bean
    @JobScope
    public Step chunkStep() {
        return stepBuilderFactory.get("chunkStep")
                .<String, String>chunk(10) // 첫번째 제네릭은 Reader에서 반환할 타입, 두번째 제네릭은 Writer에 파라미터로 넘어올 타입
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }
}
