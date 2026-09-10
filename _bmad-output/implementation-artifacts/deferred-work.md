# Deferred Work

- source_spec: `_bmad-output/implementation-artifacts/spec-myapihandler-unit-test.md`
  summary: `.gitattributes` has no `* text=auto` rule, so `.gitignore` keeps acquiring mixed CRLF/LF line endings.
  evidence: `git diff` on `.gitignore` emits `warning: in the working copy of '.gitignore', LF will be replaced by CRLF`; `.gitattributes` covers only `/gradlew`, `*.bat`, `*.jar`.

- source_spec: `_bmad-output/implementation-artifacts/spec-myapihandler-unit-test.md`
  summary: The pending `.gitignore` additions skip the `### Section ###` header convention every other block in the file follows.
  evidence: Existing blocks are headed (`### AWS SAM ###`, `### VS Code ###`); the four new BMad/loop lines are bare appends.

- source_spec: `_bmad-output/implementation-artifacts/spec-myapihandler-unit-test.md`
  summary: The BMad tooling trees are half-ignored — `.bmad-loop/`, `.claude/` and `_bmad/` still show as untracked.
  evidence: `.gitignore` ignores only 4 sub-paths, so `git status` reports all three trees as `??`; each tree needs a per-tree decision (commit or ignore), not partial coverage.

- source_spec: `_bmad-output/implementation-artifacts/spec-myapihandler-unit-test.md`
  summary: `_bmad-output/` is not ignored and will dirty the worktree as soon as any artifact is written into it.
  evidence: The directory already exists; git hid it only because it was empty. This spec's own trace file is the first real artifact in it.

- source_spec: `_bmad-output/implementation-artifacts/spec-myapihandler-unit-test.md`
  summary: `.bmad-loop/policy.toml` is ignored alongside machine-local `runs/` and `cache/`, so loop policy can never be shared or version-controlled.
  evidence: `policy.toml` reads as shared configuration, unlike the two state directories it is grouped with in `.gitignore`.

- source_spec: `_bmad-output/implementation-artifacts/spec-myapihandler-unit-test.md`
  summary: Neither `CLAUDE.md` nor `working-notes.md` explains what `.bmad-loop/`, `_bmad/`, `.claude/` and `_bmad-output/` are.
  evidence: A future reader cannot tell whether these trees are installed tooling or project code, so cannot tell whether deleting them is safe.

- source_spec: `_bmad-output/implementation-artifacts/spec-myapihandler-unit-test.md`
  summary: `working-notes.md` — the tracked progress log — does not record the handler message change or the new test suite.
  evidence: The log ends at "Next step - try a small change on BMAD"; that change has since been made and left unrecorded.

- source_spec: `_bmad-output/implementation-artifacts/spec-myapihandler-unit-test.md`
  summary: `MyApiHandler.handleRequest` NPEs on a null `Context` (or a null logger) because it dereferences `context.getLogger()` unconditionally.
  evidence: `src/main/java/com/nfr/MyApiHandler.java:12` logs before building the response; this is production behaviour and was not introduced by the test, so it was not patched here.

- source_spec: `_bmad-output/implementation-artifacts/spec-myapihandler-unit-test.md`
  summary: Nothing guards the deploy-critical handler string — renaming the class or method keeps the suite green and breaks the deploy.
  evidence: `template.yaml` names `com.nfr.MyApiHandler::handleRequest` as a plain string. A reflective assertion that the configured handler still resolves would catch it.

- source_spec: `_bmad-output/implementation-artifacts/spec-myapihandler-unit-test.md`
  summary: The response promises CORS the API does not implement — permissive `Access-Control-Allow-Origin: *` with no `Cors` property and no `OPTIONS` route.
  evidence: `template.yaml` declares only `Method: get`, so a real cross-origin browser preflight still fails despite the header.

- source_spec: `_bmad-output/implementation-artifacts/spec-myapihandler-unit-test.md`
  summary: There is no CI, so the new suite runs only when someone remembers `.\gradlew.bat test` locally.
  evidence: No `.github/` directory exists; the change adds regression tests without adding anything that enforces them.

- source_spec: `_bmad-output/implementation-artifacts/spec-myapihandler-unit-test.md`
  summary: `NfrApplicationTests` remains an empty `@SpringBootTest contextLoads` over the dead Spring path, paying context-startup cost on every run.
  evidence: If dead it should be deleted; if kept deliberately, that rationale belongs in the file rather than only in `MyApiHandlerTest`'s Javadoc.

- source_spec: `_bmad-output/implementation-artifacts/spec-myapihandler-unit-test.md`
  summary: The response payload carries no build or version marker, so the greeting wording is the only signal that a deploy shipped new code.
  evidence: `recreate.ps1` yields a fresh endpoint hostname each cycle, making the message string the de facto deploy fingerprint.
