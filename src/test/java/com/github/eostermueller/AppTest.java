package com.github.eostermueller;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.BeforeClass;

public class AppTest 
{
	private static String JDBC_URL ="jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
	private static DataSource dataSource = null;
	
	public static DataSource getDataSource() {
		return dataSource;
	}
	@BeforeClass 
	public static void initDb() throws SQLException {

		 dataSource = JdbcConnectionPool.create(JDBC_URL, "user", "password");
		Connection conn = getDataSource().getConnection();
		 
		 conn.createStatement().executeUpdate("CREATE TABLE data ("
		   +" key VARCHAR(255) PRIMARY KEY,"
		   +" value VARCHAR(1023) )");
		 conn.createStatement().executeUpdate("INSERT INTO DATA values ('foo', 'bar')");
		 conn.createStatement().executeUpdate("INSERT INTO DATA values ('hello', 'world')");
	}
	
	@Test
	public void canLocateRegisteredJdbcDriver() {
		int driverCount = 0;
        Driver driver = null;
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            driver = drivers.nextElement();
            driverCount++;
        }
        assertEquals("Should have found just 1 JDBC driver, the H2 one that we just exercised",1, driverCount);
        assertEquals("Could not find the H2 JDBC driver that we just exercised","org.h2.Driver", driver.getClass().getName());
		
	}

	
	@AfterClass public static void shutdownDb() throws SQLException {
		
		getDataSource().getConnection().close();
	}
	@Test
	public void querySingleRecord() throws SQLException {
		Connection conn = getDataSource().getConnection();
		String rc = "<undefined>";
		try (
			PreparedStatement stmt = conn.prepareStatement("SELECT value from data where key='foo'");  
			ResultSet rs = stmt.executeQuery()) {
			rs.next();
			rc = rs.getString(1);
			assertEquals("Didn't find the value we inserted into the H2 in-mem table",
					"bar", rc);
		}
	}
	
}
