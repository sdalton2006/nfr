# Tears down the `nfr` CloudFormation stack and deploys it again from scratch.
# Parameters (stack name, region, profile, capabilities) come from samconfig.toml.
#
#   .\recreate.ps1              full delete + build + deploy cycle
#   .\recreate.ps1 -SkipDelete  build + deploy only (ordinary update)

[CmdletBinding()]
param(
    [switch]$SkipDelete
)

$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

# PowerShell 5.1 does not fail on a non-zero exit from a native exe, so check it explicitly.
function Invoke-Step {
    param([string]$Name, [scriptblock]$Step)

    Write-Host ""
    Write-Host "==> $Name" -ForegroundColor Cyan
    & $Step
    if ($LASTEXITCODE -ne 0) { throw "$Name failed (exit code $LASTEXITCODE)" }
}

if (-not $SkipDelete) {
    Invoke-Step 'sam delete' { sam delete }
}

# build.toml caches the resolved runtime, handler and BuildMethod from the previous
# run; stale entries survive a teardown and produce confusing build behaviour.
if (Test-Path .aws-sam) {
    Write-Host "==> clearing .aws-sam" -ForegroundColor Cyan
    Remove-Item -Recurse -Force .aws-sam
}

Invoke-Step 'sam build'  { sam build }
Invoke-Step 'sam deploy' { sam deploy }

Write-Host ""
Write-Host "Deployed. Endpoint (also listed in the Outputs table above):" -ForegroundColor Green

# `sam list` rather than `aws cloudformation describe-stacks`: the AWS CLI is a separate
# install from the SAM CLI and is not assumed to be present. Stack name, region and
# profile come from [default.global.parameters] in samconfig.toml, so no arguments here.
(sam list stack-outputs --output json | ConvertFrom-Json) |
    Where-Object { $_.OutputKey -eq 'HelloWorldApiUrl' } |
    ForEach-Object { $_.OutputValue }
