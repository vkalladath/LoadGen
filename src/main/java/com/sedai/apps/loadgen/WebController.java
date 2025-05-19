package com.sedai.apps.loadgen;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.common.util.concurrent.RateLimiter;

@RestController
public class WebController {
	private static long count = 0;

	private static final String ALPHA_NUMERIC_STRING = " abcdefghijklmnopqrstuvwxyz ABCDEFGHIJKLMNOPQRSTUVWXYZ 0123456789";

	@Autowired
	private Environment env;

	@Value("${CLIENT_ID:DEFAULT_CLIENT}")
	private String clientId;

	private Map<String, RateLimiter> ratelimiterMap = new HashMap<String, RateLimiter>();

	private RateLimiter globalRateLimiter;

	// cached config data
	private LoadConfig loadConfig;
	
	@RequestMapping("/health")
	public String status() {
		return "healthy";
	}

	@RequestMapping("/")
	public String index(@RequestParam(name = "MIN_LATENCY", defaultValue = "0") int minLatency,
			@RequestParam(name = "MAX_LATENCY", defaultValue = "0") int maxLatency,
			@RequestParam(name = "RESPONSE_SIZE", defaultValue = "1024") int responseSize) {
		// wait for rate limiter
		applyRateLimit();

		String configURL = env.getProperty("CONFIG_URL");
		String configFile = env.getProperty("CONFIG_FILE");
		// If config url is not set, respond in dumb mode
		if (configURL == null || configFile == null) {
			String randomText = randomAlphaNumeric(responseSize);
			latency(minLatency, maxLatency);
			return "<h1>" + count++ + "</h1>\r\n<p>" + randomText;
		}

		// read from config url. url from env variable
		loadConfig = getConfig(configURL, configFile);
		List<BackendConfig> backendConfigs = loadConfig.getBackendConfigs();
		if (backendConfigs == null || backendConfigs.size() < 1) {
			throw new RuntimeException("No valid response from Config URL");
		}

		for (BackendConfig config : backendConfigs) {
			String response = getResponseFromURL(config);
			processData(response, config.getTransform(), config.getDataTransformIterations());
		}

		return randomAlphaNumeric(loadConfig.getResponseSize());

	}

	private String processData(String data, DataTransform transform, int iterations) {
		String response = "";
		for (int i = 0; i < iterations; i++) {
			response = processData(data + response, transform);
		}
		return response;
	}

	private String processData(String response, DataTransform transform) {
		switch (transform) {
		case BASE64:
			break;
		case MD5:
			return Hashing.md5().hashString(response, Charsets.UTF_8).toString();
		case SHA1_HASH:
			return Hashing.sha1().hashString(response, Charsets.UTF_8).toString();
		case CRC32:
			return Hashing.crc32().hashString(response, Charsets.UTF_8).toString();
		case SHA256:
			return Hashing.sha256().hashString(response, Charsets.UTF_8).toString();
		case SHA512:
			return Hashing.sha512().hashString(response, Charsets.UTF_8).toString();
		default:
			return "";
		}
		return response;

	}

	private String getResponseFromURL(BackendConfig config) {
		String url = config.getUrl() + "?MIN_LATENCY={min}&MAX_LATENCY={max}&RESPONSE_SIZE={size}&payload={payload}";
		RestTemplate restTemplate = new RestTemplate();
		Map<String, String> params = new HashMap<>();
		params.put("min", "" + config.getMinLatencyMillis());
		params.put("max", "" + config.getMaxLatencyMillis());
		params.put("size", "" + config.getResponseSize());
		params.put("payload", randomAlphaNumeric(config.getRequestSize()));
		ResponseEntity<String> response = restTemplate.getForEntity(url, String.class, params);
		return response.getBody();
	}

	private LoadConfig getConfig(String configURL, String configFile) {
		boolean reload = true;
		if (loadConfig != null) {
			Instant currentTime = Instant.now();
			if (loadConfig.lastUpdatedTime
					.isAfter(currentTime.minus(loadConfig.getConfigReloadIntervalMinutes(), ChronoUnit.MINUTES))) {
				reload = false;
			}
		}
		if (reload) {
			RestTemplate restTemplate = new RestTemplate();
			Map<String, String> params = new HashMap<>();
			configURL = configURL + "/" + configFile;
			try {
				ResponseEntity<LoadConfig> response = restTemplate.getForEntity(configURL, LoadConfig.class, params);
				LoadConfig config = response.getBody();
				config.lastUpdatedTime = Instant.now();
				return config;
			} catch (Exception e) { // fall back to previous config on error
				e.printStackTrace();
				return loadConfig;
			}
		}
		return loadConfig;
	}

	protected void applyRateLimit() {
		RateLimiter rateLimiter = getRateLimiter();
		if (rateLimiter != null) {
			double waitTime = rateLimiter.acquire();
			if(waitTime > 0.0) {
				System.out.println("Throttled for " + waitTime + " seconds.");
			}
		}
	}

	private RateLimiter getRateLimiter() {

		if (clientId != null && env.getProperty("PER_CLIENT_RATE_LIMIT") != null) {
			if (ratelimiterMap.containsKey(clientId)) {
				return ratelimiterMap.get(clientId);
			} else {
				RateLimiter rl = RateLimiter.create(Double.parseDouble(env.getProperty("PER_CLIENT_RATE_LIMIT")));
				ratelimiterMap.put(clientId, rl);
				return rl;
			}
		} else if (env.getProperty("GLOBAL_RATE_LIMIT") != null) {
			if (globalRateLimiter == null) {
				globalRateLimiter = RateLimiter.create((Double.parseDouble(env.getProperty("GLOBAL_RATE_LIMIT"))));
			}
			return globalRateLimiter;
		}

		return null;
	}

	private void latency(int minLatency, int maxLatency) {
		if (minLatency == maxLatency) {
			sleep(minLatency);
		} else if (maxLatency > minLatency) {
			Random random = new Random();
			sleep(minLatency + random.nextInt(maxLatency - minLatency) + 1);
		}
	}

	/**
	 * @param latency in milliseconds
	 */
	private void sleep(int latency) {
		try {
			Thread.sleep(latency);
		} catch (InterruptedException e) {
		}
	}

	public String randomAlphaNumeric(int count) {
		StringBuilder builder = new StringBuilder();
		while (count-- != 0) {
			int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
			builder.append(ALPHA_NUMERIC_STRING.charAt(character));
		}
		return builder.toString();
	}

}