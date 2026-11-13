package com.attendance;

/**
 * Lightweight test framework (no JUnit dependency).
 */
public class TestFramework {

    private static int passed = 0;
    private static int failed = 0;

    public static void assertEquals(Object expected, Object actual, String testName) {
        if (expected == null && actual == null) {
            pass(testName);
        } else if (expected != null && expected.equals(actual)) {
            pass(testName);
        } else {
            fail(testName, "Expected: " + expected + ", Actual: " + actual);
        }
    }

    public static void assertEqualsDouble(double expected, double actual, double delta, String testName) {
        if (Math.abs(expected - actual) <= delta) {
            pass(testName);
        } else {
            fail(testName, "Expected: " + expected + " (±" + delta + "), Actual: " + actual);
        }
    }

    public static void assertTrue(boolean condition, String testName) {
        if (condition) {
            pass(testName);
        } else {
            fail(testName, "Expected true but was false");
        }
    }

    public static void assertFalse(boolean condition, String testName) {
        if (!condition) {
            pass(testName);
        } else {
            fail(testName, "Expected false but was true");
        }
    }

    public static void assertThrows(Class<? extends Exception> expected, Runnable code, String testName) {
        try {
            code.run();
            fail(testName, "Expected " + expected.getSimpleName() + " but no exception was thrown");
        } catch (Exception e) {
            if (expected.isInstance(e)) {
                pass(testName);
            } else {
                fail(testName, "Expected " + expected.getSimpleName() + " but got " +
                        e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }

    /** Expect ANY exception (for broad validation). */
    public static void assertAnyThrows(Runnable code, String testName) {
        try {
            code.run();
            fail(testName, "Expected an exception but none was thrown");
        } catch (Exception e) {
            pass(testName + " (caught " + e.getClass().getSimpleName() + ")");
        }
    }

    private static void pass(String testName) {
        passed++;
        System.out.println("  ✓ PASS: " + testName);
    }

    private static void fail(String testName, String detail) {
        failed++;
        System.out.println("  ✗ FAIL: " + testName + " — " + detail);
    }

    public static void printSummary() {
        int total = passed + failed;
        System.out.println("\n=========================================");
        System.out.println("  TEST RESULTS: " + passed + "/" + total + " passed, " + failed + " failed");
        System.out.println("=========================================");
    }

    public static int getFailedCount() { return failed; }

    public static void reset() { passed = 0; failed = 0; }
}
