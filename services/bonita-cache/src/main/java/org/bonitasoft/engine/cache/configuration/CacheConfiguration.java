/**
 * Copyright (C) 2024 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.cache.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfiguration {

    public static final String APPLICATION_TOKEN_CACHE_NAME = "application-token";

    @Bean
    public org.bonitasoft.engine.cache.CacheConfiguration applicationTokenCacheConfiguration(
            @Value("${bonita.runtime.cache.application-token.maxElementsInMemory:1000}") final int maxElementsInMemory,
            @Value("${bonita.runtime.cache.application-token.inMemoryOnly:true}") final boolean inMemoryOnly,
            @Value("${bonita.runtime.cache.application-token.maxElementsOnDisk:20000}") final int maxElementsOnDisk,
            @Value("${bonita.runtime.cache.application-token.eternal:false}") final boolean eternal,
            @Value("${bonita.runtime.cache.application-token.evictionPolicy:LRU}") final String evictionPolicy,
            @Value("${bonita.runtime.cache.application-token.copyOnRead:false}") final boolean copyOnRead,
            @Value("${bonita.runtime.cache.application-token.copyOnWrite:false}") final boolean copyOnWrite,
            @Value("${bonita.runtime.cache.application-token.readIntensive:false}") final boolean readIntensive,
            @Value("${bonita.runtime.cache.application-token.timeToLiveSeconds:3600}") final int timeToLiveSeconds) {
        var cacheConfiguration = new org.bonitasoft.engine.cache.CacheConfiguration();
        cacheConfiguration.setName(APPLICATION_TOKEN_CACHE_NAME);
        cacheConfiguration.setMaxElementsInMemory(maxElementsInMemory);
        cacheConfiguration.setInMemoryOnly(inMemoryOnly);
        cacheConfiguration.setMaxElementsOnDisk(maxElementsOnDisk);
        cacheConfiguration.setEternal(eternal);
        cacheConfiguration.setEvictionPolicy(evictionPolicy);
        cacheConfiguration.setCopyOnRead(copyOnRead);
        cacheConfiguration.setCopyOnWrite(copyOnWrite);
        cacheConfiguration.setReadIntensive(readIntensive);
        cacheConfiguration.setTimeToLiveSeconds(timeToLiveSeconds);
        return cacheConfiguration;
    }
}
