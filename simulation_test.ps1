# ============================================================================
# LinkBit MVP - Full Lifecycle Simulation Test Script (ROBUST)
# ============================================================================

$BASE = "http://localhost:8080"
$results = @()

function Log($phase, $step, $status, $detail) {
    $entry = [PSCustomObject]@{
        Timestamp = (Get-Date -Format "yyyy-MM-dd HH:mm:ss")
        Phase = $phase
        Step = $step
        Status = $status
        Detail = $detail
    }
    $script:results += $entry
    $color = if ($status -eq "PASS") { "Green" } elseif ($status -eq "FAIL") { "Red" } else { "Yellow" }
    Write-Host "[$status] $phase > $step : $detail" -ForegroundColor $color
    if ($status -eq "FAIL") { throw "Simulation failed at $phase > $step" }
}

function ApiCall($method, $uri, $body, $token) {
    $headers = @{}
    if ($token) { $headers["Authorization"] = "Bearer $token" }
    $params = @{ Uri = "$BASE$uri"; Method = $method; ContentType = "application/json"; Headers = $headers }
    if ($body) { $params["Body"] = ($body | ConvertTo-Json -Depth 5) }
    try {
        $resp = Invoke-RestMethod @params
        return @{ Success = $true; Data = $resp }
    } catch {
        $statusCode = 0
        $errBody = ""
        try { 
            $statusCode = [int]$_.Exception.Response.StatusCode
            $streamReader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $errBody = $streamReader.ReadToEnd()
        } catch {}
        return @{ Success = $false; StatusCode = $statusCode; Error = $errBody; Exception = $_.Exception.Message }
    }
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host " LINKBIT MVP - FULL LIFECYCLE SIMULATION" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# --- ADMIN LOGIN ---
$r = ApiCall "POST" "/auth/login" @{ email="admin@linkbit.com"; password="password" }
if (-not $r.Success) { throw "Admin login failed" }
$adminToken = $r.Data.accessToken

# --- PHASE 1: USER REGISTRATION & KYC ---
Write-Host "`n--- PHASE 1: User Registration & KYC ---" -ForegroundColor Magenta

ApiCall "POST" "/auth/register" @{ name="Lender"; email="lender@test.com"; password="Password123!"; dob="1990-01-01" }
$r = ApiCall "POST" "/auth/login" @{ email="lender@test.com"; password="Password123!" }
if (-not $r.Success) { throw "Lender login failed" }
$lToken = $r.Data.accessToken
$r = ApiCall "POST" "/auth/kyc/submit" @{ fullLegalName="Lender Full"; bankAccountNumber="123"; ifsc="IFSC"; upiId="lender@upi" } $lToken
if ($r.Success) { Log "Phase1" "Lender KYC" "PASS" "Submitted" } else { Log "Phase1" "Lender KYC" "FAIL" $r.Error }

ApiCall "POST" "/auth/register" @{ name="Borrower"; email="borrower@test.com"; password="Password123!"; dob="1995-01-01" }
$r = ApiCall "POST" "/auth/login" @{ email="borrower@test.com"; password="Password123!" }
if (-not $r.Success) { throw "Borrower login failed" }
$bToken = $r.Data.accessToken
$r = ApiCall "POST" "/auth/kyc/submit" @{ fullLegalName="Borrower Full"; bankAccountNumber="456"; ifsc="IFSC"; upiId="borrower@upi" } $bToken
if ($r.Success) { Log "Phase1" "Borrower KYC" "PASS" "Submitted" } else { Log "Phase1" "Borrower KYC" "FAIL" $r.Error }

$apps = (ApiCall "GET" "/admin/kyc-applications" $null $adminToken).Data
foreach ($app in $apps) {
    $r = ApiCall "POST" "/admin/users/$($app.userId)/kyc/approve" $null $adminToken
    if ($r.Success) { Log "Phase1" "Admin Approve" "PASS" "Approved $($app.fullLegalName)" }
}

# --- PHASE 4: OFFER & CONNECTION ---
Write-Host "`n--- PHASE 4: Offer Creation & Connection ---" -ForegroundColor Magenta

$lToken = (ApiCall "POST" "/auth/login" @{ email="lender@test.com"; password="Password123!" }).Data.accessToken
$bToken = (ApiCall "POST" "/auth/login" @{ email="borrower@test.com"; password="Password123!" }).Data.accessToken

$r = ApiCall "POST" "/offers" @{ loan_amount_inr=5000; interest_rate=10; expected_ltv_percent=50; tenure_days=30 } $lToken
if ($r.Success) { Log "Phase4" "Create Offer" "PASS" "Success" } else { Log "Phase4" "Create Offer" "FAIL" $r.Error }

$offer = (ApiCall "GET" "/offers" $null $bToken).Data | Where-Object { $_.loan_amount -eq 5000 } | Select-Object -First 1
$r = ApiCall "POST" "/loans/connect" @{ offer_id = $offer.offer_id } $bToken
if ($r.Success) { 
    $loanId = $r.Data
    Log "Phase4" "Connect Offer" "PASS" "Loan ID: $loanId" 
} else { Log "Phase4" "Connect Offer" "FAIL" $r.Error }

# --- PHASE 6: NEGOTIATION & SIGNING ---
Write-Host "`n--- PHASE 6: Negotiation & Signing ---" -ForegroundColor Magenta

$r1 = ApiCall "POST" "/loans/$loanId/finalize" $null $bToken
$r2 = ApiCall "POST" "/loans/$loanId/finalize" $null $lToken
if ($r1.Success -and $r2.Success) { Log "Phase6" "Finalize" "PASS" "Both parties finalized" } else { Log "Phase6" "Finalize" "FAIL" "B:$($r1.StatusCode) L:$($r2.StatusCode)" }

$r1 = ApiCall "POST" "/loans/$loanId/sign" @{ signature_string="BORROWER_SIG" } $bToken
$r2 = ApiCall "POST" "/loans/$loanId/sign" @{ signature_string="LENDER_SIG" } $lToken
if ($r1.Success -and $r2.Success) { Log "Phase6" "Sign" "PASS" "Both parties signed" } else { Log "Phase6" "Sign" "FAIL" "B:$($r1.StatusCode) L:$($r2.StatusCode)" }

# --- PHASE 8: FEE PAYMENTS ---
Write-Host "`n--- PHASE 8: Fee Payments ---" -ForegroundColor Magenta

$bFee = (ApiCall "POST" "/payments/fee/pay" @{ loan_id = $loanId } $bToken).Data
$lFee = (ApiCall "POST" "/payments/fee/pay" @{ loan_id = $loanId } $lToken).Data
if ($bFee -and $lFee) { Log "Phase8" "Initiate Fees" "PASS" "Both parties initiated fees" } else { Log "Phase8" "Initiate Fees" "FAIL" "Missing fee IDs" }

$r1 = ApiCall "POST" "/admin/fees/$($bFee.fee_id)/verify" $null $adminToken
$r2 = ApiCall "POST" "/admin/fees/$($lFee.fee_id)/verify" $null $adminToken
if ($r1.Success -and $r2.Success) { Log "Phase8" "Admin Verify Fees" "PASS" "Both fees verified" } else { Log "Phase8" "Admin Verify Fees" "FAIL" "B:$($r1.StatusCode) L:$($r2.StatusCode) $($r1.Error) $($r2.Error)" }

# --- PHASE 9: COLLATERAL ---
Write-Host "`n--- PHASE 9: Collateral Deposit ---" -ForegroundColor Magenta

ApiCall "POST" "/loans/$loanId/escrow/generate" $null $bToken
$r = ApiCall "POST" "/loans/$loanId/deposit" @{ amount_btc=0.002 } $bToken
if ($r.Success) { Log "Phase9" "Deposit" "PASS" "Collateral deposited" } else { Log "Phase9" "Deposit" "FAIL" $r.Error }

$r = ApiCall "POST" "/admin/loans/$loanId/verify-deposit" $null $adminToken
if ($r.Success) { Log "Phase9" "Verify Deposit" "PASS" "Deposit verified" } else { Log "Phase9" "Verify Deposit" "FAIL" $r.Error }

# --- PHASE 10: DISBURSEMENT ---
Write-Host "`n--- PHASE 10: Disbursement ---" -ForegroundColor Magenta

$r = ApiCall "POST" "/loans/$loanId/disburse" @{ transaction_reference="TX-DISB-001"; proof_image_url="http://proof" } $lToken
if ($r.Success) { Log "Phase10" "Disburse" "PASS" "Lender marked disbursed" } else { Log "Phase10" "Disburse" "FAIL" $r.Error }

$r = ApiCall "POST" "/loans/$loanId/confirm-receipt" $null $bToken
if ($r.Success) { Log "Phase10" "Acknowledge" "PASS" "Borrower acknowledged receipt" } else { Log "Phase10" "Acknowledge" "FAIL" $r.Error }

# --- PHASE 11: REPAYMENT ---
Write-Host "`n--- PHASE 11: Repayment ---" -ForegroundColor Magenta

$details = (ApiCall "GET" "/loans/$loanId/details" $null $bToken).Data
$repayAmount = $details.totalOutstanding
$r = ApiCall "POST" "/loans/$loanId/repay" @{ amount=$repayAmount; transaction_reference="TX-REPAY-001"; proof_image_url="http://proof" } $bToken
if ($r.Success) { Log "Phase11" "Repay" "PASS" "Repayment submitted: $repayAmount" } else { Log "Phase11" "Repay" "FAIL" $r.Error }

$pendingRepays = (ApiCall "GET" "/admin/repayments/pending" $null $adminToken).Data
$repayment = $pendingRepays | Where-Object { $_.loanId -eq $loanId } | Select-Object -First 1
if (-not $repayment) { Log "Phase11" "Find Repayment" "FAIL" "Not found in pending list" }

$r = ApiCall "POST" "/admin/repayments/$($repayment.repaymentId)/verify" $null $adminToken
if ($r.Success) { Log "Phase11" "Admin Verify" "PASS" "Repayment verified" } else { Log "Phase11" "Admin Verify" "FAIL" "$($r.StatusCode) $($r.Error)" }

# --- PHASE 12: FINAL CHECK ---
Write-Host "`n--- PHASE 12: Final Check ---" -ForegroundColor Magenta
$final = (ApiCall "GET" "/loans/$loanId/details" $null $bToken).Data
Log "Phase12" "Final Status" "PASS" "Status: $($final.status)"

if ($final.status -eq "CLOSED") {
    Write-Host "`nLIFECYCLE TEST COMPLETED SUCCESSFULLY!`n" -ForegroundColor Green
} else {
    Write-Host "`nLIFECYCLE TEST FAILED. Final status: $($final.status)`n" -ForegroundColor Red
    exit 1
}

$results | Format-Table -Property Phase, Step, Status, Detail -AutoSize
