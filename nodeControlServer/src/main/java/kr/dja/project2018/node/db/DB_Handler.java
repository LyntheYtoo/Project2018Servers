package kr.dja.project2018.node.db;

import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;

import org.sqlite.JDBC;
import org.sqlite.SQLiteConfig;

import kr.dja.project2018.node.IServiceModule;
import kr.dja.project2018.node.NodeControlCore;
import kr.dja.project2018.node.util.tablebuilder.Row;
import kr.dja.project2018.node.util.tablebuilder.StringTableBuilder;

public class DB_Handler implements IServiceModule
{
	public static final String PROP_DB_FILE = "databaseFile";
	
	public static final Logger databaseLogger = NodeControlCore.createLogger(DB_Handler.class.getName().toLowerCase(), "db");
	private Connection connection;
	private SQLiteConfig config;
	private boolean isOpened = false;
	
	static
	{
		try
		{
			Class.forName("org.sqlite.JDBC");
		}
		catch (Exception e)
		{
			databaseLogger.log(Level.SEVERE, "JDBC �ε� ����", e);
		}
	}
	
	public DB_Handler()
	{
		this.config = new SQLiteConfig();
	}
	
	public boolean executeQuery(String query)
	{
		if(!this.isOpened) return false;
		PreparedStatement prep = null;
		try
		{
			prep = this.connection.prepareStatement(query);
			prep.execute();
		}
		catch (SQLException e)
		{
			databaseLogger.log(Level.SEVERE, "���� ����("+query+")", e);
			return false;
		}
		return true;
	}
	
	public CachedRowSet query(String query)
	{
		if(!this.isOpened) return null;
		CachedRowSet crs = null;
		PreparedStatement prep = null;
		ResultSet rs = null;
		
		try
		{
			prep = this.connection.prepareStatement(query);
			rs = prep.executeQuery();
			
		}
		catch (SQLException e)
		{
			databaseLogger.log(Level.SEVERE, "���� ����("+query+")", e);
			return null;
		}
		try
		{
			crs = RowSetProvider.newFactory().createCachedRowSet();
			crs.populate(rs);
		}
		catch (SQLException e)
		{
			databaseLogger.log(Level.SEVERE, "CachedRowSet ����� ����", e);
		}
		
		return crs;
	}
	
	@Override
	public boolean start()
	{
		if(this.isOpened) this.stop();
		String path = DB_Handler.class.getProtectionDomain().getCodeSource().getLocation().getPath()+
				NodeControlCore.getProp(PROP_DB_FILE);
		databaseLogger.log(Level.INFO, "�����ͺ��̽� ���� ("+path+")");
		try
		{
			this.connection = DriverManager.getConnection(JDBC.PREFIX+path, this.config.toProperties());
			this.connection.setAutoCommit(true);
		}
		catch(SQLException e)
		{
			databaseLogger.log(Level.SEVERE, "�����ͺ��̽� ���� ����", e);
			return false;
		}
		this.isOpened = true;
		return true;
	}

	@Override
	public void stop()
	{
		if (!this.isOpened) return;

		try
		{
			this.connection.close();
		}
		catch (SQLException e)
		{
			databaseLogger.log(Level.SEVERE, "�����ͺ��̽� �ݱ� ����", e);
		}
		this.isOpened = false;

	}
	
	public static void printResultSet(CachedRowSet rs)
	{// https://gist.github.com/jimjam88/8559599
		databaseLogger.log(Level.INFO, "-- ResultSet INFO --");
		StringTableBuilder tb = new StringTableBuilder("No", "");
		
		try
		{
			if(!rs.isBeforeFirst()) rs.beforeFirst();
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnsNumber = rsmd.getColumnCount();
			
			for(int i = 1; i <= columnsNumber; ++i)
			{
				tb.addHeadData(rsmd.getColumnName(i));
			}
			
			for(int i = 1; rs.next(); ++i)
			{
				Row r = tb.addRow(String.valueOf(i));
				for (int j = 1; j <= columnsNumber; ++j)
				{
					r.put(rs.getString(j));
				}
			}
			rs.beforeFirst();
		}
		catch (Exception e)
		{
			databaseLogger.log(Level.WARNING, "����Ʈ ����", e);
		}
		System.out.println(tb.build());
	}
	
	public static boolean isExist(CachedRowSet rs, String key, int col)
	{
		try
		{
			if(!rs.isBeforeFirst()) rs.beforeFirst();
			int columnsNumber = rs.getMetaData().getColumnCount();
			if(!(col > 0 && col <= columnsNumber))
			{
				databaseLogger.log(Level.WARNING, "�˻� column no ����(1~"+columnsNumber+" input:"+col);
				return false;
			}
			while(rs.next())
			{
				if(rs.getString(col).equals(key))
				{
					rs.beforeFirst();
					return true;
				}
			}
		}
		catch (SQLException e)
		{
			databaseLogger.log(Level.WARNING, "�˻� ����", e);
		}
		return false;
	}
	
	public static String[][] toArray(CachedRowSet rs)
	{
		LinkedList<String[]> list = null;
		try
		{
			if(!rs.isBeforeFirst()) rs.beforeFirst();
			list = new LinkedList<String[]>();
			int columnsNumber = rs.getMetaData().getColumnCount();
			
			while(rs.next())
			{
				String[] rowArr = new String[columnsNumber];
				for(int i = 1; i <= columnsNumber; ++i)
				{
					rowArr[i - 1] = rs.getString(i);
				}
				list.add(rowArr);
			}
			rs.beforeFirst();
		}
		catch (SQLException e)
		{
			databaseLogger.log(Level.WARNING, "toArray ����", e);
			return null;
		}
		String[][] arr = new String[list.size()][];
		list.toArray(arr);
		return arr;
	}
}
