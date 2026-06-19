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
import com.intechcore.scomponents.tools.cutter.annotations.CutCodeProcessConfig;
import com.intechcore.scomponents.tools.cutter.annotations.common.BoolForce;
import com.intechcore.scomponents.tools.cutter.annotations.common.ParamType;

import java.util.concurrent.CompletableFuture;

public class CompletableFutureTest {
    public CompletableFuture<Void> run(int param2) {
        return this.CompletableFutureWithInterface1("CompletableFutureWithInterface1", param2)
                .thenCompose(fi1 -> this.CompletableFutureWithInterface2("CompletableFutureWithInterface2", param2)
                .thenCompose(fi2 -> this.CompletableFutureWithInterface3("CompletableFutureWithInterface3", param2)
                .thenCompose(fi3 -> this.CompletableFutureWithString1("CompletableFutureWithString1", param2)
                .thenCompose(s1 -> this.CompletableFutureWithString2("CompletableFutureWithString2", param2)
                .thenCompose(s2 -> this.CompletableFutureWithString3("CompletableFutureWithString3", param2)
                .thenCompose(s3 -> this.CompletableFutureWithVoid1("CompletableFutureWithVoid1", param2)
                .thenAccept(unused1 -> this.CompletableFutureWithVoid2("CompletableFutureWithVoid2", param2)
                .thenAccept(unused2 -> this.CompletableFutureWithVoid3("CompletableFutureWithVoid3", param2)
                        .thenRun(() -> {
                    System.out.println("CompletableFutureWithInterface1 -> ");
                            IFakeInterface.checkFakeInterfaceValues(fi1);
                            System.out.println(App.TEST_RESULTS_SEPARATOR);
                    System.out.println("CompletableFutureWithInterface2 -> ");
                            IFakeInterface.checkFakeInterfaceValues(fi2);
                            System.out.println(App.TEST_RESULTS_SEPARATOR);
                    System.out.println("CompletableFutureWithInterface3 -> ");
                            IFakeInterface.checkFakeInterfaceValues(fi3);
                            System.out.println(App.TEST_RESULTS_SEPARATOR);

                    System.out.println("CompletableFutureWithString1 -> " + (s1 != null ? s1 : "<EMPTY>"));
                            System.out.println(App.TEST_RESULTS_SEPARATOR);
                    System.out.println("CompletableFutureWithString2 -> " + (s2 != null ? s2 : "<EMPTY>"));
                            System.out.println(App.TEST_RESULTS_SEPARATOR);
                    System.out.println("CompletableFutureWithString3 -> " + (s3 != null ? s3 : "<EMPTY>"));
                            System.out.println(App.TEST_RESULTS_SEPARATOR);
        })))))))));
    }

    @CutCode
    @CutCodeProcessConfig(logProcessing = BoolForce.FORCE_TRUE)
    private CompletableFuture<IFakeInterface> CompletableFutureWithInterface1(String param1, int param2) {
        return createCompletableFutureWithInterface(param1, param2);
    }

    @CutCode(profile = "profile1")
    private CompletableFuture<IFakeInterface> CompletableFutureWithInterface2(String param1, int param2) {
        return createCompletableFutureWithInterface(param1, param2);
    }

    @CutCode(withCall = "System.out.println", callParams = {"CompletableFutureWithInterface3 - replaced 1"})
    @CutCode(withCall = "System.out.println", callParams = {"param1"}, callParamsTypes = {ParamType.VARIABLE})
    @CutCode(profile = "profile1")
    private CompletableFuture<IFakeInterface> CompletableFutureWithInterface3(String param1, int param2) {
        return createCompletableFutureWithInterface(param1, param2);
    }

    private static CompletableFuture<IFakeInterface> createCompletableFutureWithInterface(String param1, int param2) {
        return CompletableFuture.supplyAsync(() -> {
            String result = param1 + " Future with interface -> " + param2;
            System.out.println(result);
            return IFakeInterface.create();
        });
    }

    @CutCode
    private CompletableFuture<Void> CompletableFutureWithVoid1(String param1, int param2) {
        return createCompletableFutureWithVoid(param1, param2);
    }

    @CutCode(profile = "profile1")
    private CompletableFuture<Void> CompletableFutureWithVoid2(String param1, int param2) {
        return createCompletableFutureWithVoid(param1, param2);
    }

    @CutCode(withCall = "System.out.println", callParams = {"CompletableFutureWithInterface3 - replaced 1"})
    @CutCode(withCall = "System.out.println", callParams = {"param1"}, callParamsTypes = {ParamType.VARIABLE})
    @CutCode(profile = "profile1")
    private CompletableFuture<Void> CompletableFutureWithVoid3(String param1, int param2) {
        return createCompletableFutureWithVoid(param1, param2);
    }

    private static CompletableFuture<Void> createCompletableFutureWithVoid(String param1, int param2) {
        return CompletableFuture.runAsync(() -> {
            String result = param1 + " Future with void -> " + param2;
            System.out.println(result);
        });
    }

    @CutCode
    private CompletableFuture<String> CompletableFutureWithString1(String param1, int param2) {
        return createCompletableFutureWithString(param1, param2);
    }

    @CutCode(profile = "profile1")
    private CompletableFuture<String> CompletableFutureWithString2(String param1, int param2) {
        return createCompletableFutureWithString(param1, param2);
    }

    @CutCode(withCall = "System.out.println", callParams = {"CompletableFutureWithString3 - replaced with param2, result:"})
    @CutCode(withCall = "System.out.println", callParams = {"param2"}, callParamsTypes = {ParamType.VARIABLE})
    @CutCode(profile = "profile1")
    private CompletableFuture<String> CompletableFutureWithString3(String param1, int param2) {
        return createCompletableFutureWithString(param1, param2);
    }

    private static CompletableFuture<String> createCompletableFutureWithString(String param1, int param2) {
        return CompletableFuture.supplyAsync(() -> {
            String result = param1 + " Future with String -> " + param2;
            System.out.println(result);
            return result;
        });
    }
}
