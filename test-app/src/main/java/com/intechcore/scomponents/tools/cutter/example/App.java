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

import com.intechcore.scomponents.tools.cutter.annotations.CutCode;
import com.intechcore.scomponents.tools.cutter.annotations.common.ParamType;

public class App {
    public static final String TEST_RESULTS_SEPARATOR = "----------------------------------------------------";
    public static void main(String[] args) {
        System.out.println("START " + TEST_RESULTS_SEPARATOR);

        void_method_cut_all("void_method_cut_all");
        System.out.println(App.TEST_RESULTS_SEPARATOR);
        void_method_replace_with_one_call();
        System.out.println(App.TEST_RESULTS_SEPARATOR);
        void_method_replace_with_several_calls();
        System.out.println(App.TEST_RESULTS_SEPARATOR);
        void_method_replace_with_call_with_variable("void_method_replace_with_call_with_variable - replaced from variable");
        System.out.println(App.TEST_RESULTS_SEPARATOR);
        void_method_replace_with_call_with_profile("void_method_replace_with_call_with_profile - replaced from variable");
        System.out.println(App.TEST_RESULTS_SEPARATOR);
        void_method_replace_with_call_with_profile_cat_all("void_method_replace_with_call_with_profile_cat_all");
        System.out.println(App.TEST_RESULTS_SEPARATOR);

        CompletableFutureTest service = new CompletableFutureTest();
        service.run(3).join();

        PrimitiveTypesTests primitiveTypes = new PrimitiveTypesTests();
        primitiveTypes.run();

        CustomTypesTest customTypes = new CustomTypesTest();
        customTypes.run();

        System.out.println("END   " + TEST_RESULTS_SEPARATOR);
    }

    @CutCode
    public static void void_method_cut_all(String message) {
        System.out.println("MESSAGE - " + message);
    }

    @CutCode(withCall = "System.out.println", callParams = {"void_method_replace_with_one_call - replaced"})
    private static void void_method_replace_with_one_call() {
        System.out.println("original");
    }

    @CutCode(withCall = "System.out.println", callParams = {"void_method_replace_with_several_calls - replaced 1"})
    @CutCode(withCall = "System.out.println", callParams = {"void_method_replace_with_several_calls - replaced 2"})
    @CutCode(withCall = "System.out.println", callParams = {"void_method_replace_with_several_calls - replaced 3"})
    private static void void_method_replace_with_several_calls() {
        System.out.println("original");
    }

    @CutCode(withCall = "System.out.println", callParams = {"void_method_replace_with_call_with_variable - replaced 1"})
    @CutCode(withCall = "System.out.println", callParams = {"message"}, callParamsTypes = {ParamType.VARIABLE})
    private static void void_method_replace_with_call_with_variable(String message) {
        System.out.println("original " + message);
    }

    @CutCode(withCall = "System.out.println", callParams = {"void_method_replace_with_call_with_profile - replaced 1"})
    @CutCode(withCall = "System.out.println", callParams = {"message"}, callParamsTypes = {ParamType.VARIABLE})
    @CutCode(profile = "profile1")
    private static void void_method_replace_with_call_with_profile(String message) {
        System.out.println("original " + message);
    }

    @CutCode(profile = "profile-cut-all")
    private static void void_method_replace_with_call_with_profile_cat_all(String message) {
        System.out.println("original " + message);
    }
}
