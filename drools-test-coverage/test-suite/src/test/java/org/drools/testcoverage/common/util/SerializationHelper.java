/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.testcoverage.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.drools.core.marshalling.impl.ProtobufMarshaller;
import org.drools.core.util.DroolsStreamUtils;
import org.kie.api.KieBase;
import org.kie.api.marshalling.ObjectMarshallingStrategy;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieSession;
import org.kie.internal.marshalling.MarshallerFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;

/**
 * Marshalling helper class to perform serialize/de-serialize a given object
 */
public class SerializationHelper {

    public static <T> T serializeObject(final T obj) throws IOException, ClassNotFoundException {
        return serializeObject(obj, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> T serializeObject(final T obj, final ClassLoader classLoader) throws IOException, ClassNotFoundException {
        return (T) DroolsStreamUtils.streamIn(DroolsStreamUtils.streamOut(obj), classLoader);
    }

    public static StatefulKnowledgeSession getSerialisedStatefulKnowledgeSession(final KieSession ksession,
                                                                                 final boolean dispose) throws IOException, ClassNotFoundException {
        return getSerialisedStatefulKnowledgeSession(ksession, dispose, true);
    }

    public static StatefulKnowledgeSession getSerialisedStatefulKnowledgeSession(final KieSession ksession,
                                                                                 final boolean dispose,
                                                                                 final boolean testRoundTrip) throws IOException, ClassNotFoundException {
        return getSerialisedStatefulKnowledgeSession(ksession, ksession.getKieBase(), dispose, testRoundTrip);
    }

    public static StatefulKnowledgeSession getSerialisedStatefulKnowledgeSession(final KieSession ksession,
                                                                                 final KieBase kbase,
                                                                                 final boolean dispose) throws IOException, ClassNotFoundException {
        return getSerialisedStatefulKnowledgeSession(ksession, kbase, dispose, true);
    }

    public static StatefulKnowledgeSession getSerialisedStatefulKnowledgeSession(final KieSession ksession,
                                                                                 final KieBase kbase,
                                                                                 final boolean dispose,
                                                                                 final boolean testRoundTrip) throws IOException, ClassNotFoundException {
        final ProtobufMarshaller marshaller =
                (ProtobufMarshaller) MarshallerFactory.newMarshaller(
                        kbase,
                        (ObjectMarshallingStrategy[]) ksession.getEnvironment().get(EnvironmentName.OBJECT_MARSHALLING_STRATEGIES));
        final long time = ksession.getSessionClock().getCurrentTime();
        // make sure globas are in the environment of the session
        ksession.getEnvironment().set(EnvironmentName.GLOBALS, ksession.getGlobals());

        // Serialize object
        final byte[] b1;
        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            marshaller.marshall(bos, ksession, time);
            b1 = bos.toByteArray();
        }

        // Deserialize object
        final StatefulKnowledgeSession ksession2;

        try (final ByteArrayInputStream bais = new ByteArrayInputStream(b1)) {
            ksession2 = marshaller.unmarshall(bais, ksession.getSessionConfiguration(), ksession.getEnvironment());
        }

        if (testRoundTrip) {
            // Reserialize and check that byte arrays are the same
            final byte[] b2;
            try (final ByteArrayOutputStream bos2 = new ByteArrayOutputStream()) {
                marshaller.marshall(bos2, ksession2, time);
                b2 = bos2.toByteArray();
            }

            // bytes should be the same.
            Assertions.assertThat(b1).isEqualTo(b2);
        }

        if (dispose) {
            ksession.dispose();
        }

        return ksession2;
    }
}
