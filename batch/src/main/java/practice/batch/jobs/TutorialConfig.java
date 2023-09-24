package practice.batch.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import practice.batch.tasklet.TutorialTasklet;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class TutorialConfig {

    private final JobBuilderFactory jobBuilderFactory; //Job 빌더 생성용
    private final StepBuilderFactory stepBuilderFactory; //Step 빌더 생성용

    //JobBuilderFactory 를 통해서 tutorialJob 생성
//    @Bean
    public Job tutorialJob() {
        return jobBuilderFactory.get("tutorialJob")
                .start(tutorialStep())
                .next(nextStep())
                .next(lastStep())
                .build();
    }

    @Bean
    public Job flowJob() {
        // Job 생성
        return jobBuilderFactory.get("exampleJob")
                .start(startStep())
                .on(ExitStatus.FAILED.getExitCode()) // startStep의 ExitStatus가 FAILED일 경우
                .to(failOverStep()) // failOver Step을 실행 시킨다.
                .on("*") // failOver Step의 결과와 상관없이
                .to(writeStep()) // write Step을 실행 시킨다.
                .from(startStep()) // startStep이 FAILED가 아니고 COMPLETED일 경우
                .on(ExitStatus.COMPLETED.getExitCode())
                .to(processStep()) // process Step을 실행 시킨다
                .on("*") // process Step의 결과와 상관없이
                .to(writeStep()) // write Step을 실행 시킨다.
                .from(startStep()) // startStep의 결과가 FAILED, COMPLETED가 아닌 모든 경우
                .on("*")
                .to(writeStep()) // write Step을 실행시킨다.
                .on("*") // write Step의 결과와 상관없이
                .end() // Flow를 종료시킨다.
                .end()
                .build();
    }

    @Bean
    public Step startStep() {
        // 첫번째 Step 생성
        return stepBuilderFactory.get("startStep")
                .tasklet((contribution, chunkContext) -> {
                    log.info("Start Step!");

                    int a = (int) (Math.random() * 10);

                    String result = "COMPLETED";

                    if(a % 3 == 1) {
                        result = "FAIL";
                    }else if(a % 3 == 2) {
                        result = "UNKNOWN";
                    }
                    // Flow에서 on은 RepeatStatus가 아닌 ExitStatus를 바라본다.
                    if ("COMPLETED".equals(result))
                        contribution.setExitStatus(ExitStatus.COMPLETED);
                    else if ("FAIL".equals(result))
                        contribution.setExitStatus(ExitStatus.FAILED);
                    else if ("UNKNOWN".equals(result))
                        contribution.setExitStatus(ExitStatus.UNKNOWN);

                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step failOverStep() {
        // 실패 시 수행할 Step 생성
        return stepBuilderFactory.get("failOverStep")
                .tasklet((contribution, chunkContext) -> {
                    log.info("FailOver Step!");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step processStep() {
        // 처리를 위한 Step 생성
        return stepBuilderFactory.get("processStep")
                .tasklet((contribution, chunkContext) -> {
                    log.info("Process Step!");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step writeStep() {
        // 결과를 기록하기 위한 Step 생성
        return stepBuilderFactory.get("writeStep")
                .tasklet((contribution, chunkContext) -> {
                    log.info("Write Step!");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    //StepBuilderFactory 를 통해서 tutorialStep 생성
    @Bean
    public Step tutorialStep() {
        return stepBuilderFactory.get("tutorialStep")
                .tasklet(new TutorialTasklet()) //Tasklet 설정
                .build();
    }

    @Bean
    public Step nextStep() {
        return stepBuilderFactory.get("nextStep")
                .tasklet(((contribution, chunkContext) -> {
                    log.info("Next step");
                    return RepeatStatus.FINISHED;
                }))
                .build();
    }

    @Bean Step lastStep() {
        return stepBuilderFactory.get("lastStep")
                .tasklet(((contribution, chunkContext) -> {
                    log.info("Last step");
                    return RepeatStatus.FINISHED;
                }))
                .build();
    }

}
