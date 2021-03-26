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
package org.apache.logging.log4j.core.lookup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

import java.util.Calendar;

import org.apache.logging.log4j.core.AbstractLogEvent;
import org.junit.Test;

/**
 *
 */
public class DateLookupTest {

	public AbstractLogEvent mockAbstractLogEvent1() {
		long mockFieldVariableSerialVersionUID = -2663819677970643109L;
		AbstractLogEvent mockInstance = spy(AbstractLogEvent.class);
		doAnswer((stubInvo) -> {
			final Calendar cal = Calendar.getInstance();
			cal.set(2011, 11, 30, 10, 56, 35);
			return cal.getTimeInMillis();
		}).when(mockInstance).getTimeMillis();
		return mockInstance;
	}

	@Test
	public void testLookup() {
		final StrLookup lookup = new DateLookup();
		final AbstractLogEvent event = mockAbstractLogEvent1();
		final String value = lookup.lookup(event, "MM/dd/yyyy");
		assertNotNull(value);
		assertEquals("12/30/2011", value);
	}
}
