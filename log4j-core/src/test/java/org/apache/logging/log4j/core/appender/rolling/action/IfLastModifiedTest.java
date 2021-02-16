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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.attribute.FileTime;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Tests the FileAgeFilter class.
 */
public class IfLastModifiedTest {

	@Test
	public void testGetDurationReturnsConstructorValue() {
		final IfLastModified filter = IfLastModified.createAgeCondition(Duration.parse("P7D"));
		assertEquals(0, filter.getAge().compareTo(Duration.parse("P7D")));
	}

	@Test
	public void testAcceptsIfFileAgeEqualToDuration() {
		final IfLastModified filter = IfLastModified.createAgeCondition(Duration.parse("PT33S"));
		final DummyFileAttributes attrs = new DummyFileAttributes();
		final long age = 33 * 1000;
		attrs.lastModified = FileTime.fromMillis(System.currentTimeMillis() - age);
		assertTrue(filter.accept(null, null, attrs));
	}

	@Test
	public void testAcceptsIfFileAgeExceedsDuration() {
		final IfLastModified filter = IfLastModified.createAgeCondition(Duration.parse("PT33S"));
		final DummyFileAttributes attrs = new DummyFileAttributes();
		final long age = 33 * 1000 + 5;
		attrs.lastModified = FileTime.fromMillis(System.currentTimeMillis() - age);
		assertTrue(filter.accept(null, null, attrs));
	}

	@Test
	public void testDoesNotAcceptIfFileAgeLessThanDuration() {
		final IfLastModified filter = IfLastModified.createAgeCondition(Duration.parse("PT33S"));
		final DummyFileAttributes attrs = new DummyFileAttributes();
		final long age = 33 * 1000 - 5;
		attrs.lastModified = FileTime.fromMillis(System.currentTimeMillis() - age);
		assertFalse(filter.accept(null, null, attrs));
	}

	@Test
	public void testAcceptCallsNestedConditionsOnlyIfPathAccepted() throws Exception {
		final PathCondition counter = Mockito.mock(PathCondition.class);
		boolean counterAccept;
		int[] counterBeforeFileTreeWalkCount = new int[1];
		int[] counterAcceptCount = new int[1];
		counterAccept = true;
		Mockito.doAnswer((stubInvo) -> {
			counterBeforeFileTreeWalkCount[0]++;
			return null;
		}).when(counter).beforeFileTreeWalk();
		Mockito.when(counter.accept(Mockito.any(), Mockito.any(), Mockito.any())).thenAnswer((stubInvo) -> {
			counterAcceptCount[0]++;
			return counterAccept;
		});
		final IfLastModified filter = IfLastModified.createAgeCondition(Duration.parse("PT33S"), counter);
		final DummyFileAttributes attrs = new DummyFileAttributes();
		final long oldEnough = 33 * 1000 + 5;
		attrs.lastModified = FileTime.fromMillis(System.currentTimeMillis() - oldEnough);

		assertTrue(filter.accept(null, null, attrs));
		assertEquals(1, counterAcceptCount[0]);
		assertTrue(filter.accept(null, null, attrs));
		assertEquals(2, counterAcceptCount[0]);
		assertTrue(filter.accept(null, null, attrs));
		assertEquals(3, counterAcceptCount[0]);

		final long tooYoung = 33 * 1000 - 5;
		attrs.lastModified = FileTime.fromMillis(System.currentTimeMillis() - tooYoung);
		assertFalse(filter.accept(null, null, attrs));
		assertEquals(3, counterAcceptCount[0]); // no increase
		assertFalse(filter.accept(null, null, attrs));
		assertEquals(3, counterAcceptCount[0]);
		assertFalse(filter.accept(null, null, attrs));
		assertEquals(3, counterAcceptCount[0]);
	}

	@Test
	public void testBeforeTreeWalk() throws Exception {
		final PathCondition counter = Mockito.mock(PathCondition.class);
		boolean counterAccept;
		int[] counterBeforeFileTreeWalkCount = new int[1];
		int[] counterAcceptCount = new int[1];
		counterAccept = true;
		Mockito.doAnswer((stubInvo) -> {
			counterBeforeFileTreeWalkCount[0]++;
			return null;
		}).when(counter).beforeFileTreeWalk();
		Mockito.when(counter.accept(Mockito.any(), Mockito.any(), Mockito.any())).thenAnswer((stubInvo) -> {
			counterAcceptCount[0]++;
			return counterAccept;
		});
		final IfLastModified filter = IfLastModified.createAgeCondition(Duration.parse("PT33S"), counter, counter,
				counter);
		filter.beforeFileTreeWalk();
		assertEquals(3, counterBeforeFileTreeWalkCount[0]);
	}
}
