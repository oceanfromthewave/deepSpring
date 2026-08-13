package com.deepspring.jdbc;

import com.deepspring.member.Member;
import com.deepspring.member.MemberRepository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JdbcMemberRepository implements MemberRepository
{

	private static final String URL = "jdbc:h2:mem:deepspring;DB_CLOSE_DELAY=-1";
	private static final String USER = "sa";
	private static final String PASSWORD = "";

	public JdbcMemberRepository()
	{
		try(Connection conn = DriverManager.getConnection(URL, USER, PASSWORD); Statement stmt = conn.createStatement())
		{
			stmt.execute("CREATE TABLE IF NOT EXISTS member (" + "id BIGINT PRIMARY KEY, " + "name VARCHAR(255), " + "grade VARCHAR(50))");
		}
		catch(SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public Member findById(Long memberId)
	{
		String sql = "SELECT id, name, grade FROM member WHERE id = ?";
		try(Connection conn = DriverManager.getConnection(URL, USER, PASSWORD); PreparedStatement pstmt = conn.prepareStatement(sql))
		{
			pstmt.setLong(1, memberId);
			try(ResultSet rs = pstmt.executeQuery())
			{
				if(rs.next())
				{
					return new Member(rs.getLong("id"), rs.getString("name"), Member.Grade.valueOf(rs.getString("grade")));
				}
				return null;
			}
		}
		catch(SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void save(Member member)
	{
		String sql = "INSERT INTO member (id, name, grade) VALUES (?, ?, ?)";
		try(Connection conn = DriverManager.getConnection(URL, USER, PASSWORD); PreparedStatement pstmt = conn.prepareStatement(sql))
		{
			pstmt.setLong(1, member.getId());
			pstmt.setString(2, member.getName());
			pstmt.setString(3, member.getGrade().name());
			pstmt.executeUpdate();
		}
		catch(SQLException e)
		{
			throw new RuntimeException(e);
		}
	}
}
