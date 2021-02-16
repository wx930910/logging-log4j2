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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Tests the Not composite condition.
 */
public class IfNotTest {

	@Test
	public void test() {
		assertTrue(FixedCondition.mockPathCondition1(true).accept(null, null, null));
		assertFalse(IfNot.createNotCondition(FixedCondition.mockPathCondition1(true)).accept(null, null, null));

		assertFalse(FixedCondition.mockPathCondition1(false).accept(null, null, null));
		assertTrue(IfNot.createNotCondition(FixedCondition.mockPathCondition1(false)).accept(null, null, null));
	}

	@Test
	public void testEmptyIsFalse() {
		assertThrows(NullPointerException.class, () -> IfNot.createNotCondition(null).accept(null, null, null));
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
		final IfNot not = IfNot.createNotCondition(counter);
		not.beforeFileTreeWalk();
		assertEquals(1, counterBeforeFileTreeWalkCount[0]);
	}

}
