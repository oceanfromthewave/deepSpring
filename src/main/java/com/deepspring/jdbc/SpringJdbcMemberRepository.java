package com.deepspring.jdbc;

import com.deepspring.member.Member;
import com.deepspring.member.MemberRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;

public class SpringJdbcMemberRepository implements MemberRepository
{

	private final JdbcTemplate jdbcTemplate;

	public SpringJdbcMemberRepository(DataSource dataSource)
	{
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS member (" + "id BIGINT PRIMARY KEY, " + "name VARCHAR(255), " + "grade VARCHAR(50))");
	}

	private static final RowMapper<Member> MEMBER_ROW_MAPPER = (rs, rowNum) -> new Member(rs.getLong("id"), rs.getString("name"),
			Member.Grade.valueOf(rs.getString("grade")));

	@Override
	public Member findById(Long memberId)
	{
		String sql = "SELECT id, name, grade FROM member WHERE id = ?";
		return jdbcTemplate.query(sql, MEMBER_ROW_MAPPER, memberId).stream().findFirst().orElse(null);
	}

	@Override
	public void save(Member member)
	{
		String sql = "INSERT INTO member (id, name, grade) VALUES (?, ?, ?)";
		jdbcTemplate.update(sql, member.getId(), member.getName(), member.getGrade().name());
	}
}
