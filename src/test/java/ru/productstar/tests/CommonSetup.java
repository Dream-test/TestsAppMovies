package ru.productstar.tests;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public interface CommonSetup {

    @BeforeAll
    static void initDBConnection() {
        InitDBConnection.initDBConnection();
    }

    @AfterAll
    static void closeDBConnection() {
        InitDBConnection.closeDBConnection();
    }


}
