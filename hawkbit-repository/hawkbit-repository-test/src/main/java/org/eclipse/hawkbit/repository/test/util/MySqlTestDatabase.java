/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.test.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * A {@link TestExecutionListener} for creating and dropping MySql schemas if
 * tests are setup with MySql.
 */
public class MySqlTestDatabase extends AbstractTestExecutionListener {

    private static final Logger LOG = LoggerFactory.getLogger(MySqlTestDatabase.class);
    private String schemaName;
    private String uri;
    private final String username;
    private final String password;

    /**
     * Constructor.
     */
    public MySqlTestDatabase() {
        this.username = System.getProperty("spring.datasource.username");
        this.password = System.getProperty("spring.datasource.password");
        this.uri = System.getProperty("spring.datasource.url");
        createSchemaUri();
    }

    private void createSchemaUri() {
        schemaName = "SP" + RandomStringUtils.randomAlphanumeric(10);
        this.uri = this.uri.substring(0, uri.lastIndexOf('/') + 1);

        System.setProperty("spring.datasource.url", uri + schemaName);
    }

    @Override
    public void beforeTestClass(final TestContext testContext) throws Exception {
        if (isRunningWithMySql()) {
            createSchema();
        }
    }

    @Override
    public void afterTestClass(final TestContext testContext) throws Exception {
        if (isRunningWithMySql()) {
            dropSchema();
        }
    }

    private boolean isRunningWithMySql() {
        return "MYSQL".equals(System.getProperty("spring.jpa.database"));
    }

    private void createSchema() {
        try (Connection connection = DriverManager.getConnection(uri, username, password)) {
            try (PreparedStatement statement = connection.prepareStatement("CREATE SCHEMA " + schemaName + ";")) {
                statement.execute();
                LOG.info("Schema {} created on uri {}", schemaName, uri);
            } finally {
                connection.commit();
            }
        } catch (final SQLException e) {
            LOG.error("Schema creation failed!", e);
        }

    }

    private void dropSchema() {
        try (Connection connection = DriverManager.getConnection(uri, username, password)) {
            try (PreparedStatement statement = connection.prepareStatement("DROP SCHEMA " + schemaName + ";")) {
                statement.execute();
                LOG.info("Schema {} dropped on uri {}", schemaName, uri);
            } finally {
                connection.commit();
            }
        } catch (final SQLException e) {
            LOG.error("Schema drop failed!", e);
        }
    }
}