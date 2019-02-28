/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.shardingjdbc.jdbc.core.statement;

import com.google.common.collect.Collections2;
import lombok.SneakyThrows;
import org.apache.shardingsphere.core.optimizer.OptimizeEngineFactory;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.rewrite.EncryptSQLRewriteEngine;
import org.apache.shardingsphere.core.rewrite.SQLBuilder;
import org.apache.shardingsphere.shardingjdbc.jdbc.adapter.AbstractShardingPreparedStatementAdapter;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.connection.EncryptConnection;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.resultset.EncryptResultSet;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Encrypt prepared statement.
 *
 * @author panjuan
 */
public final class EncryptPreparedStatement extends AbstractShardingPreparedStatementAdapter {
    
    private final PreparedStatement statement;
    
    private final EncryptConnection connection;
    
    private EncryptResultSet resultSet;
    
    @SneakyThrows
    public EncryptPreparedStatement(final EncryptConnection connection, final String sql) {
        statement = connection.getConnection().prepareStatement(getRewriteSQL(sql));
        this.connection = connection;
    }
    
    @SneakyThrows
    public EncryptPreparedStatement(final EncryptConnection connection, final String sql, final int resultSetType, final int resultSetConcurrency) {
        statement = connection.getConnection().prepareStatement(getRewriteSQL(sql), resultSetType, resultSetConcurrency);
        this.connection = connection;
    }
    
    @SneakyThrows
    public EncryptPreparedStatement(final EncryptConnection connection, final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) {
        statement = connection.getConnection().prepareStatement(getRewriteSQL(sql), resultSetType, resultSetConcurrency, resultSetHoldability);
        this.connection = connection;
    }
    
    @SneakyThrows
    public EncryptPreparedStatement(final EncryptConnection connection, final String sql, final int autoGeneratedKeys) {
        statement = connection.getConnection().prepareStatement(getRewriteSQL(sql), autoGeneratedKeys);
        this.connection = connection;
    }
    
    @SneakyThrows
    public EncryptPreparedStatement(final EncryptConnection connection, final String sql, final int[] columnIndexes) {
        statement = connection.getConnection().prepareStatement(getRewriteSQL(sql), columnIndexes);
        this.connection = connection;
    }
    
    @SneakyThrows
    public EncryptPreparedStatement(final EncryptConnection connection, final String sql, final String[] columnNames) {
        statement = connection.getConnection().prepareStatement(getRewriteSQL(sql), columnNames);
        this.connection = connection;
    }
    
    @Override
    public ResultSet executeQuery() throws SQLException {
        ResultSet resultSet = statement.executeQuery();
        this.resultSet = new EncryptResultSet(this, resultSet, connection.getEncryptRule());
        return resultSet;
    }
    
    private String getRewriteSQL(final String sql) {
        SQLStatement sqlStatement = connection.getEncryptSQLParsingEngine().parse(false, sql);
        OptimizeEngineFactory.newInstance(connection.getEncryptRule(), sqlStatement, new LinkedList<>()).optimize();
        SQLBuilder sqlBuilder = new EncryptSQLRewriteEngine(connection.getEncryptRule(), sql, connection.getDatabaseType(), sqlStatement, new LinkedList<>()).rewrite();
        return sqlBuilder.toSQL().getSql();
    }
    
    @Override
    public ResultSet getResultSet() {
        return resultSet;
    }
    
    @Override
    public int executeUpdate() throws SQLException {
        int result = statement.executeUpdate();
        clearBatch();
        return result;
    }
    
    @Override
    public boolean execute() throws SQLException {
        boolean result = statement.execute();
        clearBatch();
        return result;
    }
    
    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return statement.getGeneratedKeys();
    }
    
    @Override
    public void addBatch() throws SQLException {
        statement.addBatch();
        
    }
    
    @Override
    public void clearBatch() {
        resultSet = null;
        clearParameters();
    }
    
    private final class EncryptPreparedStatementMetaData {
        
        private final int resultSetType;
    
        private final int resultSetConcurrency;
    
        private final int resultSetHoldability;
    
        private final int autoGeneratedKeys;
    
        private final int[] columnIndexes;
    
        private final String[] columnNames;
        
        private EncryptPreparedStatementMetaData(final int resultSetType, final int resultSetConcurrency) {
            this.resultSetType = resultSetType;
            this.resultSetConcurrency = resultSetConcurrency;
            resultSetHoldability = -1;
            autoGeneratedKeys = -1;
            columnIndexes = null;
            columnNames = null;
        }
    
        private EncryptPreparedStatementMetaData(final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) {
            this.resultSetType = resultSetType;
            this.resultSetConcurrency = resultSetConcurrency;
            this.resultSetHoldability = resultSetHoldability;
            autoGeneratedKeys = -1;
            columnIndexes = null;
            columnNames = null;
        }
    
        private EncryptPreparedStatementMetaData(final int autoGeneratedKeys) {
            resultSetType = -1;
            resultSetConcurrency = -1;
            resultSetHoldability = -1;
            this.autoGeneratedKeys = autoGeneratedKeys;
            columnIndexes = null;
            columnNames = null;
        }
    
        private EncryptPreparedStatementMetaData(final int[] columnIndexes) {
            resultSetType = -1;
            resultSetConcurrency = -1;
            resultSetHoldability = -1;
            autoGeneratedKeys = -1;
            this.columnIndexes = columnIndexes;
            columnNames = null;
        }
    
        private EncryptPreparedStatementMetaData(final String[] columnNames) {
            resultSetType = -1;
            resultSetConcurrency = -1;
            resultSetHoldability = -1;
            autoGeneratedKeys = -1;
            columnIndexes = null;
            this.columnNames = columnNames;
        }
    }
}
