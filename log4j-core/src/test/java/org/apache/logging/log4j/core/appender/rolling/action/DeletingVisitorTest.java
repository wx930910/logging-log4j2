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

package org.apache.logging.log4j.core.appender.rolling.action;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests the {@code DeletingVisitor} class.
 */
public class DeletingVisitorTest {
	/**
	 * Modifies {@code DeletingVisitor} for testing: instead of actually deleting a
	 * file, it adds the path to a list for later verification.
	 */
	static class DeletingVisitorHelper extends DeletingVisitor {
		List<Path> deleted = new ArrayList<>();

		public DeletingVisitorHelper(final Path basePath, final List<? extends PathCondition> pathFilters,
				final boolean testMode) {
			super(basePath, pathFilters, testMode);
		}

		@Override
		protected void delete(final Path file) throws IOException {
			deleted.add(file); // overrides and stores path instead of deleting
		}
	}

	@Test
	public void testAcceptedFilesAreDeleted() throws IOException {
		final Path base = Paths.get("/a/b/c");
		final FixedCondition ACCEPT_ALL = new FixedCondition(true);
		final DeletingVisitorHelper visitor = new DeletingVisitorHelper(base, Collections.singletonList(ACCEPT_ALL),
				false);

		final Path any = Paths.get("/a/b/c/any");
		visitor.visitFile(any, null);
		assertTrue(visitor.deleted.contains(any));
	}

}
