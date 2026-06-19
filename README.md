# Intechcore Code Cutter: Compile-Time Code Cutter for Java

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Hits-of-Code](https://hitsofcode.com/github/Scomponents/code-cutter?branch=master&exclude=)](https://hitsofcode.com/github/Scomponents/code-cutter/view?branch=master&exclude=)
[![Maven Central Version](https://img.shields.io/maven-central/v/com.intechcore.scomponents.tools.cutter/processor?filter=*java8)](https://central.sonatype.com/artifact/com.intechcore.scomponents.tools.cutter/processor/2.1.0-java8)
[![Maven Central Version](https://img.shields.io/maven-central/v/com.intechcore.scomponents.tools.cutter/processor?filter=!*java8)](https://central.sonatype.com/artifact/com.intechcore.scomponents.tools.cutter/processor/2.1.0)


`Intechcore Code Cutter` is a powerful compile-time code processing tool for Java applications, designed to "cut" or replace method bodies based on custom annotations. This allows for dynamic alteration of code behavior at compile-time, providing extreme flexibility for feature toggling, A/B testing, or environment-specific code generation without modifying the source directly.

## ✨ Features

*   **Compile-Time Code Transformation:** Modifies method bodies during compilation using custom annotations, ensuring no runtime overhead.
*   **Java 8 and 11+ Compatibility:** Built to work seamlessly across different Java versions, leveraging the standard Annotation Processing API.
*   **Flexible Configuration:** Supports both global and method-specific local configurations to control processing behavior.
*   **Profile-Based Overrides:** Define reusable profiles for common code cutting scenarios and apply them to annotations.
*   **Default Value Handling:** Specify default values for parameters using `ParamType.LITERAL` and `ParamType.VARIABLE`.
*   **CompletableFuture Support:** Compile-time processing of methods returning `CompletableFuture` with interface proxies.
*   **Interface Proxy Mechanisms:** Generate proxy implementations for interfaces in `CompletableFuture` chains.

See the [CHANGELOG](CHANGELOG.md) for detailed release notes.

## 🚀 How it Works

`Intechcore Code Cutter` operates through a custom Annotation Processor (`CutCodeProcessor`). When a method is annotated with `@CutCode` (or multiple `@CutCode` annotations via `@CutCodes`), the processor intercepts the compilation of that method. Instead of compiling the original method body, it generates new code based on the attributes specified in the `@CutCode` annotation, effectively replacing the original implementation.

### Global vs. Local Configuration

The processor can be configured globally via compiler arguments. These global settings can then be overridden for specific methods using the `@CutCodeProcessConfig` annotation.

**Global Configuration Example (in `pom.xml` for Maven):**

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.15.0</version>
            <configuration>
                <annotationProcessorPaths>
                    <path>
                        <groupId>com.intechcore.scomponents.tools.cutter</groupId>
                        <artifactId>processor</artifactId>
                        <version>2.1.0</version> <!-- For Java 8, use version 2.1.0-java8 -->
                    </path>
                </annotationProcessorPaths>
                <compilerArgs>
                    <arg>-AINTECHCORE_CUTTER_SETTINGS={"logProcessing":true, "returnThisIfFound":false}</arg>
                    <arg>-AINTECHCORE_CUTTER_PROFILES=[{"name":"profile1","withCall":"System.out.println", "params":["From Profile 1"]}, {"name":"profile-cut-all"}]</arg>
                </compilerArgs>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### Overridden Profiles with Examples

Profiles allow you to define common `CutCode` configurations and apply them by name. Attributes directly specified on an `@CutCode` annotation will override those defined in the profile.

**Example Usage:**

Consider a method `myMethod` and two profiles configured globally:
*   `profile1`: `withCall = "ProfileLogger.log", callParams = {"Profile 1 message"}`
*   `profile2`: `withCall = "AnotherLogger.log", callParams = {"Profile 2 message"}`

```java
import com.intechcore.scomponents.tools.cutter.annotations.CutCode;
import com.intechcore.scomponents.tools.cutter.annotations.CutCodeProcessConfig;
import com.intechcore.scomponents.tools.cutter.annotations.common.BoolForce;
import com.intechcore.scomponents.tools.cutter.annotations.common.ParamType;

public class MyClass {

    // Using a profile: will use "ProfileLogger.log"
    @CutCode(profile = "profile1")
    public void methodWithProfile() {
        System.out.println("Original methodWithProfile behavior");
    }

    // Overriding a profile's call: will use System.out.println instead of ProfileLogger.log
    @CutCode(profile = "profile1", withCall = "System.out.println", callParams = {"Overridden profile message"})
    public void methodWithProfileOverride() {
        System.out.println("Original methodWithProfileOverride behavior");
    }

    // Using multiple CutCode annotations, some with profiles, some overriding
    @CutCode(profile = "profile1", callParams = {"First call from profile 1"}) // Uses profile1's withCall
    @CutCode(profile = "profile2", callParams = {"Second call from profile 2"}) // Uses profile2's withCall
    @CutCode(withCall = "System.err.println", callParams = {"Directly specified call", "someVar"}, callParamsTypes = {ParamType.LITERAL, ParamType.VARIABLE})
    @CutCodeProcessConfig(logProcessing = BoolForce.FORCE_TRUE) // Local config override
    public String complexCutMethod(String someVar) {
        return "Original complexCutMethod behavior: " + someVar;
    }
}
```

## 🧪 Test Application

A `test-app` module is included in this repository, showcasing various use cases and configurations of the `Intechcore Code Cutter` in action. It demonstrates how `CutCode` annotations can be applied to different method types, including those returning `CompletableFuture`, primitive types, and custom objects, as well as the effects of global and local processing configurations.

*   [Explore the Test Application Source Code](./test-app/src/main/java/com/intechcore/scomponents/tools/cutter/example/App.java)

## 🤝 Contributing

Contributions are welcome! Please feel free to open issues or submit pull requests.

## 📄 License

This project is licensed under the Apache License, Version 2.0. See the `LICENSE` file for more details.
