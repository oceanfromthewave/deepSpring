package com.deepspring.boot;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
public class AppProps
{
	private String name;
	private String version;
	private Admin admin = new Admin();

	// getter/setter 다 필요함 (binder가 setter로 값 주입함)
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getVersion()
	{
		return version;
	}

	public void setVersion(String version)
	{
		this.version = version;
	}

	public Admin getAdmin()
	{
		return admin;
	}

	public void setAdmin(Admin admin)
	{
		this.admin = admin;
	}

	public static class Admin
	{
		private String email;
		private int maxUsers;

		public String getEmail()
		{
			return email;
		}

		public void setEmail(String email)
		{
			this.email = email;
		}

		public int getMaxUsers()
		{
			return maxUsers;
		}

		public void setMaxUsers(int maxUsers)
		{
			this.maxUsers = maxUsers;
		}
	}
}
