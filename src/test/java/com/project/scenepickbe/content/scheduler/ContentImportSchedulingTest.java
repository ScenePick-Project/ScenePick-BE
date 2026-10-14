package com.project.scenepickbe.content.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.config.CronTask;

import com.project.scenepickbe.content.service.ContentCommandService;
import com.project.scenepickbe.infrastructure.tmdb.TmdbClient;

class ContentImportSchedulingTest {
	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withUserConfiguration(SchedulingConfig.class);

	@Test
	void automaticImportIsDisabledByDefault() {
		contextRunner.run(context -> {
			assertThat(context).hasNotFailed();
			assertThat(context.getBean(ScheduledAnnotationBeanPostProcessor.class).getScheduledTasks()).isEmpty();
			verifyNoInteractions(context.getBean(TmdbClient.class), context.getBean(ContentCommandService.class));
		});
	}

	@Test
	void explicitDisabledCronDoesNotRegisterATask() {
		contextRunner.withPropertyValues("tmdb.import.cron=-").run(context -> {
			assertThat(context.getBean(ScheduledAnnotationBeanPostProcessor.class).getScheduledTasks()).isEmpty();
		});
	}

	@Test
	void configuredCronRegistersExactlyOneTask() {
		String cron = "0 0 0 29 2 *";
		contextRunner.withPropertyValues("tmdb.import.cron=" + cron).run(context -> {
			var tasks = context.getBean(ScheduledAnnotationBeanPostProcessor.class).getScheduledTasks();
			assertThat(tasks).hasSize(1);
			assertThat(((CronTask)tasks.iterator().next().getTask()).getExpression()).isEqualTo(cron);
		});
	}

	@Configuration(proxyBeanMethods = false)
	@EnableScheduling
	@Import(ContentImportScheduler.class)
	static class SchedulingConfig {
		@Bean
		TmdbClient tmdbClient() {
			return mock(TmdbClient.class);
		}

		@Bean
		ContentCommandService contentCommandService() {
			return mock(ContentCommandService.class);
		}
	}
}
