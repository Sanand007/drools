/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.dmn.core.imports;

import org.junit.Test;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.core.BaseInterpretedVsCompiledTest;
import org.kie.dmn.core.util.DMNRuntimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.kie.dmn.core.util.DMNTestUtil.getAndAssertModelNoErrors;
import static org.kie.dmn.core.util.DynamicTypeUtils.entry;
import static org.kie.dmn.core.util.DynamicTypeUtils.mapOf;

public class ImportsTest extends BaseInterpretedVsCompiledTest {

    public ImportsTest( boolean useExecModelCompiler ) {
        super( useExecModelCompiler );
    }

    public static final Logger LOG = LoggerFactory.getLogger(ImportsTest.class);

    @Test
    public void testImportDependenciesForDTInAContext() {
        DMNRuntime runtime = DMNRuntimeUtil.createRuntimeWithAdditionalResources("Imported_Model.dmn",
                                                                                 this.getClass(),
                                                                                 "Import_BKM_and_have_a_Decision_Ctx_with_DT.dmn");

        DMNModel importedModel = runtime.getModel("http://www.trisotech.com/definitions/_f27bb64b-6fc7-4e1f-9848-11ba35e0df36",
                                                  "Imported Model");
        assertThat(importedModel, notNullValue());
        assertThat(DMNRuntimeUtil.formatMessages(importedModel.getMessages()), importedModel.hasErrors(), is(false));

        DMNModel dmnModel = runtime.getModel("http://www.trisotech.com/definitions/_c3e08836-7973-4e4d-af2b-d46b23725c13",
                                             "Import BKM and have a Decision Ctx with DT");
        assertThat(dmnModel, notNullValue());
        assertThat(DMNRuntimeUtil.formatMessages(dmnModel.getMessages()), dmnModel.hasErrors(), is(false));

        DMNContext context = runtime.newContext();
        context.set("A Person", mapOf(entry("name", "John"), entry("age", 47)));

        DMNResult evaluateAll = runtime.evaluateAll(dmnModel, context);
        assertThat(DMNRuntimeUtil.formatMessages(evaluateAll.getMessages()), evaluateAll.hasErrors(), is(false));

        LOG.debug("{}", evaluateAll);
        assertThat(evaluateAll.getDecisionResultByName("A Decision Ctx with DT").getResult(), is("Respectfully, Hello John!"));
    }
    
    @Test
    public void testImport2BKMs() {
        DMNRuntime runtime = DMNRuntimeUtil.createRuntimeWithAdditionalResources("Do_say_hello_with_2_bkms.dmn",
                                                                                 this.getClass(),
                                                                                 "Saying_hello_2_bkms.dmn");

        DMNModel importedModel = runtime.getModel("http://www.trisotech.com/dmn/definitions/_16a48e7a-0687-4c2d-b402-42925084fa1a",
                                                  "Saying hello 2 bkms");
        assertThat(importedModel, notNullValue());
        assertThat(DMNRuntimeUtil.formatMessages(importedModel.getMessages()), importedModel.hasErrors(), is(false));

        DMNModel dmnModel = runtime.getModel("http://www.trisotech.com/dmn/definitions/_01a65215-7e0d-47ac-845a-a768f6abf7fe",
                                             "Do say hello with 2 bkms");
        assertThat(dmnModel, notNullValue());
        assertThat(DMNRuntimeUtil.formatMessages(dmnModel.getMessages()), dmnModel.hasErrors(), is(false));

        DMNContext context = runtime.newContext();
        context.set("Person name", "John");

        DMNResult evaluateAll = runtime.evaluateAll(dmnModel, context);
        assertThat(DMNRuntimeUtil.formatMessages(evaluateAll.getMessages()), evaluateAll.hasErrors(), is(false));

        LOG.debug("{}", evaluateAll);
        assertThat(evaluateAll.getDecisionResultByName("Say hello decision").getResult(), is("Hello, John"));
        assertThat(evaluateAll.getDecisionResultByName("what about hello").getResult(), is("Hello"));
    }

