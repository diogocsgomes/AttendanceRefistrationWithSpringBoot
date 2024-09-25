package pt.isec.pd.tp_pd.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import pt.isec.pd.tp_pd.data.User;
import pt.isec.pd.tp_pd.database.DatabaseManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class UserAuthenticationProvider implements AuthenticationProvider {
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String email = authentication.getName();
		String password = authentication.getCredentials().toString();

		User user = null;

		try {
			user = DatabaseManager.getInstance(null).login(email, password);
		} catch (SQLException e) {
			System.out.println("Error: " + e);
		}

		if(user == null){
			return  null;
		}

		if (user.isAdmin()) {
			List<GrantedAuthority> authorities = new ArrayList<>();
			authorities.add(new SimpleGrantedAuthority("ADMIN"));
			return new UsernamePasswordAuthenticationToken(email, password, authorities);
		} else{
			List<GrantedAuthority> authorities = new ArrayList<>();
			authorities.add(new SimpleGrantedAuthority("PARTICIPANT"));
			return new UsernamePasswordAuthenticationToken(email, password, authorities);
		}
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}
}
