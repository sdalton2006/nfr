# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this project is

A single-module Gradle project that deploys one Java Lambda behind an API Gateway `GET /hello` endpoint, driven by AWS SAM. It was scaffolded from Spring Initializr, so a fair amount of Spring Boot machinery is present but **unused at runtime** — see "Two unrelated entry points" below before assuming the Spring context matters.

Stack: Java 25 toolchain, Gradle 9.5.1 (wrapper), Spring Boot plugin 4.1.0, GradleUp Shadow 9.6.1, AWS SAM CLI.

## Commands

Gradle (PowerShell — use `./gradlew` from the Bash tool):

```powershell
.\gradlew.bat build              # compile + test + jars
.\gradlew.bat shadowJar          # fat jar -> build/libs/nfr-0.0.1-SNAPSHOT-aws.jar
.\gradlew.bat test
.\gradlew.bat test --tests "com.nfr.NfrApplicationTests"              # single class
.\gradlew.bat test --tests "com.nfr.NfrApplicationTests.contextLoads" # single method
```

No linter or formatter is configured; don't invent one.

SAM (`sam` is on PATH at `C:\Program Files\Amazon\AWSSAMCLI\bin\sam.cmd`):

```powershell
sam build
sam deploy                                # create-or-update stack `nfr`, idempotent
sam delete                                # tear the stack down
.\delete.ps1                              # tear down all AWS resources, no-op if none exist
.\recreate.ps1                            # delete + clear .aws-sam + build + deploy
.\recreate.ps1 -SkipDelete                # build + deploy only
sam local invoke MyExistingApiFunction    # requires Docker
```

Those commands take no arguments because `samconfig.toml` holds everything: stack name `nfr`, region `eu-west-2`, profile `simon`, `CAPABILITY_IAM`, and `resolve_s3 = true` (SAM manages its own artifact bucket, so no bucket needs pre-creating before a recreate).

`sam deploy` is the deploy path to prefer. IntelliJ's **Sync Serverless Application** action runs `sam sync`, which bypasses CloudFormation and causes stack drift — it's an inner-loop tool, not a deploy. Its parameters live in the gitignored `.idea/` run config and differ from `samconfig.toml` (it passes `--s3-bucket nfr.uk.s3` and `CAPABILITY_NAMED_IAM CAPABILITY_AUTO_EXPAND`).

## Architecture

### Two unrelated entry points

- `MyApiHandler` — the real Lambda. A plain `RequestHandler<Map<String,Object>, Map<String,Object>>` with **no Spring involvement whatsoever**. It hand-builds the API Gateway proxy response envelope (`statusCode` / `headers` / `body` keys in a `HashMap`, body as a hand-escaped JSON string) rather than using `aws-lambda-java-events` types, which aren't a dependency.
- `NfrApplication` — vestigial `@SpringBootApplication` `main()` from the Initializr scaffold. Nothing invokes it in the deployed artifact. `NfrApplicationTests` (`@SpringBootTest contextLoads`) tests only this dead path, so a green test run says nothing about whether the Lambda works.

Consequence: adding Spring beans or `application.properties` config has no effect on the deployed function unless you first wire in a Spring-aware adapter. Changes that matter to the Lambda go in `MyApiHandler`.

### The packaging conflict

Three jar strategies coexist and disagree:

1. Spring Boot plugin contributes `bootJar`.
2. `build.gradle` configures `shadowJar` with classifier `aws` to produce a fat jar.
3. **SAM's Gradle builder ignores both.** For a `java*` runtime with a `build.gradle` next to `CodeUri`, aws-lambda-builders produces the *exploded* layout — compiled classes at the artifact root plus dependency jars under `lib/` — so `build/libs/nfr-0.0.1-SNAPSHOT-aws.jar` is never what gets deployed.

To deploy the fat jar instead, the mechanism is `BuildMethod: makefile` plus a `build-MyExistingApiFunction` target that runs `shadowJar` and copies the jar into `$(ARTIFACTS_DIR)`.

### template.yaml

