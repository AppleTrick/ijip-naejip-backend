package com.ssafy.home.handler;

import com.ssafy.home.dto.User.AgeGroup;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AgeGroupTypeHandler extends BaseTypeHandler<AgeGroup> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, AgeGroup parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.getValue());
    }

    @Override
    public AgeGroup getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return getAgeGroup(value);
    }

    @Override
    public AgeGroup getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return getAgeGroup(value);
    }

    @Override
    public AgeGroup getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return getAgeGroup(value);
    }

    private AgeGroup getAgeGroup(String value) {
        if (value == null) return null;
        return AgeGroup.from(value);
    }
}
