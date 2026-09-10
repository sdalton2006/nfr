---
title: 'Unit tests for MyApiHandler'
type: 'chore'
created: '2026-09-04'
status: 'done'
route: 'one-shot'
---

# Unit tests for MyApiHandler

## Intent

**Problem:** `MyApiHandler` — the only code that actually runs in the deployed Lambda — had no test at all. The single existing test, `NfrApplicationTests`, exercises the vestigial Spring path, so a green run said nothing about whether the endpoint works.

**Approach:** Added a plain JUnit 5 suite that calls `handleRequest` directly and asserts the API Gateway proxy envelope (`statusCode` / `headers` / hand-built JSON `body`), plus a second suite that reflectively resolves the handler string from `template.yaml` so a class or method rename fails in tests instead of at deploy time. Test dependencies were declared explicitly and Mockito moved to a JVM agent rather than self-attaching.

## Suggested Review Order

**Lambda response contract**

- Entry point: the envelope shape the deployed function must return.
  [`MyApiHandlerTest.java:44`](../../src/test/java/com/nfr/MyApiHandlerTest.java#L44)

- Exact-byte assertion on the hand-built JSON body; catches a broken escape.
  [`MyApiHandlerTest.java:61`](../../src/test/java/com/nfr/MyApiHandlerTest.java#L61)

- Proves no code path reads the event, so adding one cannot silently NPE.
  [`MyApiHandlerTest.java:91`](../../src/test/java/com/nfr/MyApiHandlerTest.java#L91)

- Logger is stubbed because the handler logs unconditionally before responding.
  [`MyApiHandlerTest.java:31`](../../src/test/java/com/nfr/MyApiHandlerTest.java#L31)

- Verifies the CloudWatch log line survives refactoring.
  [`MyApiHandlerTest.java:72`](../../src/test/java/com/nfr/MyApiHandlerTest.java#L72)

**Deploy wiring guard**

- Line-scans the template rather than parsing YAML, which `!Sub` tags break.
  [`TemplateHandlerWiringTest.java:34`](../../src/test/java/com/nfr/TemplateHandlerWiringTest.java#L34)

- Reflectively resolves the configured class; a rename now fails here.
  [`TemplateHandlerWiringTest.java:59`](../../src/test/java/com/nfr/TemplateHandlerWiringTest.java#L59)

- Pins the erased `(Map, Context)` signature the runtime invokes.
  [`TemplateHandlerWiringTest.java:66`](../../src/test/java/com/nfr/TemplateHandlerWiringTest.java#L66)

- Confirms the public no-arg constructor the runtime needs still exists.
  [`TemplateHandlerWiringTest.java:76`](../../src/test/java/com/nfr/TemplateHandlerWiringTest.java#L76)

**Build configuration**

- Mockito loaded as a JVM agent; self-attaching is deprecated and will break.
  [`build.gradle:54`](../../build.gradle#L54)

- Agent jar resolved lazily to keep the test task configuration-cache friendly.
  [`build.gradle:49`](../../build.gradle#L49)

- Test deps declared explicitly so Lambda tests stop leaning on Spring's starter.
  [`build.gradle:40`](../../build.gradle#L40)

**Documentation**

- Corrects the now-false claim that a green run says nothing about the Lambda.
  [`CLAUDE.md:46`](../../CLAUDE.md#L46)
