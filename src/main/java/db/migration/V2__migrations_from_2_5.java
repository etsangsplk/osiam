/**
 * The MIT License (MIT)
 *
 * Copyright (C) 2013-2016 tarent solutions GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package db.migration;

import org.flywaydb.core.api.migration.MigrationChecksumProvider;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class V2__migrations_from_2_5 implements JdbcMigration, MigrationChecksumProvider {

    private final Map<String, Long> internalIds = new HashMap<>();
    private Connection connection;

    @Override
    public Integer getChecksum() {
        return 1546262391;
    }

    @Override
    public void migrate(Connection connection) throws Exception {
        this.connection = connection;
        removeClient("auth-server");
        addDisplayToMultiValuedAttributes();
    }

    private void addDisplayToMultiValuedAttributes() throws SQLException {
        try (PreparedStatement statement = connection
                .prepareStatement("ALTER TABLE scim_address ADD display VARCHAR(255)")) {
            statement.execute();
        }

        try (PreparedStatement statement = connection
                .prepareStatement("ALTER TABLE scim_certificate ADD display VARCHAR(255)")) {
            statement.execute();
        }

        try (PreparedStatement statement = connection
                .prepareStatement("ALTER TABLE scim_email ADD display VARCHAR(255)")) {
            statement.execute();
        }

        try (PreparedStatement statement = connection
                .prepareStatement("ALTER TABLE scim_entitlements ADD display VARCHAR(255)")) {
            statement.execute();
        }

        try (PreparedStatement statement = connection
                .prepareStatement("ALTER TABLE scim_im ADD display VARCHAR(255)")) {
            statement.execute();
        }

        try (PreparedStatement statement = connection
                .prepareStatement("ALTER TABLE scim_phonenumber ADD display VARCHAR(255)")) {
            statement.execute();
        }

        try (PreparedStatement statement = connection
                .prepareStatement("ALTER TABLE scim_photo ADD display VARCHAR(255)")) {
            statement.execute();
        }

        try (PreparedStatement statement = connection
                .prepareStatement("ALTER TABLE scim_roles ADD display VARCHAR(255)")) {
            statement.execute();
        }
    }

    private void removeClient(String clientId) throws SQLException {
        long internalId = toInternalId(clientId);
        if (internalId < 0) {
            return;
        }
        try (PreparedStatement statement = connection
                .prepareStatement("delete from osiam_client_scopes where id = ?")) {
            statement.setLong(1, internalId);
            statement.execute();
        }
        try (PreparedStatement statement = connection
                .prepareStatement("delete from osiam_client_grants where id = ?")) {
            statement.setLong(1, internalId);
            statement.execute();
        }
        try (PreparedStatement statement = connection
                .prepareStatement("delete from osiam_client where internal_id = ?")) {
            statement.setLong(1, internalId);
            statement.execute();
        }
    }

    private long toInternalId(String clientId) {
        if (!internalIds.containsKey(clientId)) {
            try (PreparedStatement statement = connection
                    .prepareStatement("select internal_id from osiam_client where id = ?")) {
                statement.setString(1, clientId);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    internalIds.put(clientId, resultSet.getLong(1));
                } else {
                    internalIds.put(clientId, -1L);
                }
            } catch (SQLException e) {
                internalIds.put(clientId, -1L);
            }
        }
        return internalIds.get(clientId);
    }
}
