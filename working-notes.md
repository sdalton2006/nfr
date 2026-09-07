# Working Notes

## BMAD 
[BMAD git project](https://github.com/bmad-code-org/bmad-method) - official documentation
- installed required pre-requisite tools, npm, python and uv
  - also attempted to install additional tools, but failed at Visual Studio and I aborted
- BMad Loop installed with Claude code. To finish setup, run the bmad-loop-setup skill from your agent: 
  - use the bmad-loop-setup skill

[BMAD website](https://www.bmadcode.com/) with additional learning info

| +-BMAD is ready to use!------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| |    ✓  Shared scripts                                                                                                                                                                   |
| |    ✓  BMad Core Module (v6.11.0, installed)                                                                                                                                            |
| |    ✓  BMad Method (v6.11.0, installed)                                                                                                                                                 |
| |    ✓  BMad Builder (v2.2.2, installed)                                                                                                                                                 |
| |    ✓  CIS: Creative Innovation Suite (v0.3.2, installed)                                                                                                                               |
| |    ✓  Test Architect (v1.24.0, installed)                                                                                                                                              |
| |    ✓  BMAD Loop Skills (v0.11.1, installed)                                                                                                                                            |
| |    ✓  Module directories                                                                                                                                                               |
| |    ✓  Configurations (generated)                                                                                                                                                       |
| |    ✓  Help catalog                                                                                                                                                                     |
| |    ✓  claude-code (77 skills → .claude/skills)                                                                                                                                         |
| |                                                                                                                                                                                        |
| |    Installed to: C:\Users\theda\_bmad                                                                                                                                                  |
| |                                                                                                                                                                                        |
| |    Get started:                                                                                                                                                                        |
| |      1. Launch your AI agent from your project folder                                                                                                                                  |
| |      2. Not sure what to do? Invoke the bmad-help skill and ask it what to do!                                                                                                         |
| |                                                                                                                                                                                        |
| |      Blog, Docs and Guides: https://bmadcode.com/                                                                                                                                      |
| |      Community: https://discord.gg/gk8jAdXWmj                                                                                                                                          |
| +----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+

Pre-requisites for BMAD loop skills is psmux and power shell version 7. psmux allows tasks to run on multiple PS terminals and to continue the task is the terminal is stopped.

**4-Sep-26**
- Tried a [small change on BMAD](https://docs.bmad-method.org/existing-codebases/start-in-an-existing-codebase/)
The change was to create a unit test for myApiHandler and the template.yaml file
- Changes have been committed.
- Mockito was used and learnt.
- Need to check a bit more on BMAD & Claude folders that are not currently in git.
- Read the MyApiHandler test but not read the templates.yaml test yet.
- Need to run the unit test again to see if its working.

**7-Sep-26**
- Read the templates.yaml test
- Need to check a bit more on BMAD & Claude folders that are not currently in git.
- Need to run the unit test again to see if its working.
- Need to check which skill file was updated to create this unit test.

