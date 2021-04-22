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
package org.apache.logging.log4j;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

import java.net.URI;

import org.apache.logging.log4j.spi.LoggerContext;
import org.apache.logging.log4j.spi.LoggerContextFactory;

/**
 *
 */
public class TestLoggerContextFactory {

	public static LoggerContextFactory mockLoggerContextFactory1() {
		LoggerContext mockFieldVariableContext = new TestLoggerContext();
		LoggerContextFactory mockInstance = spy(LoggerContextFactory.class);
		doAnswer((stubInvo) -> {
			return mockFieldVariableContext;
		}).when(mockInstance).getContext(any(String.class), any(ClassLoader.class), any(Object.class), anyBoolean(),
				any(URI.class), any(String.class));
		doAnswer((stubInvo) -> {
			return mockFieldVariableContext;
		}).when(mockInstance).getContext(any(String.class), any(ClassLoader.class), any(Object.class), anyBoolean());
		return mockInstance;
	}
}
