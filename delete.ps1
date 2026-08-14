# Deletes every AWS resource this project deploys, by tearing down its CloudFormation stack.
#
#   .\delete.ps1
#
# Safe to run when nothing is deployed: it says so and exits 0.

[CmdletBinding()]
param()

Set-Location $PSScriptRoot

# `sam delete` is the whole job. Stack name (`nfr`), region and profile come from
# [default.global.parameters] in samconfig.toml, so no arguments are needed here, and
# --no-prompts keeps it non-interactive even if no_prompts is ever dropped from that file.
#
# It removes the Lambda, its IAM role, the REST API and both stages (Prod and Stage),
# along with the build artifacts sam uploaded to its managed S3 bucket.
#
# One thing it cannot remove: the /aws/lambda/<function-name> CloudWatch log group. Lambda
# creates that on first invoke, so CloudFormation never owned it and it survives teardown.
Write-Host "==> sam delete" -ForegroundColor Cyan

# 'Continue' rather than 'Stop': in PowerShell 5.1, native stderr merged via 2>&1 arrives
# as ErrorRecords, and 'Stop' would treat those as fatal even when sam exits 0.
$ErrorActionPreference = 'Continue'

$output = & sam delete --no-prompts 2>&1 | Out-String
$exitCode = $LASTEXITCODE

Write-Host $output

# Checked before the exit code, because an absent stack is a normal outcome here and
# different sam versions report it as either a zero or a non-zero exit.
if ($output -match 'does not exist') {
    Write-Host "Nothing to delete: no AWS resources for this project exist." -ForegroundColor Green
    exit 0
}

if ($exitCode -eq 0) {
    Write-Host "Deleted. The Lambda, IAM role and REST API are gone." -ForegroundColor Green
    Write-Host "Any /aws/lambda/* log group from a past invoke is left behind." -ForegroundColor DarkGray
    exit 0
}

Write-Host "sam delete failed (exit code $exitCode)." -ForegroundColor Red
exit $exitCode
