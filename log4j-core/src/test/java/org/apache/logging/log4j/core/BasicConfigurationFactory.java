/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package org.apache.logging.log4j.core;

import java.net.URI;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.mockito.Mockito;

/**
 *
 */
public class BasicConfigurationFactory extends ConfigurationFactory {

	public static AbstractConfiguration mockAbstractConfiguration1() {
		String mockFieldVariableDEFAULT_LEVEL = "org.apache.logging.log4j.level";
		AbstractConfiguration mockInstance = Mockito.mock(AbstractConfiguration.class, Mockito.withSettings()
				.useConstructor(null, ConfigurationSource.NULL_SOURCE).defaultAnswer(Mockito.CALLS_REAL_METHODS));
		final LoggerConfig root = mockInstance.getRootLogger();
		final String name = System.getProperty(mockFieldVariableDEFAULT_LEVEL);
		final Level level = (name != null && Level.getLevel(name) != null) ? Level.getLevel(name) : Level.ERROR;
		root.setLevel(level);
		return mockInstance;
	}

	@Override
	public Configuration getConfiguration(final LoggerContext loggerContext, final String name,
			final URI configLocation) {
		return BasicConfigurationFactory.mockAbstractConfiguration1();
	}

	@Override
	public String[] getSupportedTypes() {
		return null;
	}

	@Override
	public Configuration getConfiguration(final LoggerContext loggerContext, final ConfigurationSource source) {
		return null;
	}
}
