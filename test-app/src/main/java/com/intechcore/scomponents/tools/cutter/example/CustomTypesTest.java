/*
 * Copyright (c) 2026-present, Intechcore GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intechcore.scomponents.tools.cutter.example;

import com.intechcore.scomponents.tools.cutter.annotations.common.BoolForce;
import com.intechcore.scomponents.tools.cutter.annotations.CutCode;
import com.intechcore.scomponents.tools.cutter.annotations.CutCodeProcessConfig;
import com.intechcore.scomponents.tools.cutter.annotations.common.ParamType;

import java.util.Objects;

public class CustomTypesTest {
    public void run() {
        CustomType value = this.createInstance("Create Instance of Custom Type -> Param 1", 42);
        String stringResult = "<EMPTY>";
        int intResult = 0;
        if (value != null) {
            stringResult = value.field1;
            intResult = value.field2;
        }

        System.out.println("Custom type string result: " + stringResult);
        System.out.println(App.TEST_RESULTS_SEPARATOR);
        System.out.println("Custom type int result: " + intResult);
        System.out.println(App.TEST_RESULTS_SEPARATOR);

        CustomType value2 = this.createInstance_overrideProfile("createInstance_overrideProfile", 52);
        String stringResult2 = "<EMPTY>";
        int intResult2 = 0;
        if (value2 != null) {
            stringResult2 = value.field1;
            intResult2 = value.field2;
        }

        System.out.println("createInstance_overrideProfile string result: " + stringResult2);
        System.out.println(App.TEST_RESULTS_SEPARATOR);
        System.out.println("createInstance_overrideProfile int result: " + intResult2);
        System.out.println(App.TEST_RESULTS_SEPARATOR);

        CustomTypesTest that1 = this.testReturnThis1(25);
        System.out.println("testReturnThis1 : " + (!Objects.equals(this, that1) ? "<NULL>" : "this")); // not that == null because of compiler optimization;
        System.out.println(App.TEST_RESULTS_SEPARATOR);
        CustomTypesTest that2 = this.testReturnThis2(35);
        System.out.println("testReturnThis2 : " + (!Objects.equals(this, that2) ? "<NULL>" : "this"));
        System.out.println(App.TEST_RESULTS_SEPARATOR);

        System.out.println("Test " + IFakeInterface.class.getSimpleName() + " :");
        System.out.println(App.TEST_RESULTS_SEPARATOR);
        IFakeInterface fakeInterface = this.testInterface();
        IFakeInterface.checkFakeInterfaceValues(fakeInterface);
    }

    @CutCode(withCall = "System.out.println", callParams = {"Create Custom Type Instance - replaced 2"})
    @CutCode(withCall = "System.out.println", callParams = {"param2"}, callParamsTypes = {ParamType.VARIABLE})
    @CutCode(profile = "profile1")
    @CutCode(profile = "profile2")
    private CustomType createInstance(String param1, int param2) {
        return new CustomType(param1, param2);
    }

    private class CustomType {
        public final String field1;
        public final int field2;

        private CustomType(String field1, int field2) {
            this.field1 = field1;
            this.field2 = field2;
        }
    }

    @CutCode(profile = "profile1", callParams = {"arg1"}, callParamsTypes = {ParamType.VARIABLE})
    @CutCode(profile = "profile2", callParams = {"Overrided arg1"}, callParamsTypes = {ParamType.LITERAL})
    @CutCodeProcessConfig(logProcessing = BoolForce.FORCE_TRUE)
    private CustomType createInstance_overrideProfile(String arg1, int arg2) {
        return new CustomType(arg1, arg2);
    }

    @CutCode(withCall = "System.out.println", callParams = {"param1"}, callParamsTypes = {ParamType.VARIABLE})
    public CustomTypesTest testReturnThis1(int param1) {
        System.out.println("testReturnThis1 -> process param1 : " + ++param1);
        return this;
    }

    private final String testString = "Private Field Value";

    @CutCode(withCall = "System.out.println", callParams = {"param1"}, callParamsTypes = {ParamType.VARIABLE})
    @CutCode(withCall = "System.out.println", callParams = {"this.testString"}, callParamsTypes = {ParamType.VARIABLE})
    @CutCodeProcessConfig(
            returnThisIfFound = BoolForce.FORCE_FALSE,
            logProcessing = BoolForce.FORCE_TRUE
    )
    public CustomTypesTest testReturnThis2(int param1) {
        System.out.println("testReturnThis2 -> process param1 : " + ++param1);
        return this;
    }

    @CutCodeProcessConfig(
            returnThisIfFound = BoolForce.FORCE_FALSE,
            logProcessing = BoolForce.FORCE_TRUE
    )
    @CutCode(profile = "profile2", callParams = {"replaced testInterface"}, callParamsTypes = {ParamType.LITERAL})
    public IFakeInterface testInterface() {
        return IFakeInterface.create();
    }
}
