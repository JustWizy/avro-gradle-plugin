# Avro IDL to Schema/Protocol Gradle Plugin

A Gradle plugin that converts **Avro IDL** (`.avdl`) files into:

- **Avro schemas** (`.avsc`)
- **Avro protocol JSON** (`.avpr`, optional)

Key features:

- Incremental builds and Gradle build cache (`@CacheableTask`)
- Per-sourceSet generation (works for `main`, `test`, etc.)
- Import support in IDL (Avro 1.12 `avro-idl` APIs)
- Generated outputs are automatically added to source set **resources**
- Zero configuration for conventional layout

---

## Quick Start

### 1) Apply the plugin

Once published to the Gradle Plugin Portal:

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}
````

```kotlin
// build.gradle.kts (consumer project)
plugins {
    id("java") // or 'java-library'
    id("com.wizy.avro-idl") version "1.0.0"
}

repositories {
    mavenCentral()
}
```

> Local development: `./gradlew publishToMavenLocal` in this repo, then add `mavenLocal()` to
`pluginManagement.repositories` and use the same plugin id/version in the consumer.

### 2) Put your IDL files

By convention, place IDL under:

```
src/<sourceSet>/avro/idl
```

For example:

```
src/main/avro/idl/user.avdl
```

### 3) Build

```bash
./gradlew build
```

Generated files will appear under:

```
build/generated/avro/<sourceSet>/
  ├─ schema/   # .avsc, namespaced into subfolders
  └─ protocol/ # .avpr (if enabled)
```

They are automatically included into the source set resources and end up in your JAR.

---

## Configuration

Expose the `avroIdl { ... }` extension in your consumer project:

```kotlin
avroIdl {
    // Where to search for .avdl. If empty, defaults to src/<sourceSet>/avro/idl
    sourceDirs.set(listOf(file("src/main/avro/idl")))

    // Base output directory (defaults to build/generated/avro)
    outputBaseDir.set(layout.buildDirectory.dir("generated/avro"))

    // Also write .avpr (protocol JSON). Defaults to true.
    writeProtocolJson.set(flase)
}
```

### Task names

For each source set a task is registered:

```
avroIdlToSchema<SourceSet>
```

Examples:

* `avroIdlToSchemaMain`
* `avroIdlToSchemaTest`

`processResources` depends on these tasks.

---

## Example

**`src/main/avro/idl/user.avdl`**

```avdl
@namespace("com.example.user")
protocol UserProtocol {
  record User {
    string id;
    string name;
    union { null, string } email = null;
  }

  record Address {
    string city;
    string street;
  }

  // you can also define messages if needed
  string greetUser(User user);
}
```

**Generated (pretty JSON)**

`build/generated/avro/main/schema/com/example/user/User.avsc`

```json
{
  "type": "record",
  "name": "User",
  "namespace": "com.example.user",
  "fields": [
    {
      "name": "id",
      "type": "string"
    },
    {
      "name": "name",
      "type": "string"
    },
    {
      "name": "email",
      "type": [
        "null",
        "string"
      ],
      "default": null
    }
  ]
}
```

`build/generated/avro/main/schema/com/example/user/Address.avsc`

```json
{
  "type": "record",
  "name": "Address",
  "namespace": "com.example.user",
  "fields": [
    {
      "name": "city",
      "type": "string"
    },
    {
      "name": "street",
      "type": "string"
    }
  ]
}
```

`build/generated/avro/main/protocol/com/example/user/UserProtocol.avpr`
(if `writeProtocolJson = true`)

```json
{
  "protocol": "UserProtocol",
  "namespace": "com.example.user",
  "types": [
    "... includes User, Address ..."
  ],
  "messages": {
    "greetUser": {
      "request": [
        {
          "name": "user",
          "type": "com.example.user.User"
        }
      ],
      "response": "string"
    }
  }
}
```

---

## How it works (internals)

The plugin uses `org.apache.avro:avro-idl:1.12.0`:

* Parses `.avdl` with `IdlReader.parse(Path)` (imports supported).
* Collects named types and writes one `.avsc` per type (`Schema.toString(true)`).
* Optionally writes a single `.avpr` per protocol (`Protocol.toString(true)`).
* Outputs are namespaced into directories under `schema/` and `protocol/`.

The Gradle task is cacheable and incremental thanks to correct `@InputFiles`,`@OutputDirectory`, and
`@PathSensitive` annotations.

---

## Requirements

* Gradle **8.x? :)** (tested with 9.0)
* JDK **17** (toolchains configured)
* Avro **1.12.0** (pulled by the plugin)
* Java or Java Library plugin applied in the consumer project

---

## Multi-module usage

Apply the plugin in each module that contains `.avdl` files. Each module will get its own
`avroIdlToSchema<SourceSet>` tasks and `build/generated/avro/<sourceSet>`
outputs wired into that module’s resources.

---

## Troubleshooting

* **No `.avdl` files found**
  The task will be skipped (`@SkipWhenEmpty`). Ensure files exist under
  `src/<sourceSet>/avro/idl` or set `avroIdl.sourceDirs`.

* **Imports not resolved**
  The plugin calls
  `IdlReader.parse(Path)`. Check that imported files are reachable relative to the parsed file’s directory.

* **Don’t want `.avpr` in the JAR**
  Set `writeProtocolJson = false`, or exclude
  `protocol/**` in your packaging if you prefer to keep schemas only.

---

## Versioning

This project follows **SemVer**. Plugin id: **`com.wizy.avro-idl`**. Example:

```kotlin
id("com.wizy.avro-idl") version "1.0.0"
```
