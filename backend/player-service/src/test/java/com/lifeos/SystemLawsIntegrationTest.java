package com.lifeos;

import org.junit.jupiter.api.Disabled;

@Disabled("System laws need Java service-level testing, not DB triggers")
public class SystemLawsIntegrationTest {
    // Tests disabled - the business logic is tested through service unit tests
    // The DB trigger approach was H2-specific and not compatible with MySQL
}
