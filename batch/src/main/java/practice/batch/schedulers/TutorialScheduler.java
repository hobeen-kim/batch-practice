package practice.batch.schedulers;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TutorialScheduler {

    private final Job job; //tutorialJob 주입
    private final JobLauncher jobLauncher;

    @Scheduled(fixedDelay = 5000L) //5초마다 tutorialJob 실행
    public void executeJob() {
        try {
            jobLauncher.run(job,
                    new JobParametersBuilder()
                        .addString("time", LocalDateTime.now().toString())
                        .toJobParameters() //Job 실행 시 필요한 파라미터 설정
            );
        } catch (JobExecutionException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
