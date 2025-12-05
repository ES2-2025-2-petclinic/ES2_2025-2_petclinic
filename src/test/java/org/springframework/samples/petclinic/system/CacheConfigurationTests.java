package org.springframework.samples.petclinic.system;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.cache.autoconfigure.JCacheManagerCustomizer;

import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.MutableConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CacheConfigurationTests {

	@Mock
	private CacheManager cacheManager;

	@Test
	void petclinicCacheConfigurationCustomizer() {
		CacheConfiguration cacheConfiguration = new CacheConfiguration();

		JCacheManagerCustomizer customizer = cacheConfiguration.petclinicCacheConfigurationCustomizer();

		customizer.customize(cacheManager);

		ArgumentCaptor<Configuration> configCaptor = ArgumentCaptor.forClass(Configuration.class);

		verify(cacheManager).createCache(eq("vets"), configCaptor.capture());

		Configuration<?, ?> config = configCaptor.getValue();
		assertThat(config).isInstanceOf(MutableConfiguration.class);
		assertThat(((MutableConfiguration<?, ?>) config).isStatisticsEnabled()).isTrue();
	}

}