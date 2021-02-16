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
package org.apache.logging.log4j.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.TestLogger;
import org.apache.logging.log4j.TestLoggerContext;
import org.apache.logging.log4j.TestLoggerContextFactory;
import org.apache.logging.log4j.simple.SimpleLoggerContext;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Created by Pavel.Sivolobtchik@uxpsystems.com on 2016-10-19.
 */
public class LoggerAdapterTest {

	public static AbstractLoggerAdapter<Logger> mockAbstractLoggerAdapter2() throws Exception {
		AbstractLoggerAdapter<Logger> mockInstance = Mockito.spy(AbstractLoggerAdapter.class);
		Mockito.doAnswer((stubInvo) -> {
			String name = stubInvo.getArgument(0);
			LoggerContext context = stubInvo.getArgument(1);
			return context.getLogger(name);
		}).when(mockInstance).newLogger(Mockito.any(), Mockito.any());
		return mockInstance;
	}

	public static AbstractLoggerAdapter<Logger> mockAbstractLoggerAdapter1() throws Exception {
		AbstractLoggerAdapter<Logger> mockInstance = Mockito.spy(AbstractLoggerAdapter.class);
		return mockInstance;
	}

	private static class RunnableThreadTest implements Runnable {
		private final AbstractLoggerAdapter<Logger> adapter;
		private final LoggerContext context;
		private final CountDownLatch doneSignal;
		private final int index;
		private Map<String, Logger> resultMap;

		private final CountDownLatch startSignal;

		public RunnableThreadTest(final int index, final AbstractLoggerAdapter adapter, final LoggerContext context,
				final CountDownLatch startSignal, final CountDownLatch doneSignal) {
			this.adapter = adapter;
			this.context = context;
			this.startSignal = startSignal;
			this.doneSignal = doneSignal;
			this.index = index;
		}

		public Map<String, Logger> getResultMap() {
			return resultMap;
		}

		@Override
		public void run() {
			try {
				startSignal.await();
				resultMap = adapter.getLoggersInContext(context);
				resultMap.put(String.valueOf(index), new TestLogger());
				doneSignal.countDown();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}

	}

	private static class TestLoggerContext2 extends TestLoggerContext {
		private final String name;
		private final LoggerContextShutdownAware listener;

		public TestLoggerContext2(String name, LoggerContextShutdownAware listener) {
			this.name = name;
			this.listener = listener;
		}

		public String getName() {
			return name;
		}

		public void shutdown() {
			listener.contextShutdown(this);
		}
	}

	@Test
	public void testCleanup() throws Exception {
		final LoggerContextFactory factory = TestLoggerContextFactory.mockLoggerContextFactory1();
		final AbstractLoggerAdapter<Logger> adapter = LoggerAdapterTest.mockAbstractLoggerAdapter2();
		for (int i = 0; i < 5; ++i) {
			LoggerContext lc = adapter.getContext(Integer.toString(i));
			lc.getLogger(Integer.toString(i));
		}
		assertEquals(5, adapter.registry.size(), "Expected 5 LoggerContexts");
		Set<LoggerContext> contexts = new HashSet<>(adapter.registry.keySet());
		for (LoggerContext context : contexts) {
			((TestLoggerContext2) context).shutdown();
		}
		assertEquals(0, adapter.registry.size(), "Expected 0 LoggerContexts");
	}

	/**
	 * Testing synchronization in the getLoggersInContext() method
	 */
	@Test
	public synchronized void testGetLoggersInContextSynch() throws Exception {
		final AbstractLoggerAdapter<Logger> adapter = LoggerAdapterTest.mockAbstractLoggerAdapter1();

		final int num = 500;

		final CountDownLatch startSignal = new CountDownLatch(1);
		final CountDownLatch doneSignal = new CountDownLatch(num);

		final RunnableThreadTest[] instances = new RunnableThreadTest[num];
		LoggerContext lastUsedContext = null;
		for (int i = 0; i < num; i++) {
			if (i % 2 == 0) {
				// every other time create a new context
				lastUsedContext = new SimpleLoggerContext();
			}
			final RunnableThreadTest runnable = new RunnableThreadTest(i, adapter, lastUsedContext, startSignal,
					doneSignal);
			final Thread thread = new Thread(runnable);
			thread.start();
			instances[i] = runnable;
		}

		startSignal.countDown();
		doneSignal.await();

		for (int i = 0; i < num; i = i + 2) {
			// maps for the same context should be the same instance
			final Map<String, Logger> resultMap1 = instances[i].getResultMap();
			final Map<String, Logger> resultMap2 = instances[i + 1].getResultMap();
			assertSame(resultMap1, resultMap2, "not the same map for instances" + i + " and " + (i + 1) + ":");
			assertEquals(2, resultMap1.size());
		}
	}
}