    @Test
    public void testImport2BKMsInvoke() {
        DMNRuntime runtime = DMNRuntimeUtil.createRuntimeWithAdditionalResources("Do_invoke_hello_with_2_bkms.dmn",
                                                                                 this.getClass(),
                                                                                 "Saying_hello_2_bkms.dmn");

        DMNModel importedModel = runtime.getModel("http://www.trisotech.com/dmn/definitions/_16a48e7a-0687-4c2d-b402-42925084fa1a",
                                                  "Saying hello 2 bkms");
        assertThat(importedModel, notNullValue());
        assertThat(DMNRuntimeUtil.formatMessages(importedModel.getMessages()), importedModel.hasErrors(), is(false));

        DMNModel dmnModel = runtime.getModel("http://www.trisotech.com/definitions/_eedf6ecc-f113-4333-ace0-79b783e313e5",
                                             "Do invoke hello with 2 bkms");
        assertThat(dmnModel, notNullValue());
        assertThat(DMNRuntimeUtil.formatMessages(dmnModel.getMessages()), dmnModel.hasErrors(), is(false));

        DMNContext emptyContext = runtime.newContext();

        DMNResult evaluateAll = runtime.evaluateAll(dmnModel, emptyContext);
        assertThat(DMNRuntimeUtil.formatMessages(evaluateAll.getMessages()), evaluateAll.hasErrors(), is(false));

        LOG.debug("{}", evaluateAll);
        assertThat(evaluateAll.getDecisionResultByName("invocation of hello").getResult(), is("Hello, John"));
    }

    @Test
    public void testImport2BKMsInvokeUsingInputData() {
        // DROOLS-2746 DMN Invocation parameters resolution with imported function
        DMNRuntime runtime = DMNRuntimeUtil.createRuntimeWithAdditionalResources("Do_invoke_hello_with_2_bkms_using_inputdata.dmn",
                                                                                 this.getClass(),
                                                                                 "Saying_hello_2_bkms.dmn");

        DMNModel importedModel = runtime.getModel("http://www.trisotech.com/dmn/definitions/_16a48e7a-0687-4c2d-b402-42925084fa1a",
                                                  "Saying hello 2 bkms");
        assertThat(importedModel, notNullValue());
        assertThat(DMNRuntimeUtil.formatMessages(importedModel.getMessages()), importedModel.hasErrors(), is(false));

        DMNModel dmnModel = runtime.getModel("http://www.trisotech.com/definitions/_eedf6ecc-f113-4333-ace0-79b783e313e5",
                                             "Do invoke hello with 2 bkms");
        assertThat(dmnModel, notNullValue());
        assertThat(DMNRuntimeUtil.formatMessages(dmnModel.getMessages()), dmnModel.hasErrors(), is(false));

        DMNContext context = runtime.newContext();
        context.set("Person name", "Bob");

        DMNResult evaluateAll = runtime.evaluateAll(dmnModel, context);
        assertThat(DMNRuntimeUtil.formatMessages(evaluateAll.getMessages()), evaluateAll.hasErrors(), is(false));

        LOG.debug("{}", evaluateAll);
        assertThat(evaluateAll.getDecisionResultByName("what about hello").getResult(), is("Hello, Bob"));
    }

    @Test
    public void testImport3Levels() {
        DMNRuntime runtime = DMNRuntimeUtil.createRuntimeWithAdditionalResources("L3_Do_say_hello.dmn",
                                                                                 this.getClass(),
                                                                                 "Do_say_hello_with_2_bkms.dmn",
                                                                                 "Saying_hello_2_bkms.dmn");

        if (LOG.isDebugEnabled()) {
            runtime.addListener(DMNRuntimeUtil.createListener());
        }

        DMNModel importedModel = runtime.getModel("http://www.trisotech.com/dmn/definitions/_16a48e7a-0687-4c2d-b402-42925084fa1a",
                                                  "Saying hello 2 bkms");
        assertThat(importedModel, notNullValue());
        assertThat(DMNRuntimeUtil.formatMessages(importedModel.getMessages()), importedModel.hasErrors(), is(false));

        DMNModel dmnModel = runtime.getModel("http://www.trisotech.com/dmn/definitions/_01a65215-7e0d-47ac-845a-a768f6abf7fe",
                                             "Do say hello with 2 bkms");
        assertThat(dmnModel, notNullValue());
        assertThat(DMNRuntimeUtil.formatMessages(dmnModel.getMessages()), dmnModel.hasErrors(), is(false));

        DMNModel dmnModelL3 = runtime.getModel("http://www.trisotech.com/dmn/definitions/_820c548c-377d-463e-a62b-bb95ddc4758c",
                                               "L3 Do say hello");
        assertThat(dmnModelL3, notNullValue());
        assertThat(DMNRuntimeUtil.formatMessages(dmnModelL3.getMessages()), dmnModelL3.hasErrors(), is(false));

        DMNContext context = runtime.newContext();
        context.set("Another Name", "Bob");
        context.set("L2import", mapOf(entry("Person name", "John")));

        DMNResult evaluateAll = runtime.evaluateAll(dmnModelL3, context);
        assertThat(DMNRuntimeUtil.formatMessages(evaluateAll.getMessages()), evaluateAll.hasErrors(), is(false));

        LOG.debug("{}", evaluateAll);
        assertThat(evaluateAll.getDecisionResultByName("L3 decision").getResult(), is("Hello, Bob"));
        assertThat(evaluateAll.getDecisionResultByName("L3 view on M2").getResult(), is("Hello, John"));
        assertThat(evaluateAll.getDecisionResultByName("L3 what about hello").getResult(), is("Hello"));
    }

