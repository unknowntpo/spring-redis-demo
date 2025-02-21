import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";

// Define custom trend metrics for each endpoint
const withCacheTrend = new Trend('with_cache_duration');
const withoutCacheTrend = new Trend('without_cache_duration');

export const options = {
	scenarios: {
		withCache: {
			executor: 'constant-vus',
			vus: 10,
			duration: '30s',
			exec: 'withCache',
		},
		withoutCache: {
			executor: 'constant-vus',
			vus: 10,
			duration: '30s',
			exec: 'withoutCache',
		},
	},
};

export function withCache() {
	const res = http.get('http://localhost:8080/students/1');
	check(res, { 'status is 200': (r) => r.status === 200 });
	withCacheTrend.add(res.timings.duration); // Add duration to custom trend
	sleep(1);
}

export function withoutCache() {
	const res = http.get('http://localhost:8080/students/1?cache=false');
	check(res, { 'status is 200': (r) => r.status === 200 });
	withoutCacheTrend.add(res.timings.duration); // Add duration to custom trend
	sleep(1);
}

export function handleSummary(data) {
	data['with_cache_duration'] = withCacheTrend
	data['without_cache_duration'] = withoutCacheTrend
	return {
		"k6/summary.html": htmlReport(data),
		"k6/summary.json": JSON.stringify(data),
	};
}