Single `AWS::Serverless::Function` (`MyExistingApiFunction`) with an implicit `Api` event. SAM therefore synthesizes a REST API named `ServerlessRestApi` with two stages, `Prod` and `Stage`. The `HelloWorldApiUrl` output prints the `Prod` endpoint on every successful deploy:

```
https://<ServerlessRestApi-id>.execute-api.eu-west-2.amazonaws.com/Prod/hello
```

Do **not** add `Metadata: BuildMethod: gradle` back. It is not a valid `BuildMethod` — SAM accepts only `makefile`, `dotnet`/`dotnet7`, `rust-cargolambda`, `python-uv`, `esbuild`, or a runtime identifier used as an override — and it fails every build with `UnsupportedBuilderException: 'gradle' does not have a supported builder`. Java build tooling is auto-detected from the presence of `build.gradle` / `build.gradle.kts` / `pom.xml`. (`Runtime: java25` is valid; the installed SAM CLI recognizes it.)

## Tear-down and recreate

### delete.ps1

`.\delete.ps1` is the single-purpose teardown: it removes every AWS resource this project deploys and nothing else. It is a thin wrapper over `sam delete --no-prompts`, which is genuinely all that's needed — deleting the CloudFormation stack takes the Lambda, its IAM role, the `ServerlessRestApi` REST API and both stages with it, plus the build artifacts SAM uploaded to its managed S3 bucket. The script exists rather than a bare `sam delete` because it:

- **Reports an absent stack as success.** Running it twice, or before a first deploy, prints "Nothing to delete" and exits 0 instead of surfacing a CloudFormation error. It matches `does not exist` in the output *before* checking the exit code, since that case is a normal outcome and SAM versions differ on whether they signal it with a non-zero exit.
- **Sets `$ErrorActionPreference = 'Continue'`, deliberately.** The output is captured with `2>&1` to do that string match, and in PowerShell 5.1 a native exe's stderr merged that way arrives as `ErrorRecord` objects — under `'Stop'` a successful `sam delete` would abort the script.
- **Passes `--no-prompts` explicitly**, so it stays non-interactive even if `[default.delete.parameters] no_prompts` is removed from `samconfig.toml`.

It takes no arguments: stack name, region and profile all come from `[default.global.parameters]`. Note that it does *not* clear the local `.aws-sam` directory (that's `recreate.ps1`'s job) and cannot remove the leaked CloudWatch log groups described below — the SAM CLI has no CloudWatch Logs command, and neither the AWS CLI nor the AWS Tools for PowerShell modules are installed on this machine.

### recreate.ps1

`.\recreate.ps1` gives a clean cycle, but three things do not round-trip:

- **The endpoint URL changes.** No `FunctionName` is set, so CloudFormation generates fresh physical names and `ServerlessRestApi` gets a new API ID — a new hostname each cycle. A stable URL would need an API Gateway custom domain in a separate long-lived stack.
- **CloudWatch log groups leak.** Lambda auto-creates `/aws/lambda/<function-name>` on first invoke; CloudFormation doesn't own it, so `sam delete` leaves it behind and cycles accumulate orphans.
- **`.aws-sam/build.toml` caches the resolved runtime, handler and BuildMethod** across teardowns. `recreate.ps1` deletes the directory for this reason; do the same before diagnosing any odd build behaviour.

`[default.delete.parameters] no_prompts = true` in `samconfig.toml` means `sam delete` never asks for confirmation. That's deliberate for this dev stack.

Expect slow first invocations: a JVM cold start with `spring-boot-starter` on the classpath can approach the function's `Timeout: 15`, so an initial 502/504 is not necessarily a code defect.


##PowerShell
To ensure all powershell scripts within this project can be executed, the execution policy has been changed to Persistent, no admin needed — allows local unsigned scripts, still blocks unsigned downloaded ones by manually setting the following command.
`Set-ExecutionPolicy -Scope CurrentUser RemoteSigned` (no admin required). 
`recreate.ps1` is locally authored, so it carries no zone marker and needs no `Unblock-File`.