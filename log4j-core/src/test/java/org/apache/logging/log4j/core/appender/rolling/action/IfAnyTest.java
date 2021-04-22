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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.jupiter.api.Test;

/**
 * Tests the Or composite condition.
 */
public class IfAnyTest {

	@Test
	public void test() {
		final PathCondition TRUE = FixedCondition.mockPathCondition1(true);
		final PathCondition FALSE = FixedCondition.mockPathCondition1(false);
		assertTrue(IfAny.createOrCondition(TRUE, TRUE).accept(null, null, null));
		assertTrue(IfAny.createOrCondition(FALSE, TRUE).accept(null, null, null));
		assertTrue(IfAny.createOrCondition(TRUE, FALSE).accept(null, null, null));
		assertFalse(IfAny.createOrCondition(FALSE, FALSE).accept(null, null, null));
	}

	@Test
	public void testEmptyIsFalse() {
		assertFalse(IfAny.createOrCondition().accept(null, null, null));
	}

	@Test
	public void testBeforeTreeWalk() {
		final PathCondition counter = mock(PathCondition.class);
		boolean counterAccept;
		int[] counterAcceptCount = new int[1];
		int[] counterBeforeFileTreeWalkCount = new int[1];
		counterAccept = true;
		when(counter.accept(any(Path.class), any(Path.class), any(BasicFileAttributes.class)))
				.thenAnswer((stubInvo) -> {
					counterAcceptCount[0]++;
					return counterAccept;
				});
		doAnswer((stubInvo) -> {
			counterBeforeFileTreeWalkCount[0]++;
			return null;
		}).when(counter).beforeFileTreeWalk();
		final IfAny or = IfAny.createOrCondition(counter, counter, counter);
		or.beforeFileTreeWalk();
		assertEquals(3, counterBeforeFileTreeWalkCount[0]);
	}

}
