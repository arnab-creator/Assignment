//FIX:

public List<LoanAccount> getOverdueLoans(List<LoanAccount> accounts) {
    // FIX: Wrong initialisation of result is fixed; result.add() would throw NullPointerException
    //      on every invocation. Initialised to a new ArrayList instead.Correct initialisation done.
    List<LoanAccount> result = new ArrayList<>();

    for (LoanAccount account : accounts) {
        // FIX: dueDate may be null for restructured accounts (per field contract).
        //      Calling .before() on a null Date throws NullPointerException. Added null-guard.
        if (account.getDueDate() != null && account.getDueDate().before(new Date())) {
            // FIX: original > 0 is logically correct for excluding zero-balance
            //      accounts. No change to comparison value needed. But in a
            //      production banking system, double is unsafe for currency; BigDecimal
            //      should be used, but field type is outside this fix's scope hence just pointed out.
            if (account.getOutstandingBalance() > 0) {
                result.add(account);
            }
        }
    }
    return result;
}

--------------------------------------------------------------------------------------------------------------------------------------------------------

JUnit test that reproduces the bug and asserts the fix.

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Task1BonusTest {

    private final LoanAccountService service = new LoanAccountService();

    // Reproduces Defect 1 — result was null, add() throws NullPointerException
    @Test
    void shouldReturnEmptyList_whenNoAccountsAreOverdue() {
        List<LoanAccount> accounts = new ArrayList<>();
        List<LoanAccount> result = service.getOverdueLoans(accounts);
        assertNotNull(result, "Result list must never be null");
    }

    // Reproduces Defect 2 — null dueDate throws NullPointerException
    @Test
    void shouldSkipAccount_whenDueDateIsNull() {
        LoanAccount account = new LoanAccount();
        account.setDueDate(null);
        account.setOutstandingBalance(5000.0);
        account.setAccountId("ACC001");

        List<LoanAccount> accounts = List.of(account);

        assertDoesNotThrow(() -> service.getOverdueLoans(accounts),
                "Null dueDate should be skipped, not throw NullPointerException");
    }

    // Reproduces Defect 3 — zero balance accounts should be excluded
    @Test
    void shouldExcludeAccount_whenOutstandingBalanceIsZero() {
        LoanAccount account = new LoanAccount();
        account.setDueDate(new Date(System.currentTimeMillis() - 86400000L)); // yesterday
        account.setOutstandingBalance(0.0);
        account.setAccountId("ACC002");

        List<LoanAccount> accounts = List.of(account);
        List<LoanAccount> result = service.getOverdueLoans(accounts);

        assertTrue(result.isEmpty(), "Zero balance account must not appear in overdue list");
    }

    // Asserts correct result — overdue account with positive balance is included
    @Test
    void shouldIncludeAccount_whenOverdueAndBalancePositive() {
        LoanAccount account = new LoanAccount();
        account.setDueDate(new Date(System.currentTimeMillis() - 86400000L)); // yesterday
        account.setOutstandingBalance(10000.0);
        account.setAccountId("ACC003");

        List<LoanAccount> accounts = List.of(account);
        List<LoanAccount> result = service.getOverdueLoans(accounts);

        assertEquals(1, result.size(), "Overdue account with positive balance must be included");
        assertEquals("ACC003", result.get(0).getAccountId());
    }
}