/*
 * Copyright 2018 data Artisans GmbH, 2019 Ververica GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ververica.flinktraining.exercises.datastream_java.testing;

import com.ververica.flinktraining.exercises.datastream_java.datatypes.TaxiFare;
import com.ververica.flinktraining.exercises.datastream_java.datatypes.TaxiRide;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.watermark.Watermark;

public abstract class TestSource implements SourceFunction {
    private volatile boolean running = true;
    protected Object[] testStream;

    @Override
    public void run(SourceContext ctx) throws Exception {
        for (int i = 0; (i < testStream.length) && running; i++) {
            if (testStream[i] instanceof TaxiRide) {
                TaxiRide ride = (TaxiRide) testStream[i];
                ctx.collectWithTimestamp(ride, ride.getEventTime());
            } else if (testStream[i] instanceof TaxiFare) {
                TaxiFare fare = (TaxiFare) testStream[i];
                ctx.collectWithTimestamp(fare, fare.getEventTime());
            } else if (testStream[i] instanceof String) {
                String s = (String) testStream[i];
                ctx.collectWithTimestamp(s, 0);
            } else if (testStream[i] instanceof Long) {
                Long ts = (Long) testStream[i];
                ctx.emitWatermark(new Watermark(ts));
            } else {
                throw new RuntimeException(testStream[i].toString());
            }
        }
        // test sources are finite, so they have a Long.MAX_VALUE watermark when they finishes
    }

    @Override
    public void cancel() {
        running = false;
    }
}