    @Test
    public void testImportHardcodedDecisions() {
        DMNRuntime runtime = DMNRuntimeUtil.createRuntimeWithAdditionalResources("Spell_Greeting.dmn",
                                                                                 this.getClass(),
                                                                                 "Import_Spell_Greeting.dmn");

        DMNModel importedModel = runtime.getModel("http://www.trisotech.com/dmn/definitions/_88f4fc88-1eb2-4188-a721-5720cf5565ce",
                                                  "Spell Greeting");
        assertThat(importedModel, notNullValue());
        assertThat(DMNRuntimeUtil.formatMessages(importedModel.getMessages()), importedModel.hasErrors(), is(false));

        DMNModel dmnModel = runtime.getModel("http://www.trisotech.com/dmn/definitions/_d67f19e9-7835-4cad-9c80-16b8423cc392",
                                             "Import Spell Greeting");
        assertThat(dmnModel, notNullValue());
        assertThat(DMNRuntimeUtil.formatMessages(dmnModel.getMessages()), dmnModel.hasErrors(), is(false));

        DMNContext context = runtime.newContext();
        context.set("Person Name", "John");

        DMNResult evaluateAll = runtime.evaluateAll(dmnModel, context);
        assertThat(DMNRuntimeUtil.formatMessages(evaluateAll.getMessages()), evaluateAll.hasErrors(), is(false));

        LOG.debug("{}", evaluateAll);
        assertThat(evaluateAll.getDecisionResultByName("Say the Greeting to Person").getResult(), is("Hello, John"));
    }

    @Test
    public void testImportTransitiveBaseModel() {
        DMNRuntime runtime = DMNRuntimeUtil.createRuntimeWithAdditionalResources("Sayhello1ID1D.dmn",
                                                                                 this.getClass(),
                                                                                 "ModelB.dmn",
                                                                                 "ModelB2.dmn",
                                                                                 "ModelC.dmn");
        getAndAssertModelNoErrors(runtime, "http://www.trisotech.com/dmn/definitions/_ae5b3c17-1ac3-4e1d-b4f9-2cf861aec6d9", "Say hello 1ID1D");
    }

    @Test
    public void testImportTransitiveEvaluate2Layers() {
        DMNRuntime runtime = DMNRuntimeUtil.createRuntimeWithAdditionalResources("Sayhello1ID1D.dmn",
                                                                                 this.getClass(),
                                                                                 "ModelB.dmn",
                                                                                 "ModelB2.dmn",
                                                                                 "ModelC.dmn");
        final DMNModel dmnModel = getAndAssertModelNoErrors(runtime, "http://www.trisotech.com/dmn/definitions/_2a1d771a-a899-4fef-abd6-fc894332337c", "Model B");

        DMNContext context = runtime.newContext();
        context.set("modelA", mapOf(entry("Person name", "John")));

        DMNResult evaluateAll = runtime.evaluateAll(dmnModel, context);
        LOG.debug("{}", evaluateAll);
        assertThat(DMNRuntimeUtil.formatMessages(evaluateAll.getMessages()), evaluateAll.hasErrors(), is(false));

        assertThat(evaluateAll.getDecisionResultByName("Evaluating Say Hello").getResult(), is("Evaluating Say Hello to: Hello, John"));
    }

    @Test
    public void testImportTransitiveEvaluate3Layers() {
        DMNRuntime runtime = DMNRuntimeUtil.createRuntimeWithAdditionalResources("Sayhello1ID1D.dmn",
                                                                                 this.getClass(),
                                                                                 "ModelB.dmn",
                                                                                 "ModelB2.dmn",
                                                                                 "ModelC.dmn");
        final DMNModel dmnModel = getAndAssertModelNoErrors(runtime, "http://www.trisotech.com/dmn/definitions/_10435dcd-8774-4575-a338-49dd554a0928", "Model C");

        DMNContext context = runtime.newContext();
        context.set("Model B", mapOf(entry("modelA", mapOf(entry("Person name", "B.A.John")))));
        context.set("Model B2", mapOf(entry("modelA", mapOf(entry("Person name", "B2.A.John2")))));

        DMNResult evaluateAll = runtime.evaluateAll(dmnModel, context);
        LOG.debug("{}", evaluateAll);
        assertThat(DMNRuntimeUtil.formatMessages(evaluateAll.getMessages()), evaluateAll.hasErrors(), is(false));

        assertThat(evaluateAll.getDecisionResultByName("Model C Decision based on Bs").getResult(), is("B: Evaluating Say Hello to: Hello, B.A.John; B2:Evaluating Say Hello to: Hello, B2.A.John2"));
    }

}

