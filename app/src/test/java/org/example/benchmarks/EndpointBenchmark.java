package org.example.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.concurrent.TimeUnit;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class EndpointBenchmark {

	@Autowired
	private TestRestTemplate restTemplate;

	@LocalServerPort
	private int port;

	@Benchmark
	public void benchmarkEndpoint() {
		String url = "http://localhost:" + port + "/your-endpoint";
		restTemplate.getForObject(url, String.class);
	}

	@Setup(Level.Trial)
	public void setup() {
		// Any setup code if needed
	}

	public static void main(String[] args) throws Exception {
		Options opt = new OptionsBuilder()
				.include(EndpointBenchmark.class.getSimpleName())
				.forks(1)
				.build();
		new Runner(opt).run();
	}
}
