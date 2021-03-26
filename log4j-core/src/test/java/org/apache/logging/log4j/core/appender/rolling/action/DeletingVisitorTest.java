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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * Tests the {@code DeletingVisitor} class.
 */
public class DeletingVisitorTest {
	public static DeletingVisitor mockDeletingVisitor1(final Path basePath,
			final List<? extends PathCondition> pathFilters, final boolean testMode) throws IOException {
		List<Path> mockFieldVariableDeleted = new ArrayList<>();
		DeletingVisitor mockInstance = spy(new DeletingVisitor(basePath, pathFilters, testMode));
		doAnswer((stubInvo) -> {
			Path file = stubInvo.getArgument(0);
			mockFieldVariableDeleted.add(file);
			return null;
		}).when(mockInstance).delete(any());
		return mockInstance;
	}

	@Test
	public void testAcceptedFilesAreDeleted() throws IOException, IOException {
		final Path base = Paths.get("/a/b/c");
		final PathCondition ACCEPT_ALL = FixedCondition.mockPathCondition1(true);
		final DeletingVisitor visitor = spy(new DeletingVisitor(base, Collections.singletonList(ACCEPT_ALL), false));
		ArgumentCaptor<Path> visitorDeletedCaptor = ArgumentCaptor.forClass(Path.class);
		doNothing().when(visitor).delete(visitorDeletedCaptor.capture());

		final Path any = Paths.get("/a/b/c/any");
		visitor.visitFile(any, null);
		assertTrue(visitorDeletedCaptor.getAllValues().contains(any));
	}

	@Test
	public void testRejectedFilesAreNotDeleted() throws IOException, IOException {
		final Path base = Paths.get("/a/b/c");
		final PathCondition REJECT_ALL = FixedCondition.mockPathCondition1(false);
		final DeletingVisitor visitor = spy(new DeletingVisitor(base, Collections.singletonList(REJECT_ALL), false));
		ArgumentCaptor<Path> visitorDeletedCaptor = ArgumentCaptor.forClass(Path.class);
		doNothing().when(visitor).delete(visitorDeletedCaptor.capture());

		final Path any = Paths.get("/a/b/c/any");
		visitor.visitFile(any, null);
		assertFalse(visitorDeletedCaptor.getAllValues().contains(any));
	}

	@Test
	public void testAllFiltersMustAcceptOrFileIsNotDeleted() throws IOException, IOException {
		final Path base = Paths.get("/a/b/c");
		final PathCondition ACCEPT_ALL = FixedCondition.mockPathCondition1(true);
		final PathCondition REJECT_ALL = FixedCondition.mockPathCondition1(false);
		final List<? extends PathCondition> filters = Arrays.asList(ACCEPT_ALL, ACCEPT_ALL, REJECT_ALL);
		final DeletingVisitor visitor = spy(new DeletingVisitor(base, filters, false));
		ArgumentCaptor<Path> visitorDeletedCaptor = ArgumentCaptor.forClass(Path.class);
		doNothing().when(visitor).delete(visitorDeletedCaptor.capture());

		final Path any = Paths.get("/a/b/c/any");
		visitor.visitFile(any, null);
		assertFalse(visitorDeletedCaptor.getAllValues().contains(any));
	}

	@Test
	public void testIfAllFiltersAcceptFileIsDeleted() throws IOException, IOException {
		final Path base = Paths.get("/a/b/c");
		final PathCondition ACCEPT_ALL = FixedCondition.mockPathCondition1(true);
		final List<? extends PathCondition> filters = Arrays.asList(ACCEPT_ALL, ACCEPT_ALL, ACCEPT_ALL);
		final DeletingVisitor visitor = spy(new DeletingVisitor(base, filters, false));
		ArgumentCaptor<Path> visitorDeletedCaptor = ArgumentCaptor.forClass(Path.class);
		doNothing().when(visitor).delete(visitorDeletedCaptor.capture());

		final Path any = Paths.get("/a/b/c/any");
		visitor.visitFile(any, null);
		assertTrue(visitorDeletedCaptor.getAllValues().contains(any));
	}

	@Test
	public void testInTestModeFileIsNotDeletedEvenIfAllFiltersAccept() throws IOException, IOException {
		final Path base = Paths.get("/a/b/c");
		final PathCondition ACCEPT_ALL = FixedCondition.mockPathCondition1(true);
		final List<? extends PathCondition> filters = Arrays.asList(ACCEPT_ALL, ACCEPT_ALL, ACCEPT_ALL);
		final DeletingVisitor visitor = spy(new DeletingVisitor(base, filters, true));
		ArgumentCaptor<Path> visitorDeletedCaptor = ArgumentCaptor.forClass(Path.class);
		doNothing().when(visitor).delete(visitorDeletedCaptor.capture());

		final Path any = Paths.get("/a/b/c/any");
		visitor.visitFile(any, null);
		assertFalse(visitorDeletedCaptor.getAllValues().contains(any));
	}

	@Test
	public void testVisitFileRelativizesAgainstBase() throws IOException, IOException {

		final PathCondition filter = new PathCondition() {

			@Override
			public boolean accept(final Path baseDir, final Path relativePath, final BasicFileAttributes attrs) {
				final Path expected = Paths.get("relative");
				assertEquals(expected, relativePath);
				return true;
			}

			@Override
			public void beforeFileTreeWalk() {
			}
		};
		final Path base = Paths.get("/a/b/c");
		final DeletingVisitor visitor = DeletingVisitorTest.mockDeletingVisitor1(base,
				Collections.singletonList(filter), false);

		final Path child = Paths.get("/a/b/c/relative");
		visitor.visitFile(child, null);
	}

	@Test
	public void testNoSuchFileFailure() throws IOException, IOException {
		final DeletingVisitor visitor = DeletingVisitorTest.mockDeletingVisitor1(Paths.get("/a/b/c"),
				Collections.emptyList(), true);
		assertEquals(FileVisitResult.CONTINUE,
				visitor.visitFileFailed(Paths.get("doesNotExist"), new NoSuchFileException("doesNotExist")));
	}

	@Test
	public void testIOException() throws IOException {
		final DeletingVisitor visitor = DeletingVisitorTest.mockDeletingVisitor1(Paths.get("/a/b/c"),
				Collections.emptyList(), true);
		IOException exception = new IOException();
		try {
			visitor.visitFileFailed(Paths.get("doesNotExist"), exception);
			fail();
		} catch (IOException e) {
			assertSame(exception, e);
		}
	}
}
