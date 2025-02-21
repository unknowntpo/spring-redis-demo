package org.example.benchmarks;

import org.example.App;
import org.example.configs.RedisConfig;
import org.example.dtos.StudentDTO;
import org.example.repositories.jpa.StudentRepository;
import org.example.repositories.redis.StudentRedisRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.actuate.autoconfigure.wavefront.WavefrontProperties;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.core.env.Environment;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(value = 1)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
@Tag("benchmark")
/**
 * Benchmark
 * 
 * @see https://www.cnblogs.com/linyb-geek/p/18041030
 */
public class StudentEndpointBenchmark {

	private TestRestTemplate restTemplate;
	private StudentRepository studentRepository;
	private StudentRedisRepository studentRedisRepository;
	private RedisTemplate<String, Object> redisTemplate;
	private Integer studentId;
	private String cacheKey;
	private StudentDTO studentDto;
	private ConfigurableApplicationContext context;

	public static void main(String[] args) throws RunnerException {
		String report = "hello-jmhReport.json";
		Options opt = new OptionsBuilder()
				.include(StudentEndpointBenchmark.class.getSimpleName())
				// 参数优先级顺序：类 ＜ 方法 ＜ Options
				// 因此如下配置会覆盖@Warmup配置
				.warmupIterations(1)
				.warmupTime(TimeValue.seconds(5))
				// 报告输出.可以将结果上传到 https://jmh.morethan.io 或者/http://deepoove.com/jmh-visual
				// 进行分析
				.result(report)
				// 报告格式
				.resultFormat(ResultFormatType.JSON).build();
		new Runner(opt).run();
	}

	@Setup(Level.Trial)
	public void setupTrial() {
		// Start Spring context with random port
		context = SpringApplication.run(App.class, "--server.port=0");
		// context = new SpringApplicationBuilder(WavefrontProperties.Application.class)
		// // Replace with your main
		// // Application class
		// .web(WebApplicationType.SERVLET)
		// .run("--server.port=0"); // Random port

		// Get beans from context
		studentRepository = context.getBean(StudentRepository.class);
		studentRedisRepository = context.getBean(StudentRedisRepository.class);
		RedisConfig config = context.getBean(RedisConfig.class);
		redisTemplate = config.redisTemplate();

		// Configure TestRestTemplate with the random port
		int port = context.getBean(Environment.class)
				.getProperty("local.server.port", Integer.class, 0);
		restTemplate = new TestRestTemplate();
		restTemplate.setUriTemplateHandler(
				new DefaultUriBuilderFactory("http://localhost:" + port));

		// Clean up everything at the start
		studentRepository.deleteAll();
		Objects.requireNonNull(redisTemplate.getConnectionFactory())
				.getConnection().flushAll();
	}

	@Setup(Level.Iteration)
	public void setup() {
		// Create test student via API
		studentDto = new StudentDTO("Test Student");
		ResponseEntity<StudentDTO> response = restTemplate.postForEntity("/students", studentDto, StudentDTO.class);
		studentId = Objects.requireNonNull(response.getBody()).getId();
		cacheKey = String.format("student:%d", studentId);
	}

	@TearDown(Level.Trial)
	public void tearDownTrial() {
		try {
			// Clean up data first
			if (studentRepository != null) {
				studentRepository.deleteAll();
			}
			if (redisTemplate != null) {
				Objects.requireNonNull(redisTemplate.getConnectionFactory())
						.getConnection().flushAll();
			}

			// Shutdown Spring context
			if (context != null) {
				context.close();

				// Force stop any remaining threads
				Thread.getAllStackTraces().keySet().stream()
						.filter(thread -> thread.getName().contains("container-"))
						.forEach(thread -> {
							try {
								thread.interrupt();
							} catch (Exception ignored) {
							}
						});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Benchmark
	public void getStudentWithoutCache(Blackhole blackhole) {
		ResponseEntity<StudentDTO> response = restTemplate.postForEntity(
				"student/{id}?cache=false",
				studentDto,
				StudentDTO.class);
		blackhole.consume(response);
	}

	@Benchmark
	public void getStudentWithCache(Blackhole blackhole) {
		ResponseEntity<StudentDTO> response = restTemplate.getForEntity(
				"/students/{id}",
				StudentDTO.class,
				studentId);
		blackhole.consume(response.getBody());
	}

	@Benchmark
	public void getStudentWithCacheMiss(Blackhole blackhole) {
		// Clear cache
		redisTemplate.delete(cacheKey);

		// Get student via API (will populate cache)
		ResponseEntity<StudentDTO> response = restTemplate.getForEntity(
				"/students/{id}",
				StudentDTO.class,
				studentId);
		blackhole.consume(response.getBody());

		// Verify it's in cache now
		blackhole.consume(studentRedisRepository.getById(studentId));
	}

	// @Benchmark
	// @Threads(10)
	// public void getStudentConcurrent(Blackhole blackhole) {
	// ResponseEntity<StudentDTO> response = restTemplate.getForEntity(
	// "/students/{id}",
	// StudentDTO.class,
	// studentId);
	// blackhole.consume(response.getBody());
	// }
}
