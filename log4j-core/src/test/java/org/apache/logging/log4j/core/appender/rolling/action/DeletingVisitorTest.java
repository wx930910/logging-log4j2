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
import org.mockito.Mockito;

/**
 * Tests the {@code DeletingVisitor} class.
 */
public class DeletingVisitorTest {
	@Test
	public void testAcceptedFilesAreDeleted() throws IOException, Exception {
		final Path base = Paths.get("/a/b/c");
		final FixedCondition ACCEPT_ALL = new FixedCondition(true);
		final DeletingVisitor visitor = Mockito
				.spy(new DeletingVisitor(base, Collections.singletonList(ACCEPT_ALL), false));
		List<Path> visitorDeleted = new ArrayList<>();
		Mockito.doAnswer((stubInvo) -> {
			Path file = stubInvo.getArgument(0);
			visitorDeleted.add(file);
			return null;
		}).when(visitor).delete(Mockito.any());

		final Path any = Paths.get("/a/b/c/any");
		visitor.visitFile(any, null);
		assertTrue(visitorDeleted.contains(any));
	}

}
