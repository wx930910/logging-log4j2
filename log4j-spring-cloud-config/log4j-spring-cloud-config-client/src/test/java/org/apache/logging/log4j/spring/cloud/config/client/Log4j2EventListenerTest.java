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
package org.apache.logging.log4j.spring.cloud.config.client;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.core.util.Source;
import org.apache.logging.log4j.core.util.Watcher;
import org.apache.logging.log4j.junit.LoggerContextRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Class Description goes here.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringConfiguration.class })
public class Log4j2EventListenerTest {

	public static Watcher mockWatcher1(AtomicInteger count) {
		AtomicInteger mockFieldVariableCount;
		Watcher mockInstance = Mockito.mock(Watcher.class);
		mockFieldVariableCount = count;
		Mockito.when(mockInstance.isModified()).thenAnswer((stubInvo) -> {
			mockFieldVariableCount.incrementAndGet();
			return false;
		});
		Mockito.when(mockInstance.newWatcher(Mockito.any(), Mockito.any(), Mockito.anyLong())).thenReturn(mockInstance);
		return mockInstance;
	}

	private static final String CONFIG = "log4j-console.xml";
	private static final String DIR = "target/logs";

	public static LoggerContextRule loggerContextRule = LoggerContextRule
			.createShutdownTimeoutLoggerContextRule(CONFIG);

	@Rule
	public RuleChain chain = loggerContextRule.withCleanFoldersRule(DIR);

	@Autowired
	private ApplicationEventPublisher publisher;

	@Test
	public void test() throws Exception {
		AtomicInteger count = new AtomicInteger(0);
		Source source = new Source(new File("test.java"));
		loggerContextRule.getLoggerContext().getConfiguration().getWatchManager().watch(source,
				Log4j2EventListenerTest.mockWatcher1(count));
		publisher.publishEvent(new EnvironmentChangeEvent(new HashSet<>()));
		assertTrue(count.get() > 0);
	}
}
