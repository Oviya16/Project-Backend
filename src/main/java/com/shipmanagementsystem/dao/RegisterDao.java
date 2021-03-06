package com.shipmanagementsystem.dao;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.shipmanagementsystem.mapper.RegisterMapper;
import com.shipmanagementsystem.mapper.TempMapper;
import com.shipmanagementsystem.model.RegisterModel;
import com.shipmanagementsystem.model.TempModel;


/**
 * The Class RegisterDao.
 */
@Repository
public class RegisterDao {

	/** The select. */
	private final String SELECT = "select * from users;";
	
	/** The insert. */
	private final String INSERT="insert into users(first_name,last_name,dob,gender,contact_number,category,user_id,password) values(?,?,?,?,?,?,?,?);";
	
	/** The login. */
	private final String LOGIN = "select password from users where user_id = ? ";
	
	/** The get role. */
	private final String GET_ROLE = "select category from users where user_id = ?";
	
	/** The temp. */
	private final String TEMP="insert into temp(first_name,last_name,dob,gender,contact_number,category,user_id,password,rejected) values(?,?,?,?,?,?,?,?,false);";
	
	/** The get user. */
	private final String GET_USER="select password from temp where user_id=?;";
	
	/** The get status. */
	private final String GET_STATUS="select rejected from temp where user_id=?;";
	
	/** The approve. */
	private final String APPROVE="insert into users(first_name,last_name,dob,gender,contact_number,category,user_id,password) select first_name,last_name,dob,gender,contact_number,category,user_id,password from temp where user_id=?;";
	
	/** The remove. */
	private final String REMOVE="delete from temp where user_id=?;";
	
	/** The reject. */
	private final String REJECT="update temp set rejected=true where user_id=?;";
	
	/** The get temp. */
	private final String GET_TEMP="select * from temp;";
	
	/** The jdbc template. */
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	
	/**
	 * Gets the all users.
	 *
	 * @return the all users
	 */
	public List<RegisterModel> getAllUsers(){
		return jdbcTemplate.query(SELECT, new RegisterMapper());
	}
	
	/**
	 * Insert user.
	 *
	 * @param user the user
	 * @return true, if successful
	 */
	public boolean insertUser(RegisterModel user) {
		java.sql.Date date = new java.sql.Date(user.getDob().getTime());
		if(user.getCategory().equals("manager")|| user.getCategory().equals("employee")) {
			if (jdbcTemplate.update(TEMP, user.getFirstName(), user.getLastName(),
					date,user.getGender(),user.getContactNumber(),user.getCategory(), user.getUserId()
					,user.getPassword()) != 0) {
				return true;
			}
			return false;
		}
		if (jdbcTemplate.update(INSERT, user.getFirstName(), user.getLastName(),
				date,user.getGender(),user.getContactNumber(),user.getCategory(), user.getUserId()
				,user.getPassword()) != 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * Check login.
	 *
	 * @param user the user
	 * @return the string
	 */
	public String checkLogin(RegisterModel user) {
		String loginStatus;
		try {
			String password = this.jdbcTemplate.queryForObject(LOGIN, String.class, new Object[] { user.getUserId() });
			if (password.equals(user.getPassword())) {
				loginStatus = "Logged In";
				String role = this.jdbcTemplate.queryForObject(GET_ROLE, String.class,
						new Object[] { user.getUserId() });
				loginStatus += role;
				System.out.println(loginStatus);
			} else {
				loginStatus = "Password";
			}
		} catch (Exception e) {
			try {
			String password = this.jdbcTemplate.queryForObject(GET_USER, String.class, new Object[] { user.getUserId() });
			boolean rejected=this.jdbcTemplate.queryForObject(GET_STATUS, Boolean.class,new Object[] { user.getUserId() });
			if(rejected) {
				loginStatus="Rejected";
			}
			else {
				loginStatus="Pending";
			}
			} catch (Exception e1) {
				loginStatus = "UserId";
			}
			
		}
		return loginStatus;
	}

/**
 * Approve users.
 *
 * @param user the user
 * @return true, if successful
 */
public boolean approveUsers(RegisterModel user) {
	if (jdbcTemplate.update(APPROVE, user.getUserId()) != 0) {
		jdbcTemplate.update(REMOVE,user.getUserId());
		return true;
		
	}
	return false;
}

/**
 * Reject users.
 *
 * @param user the user
 * @return true, if successful
 */
public boolean rejectUsers(RegisterModel user) {
	if (jdbcTemplate.update(REJECT, user.getUserId()) != 0) {
		return true;
	}
	return false;
}

/**
 * Gets the pending users.
 *
 * @return the pending users
 */
public List<TempModel> getPendingUsers(){
	return jdbcTemplate.query(GET_TEMP,new TempMapper());
}
}