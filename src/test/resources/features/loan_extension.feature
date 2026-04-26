Feature: Loan Extension Management

  As a borrower
  I want to request an extension for my active loan
  So that I have more time to repay the principal

  As a lender
  I want to review and respond to extension requests
  So that I can agree to new terms or decline them

  Background:
    Given a borrower named "Alice" and a lender named "Bob"
    And an active loan exists between "Alice" and "Bob" with 1 months tenure

  Scenario: Borrower successfully requests an extension
    When "Alice" requests an extension of 1 months with 12% interest
    Then the loan status should be "EXTENSION_REQUESTED"
    And a pending extension request should exist for the loan

  Scenario: Lender approves an extension request
    Given "Alice" has a pending extension request for 1 months
    When "Bob" approves the extension request
    Then the loan status should be "ACTIVE"
    Then the loan tenure should be 1 months
    And the extension request status should be "APPROVED"

  Scenario: Lender rejects an extension request
    Given "Alice" has a pending extension request for 1 months
    When "Bob" rejects the extension request
    Then the loan status should be "ACTIVE"
    And the loan tenure should be 30 days
    And the extension request status should be "REJECTED"

  Scenario: Unauthorized user cannot request an extension
    When "Bob" tries to request an extension for "Alice"'s loan
    Then the request should be denied with an error "Only the borrower can request an extension"
