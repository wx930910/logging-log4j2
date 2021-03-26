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
package org.apache.logging.log4j.cassandra;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Permission;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;

import org.apache.cassandra.service.CassandraDaemon;
import org.apache.logging.log4j.LoggingException;
import org.apache.logging.log4j.core.util.Cancellable;
import org.apache.logging.log4j.core.util.Closer;
import org.apache.logging.log4j.core.util.Log4jThreadFactory;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.junit.rules.ExternalResource;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

/**
 * JUnit rule to set up and tear down a Cassandra database instance.
 */
public class CassandraRule extends ExternalResource {

	public static Cancellable mockCancellable1(final CountDownLatch latch) {
		CassandraDaemon mockFieldVariableDaemon = new CassandraDaemon();
		CountDownLatch mockFieldVariableLatch;
		Cancellable mockInstance = mock(Cancellable.class);
		mockFieldVariableLatch = latch;
		doAnswer((stubInvo) -> {
			try {
				mockFieldVariableDaemon.init(null);
			} catch (final IOException e) {
				throw new LoggingException("Cannot initialize embedded Cassandra instance", e);
			}
			mockFieldVariableDaemon.start();
			mockFieldVariableLatch.countDown();
			return null;
		}).when(mockInstance).run();
		doAnswer((stubInvo) -> {
			if (PropertiesUtil.getProperties().isOsWindows()) {
				cancelOnWindows(mockFieldVariableDaemon);
			} else {
				mockFieldVariableDaemon.stop();
			}
			return null;
		}).when(mockInstance).cancel();
		return mockInstance;
	}

	private static static void cancelOnWindows(CassandraDaemon daemon) {
		final SecurityManager currentSecurityManager = System.getSecurityManager();
		try {
			final SecurityManager securityManager = new SecurityManager() {
				@Override
				public void checkPermission(final Permission permission) {
					final String permissionName = permission.getName();
					if (permissionName != null && permissionName.startsWith("exitVM")) {
						throw new SecurityException("test");
					}
				}
			};
			System.setSecurityManager(securityManager);
			daemon.stop();
		} catch (final SecurityException ex) {
		} finally {
			System.setSecurityManager(currentSecurityManager);
		}
	}

	private static final ThreadFactory THREAD_FACTORY = Log4jThreadFactory.createThreadFactory("Cassandra");

	private final CountDownLatch latch = new CountDownLatch(1);
	private final Cancellable embeddedCassandra = CassandraRule.mockCancellable1(latch);
	private final String keyspace;
	private final String tableDdl;
	private Cluster cluster;

	public CassandraRule(final String keyspace, final String tableDdl) {
		this.keyspace = keyspace;
		this.tableDdl = tableDdl;
	}

	public Cluster getCluster() {
		return cluster;
	}

	public Session connect() {
		return cluster.connect(keyspace);
	}

	@Override
	protected void before() throws Throwable {
		final Path root = Files.createTempDirectory("cassandra");
		Files.createDirectories(root.resolve("data"));
		final Path config = root.resolve("cassandra.yml");
		Files.copy(getClass().getResourceAsStream("/cassandra.yaml"), config);
		System.setProperty("cassandra.config", "file:" + config.toString());
		System.setProperty("cassandra.storagedir", root.toString());
		System.setProperty("cassandra-foreground", "true"); // prevents Cassandra from closing stdout/stderr
		THREAD_FACTORY.newThread(embeddedCassandra).start();
		latch.await();
		cluster = Cluster.builder().addContactPoints(InetAddress.getLoopbackAddress()).build();
		try (final Session session = cluster.connect()) {
			session.execute("CREATE KEYSPACE " + keyspace + " WITH REPLICATION = "
					+ "{ 'class': 'SimpleStrategy', 'replication_factor': 2 };");
		}
		try (final Session session = connect()) {
			session.execute(tableDdl);
		}
	}

	@Override
	protected void after() {
		Closer.closeSilently(cluster);
		embeddedCassandra.cancel();
	}
}
